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

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
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
import org.gatein.portal.content.WindowContentContext;

/**
 * @author Julien Viet
 */
class GateInPortletInvocationContext implements PortletInvocationContext {

    /** . */
    final PortletContentProvider portletManager;

    /** . */
    private final WindowContentContext<org.exoplatform.portal.pom.spi.portlet.Portlet> window;

    /** . */
    private final ContextLifeCycle lifeCycle;

    /** . */
    private final PortletContent state;

    GateInPortletInvocationContext(PortletContentProvider portletManager, PortletContent state, WindowContentContext<org.exoplatform.portal.pom.spi.portlet.Portlet> window, ContextLifeCycle lifeCycle) {
        this.portletManager = portletManager;
        this.window = window;
        this.lifeCycle = lifeCycle;
        this.state = state;
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
        Request current = Request.getCurrent();
        if (current != null) {
            ContextLifeCycle currentLF = current.suspend();
            try {
                return doRenderURL(containerURL, format);
            } finally {
                currentLF.resume();
            }
        } else {
            return doRenderURL(containerURL, format);
        }
    }

    private String doRenderURL(ContainerURL containerURL, URLFormat format) {
        lifeCycle.resume();
        try {
            return getDispatch(containerURL).toString();
        } finally {
            Request.getCurrent().suspend();
        }
    }

    private String getDispatch(ContainerURL containerURL) {
        if (containerURL instanceof RenderURL) {
            RenderURL renderURL = (RenderURL) containerURL;
            ParametersStateString ns = (ParametersStateString) renderURL.getNavigationalState();
            PortletContent copy = null;
            if (ns != null) {
                copy = new PortletContent(state);
                copy.parameters = ns.getParameters();
            }
            if (renderURL.getWindowState() != null) {
                if (copy == null) {
                    copy = new PortletContent(state);
                }
                copy.windowState = renderURL.getWindowState();
            }
            if (renderURL.getMode() != null) {
                if (copy == null) {
                    copy = new PortletContent(state);
                }
                copy.mode = renderURL.getMode();
            }
            return window.createRenderURL(copy, renderURL.getPublicNavigationalStateChanges());
        } else if (containerURL instanceof ActionURL) {
            ActionURL actionURL = (ActionURL) containerURL;
            ParametersStateString is = (ParametersStateString) actionURL.getInteractionState();
            String targetWindowState = containerURL.getWindowState() != null && !org.gatein.pc.api.WindowState.NORMAL.equals(containerURL.getWindowState()) ? containerURL.getWindowState().toString() : null;
            String targetMode = containerURL.getMode() != null && !Mode.VIEW.equals(containerURL.getMode()) ? containerURL.getMode().toString() : null;
            return window.createActionURL(is != null ? is.getParameters() : null, targetWindowState, targetMode);
        } else {
            ResourceURL resourceURL = (ResourceURL) containerURL;
            CacheLevel level = resourceURL.getCacheability();
            ParametersStateString rs = (ParametersStateString) resourceURL.getResourceState();
            Map<String, String[]> parameters = rs != null ? rs.getParameters() : null;
            return window.creatResourceURL(level, parameters, resourceURL.getResourceId());
        }
    }

    @Override
    public void renderURL(Writer writer, ContainerURL containerURL, URLFormat format) throws IOException {
        throw new UnsupportedOperationException("todo");
    }
}
