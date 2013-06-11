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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import juzu.impl.common.PercentCodec;
import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import juzu.io.Encoding;
import juzu.request.Phase;
import org.gatein.common.i18n.LocalizedString;
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
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.NavigationInfo;
import org.gatein.pc.api.info.ParameterInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.invocation.ActionInvocation;
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
import org.gatein.portal.servlet.Context;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class WindowContext implements PortletInvocationContext {

    /** . */
    private static final String[] TITLE_KEYS = { MetaInfo.TITLE, MetaInfo.SHORT_TITLE, MetaInfo.DISPLAY_NAME };

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
    public final PageContext page;

    /** The intrisic state. */
    public final WindowData state;

    /** The related lazy loaded portlet and cached. */
    private Portlet portlet;

    WindowContext(WindowData state, PageContext page) {
        this.page = page;
        this.state = state;
        this.portlet = null;
    }

    WindowContext(WindowContext that, PageContext page) {
        this.page = page;
        this.state = new WindowData(that.state);
        this.portlet = null;
    }

    public Map<String, String[]> getParameters() {
        return state.getParameters();
    }

    public String[] getParameter(String name) {
        return state.getParameter(name);
    }

    public void setParameter(String name, String[] value) {
        state.setParameter(name, value);
    }

    public String resolveTitle(Locale locale) {
        Portlet portlet = getPortlet();
        PortletInfo info = portlet.getInfo();
        String resolved = resolveMetaValue(info, locale, TITLE_KEYS, 0);
        if (resolved == null) {
            resolved = info.getName();
        }
        return resolved;
    }

    private String resolveMetaValue(PortletInfo info, Locale locale, String[] keys, int index) {
        String resolved = null;
        if (index < keys.length) {
            LocalizedString display = info.getMeta().getMetaValue(keys[index]);
            if (display != null) {
                LocalizedString.Value value = display.getValue(locale, true);
                if (value != null) {
                    resolved = value.getString();
                    if (resolved == null) {
                        resolved = resolveMetaValue(info, locale, keys, index + 1);
                    }
                }
            }
        }
        return resolved;
    }

    public Iterable<Map.Entry<QName, String[]>> getPublicParametersChanges(Map<String, String[]> changes) {
        LinkedHashMap<QName, String[]> pc = new LinkedHashMap<QName, String[]>();
        NavigationInfo info = getPortlet().getInfo().getNavigation();
        for (Map.Entry<String, String[]> change : changes.entrySet()) {
            ParameterInfo parameterInfo = info.getPublicParameter(change.getKey());
            if (parameterInfo != null) {
                pc.put(parameterInfo.getName(), change.getValue());
            }
        }
        return pc.entrySet();
    }

    public PortletInvocationResponse processAction(
            org.gatein.pc.api.WindowState windowState,
            Mode mode,
            Map<String, String[]> interactionState) throws PortletInvokerException {
        ActionInvocation action = new ActionInvocation(this);
        action.setClientContext(new GateInClientContext());
        action.setPortalContext(new AbstractPortalContext());
        action.setInstanceContext(new AbstractInstanceContext(state.name, AccessMode.READ_ONLY));
        action.setWindowContext(new AbstractWindowContext(state.name));
        action.setUserContext(new AbstractUserContext());
        action.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
        action.setRequest(Context.getCurrentRequest());
        action.setResponse(Context.getCurrentResponse());
        action.setTarget(getPortlet().getContext());
        action.setMode(mode != null ? mode : Mode.VIEW);
        action.setWindowState(windowState != null ? windowState : org.gatein.pc.api.WindowState.NORMAL);
        action.setNavigationalState(ParametersStateString.create());
        action.setInteractionState(interactionState != null ? ParametersStateString.create(interactionState) : null);
        action.setPublicNavigationalState(computePublicParameters());

        //
        PortletInvoker invoker = page.portletManager.getInvoker();
        Request current = Request.getCurrent();
        ContextLifeCycle clf = current.suspend();
        try {
            return invoker.invoke(action);
        } finally {
            clf.resume();
        }
    }

    /**
     * Capture some context here.
     *
     * @return the callable for rendering a portlet
     */
    public RenderTask createRenderTask() {
        return new RenderTask(this);
    }

    public PortletInvocationResponse serveResource(String id, Map<String, String[]> resourceState) throws PortletInvokerException {

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
            if (page.hasParameters()) {
                cacheability = CacheLevel.PAGE;
            } else {
                cacheability = CacheLevel.PORTLET;
            }
        }

        //
        ResourceInvocation resource = new ResourceInvocation(this);
        resource.setResourceId(id);
        resource.setCacheLevel(cacheability);
        resource.setClientContext(new GateInClientContext());
        resource.setPortalContext(new AbstractPortalContext());
        resource.setInstanceContext(new AbstractInstanceContext(state.name, AccessMode.READ_ONLY));
        resource.setWindowContext(new AbstractWindowContext(state.name));
        resource.setUserContext(new AbstractUserContext());
        resource.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
        resource.setRequest(Context.getCurrentRequest());
        resource.setResponse(Context.getCurrentResponse());
        resource.setTarget(getPortlet().getContext());
        resource.setMode(mode);
        resource.setWindowState(windowState);
        resource.setNavigationalState(navigationalState);
        resource.setResourceState(resourceState != null ? ParametersStateString.create(resourceState) : null);
        resource.setPublicNavigationalState(computePublicParameters());
        return page.portletManager.getInvoker().invoke(resource);
    }

    Map<String, String[]> computePublicParameters() {
        Map<String, String[]> publicParameters = null;
        if (page.hasParameters()) {
            publicParameters = NO_PARAMETERS;
            NavigationInfo info = getPortlet().getInfo().getNavigation();
            for (Map.Entry<QName, String[]> parameter : page.getParameters().entrySet()) {
                ParameterInfo parameterInfo = info.getPublicParameter(parameter.getKey());
                if (parameterInfo != null) {
                    if (publicParameters == NO_PARAMETERS) {
                        publicParameters = new HashMap<String, String[]>();
                    }
                    publicParameters.put(parameterInfo.getId(), parameter.getValue());
                }
            }
        }
        return publicParameters;
    }

    public Portlet getPortlet() {

        if (portlet == null) {
            // Load the customization
            CustomizationContext<org.exoplatform.portal.pom.spi.portlet.Portlet> portletCustomization = page.customizationService.loadCustomization(state.id);

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
            PageContext.Builder a = page.builder();
            WindowData copy = a.getWindow(state.name);
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
                a.apply(getPublicParametersChanges(changes));
            }
            view = a.build(page.customizationService, page.portletManager).getDispatch();
        } else if (containerURL instanceof ActionURL) {
            ActionURL actionURL = (ActionURL) containerURL;
            ParametersStateString is = (ParametersStateString) actionURL.getInteractionState();
            Map<String, String[]> parameters = is != null ? is.getParameters() : null;
            view = dispatchOf("action", containerURL.getWindowState(), containerURL.getMode(), parameters, CacheLevel.PAGE);
        } else {
            ResourceURL resourceURL = (ResourceURL) containerURL;
            CacheLevel level = resourceURL.getCacheability();
            ParametersStateString rs = (ParametersStateString) resourceURL.getResourceState();
            Map<String, String[]> parameters = rs != null ? rs.getParameters() : null;
            view = dispatchOf("resource", state.windowState, state.mode, parameters, level);
            String id = resourceURL.getResourceId();
            if (id != null) {
                view.setParameter("javax.portlet.r", id);
            }
        }
        return view.toString();
    }

    private Phase.View.Dispatch dispatchOf(
            String phase,
            org.gatein.pc.api.WindowState windowState,
            Mode mode,
            Map<String, String[]> parameters,
            CacheLevel cacheability) {

        //
        String targetWindowState = windowState != null && !org.gatein.pc.api.WindowState.NORMAL.equals(windowState) ? windowState.toString() : null;
        String targetMode = mode != null && !Mode.VIEW.equals(mode) ? mode.toString() : null;
        Phase.View.Dispatch dispatch = Controller_.index(page.state.path, phase, state.name, targetWindowState, targetMode);

        // Encode according to cacheability
        if (cacheability == CacheLevel.PORTLET) {
            // Only encode this window
            encode(dispatch);
        } else if (cacheability == CacheLevel.PAGE) {

            // Encode all windows
            for (WindowContext w : page.windows) {
                w.encode(dispatch);
            }

            // Encode page parameters
            HashMap<String, String[]> a = new HashMap<String, String[]>(page.getParameters().size());
            for (Map.Entry<QName, String[]> b : page.getParameters().entrySet()) {
                a.put(b.getKey().getLocalPart(), b.getValue());
            }
            Encoder encoder = new Encoder(a);
            dispatch.setParameter(ENCODING, "javax.portlet.p", encoder.encode());
        }

        // Append provided parameters
        if (parameters != null) {
            for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                dispatch.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        return dispatch;
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
        if (state.parameters != null && state.parameters.size() > 0) {
            Encoder encoder = new Encoder(state.parameters);
            String encoded = encoder.encode();
            dispatch.setParameter(WindowContext.ENCODING, "javax.portlet.p." + state.name, new String[]{encoded});
        }
        if (state.windowState != null && !state.windowState.equals(org.gatein.pc.api.WindowState.NORMAL)) {
            dispatch.setParameter("javax.portlet.w." + state.name, state.windowState.toString());
        }
        if (state.mode != null && !state.mode.equals(Mode.VIEW)) {
            dispatch.setParameter("javax.portlet.m." + state.name, state.mode.toString());
        }
    }

    @Override
    public String toString() {
        return "WindowState[name=" + state.name + ",parameters=" + state.parameters + "]";
    }
}
