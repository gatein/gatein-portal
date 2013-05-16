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
package org.gatein.portal.page;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import juzu.impl.common.PercentCodec;
import juzu.io.Encoding;
import juzu.request.Phase;
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.info.NavigationInfo;
import org.gatein.pc.api.info.ParameterInfo;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.spi.AbstractInstanceContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractUserContext;
import org.gatein.pc.portlet.impl.spi.AbstractWindowContext;
import org.gatein.portal.mop.customization.CustomizationContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class WindowState implements PortletInvocationContext {

    /** . */
    private static final Map<String, String[]> NO_PARAMETERS = Collections.emptyMap();

    /**
     * Custom encoding.
     */
    public static final Encoding ENCODING = new Encoding("custom") {
        @Override
        public void encodeSegment(CharSequence s, Appendable appendable) throws IOException {
            PercentCodec.RFC3986_SEGMENT.encode(s, appendable);
        }

        @Override
        public void encodeQueryParamName(CharSequence s, Appendable appendable) throws IOException {
            PercentCodec.RFC3986_QUERY_PARAM_NAME.encode(s, appendable);
        }

        @Override
        public void encodeQueryParamValue(CharSequence s, Appendable appendable) throws IOException {
            appendable.append(s);
        }
    };
    /** . */
    private static final Pattern PORTLET_PATTERN = Pattern.compile("^([^/]+)/([^/]+)$");

    /** The related page. */
    public final PageState page;

    /** The window id. */
    public final String id;

    /** The window name. */
    public final String name;

    /** The portlet window parameters. */
    Map<String, String[]> parameters;

    /** The portlet window state. */
    org.gatein.pc.api.WindowState windowState;

    /** The portlet window state. */
    org.gatein.pc.api.Mode mode;

    /** The related lazy loaded portlet and cached. */
    private Portlet portlet;

    WindowState(NodeState node, PageState page) {

        //
        this.page = page;
        this.name = node.context.getName();
        this.id = node.context.getId();
        this.parameters = null;
        this.windowState = null;
        this.mode = null;
        this.portlet = null;
    }

    WindowState(WindowState that, PageState page) {

        //
        Map<String, String[]> parameters;
        if (that.parameters == null) {
            parameters = null;
        } else if (that.parameters.isEmpty()) {
            parameters = NO_PARAMETERS;
        } else {
            parameters = new HashMap<String, String[]>(that.parameters);
            for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                parameter.setValue(parameter.getValue().clone());
            }
        }

        //
        this.page = page;
        this.name = that.name;
        this.id = that.id;
        this.parameters = parameters;
        this.windowState = that.windowState;
        this.mode = that.mode;
        this.portlet = null;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public String[] getParameter(String name) {
        return parameters.get(name);
    }

    public void setParameter(String name, String[] value) {
        if (value.length == 0) {
            if (parameters != NO_PARAMETERS) {
                parameters.remove(name);
            }
        } else {
            if (parameters == NO_PARAMETERS) {
                parameters = new HashMap<String, String[]>();
            }
            parameters.put(name, value);
        }
    }

    public void setPublicParameters(Map<String, String[]> changes) {
        NavigationInfo info = getPortlet().getInfo().getNavigation();
        for (Map.Entry<String, String[]> change : changes.entrySet()) {
            ParameterInfo parameterInfo = info.getPublicParameter(change.getKey());
            if (parameterInfo != null) {
                page.setParameter(parameterInfo.getName(), change.getValue());
            }
        }
    }

    public PortletInvocationResponse processAction(
            org.gatein.pc.api.WindowState windowState,
            Mode mode,
            Map<String, String[]> interactionState) throws PortletInvokerException {
        ActionInvocation action = new ActionInvocation(this);
        action.setClientContext(new GateInClientContext());
        action.setPortalContext(new AbstractPortalContext());
        action.setInstanceContext(new AbstractInstanceContext(name, AccessMode.READ_ONLY));
        action.setWindowContext(new AbstractWindowContext(name));
        action.setUserContext(new AbstractUserContext());
        action.setSecurityContext(new AbstractSecurityContext(ClientRequestFilter.currentRequest.get()));
        action.setRequest(ClientRequestFilter.currentRequest.get());
        action.setResponse(ClientRequestFilter.currentResponse.get());
        action.setTarget(getPortlet().getContext());
        action.setMode(mode != null ? mode : Mode.VIEW);
        action.setWindowState(windowState != null ? windowState : org.gatein.pc.api.WindowState.NORMAL);
        action.setNavigationalState(ParametersStateString.create());
        action.setInteractionState(interactionState != null ? ParametersStateString.create(interactionState) : null);
        action.setPublicNavigationalState(computePublicParameters());
        return page.portletManager.getInvoker().invoke(action);
    }

    public PortletInvocationResponse render() throws PortletInvokerException {
        RenderInvocation render = new RenderInvocation(this);
        render.setClientContext(new GateInClientContext());
        render.setPortalContext(new AbstractPortalContext());
        render.setInstanceContext(new AbstractInstanceContext(name, AccessMode.READ_ONLY));
        render.setWindowContext(new AbstractWindowContext(name));
        render.setUserContext(new AbstractUserContext());
        render.setSecurityContext(new AbstractSecurityContext(ClientRequestFilter.currentRequest.get()));
        render.setRequest(ClientRequestFilter.currentRequest.get());
        render.setResponse(ClientRequestFilter.currentResponse.get());
        render.setTarget(getPortlet().getContext());
        render.setMode(mode != null ? mode : Mode.VIEW);
        render.setWindowState(windowState != null ? windowState : org.gatein.pc.api.WindowState.NORMAL);
        render.setNavigationalState(parameters != null ? ParametersStateString.create(parameters) : null);
        render.setPublicNavigationalState(computePublicParameters());
        return page.portletManager.getInvoker().invoke(render);
    }

    public PortletInvocationResponse serveResource(Map<String, String[]> resourceState) throws PortletInvokerException {
        ResourceInvocation resource = new ResourceInvocation(this);
        resource.setClientContext(new GateInClientContext());
        resource.setPortalContext(new AbstractPortalContext());
        resource.setInstanceContext(new AbstractInstanceContext(name, AccessMode.READ_ONLY));
        resource.setWindowContext(new AbstractWindowContext(name));
        resource.setUserContext(new AbstractUserContext());
        resource.setSecurityContext(new AbstractSecurityContext(ClientRequestFilter.currentRequest.get()));
        resource.setRequest(ClientRequestFilter.currentRequest.get());
        resource.setResponse(ClientRequestFilter.currentResponse.get());
        resource.setTarget(getPortlet().getContext());
        resource.setMode(mode != null ? mode : Mode.VIEW);
        resource.setWindowState(windowState != null ? windowState : org.gatein.pc.api.WindowState.NORMAL);
        resource.setNavigationalState(ParametersStateString.create(parameters));
        resource.setResourceState(resourceState != null ? ParametersStateString.create(resourceState) : null);
        resource.setPublicNavigationalState(computePublicParameters());
        return page.portletManager.getInvoker().invoke(resource);
    }

    private Map<String, String[]> computePublicParameters() {
        Map<String, String[]> publicParameters = NO_PARAMETERS;
        NavigationInfo info = getPortlet().getInfo().getNavigation();
        for (Map.Entry<QName, String[]> parameter : page.parameters()) {
            ParameterInfo parameterInfo = info.getPublicParameter(parameter.getKey());
            if (parameterInfo != null) {
                if (publicParameters == NO_PARAMETERS) {
                    publicParameters = new HashMap<String, String[]>();
                }
                publicParameters.put(parameterInfo.getId(), parameter.getValue());
            }
        }
        return publicParameters;
    }

    public Portlet getPortlet() {

        if (portlet == null) {
            // Load the customization
            CustomizationContext<org.exoplatform.portal.pom.spi.portlet.Portlet> portletCustomization = page.customizationService.loadCustomization(id);

            //
            PortletInvoker invoker = page.portletManager.getInvoker();
            try {
                String contentId = portletCustomization.getContentId();
                Matcher matcher = PORTLET_PATTERN.matcher(contentId);
                if (matcher.matches()) {
                    portlet = invoker.getPortlet(PortletContext.createPortletContext(matcher.group(1), matcher.group(2)));
                } else {
                    throw new Exception("Could not handle " + contentId);
                }
            } catch (Exception e) {
                // Handle me
                e.printStackTrace();
                return null;
            }
        }

        //
        return portlet;
    }

    private Phase.View.Dispatch getDispatch(
            String action,
            org.gatein.pc.api.WindowState windowState,
            Mode mode,
            Map<String, String[]> parameters) {
        String targetWindowState = windowState != null && !org.gatein.pc.api.WindowState.NORMAL.equals(windowState) ? windowState.toString() : null;
        String targetMode = mode != null && !Mode.VIEW.equals(mode) ? mode.toString() : null;
        Phase.View.Dispatch view = Controller_.index(page.path, action, name, targetWindowState, targetMode);
        for (WindowState w : page.windows) {
            w.encode(view);
        }
        if (parameters != null) {
            for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                view.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        return view;
    }

    @Override
    public MediaType getResponseContentType() {
        return MediaType.TEXT_HTML;
    }

    @Override
    public String encodeResourceURL(String url) throws IllegalArgumentException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public String renderURL(ContainerURL containerURL, URLFormat format) {
        Phase.View.Dispatch view;
        if (containerURL instanceof RenderURL) {
            RenderURL renderURL = (RenderURL) containerURL;
            WindowState copy = new PageState(page).get(name);
            ParametersStateString ns = (ParametersStateString) renderURL.getNavigationalState();
            if (ns != null) {
                copy.parameters = ns.getParameters();
            }
            if (renderURL.getWindowState() != null) {
                copy.windowState = renderURL.getWindowState();
            }
            if (renderURL.getMode() != null) {
                copy.mode = renderURL.getMode();
            }
            Map<String, String[]> changes = renderURL.getPublicNavigationalStateChanges();
            if (changes.size() > 0) {
                copy.setPublicParameters(changes);
            }
            view = copy.page.getDispatch();
        } else if (containerURL instanceof ActionURL) {
            ActionURL actionURL = (ActionURL) containerURL;
            ParametersStateString is = (ParametersStateString) actionURL.getInteractionState();
            Map<String, String[]> parameters = is != null ? is.getParameters() : null;
            view = getDispatch("action", containerURL.getWindowState(), containerURL.getMode(), parameters);
            if (page.getParameters().size() > 0) {
                HashMap<String, String[]> a = new HashMap<String, String[]>(page.getParameters().size());
                for (Map.Entry<QName, String[]> b : page.parameters()) {
                    a.put(b.getKey().getLocalPart(), b.getValue());
                }
                Encoder encoder = new Encoder(a);
                view.setParameter(ENCODING, "javax.portlet.p", encoder.encode());
            }
        } else {
            ResourceURL resourceURL = (ResourceURL) containerURL;
            CacheLevel level = resourceURL.getCacheability();

            ParametersStateString rs = (ParametersStateString) resourceURL.getResourceState();
            Map<String, String[]> parameters = rs != null ? rs.getParameters() : null;
            view = getDispatch("resource", windowState, mode, parameters);

            String id = resourceURL.getResourceId();

            if (level == CacheLevel.PORTLET || level == CacheLevel.PAGE) {



                if (level == CacheLevel.PAGE) {
                }
            }
        }
        return view.toString();
    }

    @Override
    public void renderURL(Writer writer, ContainerURL containerURL, URLFormat format) throws IOException {
        throw new UnsupportedOperationException("todo");
    }

    /**
     * Encode the navigational state of the window in the dispatch object.
     *
     * @param dispatch the dispatch
     */
    void encode(Phase.View.Dispatch dispatch) {
        if (parameters != null && parameters.size() > 0) {
            Encoder encoder = new Encoder(parameters);
            String encoded = encoder.encode();
            dispatch.setParameter(WindowState.ENCODING, "javax.portlet.p." + name, new String[]{encoded});
        }
        if (windowState != null && !windowState.equals(org.gatein.pc.api.WindowState.NORMAL)) {
            dispatch.setParameter("javax.portlet.w." + name, windowState.toString());
        }
        if (mode != null && !mode.equals(Mode.VIEW)) {
            dispatch.setParameter("javax.portlet.m." + name, mode.toString());
        }
    }

    @Override
    public String toString() {
        return "WindowState[name=" + name + ",parameters=" + parameters + "]";
    }
}
