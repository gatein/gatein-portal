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
package org.gatein.portal.web.page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import juzu.request.Phase;
import org.gatein.portal.mop.customization.CustomizationContext;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.content.ContentProvider;
import org.gatein.portal.content.ProviderRegistry;
import org.gatein.portal.content.WindowContent;

/**
 * Encapsulate state and operations on a page.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageContext implements Iterable<Map.Entry<String, WindowContext>> {

    /**
     * The page builder.
     */
    public static class Builder {

        public Builder(String path) {
            this.state = new PageData(path);
        }

        public Builder(PageData state) {
            this.state = state;
        }

        /** . */
        protected final PageData state;

        /** A map of name -> window. */
        protected final HashMap<String, WindowContent<?>> windows = new LinkedHashMap<String, WindowContent<?>>();

        public WindowContent<?> getWindow(String name) {
            return windows.get(name);
        }

        public void setWindow(String name, WindowContent<?> window) {
            windows.put(name, window);
        }

        public void apply(Iterable<Map.Entry<QName, String[]>> changes) {
            state.apply(changes);
        }

        public void setParameters(Map<String, String[]> parameters) {
            HashMap<QName, String[]> prp = new HashMap<QName, String[]>(parameters.size());
            for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                prp.put(new QName(parameter.getKey()), parameter.getValue());
            }
            state.setParameters(prp);
        }

        public void setQNameParameters(Map<QName, String[]> parameters) {
            state.setParameters(parameters);
        }

        public PageContext build() {
            PageContext context = new PageContext(state);
            for (Map.Entry<String, WindowContent<?>> window : windows.entrySet()) {
//                Serializable state = windowStates.get(window.getKey());
                context.windowMap.put(window.getKey(), new WindowContext(window.getKey(), window.getValue(), null, context));
            }
            return context;
        }
    }

    public static class ModelBuilder extends Builder {

        /** A map of name -> state. */
        private final IdentityHashMap<WindowContent<?>, CustomizationContext<?>> windowStates = new IdentityHashMap<WindowContent<?>, CustomizationContext<?>>();

        public ModelBuilder(String path) {
            super(path);
        }

        public ModelBuilder(PageData state) {
            super(state);
        }

        public NodeModel<NodeState, ElementState> asModel(
                final ProviderRegistry providerRegistry,
                final CustomizationService customizationService) {
            return new NodeModel<NodeState, ElementState>() {
                @Override
                public NodeContext<NodeState, ElementState> getContext(NodeState node) {
                    return node.context;
                }
                @Override
                public NodeState create(NodeContext<NodeState, ElementState> context) {
                    if (context.getState() instanceof ElementState.Window) {
                        CustomizationContext<?> customization = customizationService.loadCustomization(context.getId());
                        String contentId = customization.getContentId();
                        NodeState window = new NodeState(context);
                        ContentProvider<?> contentProvider = providerRegistry.resolveProvider(customization.getContentType().getValue());
                        WindowContent<?> windowState = contentProvider.getContent(contentId);
                        windows.put(window.context.getName(), windowState);
                        windowStates.put(windowState, customization);
                        return window;
                    } else {
                        return new NodeState(context);
                    }
                }
            };
        }

        public CustomizationContext<?> getCustomization(WindowContext<?> window) {
            return windowStates.get(window.content);
        }

        public PageContext build() {
            PageContext context = new PageContext(state);
            for (Map.Entry<String, WindowContent<?>> entry : windows.entrySet()) {
                WindowContent<?> window = entry.getValue();
                String windowName = entry.getKey();
                CustomizationContext<?> customization = windowStates.get(window);
                Serializable windowState = customization != null ? customization.getState() : null;
                context.windowMap.put(windowName, new WindowContext(windowName, window, windowState, context));
            }
            return context;
        }
    }

    /** The canonical navigation path. */
    public final PageData state;

    /** A map of name -> window. */
    private final HashMap<String, WindowContext> windowMap;

    /** Windows iteration. */
    protected final Iterable<WindowContext> windows;

    public PageContext(PageData state) {
        this.state = state;
        this.windowMap = new LinkedHashMap<String, WindowContext>();
        this.windows = windowMap.values();
    }

    public Builder builder() {

        Builder builder = new Builder(new PageData(state));

        // Clone the windows
        for (Map.Entry<String, WindowContext> entry : windowMap.entrySet()) {
            WindowContext window = entry.getValue();
            builder.windows.put(window.name, window.content.copy());
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
