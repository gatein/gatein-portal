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
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import juzu.Response;
import juzu.impl.common.PercentCodec;
import juzu.io.Encoding;
import juzu.request.Phase;
import org.gatein.portal.page.spi.RenderTask;
import org.gatein.portal.page.spi.WindowContent;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class WindowContext {

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

    /** The related page. */
    public final PageContext page;

    /** . */
    public final String name;

    /** The intrisic state. */
    public final WindowContent state;

    WindowContext(String name, WindowContent state, PageContext page) {
        this.name = name;
        this.page = page;
        this.state = state;
    }

    public String resolveTitle(Locale locale) {
        return state.resolveTitle(locale);
    }

    public Iterable<Map.Entry<QName, String[]>> getPublicParametersChanges(Map<String, String[]> changes) {
        return state.getPublicParametersChanges(changes);
    }

    public Response processAction(
            String windowState,
            String mode,
            Map<String, String[]> interactionState) {
        return page.portletManager.processAction(this, windowState, mode, interactionState);
    }

    /**
     * Capture some context here.
     *
     * @return the callable for rendering a portlet
     */
    public RenderTask createRenderTask() {
        return page.portletManager.createRender(this);
    }

    public Response serveResource(String id, Map<String, String[]> resourceState) {
        return page.portletManager.serveResource(this, id, resourceState);
    }

    public Map<String, String[]> computePublicParameters() {
        Map<String, String[]> publicParameters;
        if (page.hasParameters()) {
            publicParameters = state.computePublicParameters(page.getParameters());
        } else {
            publicParameters = null;
        }
        return publicParameters;
    }

    /**
     * Encode the state of this window in the dispatch object.
     *
     * @param dispatch the dispatch
     */
    public void encode(Phase.View.Dispatch dispatch) {
        encode(dispatch, state);
    }

    /**
     * Encode the provided state in the dispatch object for the current window
     *
     * @param dispatch the dispatch
     * @param state the state to encode
     */
    public void encode(Phase.View.Dispatch dispatch, WindowContent state) {
        encode(dispatch, state.getParameters(), state.getWindowState(), state.getMode());
    }

    /**
     * Encode the provided state in the dispatch object for the current window
     *
     * @param dispatch the dispatch
     * @param parameters the parameters
     * @param windowState the window state
     * @param mode the mode
     */
    public void encode(Phase.View.Dispatch dispatch, String parameters, String windowState, String mode) {
        if (parameters != null) {
            dispatch.setParameter(WindowContext.ENCODING, "javax.portlet.p." + name, parameters);
        }
        if (windowState != null) {
            dispatch.setParameter("javax.portlet.w." + name, windowState);
        }
        if (mode != null) {
            dispatch.setParameter("javax.portlet.m." + name, mode);
        }
    }

    @Override
    public String toString() {
        return "WindowState[name=" + name + ",parameters=" + state.getParameters() + "]";
    }
}
