/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.gatein.portal.page.spi.portlet;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.portlet.ResourceResponse;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.spi.AbstractInstanceContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractUserContext;
import org.gatein.pc.portlet.impl.spi.AbstractWindowContext;
import org.gatein.portal.page.NodeState;
import org.gatein.portal.page.PageContext;
import org.gatein.portal.page.WindowContext;
import org.gatein.portal.page.spi.ContentProvider;
import org.gatein.portal.page.spi.RenderTask;
import org.gatein.portal.portlet.PortletAppManager;
import org.gatein.portal.servlet.Context;

/**
 * @author Julien Viet
 */
@Singleton
public class PortletContentProvider implements ContentProvider {

    /** . */
    final PortletAppManager portletManager;

    @Inject
    public PortletContentProvider(PortletAppManager portletManager) {
        this.portletManager = portletManager;
    }

    @Override
    public PortletContent getContent(String id, NodeState nodeState) {

        //
        org.gatein.pc.api.Portlet portlet;
        PortletInvoker invoker = portletManager.getInvoker();
        try {
            Matcher matcher = PortletContent.PORTLET_PATTERN.matcher(id);
            if (matcher.matches()) {
                portlet = invoker.getPortlet(PortletContext.createPortletContext(matcher.group(1), matcher.group(2)));
            } else {
                throw new Exception("Could not handle " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            portlet = null;
        }

        //
        return new PortletContent(nodeState, portlet);
    }

    @Override
    public RenderTask createRender(WindowContext window) {
        return new PortletRenderTask(this, window);
    }

    @Override
    public Response processAction(
            WindowContext window,
            String windowState,
            String mode,
            Map<String, String[]> interactionState) {

        WindowState windowState_ = windowState != null ? WindowState.create(windowState) : WindowState.NORMAL;
        Mode mode_ = mode != null ? Mode.create(mode) : Mode.VIEW;

        //
        ActionInvocation action = new ActionInvocation(new GateInPortletInvocationContext(this, window));
        PortletContent state = (PortletContent) window.state;
        action.setClientContext(new GateInClientContext());
        action.setPortalContext(new AbstractPortalContext());
        action.setInstanceContext(new AbstractInstanceContext(window.name, AccessMode.READ_ONLY));
        action.setWindowContext(new AbstractWindowContext(window.name));
        action.setUserContext(new AbstractUserContext());
        action.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
        action.setRequest(Context.getCurrentRequest());
        action.setResponse(Context.getCurrentResponse());
        action.setTarget(state.portlet.getContext());
        action.setMode(mode_);
        action.setWindowState(windowState_);
        action.setNavigationalState(ParametersStateString.create());
        action.setInteractionState(interactionState != null ? ParametersStateString.create(interactionState) : null);
        action.setPublicNavigationalState(window.computePublicParameters());

        //
        PortletInvoker invoker = portletManager.getInvoker();
        Request current = Request.getCurrent();
        ContextLifeCycle clf = current.suspend();
        PortletInvocationResponse pir;
        try {
            pir = invoker.invoke(action);
        } catch (PortletInvokerException e) {
            return Response.error(e);
        } finally {
            clf.resume();
        }

        //
        if (pir instanceof UpdateNavigationalStateResponse) {
            UpdateNavigationalStateResponse update = (UpdateNavigationalStateResponse)pir;
            PageContext.Builder clone = window.page.builder();

            // Remove this nasty cast
            PortletContent windowClone = (PortletContent) clone.getWindow(window.name);

            ParametersStateString s = (ParametersStateString) update.getNavigationalState();
            windowClone.parameters = s.getParameters();
            if (update.getWindowState() != null) {
                windowClone.windowState = update.getWindowState();
            }
            if (update.getMode() != null) {
                windowClone.mode = update.getMode();
            }
            Map<String, String[]> changes = update.getPublicNavigationalStateUpdates();
            if (changes != null && changes.size() > 0) {
                clone.apply(window.getPublicParametersChanges(changes));
            }
            return clone.build().getDispatch().with(PropertyType.REDIRECT_AFTER_ACTION);
        } else {
            throw new UnsupportedOperationException("Not yet handled " + pir);
        }
    }

    @Override
    public Response serveResource(WindowContext window, String id, Map<String, String[]> resourceState) {
        PortletContent state = (PortletContent) window.state;
        // Determine cacheability
        CacheLevel cacheability;
        Mode mode;
        org.gatein.pc.api.WindowState windowState;
        ParametersStateString navigationalState;
        if (state.parameters == null) {
            cacheability = CacheLevel.FULL;
            navigationalState = null;

            // JULIEN : this should be null but it cannot for now as we have bugs in the portlet container
            // java.lang.NullPointerException
            // at org.gatein.pc.portlet.impl.jsr168.api.PortletRequestImpl.getWindowState(PortletRequestImpl.java:244)
            // see also : https://java.net/jira/browse/PORTLETSPEC3-27
            mode = Mode.VIEW;
            windowState = org.gatein.pc.api.WindowState.NORMAL;
        } else {
            mode = state.mode != null ? state.mode : Mode.VIEW;
            windowState = state.windowState != null ? state.windowState : org.gatein.pc.api.WindowState.NORMAL;
            navigationalState = ParametersStateString.create(state.parameters);
            if (window.page.hasParameters()) {
                cacheability = CacheLevel.PAGE;
            } else {
                cacheability = CacheLevel.PORTLET;
            }
        }

        //
        ResourceInvocation resource = new ResourceInvocation(new GateInPortletInvocationContext(this, window));
        resource.setResourceId(id);
        resource.setCacheLevel(cacheability);
        resource.setClientContext(new GateInClientContext());
        resource.setPortalContext(new AbstractPortalContext());
        resource.setInstanceContext(new AbstractInstanceContext(window.name, AccessMode.READ_ONLY));
        resource.setWindowContext(new AbstractWindowContext(window.name));
        resource.setUserContext(new AbstractUserContext());
        resource.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
        resource.setRequest(Context.getCurrentRequest());
        resource.setResponse(Context.getCurrentResponse());
        resource.setTarget(state.portlet.getContext());
        resource.setMode(mode);
        resource.setWindowState(windowState);
        resource.setNavigationalState(navigationalState);
        resource.setResourceState(resourceState != null ? ParametersStateString.create(resourceState) : null);
        resource.setPublicNavigationalState(window.computePublicParameters());

        //
        PortletInvocationResponse pir;
        try {
            pir = portletManager.getInvoker().invoke(resource);
        } catch (PortletInvokerException e) {
            return Response.error(e);
        }

        //
        if (pir instanceof ContentResponse) {
            ContentResponse content = (ContentResponse) pir;

            //
            int code = 200;
            ResponseProperties properties = content.getProperties();
            MultiValuedPropertyMap<String> headers = properties != null ? properties.getTransportHeaders() : null;
            if (headers != null) {
                String value = headers.getValue(ResourceResponse.HTTP_STATUS_CODE);
                if (value != null) {
                    try {
                        code = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
//                                                throw new ServletException("Bad " + ResourceResponse.HTTP_STATUS_CODE + "=" + value +
//                                                        " resource value", e);
                    }
                }
            }

            // Create response
            Response.Status status = Response.status(code);

            // Headers
            sendHttpHeaders(headers, status);

            // Charset
            String encoding = content.getEncoding();
            if (encoding != null) {
                Charset charset = Charset.forName(encoding);
                status.with(PropertyType.ENCODING, charset);
            }

            // Mime type
            String contentType = content.getContentType();
            if (contentType != null) {
                status.with(PropertyType.MIME_TYPE, contentType);
            }

            //
            if (content.getBytes() != null) {
                status = status.body(content.getBytes());
            } else if (content.getChars() != null) {
                status = status.body(content.getChars());
            }

            //
            return status;
        } else {
            throw new UnsupportedOperationException("No yet handled " + pir);
        }
    }

    private static void sendHttpHeaders(ResponseProperties properties, Response.Status resp) {
        if (properties != null) {
            sendHttpHeaders(properties.getTransportHeaders(), resp);
        }
    }

    private static void sendHttpHeaders(MultiValuedPropertyMap<String> httpHeaders, Response.Status resp) {
        if (httpHeaders != null) {
            for (String headerName : httpHeaders.keySet()) {
                if (!headerName.equals(ResourceResponse.HTTP_STATUS_CODE)) {
                    resp.withHeader(headerName, httpHeaders.getValue(headerName));
                }
            }
        }
    }
}
