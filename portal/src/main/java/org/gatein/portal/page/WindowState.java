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
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
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
public class WindowState implements Iterable<Map.Entry<String, String[]>>, PortletInvocationContext {

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

    /** The window state (parameters). */
    Map<String, String[]> parameters;

    /** The portlet window state. */
    org.gatein.pc.api.WindowState windowState;

    /** The portlet window state. */
    org.gatein.pc.api.Mode mode;

    /** The related portlet lazy loaded. */
    private Portlet portlet;

    WindowState(NodeState node, PageState page) {

        //
        this.page = page;
        this.name = node.context.getName();
        this.id = node.context.getId();
        this.parameters = NO_PARAMETERS;
        this.windowState = org.gatein.pc.api.WindowState.NORMAL;
        this.mode = Mode.VIEW;
        this.portlet = null;
    }

    WindowState(WindowState that, PageState page) {

        //
        Map<String, String[]> parameters;
        if (that.parameters.isEmpty()) {
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
        this.windowState = org.gatein.pc.api.WindowState.NORMAL;
        this.mode = Mode.VIEW;
        this.portlet = null;
    }

    @Override
    public Iterator<Map.Entry<String, String[]>> iterator() {
        return parameters.entrySet().iterator();
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public String[] getParameter(String name) {
        return parameters.get(name);
    }

    public void setParameter(String name, String[] value) {
        if (parameters == NO_PARAMETERS) {
            parameters = new HashMap<String, String[]>();
        }
        parameters.put(name, value);
    }

    public PortletInvocationResponse processAction(
            org.gatein.pc.api.WindowState windowState,
            Mode mode,
            Map<String, String[]> interactionState) throws PortletInvokerException {
        ActionInvocation invocation = createAction();
        invocation.setMode(mode);
        invocation.setWindowState(windowState);
        invocation.setNavigationalState(ParametersStateString.create());
        invocation.setInteractionState(interactionState != null ? ParametersStateString.create(interactionState) : null);
        return page.portletManager.getInvoker().invoke(invocation);
    }

    public PortletInvocationResponse render() throws PortletInvokerException {
        RenderInvocation render = createRender();
        render.setMode(mode);
        render.setWindowState(windowState);
        render.setNavigationalState(ParametersStateString.create(parameters));
        return page.portletManager.getInvoker().invoke(render);
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

    private Phase.View.Dispatch getDispatch(String action, String windowState, String mode, Map<String, String[]> parameters) {
        Phase.View.Dispatch view = Controller_.index(page.path, action, name, windowState, mode);
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
        String targetWindowState = null;
        org.gatein.pc.api.WindowState windowState = containerURL.getWindowState();
        if (windowState != null && !org.gatein.pc.api.WindowState.NORMAL.equals(windowState)) {
            targetWindowState = windowState.toString();
        }
        String targetMode = null;
        org.gatein.pc.api.Mode mode = containerURL.getMode();
        if (mode != null && !Mode.VIEW.equals(mode)) {
            targetMode = mode.toString();
        }
        if (containerURL instanceof RenderURL) {
            RenderURL renderURL = (RenderURL) containerURL;
            WindowState copy = new PageState(page).get(name);
            ParametersStateString ns = (ParametersStateString) renderURL.getNavigationalState();
            if (ns != null) {
                copy.parameters = ns.getParameters();
            }
            if (windowState != null) {
                copy.windowState = windowState;
            }
            if (mode != null) {
                copy.mode = mode;
            }
            view = copy.page.getDispatch();
        } else if (containerURL instanceof ActionURL) {
            ActionURL actionURL = (ActionURL) containerURL;
            ParametersStateString is = (ParametersStateString) actionURL.getInteractionState();
            Map<String, String[]> parameters = is != null ? is.getParameters() : null;
            view = getDispatch("action", targetWindowState, targetMode, parameters);
        } else {
            throw new UnsupportedOperationException("Todo");
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
        if (parameters.size() > 0) {
            Encoder encoder = new Encoder(this);
            String encoded = encoder.encode();
            dispatch.setParameter(WindowState.ENCODING, "javax.portlet.p." + name, new String[]{encoded});
        }
        if (!windowState.equals(org.gatein.pc.api.WindowState.NORMAL)) {
            dispatch.setParameter("javax.portlet.w." + name, windowState.toString());
        }
        if (!mode.equals(Mode.VIEW)) {
            dispatch.setParameter("javax.portlet.m." + name, mode.toString());
        }
    }

    private ActionInvocation createAction() {
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
        return action;
    }

    private RenderInvocation createRender() {
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
        return render;
    }

    @Override
    public String toString() {
        return "WindowState[name=" + name + ",parameters=" + parameters + "]";
    }
}
