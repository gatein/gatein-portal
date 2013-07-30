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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import juzu.request.Phase;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.gatein.portal.mop.customization.CustomizationContext;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.page.spi.ContentProvider;
import org.gatein.portal.page.spi.WindowContent;

/**
 * Encapsulate state and operations on a page.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageContext implements Iterable<Map.Entry<String, WindowContext>> {

    /**
     * The page builder.
     */
    public static class Builder implements NodeModel<NodeState, ElementState> {

        /** . */
        private final ContentProvider contentProvider;

        /** . */
        private final CustomizationService customizationService;

        public Builder(ContentProvider contentProvider, CustomizationService customizationService, String path) {
            this.contentProvider = contentProvider;
            this.customizationService = customizationService;
            this.state = new PageData(path);
        }

        public Builder(ContentProvider contentProvider, CustomizationService customizationService, PageData state) {
            this.contentProvider = contentProvider;
            this.customizationService = customizationService;
            this.state = state;
        }

        /** . */
        private PageData state;

        /** A map of name -> window. */
        private final HashMap<String, WindowContent> windows = new LinkedHashMap<String, WindowContent>();

        public WindowContent getWindow(String name) {
            return windows.get(name);
        }

        @Override
        public NodeContext<NodeState, ElementState> getContext(NodeState node) {
            return node.context;
        }

        public void apply(Iterable<Map.Entry<QName, String[]>> changes) {
            state.apply(changes);
        }

        public void setParameters(Map<QName, String[]> parameters) {
            state.setParameters(parameters);
        }

        @Override
        public NodeState create(NodeContext<NodeState, ElementState> context) {
            if (context.getState() instanceof ElementState.Window) {
                CustomizationContext<Portlet> portletCustomization = customizationService.loadCustomization(context.getId());
                String contentId = portletCustomization.getContentId();
                NodeState window = new NodeState(context);
                WindowContent windowState = contentProvider.getContent(contentId, window);
                windows.put(window.context.getName(), windowState);
                return window;
            } else {
                return new NodeState(context);
            }
        }

        public PageContext build() {
            return new PageContext(this, customizationService, contentProvider);
        }
    }

    /** . */
    final ContentProvider portletManager;

    /** . */
    final CustomizationService customizationService;

    /** The canonical navigation path. */
    public final PageData state;

    /** A map of name -> window. */
    private final HashMap<String, WindowContext> windowMap;

    /** Windows iteration. */
    public final Iterable<WindowContext> windows;

    public PageContext(
            Builder builder,
            CustomizationService customizationService,
            ContentProvider portletManager) {

        //
        LinkedHashMap<String, WindowContext> a = new LinkedHashMap<String, WindowContext>(builder.windows.size());
        for (Map.Entry<String, WindowContent> window : builder.windows.entrySet()) {
            a.put(window.getKey(), new WindowContext(window.getKey(), window.getValue(), this));
        }

        //
        this.customizationService = customizationService;
        this.portletManager = portletManager;
        this.state = builder.state;
        this.windowMap = a;
        this.windows = a.values();
    }

    public Builder builder() {

        Builder builder = new Builder(portletManager, customizationService, new PageData(state));

        // Clone the windows
        for (Map.Entry<String, WindowContext> entry : windowMap.entrySet()) {
            WindowContext window = entry.getValue();
            builder.windows.put(window.name, window.state.copy());
        }

        //
        return builder;
    }

    public WindowContext get(String name) {
        return windowMap.get(name);
    }

    @Override
    public Iterator<Map.Entry<String, WindowContext>> iterator() {
        return windowMap.entrySet().iterator();
    }

    public boolean hasParameters() {
        return state.getParameters() != null;
    }

    public Map<QName, String[]> getParameters() {
        return state.getParameters();
    }

    //

    public void encodeParameters(Phase.View.Dispatch dispatch) {
        Map<QName, String[]> parameters = state.getParameters();
        HashMap<String, String[]> a = new HashMap<String, String[]>(parameters.size());
        for (Map.Entry<QName, String[]> b : parameters.entrySet()) {
            a.put(b.getKey().getLocalPart(), b.getValue());
        }
        Encoder encoder = new Encoder(a);
        dispatch.setParameter(WindowContext.ENCODING, "javax.portlet.p", encoder.encode());
    }

    public Phase.View.Dispatch getDispatch() {
        Phase.View.Dispatch view = Controller_.index(state.path, null, null, null, null);
        for (WindowContext w : windows) {
            w.encode(view);
        }
        if (hasParameters()) {
            for (Map.Entry<QName, String[]> parameter : state.getParameters().entrySet()) {
                view.setParameter(parameter.getKey().getLocalPart(), parameter.getValue());
            }
        }
        return view;
    }
}
