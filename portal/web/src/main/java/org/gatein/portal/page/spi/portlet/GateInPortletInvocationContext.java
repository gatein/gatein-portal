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
        Phase.View.Dispatch view;
        if (containerURL instanceof RenderURL) {
            RenderURL renderURL = (RenderURL) containerURL;
            PageContext.Builder a = window.page.builder();

            // Remove this nasty cast
            PortletContent copy = (PortletContent) a.getWindow(state.getName());

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
            view = a.build().getDispatch();
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
            view = window.dispatchOf("resource", state.getWindowState(), state.getMode(), parameters, level);
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
        String targetWindowState = windowState != null && !org.gatein.pc.api.WindowState.NORMAL.equals(windowState) ? windowState.toString() : null;
        String targetMode = mode != null && !Mode.VIEW.equals(mode) ? mode.toString() : null;
        return window.dispatchOf(phase, targetWindowState, targetMode, parameters, cacheability);
    }

    @Override
    public void renderURL(Writer writer, ContainerURL containerURL, URLFormat format) throws IOException {
        throw new UnsupportedOperationException("todo");
    }
}
