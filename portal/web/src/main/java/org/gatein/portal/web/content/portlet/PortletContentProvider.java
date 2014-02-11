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
package org.gatein.portal.web.content.portlet;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.portlet.ResourceResponse;

import juzu.PropertyType;
import juzu.Response;
import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
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
import org.gatein.pc.portlet.state.producer.PortletState;
import org.gatein.portal.content.ContentDescription;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.content.Result;
import org.gatein.portal.content.WindowContentContext;
import org.gatein.portal.web.page.Encoder;
import org.gatein.portal.content.ContentProvider;
import org.gatein.portal.web.servlet.Context;

/**
 * @author Julien Viet
 */
@Singleton
public class PortletContentProvider implements ContentProvider<Portlet> {

    /** . */
    final PortletDeployer portletManager;

    @Inject
    public PortletContentProvider(PortletDeployer portletManager) {
        this.portletManager = portletManager;
    }

    @Override
    public ContentType<Portlet> getContentType() {
        return ContentType.PORTLET;
    }

    @Override
    public PortletContent getContent(String id) {
        PortletInvoker invoker = portletManager.getInvoker();
        try {
            Matcher matcher = PortletContent.PORTLET_PATTERN.matcher(id);
            if (matcher.matches()) {
                org.gatein.pc.api.Portlet portlet = invoker.getPortlet(PortletContext.createPortletContext(matcher.group(1), matcher.group(2)));
                return new PortletContent(this, portlet);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Handle me gracefully", e);
        }
    }

    Result.Action processAction(
            WindowContentContext<org.exoplatform.portal.pom.spi.portlet.Portlet> window,
            String windowState,
            String mode,
            Map<String, String[]> interactionState) {

        //
        WindowState windowState_ = windowState != null ? WindowState.create(windowState) : WindowState.NORMAL;
        Mode mode_ = mode != null ? Mode.create(mode) : Mode.VIEW;

        // Compute portlet context with prefs if there are some
        PortletContent content = (PortletContent) window.getContent();
        PortletContext target = content.portlet.getContext();
        Portlet state = window.getState();
        if (state != null) {
            target = content.as(state);
        }
        GateInInstanceContext instanceContext = new GateInInstanceContext(window.getName(), AccessMode.READ_WRITE);

        //
        ContextLifeCycle lifeCycle = Request.getCurrent().suspend();
        PortletInvocationResponse pir;
        try {

            //
            ActionInvocation action = new ActionInvocation(new GateInPortletInvocationContext(this, (PortletContent) window.getContent(), window, lifeCycle));
            action.setClientContext(new GateInClientContext());
            action.setPortalContext(new AbstractPortalContext());
            action.setInstanceContext(instanceContext);
            action.setWindowContext(new AbstractWindowContext(window.getName()));
            action.setUserContext(new AbstractUserContext());
            action.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
            action.setRequest(Context.getCurrentRequest());
            action.setResponse(Context.getCurrentResponse());
            action.setTarget(target);
            action.setMode(mode_);
            action.setWindowState(windowState_);
            action.setNavigationalState(ParametersStateString.create());
            action.setInteractionState(interactionState != null ? ParametersStateString.create(interactionState) : null);
            action.setPublicNavigationalState(window.getPublicRenderParameters());

            //
            PortletInvoker invoker = portletManager.getInvoker();
            pir = invoker.invoke(action);
        } catch (PortletInvokerException e) {
            return new Result.Error(true, e);
        } finally {
            lifeCycle.resume();
        }

        //
        if (pir instanceof UpdateNavigationalStateResponse) {

            //
            UpdateNavigationalStateResponse update = (UpdateNavigationalStateResponse)pir;
            ParametersStateString s = (ParametersStateString) update.getNavigationalState();
            String updateParameters;
            if (s.getParameters() != null && s.getParameters().size() > 0) {
                Encoder encoder = new Encoder(s.getParameters());
                updateParameters = encoder.encode();
            } else {
                updateParameters = null;
            }
            String updateWindowState;
            if (update.getWindowState() != null) {
                updateWindowState = update.getWindowState().toString();
            } else {
                updateWindowState = null;
            }
            String updateMode;
            if (update.getMode() != null) {
                updateMode = update.getMode().toString();
            } else {
                updateMode = null;
            }

            //
            Portlet stateUpdate;
            PortletContext modifiedContext = instanceContext.getModifiedContext();
            if (modifiedContext != null) {
                System.out.println("Should save new context");
                StatefulPortletContext<PortletState> a = (StatefulPortletContext<PortletState>) modifiedContext;
                stateUpdate = new Portlet();
                for (Map.Entry<String, List<String>> pref : a.getState().getProperties().entrySet()) {
                    stateUpdate.putPreference(new Preference(pref.getKey(), pref.getValue(), false));
                }
            } else {
                stateUpdate = null;
            }

            //
            return new Result.Update(updateParameters, updateWindowState, updateMode, update.getPublicNavigationalStateUpdates(), stateUpdate);
        } else {
            throw new UnsupportedOperationException("Not yet handled " + pir);
        }
    }

    Response serveResource(WindowContentContext<org.exoplatform.portal.pom.spi.portlet.Portlet> window, String id, Map<String, String[]> resourceState) {

        //
        PortletContent state = (PortletContent) window.getContent();
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

            if (window.getPublicRenderParameters() != null) {
                cacheability = CacheLevel.PAGE;
            } else {
                cacheability = CacheLevel.PORTLET;
            }
        }

        //
        ContextLifeCycle lifeCycle = Request.getCurrent().suspend();
        PortletInvocationResponse pir;
        try {

            //
            ResourceInvocation resource = new ResourceInvocation(new GateInPortletInvocationContext(this, (PortletContent) window.getContent(), window, lifeCycle));
            resource.setResourceId(id);
            resource.setCacheLevel(cacheability);
            resource.setClientContext(new GateInClientContext());
            resource.setPortalContext(new AbstractPortalContext());
            resource.setInstanceContext(new AbstractInstanceContext(window.getName(), AccessMode.READ_ONLY));
            resource.setWindowContext(new AbstractWindowContext(window.getName()));
            resource.setUserContext(new AbstractUserContext());
            resource.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
            resource.setRequest(Context.getCurrentRequest());
            resource.setResponse(Context.getCurrentResponse());
            resource.setTarget(state.portlet.getContext());
            resource.setMode(mode);
            resource.setWindowState(windowState);
            resource.setNavigationalState(navigationalState);
            resource.setResourceState(resourceState != null ? ParametersStateString.create(resourceState) : null);
            resource.setPublicNavigationalState(window.getPublicRenderParameters());

            //
            pir = portletManager.getInvoker().invoke(resource);
        } catch (PortletInvokerException e) {
            return Response.error(e);
        } finally {
            lifeCycle.resume();
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

    @Override
    public Iterable<ContentDescription> findContents(String filter, int offset, int limit) {
        try {
            Set<org.gatein.pc.api.Portlet> portlets = portletManager.getAllPortlets();
            ArrayList<ContentDescription> all = new ArrayList<ContentDescription>();
            for (org.gatein.pc.api.Portlet portlet : portlets) {
                PortletInfo info = portlet.getInfo();
                MetaInfo meta = info.getMeta();
                String title = meta.getMetaValue("title").getDefaultString();
                all.add(new ContentDescription(
                        info.getApplicationName() + "/" + info.getName(),
                        title,
                        "<img alt=\"\" src=\"/portal/assets/org/gatein/portal/web/assets/images/DefaultPortlet.png\"/>\n" +
                        "<p>" + title + "</p>"
                ));
            }
            if (offset > all.size()) {
                return Collections.emptyList();
            } else {
                int to = Math.min(all.size(), offset + limit);
                return all.subList(offset, to);
            }
        } catch (PortletInvokerException e) {
            return Collections.emptyList();
        }
    }
}
