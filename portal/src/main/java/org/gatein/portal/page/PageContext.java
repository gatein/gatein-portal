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
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.portlet.PortletAppManager;

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

        public Builder(String path) {
            this.state = new PageState(path);
        }

        public Builder(PageState state) {
            this.state = state;
        }

        /** . */
        private PageState state;

        /** A map of name -> window. */
        private final HashMap<String, WindowState> windows = new LinkedHashMap<String, WindowState>();

        public WindowState getWindow(String name) {
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
                NodeState window = new NodeState(context);
                WindowState windowState = new WindowState(window);
                windows.put(windowState.name, windowState);
                return window;
            } else {
                return new NodeState(context);
            }
        }

        public PageContext build(CustomizationService customizationService, PortletAppManager portletManager) {
            return new PageContext(this, customizationService, portletManager);
        }
    }

    /** . */
    final PortletAppManager portletManager;

    /** . */
    final CustomizationService customizationService;

    /** The canonical navigation path. */
    public final PageState state;

    /** A map of name -> window. */
    private final HashMap<String, WindowContext> windowMap;

    /** Windows iteration. */
    public final Iterable<WindowContext> windows;

    public PageContext(
            Builder builder,
            CustomizationService customizationService,
            PortletAppManager portletManager) {

        //
        LinkedHashMap<String, WindowContext> a = new LinkedHashMap<String, WindowContext>(builder.windows.size());
        for (WindowState state : builder.windows.values()) {
            a.put(state.name, new WindowContext(state, this));
        }

        //
        this.customizationService = customizationService;
        this.portletManager = portletManager;
        this.state = builder.state;
        this.windowMap = a;
        this.windows = a.values();
    }

    public Builder builder() {

        Builder builder = new Builder(new PageState(state));

        // Clone the windows
        for (Map.Entry<String, WindowContext> entry : windowMap.entrySet()) {
            WindowContext window = entry.getValue();
            builder.windows.put(window.state.name, window.state);
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
