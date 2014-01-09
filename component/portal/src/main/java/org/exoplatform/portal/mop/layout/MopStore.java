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

package org.exoplatform.portal.mop.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.mop.core.util.Tools;
import org.gatein.portal.mop.hierarchy.NodeStore;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutStore;
import org.gatein.portal.mop.Properties;
import org.gatein.portal.mop.Property;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MopStore implements LayoutStore, NodeStore<ElementState> {

    /** . */
    private static final Set<String> propertiesBlackList = Tools.set("jcr:uuid", "jcr:primaryType");

    /** . */
    private static final Set<String> windowPropertiesBlackList = Tools.set(MappedAttributes.THEME.getName(),
            MappedAttributes.TYPE.getName(), MappedAttributes.ICON.getName(), MappedAttributes.WIDTH.getName(),
            MappedAttributes.HEIGHT.getName());

    /** . */
    final String[] EMPTY_STRING = new String[0];

    /** . */
    final POMSessionManager mgr;

    public MopStore(POMSessionManager mgr) {
        this.mgr = mgr;
    }

    @Override
    public NodeStore<ElementState> begin(String rootId, boolean write) {
        return this;
    }

    @Override
    public void end(NodeStore<ElementState> store) {
        // Do nothing
    }

    @Override
    public NodeData<ElementState> loadNode(String nodeId) {
        POMSession session = mgr.getSession();
        UIComponent component = session.findObjectById(ObjectType.COMPONENT, nodeId);
        return component != null ? create(component) : null;
    }

    private NodeData<ElementState> create(UIComponent component) {
        UIContainer parent = component.getParent();
        String parentId = parent != null ? parent.getObjectId() : null;
        String[] children;
        if (component instanceof UIContainer) {
            UIContainer container = (UIContainer) component;
            List<UIComponent> components = container.getComponents();
            children = new String[components.size()];
            int index = 0;
            for (UIComponent child : components) {
                children[index++] = child.getObjectId();
            }
        } else {
            children = EMPTY_STRING;
        }
        ElementState state;
        ObjectType<? extends UIComponent> type = component.getObjectType();
        if (type == ObjectType.CONTAINER) {

            //
            List<String> accessPermissions = Collections.emptyList();
            if (component.isAdapted(ProtectedResource.class)) {
                ProtectedResource pr = component.adapt(ProtectedResource.class);
                accessPermissions = pr.getAccessPermissions();
            }

            //
            Described described = component.adapt(Described.class);
            Attributes attrs = component.getAttributes();
            state = new ElementState.Container(
                    attrs.getValue(MappedAttributes.ID),
                    attrs.getValue(MappedAttributes.NAME),
                    attrs.getValue(MappedAttributes.ICON),
                    attrs.getValue(MappedAttributes.TEMPLATE),
                    attrs.getValue(MappedAttributes.FACTORY_ID),
                    described.getName(),
                    described.getDescription(),
                    attrs.getValue(MappedAttributes.WIDTH),
                    attrs.getValue(MappedAttributes.HEIGHT),
                    "dashboard".equals(attrs.getValue(MappedAttributes.TYPE))
            );
        } else if (type == ObjectType.WINDOW) {
            UIWindow window = (UIWindow) component;
            Attributes attrs = window.getAttributes();
            Customization<?> customization = window.getCustomization();
            ContentType<?> contentType = customization.getType();
            ApplicationType applicationType = ApplicationType.getType(contentType);
            PersistentApplicationState instanceState = new PersistentApplicationState(window.getObjectId());
            HashMap<String, String> properties = new HashMap<String, String>();
            load(attrs, properties, windowPropertiesBlackList);
            List<String> accessPermissions = Collections.emptyList();
            if (window.isAdapted(ProtectedResource.class)) {
                ProtectedResource pr = window.adapt(ProtectedResource.class);
                accessPermissions = pr.getAccessPermissions();
            }
            Described described = window.adapt(Described.class);
            boolean showInfoBar = attrs.getValue(MappedAttributes.SHOW_INFO_BAR, false);
            boolean showWindowState = attrs.getValue(MappedAttributes.SHOW_WINDOW_STATE, false);
            boolean showMode = attrs.getValue(MappedAttributes.SHOW_MODE, false);
            String theme = attrs.getValue(MappedAttributes.THEME, null);
            state = new ElementState.Window(
                    org.gatein.portal.content.ContentType.forApplicationType(applicationType),
                    instanceState,
                    described.getName(),
                    attrs.getValue(MappedAttributes.ICON),
                    described.getDescription(),
                    showInfoBar,
                    showWindowState,
                    showMode,
                    theme,
                    attrs.getValue(MappedAttributes.WIDTH),
                    attrs.getValue(MappedAttributes.HEIGHT),
                    Utils.safeImmutableMap(properties));
        } else if (type == ObjectType.BODY) {
            state = new ElementState.Body();
        } else {
            throw new AssertionError();
        }
        return new NodeData<ElementState>(parentId, component.getObjectId(), component.getName(), state, children);
    }

    private ObjectType typeOf(ElementState state) {
        if (state instanceof ElementState.Body) {
            return ObjectType.BODY;
        } else if (state instanceof ElementState.Container) {
            return ObjectType.CONTAINER;
        } else if (state instanceof ElementState.Window) {
            return ObjectType.WINDOW;
        } else {
            throw new AssertionError("Should not be here");
        }
    }

    @Override
    public NodeData<ElementState>[] createNode(String parentId, String previousId, String name, ElementState state) {
        POMSession session = mgr.getSession();
        UIContainer parent = session.findObjectById(ObjectType.CONTAINER, parentId);
        UIComponent added;
        if (previousId != null) {
            UIComponent previous = session.findObjectById(ObjectType.COMPONENT, previousId);
            int index = parent.getComponents().indexOf(previous);
            added = parent.add(index + 1, typeOf(state), name);
        } else {
            added = parent.add(0, typeOf(state), name);
        }
        updateNode(added, state);
        return new NodeData[]{
                create(parent),
                create(added)
        };
    }

    @Override
    public NodeData<ElementState> destroyNode(String targetId) {
        POMSession session = mgr.getSession();
        UIComponent component = session.findObjectById(ObjectType.COMPONENT, targetId);
        UIContainer parent = component.getParent();
        parent.getComponents().remove(component);
        return create(parent);
    }

    private void updateNode(UIComponent component, ElementState state) {
        POMSession session = mgr.getSession();
        if (component instanceof UIContainer) {
            UIContainer container = (UIContainer) component;
            ElementState.Container containerState = (ElementState.Container) state;
            Described described = container.adapt(Described.class);
            described.setName(containerState.properties.get(ElementState.Container.TITLE));
            described.setDescription(containerState.properties.get(ElementState.Container.DESCRIPTION));
            Attributes dstAttrs = container.getAttributes();
            dstAttrs.setValue(MappedAttributes.ID, containerState.id);
            dstAttrs.setValue(MappedAttributes.TYPE, containerState.dashboard ? "dashboard" : null);
            dstAttrs.setValue(MappedAttributes.ICON, containerState.properties.get(ElementState.Container.ICON));
            dstAttrs.setValue(MappedAttributes.TEMPLATE, containerState.properties.get(ElementState.Container.TEMPLATE));
            dstAttrs.setValue(MappedAttributes.FACTORY_ID, containerState.properties.get(ElementState.Container.FACTORY_ID));
            dstAttrs.setValue(MappedAttributes.WIDTH, containerState.properties.get(ElementState.Container.WIDTH));
            dstAttrs.setValue(MappedAttributes.HEIGHT, containerState.properties.get(ElementState.Container.HEIGHT));
            dstAttrs.setValue(MappedAttributes.NAME, containerState.properties.get(ElementState.Container.NAME));
        } else if (component instanceof UIWindow) {
            UIWindow window = (UIWindow) component;
            ElementState.Window windowState = (ElementState.Window) state;
            Described described = window.adapt(Described.class);
            described.setName(windowState.properties.get(ElementState.Window.TITLE));
            described.setDescription(windowState.properties.get(ElementState.Window.DESCRIPTION));
            Attributes attrs = window.getAttributes();
            attrs.setValue(MappedAttributes.SHOW_INFO_BAR, windowState.properties.get(ElementState.Window.SHOW_INFO_BAR));
            attrs.setValue(MappedAttributes.SHOW_WINDOW_STATE, windowState.properties.get(ElementState.Window.SHOW_APPLICATION_STATE));
            attrs.setValue(MappedAttributes.SHOW_MODE, windowState.properties.get(ElementState.Window.SHOW_APPLICATION_MODE));
            attrs.setValue(MappedAttributes.THEME, windowState.properties.get(ElementState.Window.THEME));
            attrs.setValue(MappedAttributes.ICON, windowState.properties.get(ElementState.Window.ICON));
            attrs.setValue(MappedAttributes.WIDTH, windowState.properties.get(ElementState.Window.WIDTH));
            attrs.setValue(MappedAttributes.HEIGHT, windowState.properties.get(ElementState.Window.HEIGHT));
            save(windowState.properties, attrs, windowPropertiesBlackList);
            ApplicationState instanceState = windowState.state;
            if (instanceState instanceof TransientApplicationState) {
                if (window.getCustomization() != null) {
                    window.getCustomization().destroy();
                }
                TransientApplicationState transientState = (TransientApplicationState) instanceState;
                Customization dstCustomization = window.customize(windowState.type.getApplicationType().getContentType(), transientState.getContentId(), null);
                Object state_ = ((TransientApplicationState) instanceState).getContentState();
                if (state_ != null) {
                    dstCustomization.setState(state_);
                }
            } else if (instanceState instanceof CloneApplicationState) {
                CloneApplicationState cloneState = (CloneApplicationState) instanceState;
                UIWindow customization = (UIWindow)session.findObjectById(cloneState.getStorageId());
                window.customize(customization.getCustomization());
            } else if (instanceState instanceof PersistentApplicationState) {
                // We ignore any persistent portlet state
            } else {
                throw new IllegalArgumentException("Cannot save application with state " + instanceState);
            }
        }
    }

    @Override
    public NodeData<ElementState> updateNode(String targetId, ElementState state) {
        POMSession session = mgr.getSession();
        UIComponent target = session.findObjectById(ObjectType.COMPONENT, targetId);
        updateNode(target, state);
        return create(target);
    }

    @Override
    public NodeData<ElementState>[] moveNode(String targetId, String fromId, String toId, String previousId) {
        POMSession session = mgr.getSession();
        UIComponent moved = session.findObjectById(ObjectType.COMPONENT, targetId);
        UIContainer from = session.findObjectById(ObjectType.CONTAINER, fromId);
        UIContainer to = session.findObjectById(ObjectType.CONTAINER, toId);
        int index;
        if (previousId != null) {
            UIComponent previous = session.findObjectById(ObjectType.COMPONENT, previousId);
            index = to.getComponents().indexOf(previous) + 1;
        } else {
            index = 0;
        }
        to.getComponents().add(index, moved);
        return new NodeData[]{create(moved),create(from),create(to)};
    }

    @Override
    public NodeData<ElementState>[] renameNode(String targetId, String parentId, String name) {
        throw new UnsupportedOperationException("Does not make sense for now so should not be called");
    }

    @Override
    public void flush() {
    }

    private static void load(Attributes src, Map<String, String> dst, Set<String> blackList) {
        for (String name : src.getKeys()) {
            if (!blackList.contains(name) && !propertiesBlackList.contains(name)) {
                Object value = src.getObject(name);
                if (value instanceof String) {
                    dst.put(name, (String) value);
                }
            }
        }
    }

    public static void save(Properties src, Attributes dst, Set<String> blackList) {
        for (Property property : src) {
            String name = property.getName();
            if (!blackList.contains(name) && !propertiesBlackList.contains(name)) {
                dst.setObject(name, property.getValue());
            }
        }
    }
}
