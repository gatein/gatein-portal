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
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.hierarchy.NodePersistence;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.mop.core.util.Tools;
import org.gatein.portal.mop.layout.ElementState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MopPersistence implements NodePersistence<ElementState> {

    /** . */
    private static final Set<String> propertiesBlackList = Tools.set("jcr:uuid", "jcr:primaryType");

    /** . */
    private static final Set<String> windowPropertiesBlackList = Tools.set(MappedAttributes.THEME.getName(),
            MappedAttributes.TYPE.getName(), MappedAttributes.ICON.getName(), MappedAttributes.WIDTH.getName(),
            MappedAttributes.HEIGHT.getName());

    /** . */
    final String[] EMPTY_STRING = new String[0];

    /** . */
    final POMSession session;

    public MopPersistence(POMSession session) {
        this.session = session;
    }

    @Override
    public NodeData<ElementState> loadNode(String nodeId) {
        UIComponent component = session.findObjectById(ObjectType.COMPONENT, nodeId);
        return create(component);
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
                    Utils.safeImmutableList(accessPermissions),
                    "dashboard".equals(attrs.getValue(MappedAttributes.TYPE))
            );
        } else if (type == ObjectType.WINDOW) {
            UIWindow window = (UIWindow) component;
            Attributes attrs = window.getAttributes();
            Customization<?> customization = window.getCustomization();
            ContentType<?> contentType = customization.getType();
            String customizationid = customization.getId();
            ApplicationType applicationType = ApplicationType.getType(contentType);
            PersistentApplicationState instanceState = new PersistentApplicationState(customizationid);
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
                    applicationType,
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
                    Utils.safeImmutableMap(properties),
                    Utils.safeImmutableList(accessPermissions));
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
        UIComponent component = session.findObjectById(ObjectType.COMPONENT, targetId);
        UIContainer parent = component.getParent();
        parent.getComponents().remove(component);
        return create(parent);
    }

    private void updateNode(UIComponent component, ElementState state) {
        if (component instanceof UIContainer) {
            UIContainer container = (UIContainer) component;
            ElementState.Container containerState = (ElementState.Container) state;
            ProtectedResource pr = container.adapt(ProtectedResource.class);
            pr.setAccessPermissions(containerState.accessPermissions);
            Described described = container.adapt(Described.class);
            described.setName(containerState.title);
            described.setDescription(containerState.description);
            Attributes dstAttrs = container.getAttributes();
            dstAttrs.setValue(MappedAttributes.ID, containerState.id);
            dstAttrs.setValue(MappedAttributes.TYPE, containerState.dashboard ? "dashboard" : null);
            dstAttrs.setValue(MappedAttributes.ICON, containerState.icon);
            dstAttrs.setValue(MappedAttributes.TEMPLATE, containerState.template);
            dstAttrs.setValue(MappedAttributes.FACTORY_ID, containerState.factoryId);
            dstAttrs.setValue(MappedAttributes.WIDTH, containerState.width);
            dstAttrs.setValue(MappedAttributes.HEIGHT, containerState.height);
            dstAttrs.setValue(MappedAttributes.NAME, containerState.name);
        } else if (component instanceof UIWindow) {
            UIWindow window = (UIWindow) component;
            ElementState.Window windowState = (ElementState.Window) state;
            ProtectedResource pr = window.adapt(ProtectedResource.class);
            pr.setAccessPermissions(windowState.accessPermissions);
            Described described = window.adapt(Described.class);
            described.setName(windowState.title);
            described.setDescription(windowState.description);
            Attributes attrs = window.getAttributes();
            attrs.setValue(MappedAttributes.SHOW_INFO_BAR, windowState.showInfoBar);
            attrs.setValue(MappedAttributes.SHOW_WINDOW_STATE, windowState.showApplicationState);
            attrs.setValue(MappedAttributes.SHOW_MODE, windowState.showApplicationMode);
            attrs.setValue(MappedAttributes.THEME, windowState.theme);
            attrs.setValue(MappedAttributes.ICON, windowState.icon);
            attrs.setValue(MappedAttributes.WIDTH, windowState.width);
            attrs.setValue(MappedAttributes.HEIGHT, windowState.height);
            save(windowState.properties, attrs, windowPropertiesBlackList);
            ApplicationState instanceState = windowState.state;
            // We modify only transient portlet state
            // and we ignore any persistent portlet state
            if (instanceState instanceof TransientApplicationState) {
                //
                TransientApplicationState transientState = (TransientApplicationState) instanceState;
                // The current site
                Site currentSite = window.getPage().getSite();
                // The content id
                String contentId = transientState.getContentId();
                ContentType contentType = windowState.type.getContentType();
                // The customization that we will inherit from if not null
                Customization<?> customization = null;
                // Destroy existing window previous customization
                if (window.getCustomization() != null) {
                    window.getCustomization().destroy();
                }
                // If the existing customization is not null and matches the content id
                Customization dstCustomization;
                if (customization != null && customization.getType().equals(contentType)
                        && customization.getContentId().equals(contentId)) {
                    // If it's a customization of the current site we extend it
                    if (customization.getContext() == currentSite) {
                        dstCustomization = window.customize(customization);
                    } else {
                        // Otherwise we clone it propertly
                        Object state_ = customization.getVirtualState();
                        dstCustomization = window.customize(contentType, contentId, state_);
                    }
                } else {
                    // Otherwise we create an empty customization
                    dstCustomization = window.customize(contentType, contentId, null);
                }
                // At this point we have customized the window
                // now if we have any additional state payload we must merge it
                // with the current state
                Object state_ = ((TransientApplicationState) instanceState).getContentState();
                if (state_ != null) {
                    dstCustomization.setState(state_);
                }
            } else if (instanceState instanceof CloneApplicationState) {
                CloneApplicationState cloneState = (CloneApplicationState) instanceState;
                Customization<?> customization = session.findCustomizationById(cloneState.getStorageId());
                window.customize(customization);
            } else if (instanceState instanceof PersistentApplicationState) {
                // Do nothing
            } else {
                throw new IllegalArgumentException("Cannot save application with state " + instanceState);
            }
        }
    }

    @Override
    public NodeData<ElementState> updateNode(String targetId, ElementState state) {
        UIComponent target = session.findObjectById(ObjectType.COMPONENT, targetId);
        updateNode(target, state);
        return create(target);
    }

    @Override
    public NodeData<ElementState>[] moveNode(String targetId, String fromId, String toId, String previousId) {
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
    public void close() {
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

    public static void save(Map<String, String> src, Attributes dst, Set<String> blackList) {
        for (Map.Entry<String, String> property : src.entrySet()) {
            String name = property.getKey();
            if (!blackList.contains(name) && !propertiesBlackList.contains(name)) {
                dst.setString(name, property.getValue());
            }
        }
    }
}
