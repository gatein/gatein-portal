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

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import juzu.request.Phase;
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.portal.page.Controller_;
import org.gatein.portal.page.Encoder;
import org.gatein.portal.page.PageContext;
import org.gatein.portal.page.WindowContext;

/**
 * @author Julien Viet
 */
class GateInPortletInvocationContext implements PortletInvocationContext {

    /** . */
    final PortletContentProvider portletManager;

    /** . */
    private final WindowContext window;

    /** . */
    private final PortletContent state;

    GateInPortletInvocationContext(PortletContentProvider portletManager, WindowContext window) {
        this.portletManager = portletManager;
        this.window = window;
        this.state = (PortletContent) window.state;
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
        Phase.View.Dispatch dispatch;
        if (containerURL instanceof RenderURL) {
            RenderURL renderURL = (RenderURL) containerURL;
            PageContext.Builder a = window.page.builder();

            // Remove this nasty cast
            PortletContent copy = (PortletContent) a.getWindow(window.name);

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
                a.apply(window.getPublicParametersChanges(changes));
            }
            dispatch = a.build().getDispatch();
        } else if (containerURL instanceof ActionURL) {
            ActionURL actionURL = (ActionURL) containerURL;
            ParametersStateString is = (ParametersStateString) actionURL.getInteractionState();
            PageContext page = window.page;
            String targetWindowState = containerURL.getWindowState() != null && !org.gatein.pc.api.WindowState.NORMAL.equals(containerURL.getWindowState()) ? containerURL.getWindowState().toString() : null;
            String targetMode = containerURL.getMode() != null && !Mode.VIEW.equals(containerURL.getMode()) ? containerURL.getMode().toString() : null;
            dispatch = Controller_.index(page.state.path, "action", window.name, targetWindowState, targetMode);

            // Encode all windows
            for (WindowContext w : page.windows) {
                w.encode(dispatch);
            }

            // Encode page parameters
            page.encodeParameters(dispatch);

            //
            if (is != null) {
                for (Map.Entry<String, String[]> parameter : is.getParameters().entrySet()) {
                    dispatch.setParameter(parameter.getKey(), parameter.getValue());
                }
            }
        } else {
            ResourceURL resourceURL = (ResourceURL) containerURL;
            CacheLevel level = resourceURL.getCacheability();
            ParametersStateString rs = (ParametersStateString) resourceURL.getResourceState();
            Map<String, String[]> parameters = rs != null ? rs.getParameters() : null;
            PageContext page = window.page;

            //
            dispatch = Controller_.index(page.state.path, "resource", window.name, state.getWindowState(), state.getMode());

            //
            if (level == CacheLevel.PORTLET || level == CacheLevel.PAGE) {

                // Encode this window
                String ww = window.state.getParameters();
                if (ww == null) {
                    ww = new Encoder(PortletContent.NO_PARAMETERS).encode();
                }
                window.encode(dispatch, ww, window.state.getWindowState(), window.state.getMode());

                //
                if (level == CacheLevel.PAGE) {

                    // Encode all windows
                    for (WindowContext w : page.windows) {
                        if (w != window) {
                            w.encode(dispatch);
                        }
                    }

                    // Encode page parameters
                    page.encodeParameters(dispatch);
                }
            }

            // Append provided parameters
            if (parameters != null) {
                for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                    dispatch.setParameter(parameter.getKey(), parameter.getValue());
                }
            }

            //
            String id = resourceURL.getResourceId();
            if (id != null) {
                dispatch.setParameter("javax.portlet.r", id);
            }
        }
        return dispatch.toString();
    }

    @Override
    public void renderURL(Writer writer, ContainerURL containerURL, URLFormat format) throws IOException {
        throw new UnsupportedOperationException("todo");
    }
}
