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
package org.gatein.portal.impl.mop.mongo;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.gatein.common.io.IOTools;
import org.gatein.common.util.Tools;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.mop.Properties;
import org.gatein.portal.mop.Property;
import org.gatein.portal.mop.ValueType;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.hierarchy.NodeStore;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutError;
import org.gatein.portal.mop.layout.LayoutServiceException;

/**
 * @author Julien Viet
 */
class MongoElementStore implements NodeStore<ElementState> {

    /** . */
    private final HashMap<String, NodeData<ElementState>> nodes;

    /** . */
    private final HashMap<String, DBObject> contents;

    /** . */
    private final String rootId;
    
    private final MongoSecurityStore securityStore;

    MongoElementStore(DBObject doc, MongoSecurityStore securityStore) {

        //
        HashMap<String, NodeData<ElementState>> nodes = new HashMap<String, NodeData<ElementState>>();
        HashMap<String, DBObject> contents = new HashMap<String, DBObject>();
        DBObject elements = (DBObject) doc.get("elements");
        for (String id : elements.keySet()) {
            DBObject element = (DBObject) elements.get(id);
            String parentId = (String) element.get("parent");
            String name = (String) element.get("name");
            String[] childrenIds;
            String type = (String) element.get("type");
            DBObject docState = (DBObject) element.get("state");
            ElementState state;
            if ("container".equals(type)) {
                List<String> children = (List<String>) element.get("children");
                if (children.isEmpty()) {
                    childrenIds = ElementState.EMPTY_STRINGS;
                } else {
                    childrenIds = children.toArray(new String[children.size()]);
                }
                Properties.Builder props = Properties.EMPTY.builder();
                for (String propertyName : docState.keySet()) {
                    Object propertyValue = docState.get(propertyName);
                    if (propertyValue instanceof String) {
                        props.set(propertyName, ValueType.STRING, (String) propertyValue);
                    } else if (propertyValue instanceof Boolean) {
                        props.set(propertyName, ValueType.BOOLEAN, (Boolean) propertyValue);
                    }
                }
                state = new ElementState.Container(
                        null,
                        props.build(),
                        false);
            } else {
                if ("window".equals(type)) {
                    Properties.Builder props = Properties.EMPTY.builder();
                    for (String propertyName : docState.keySet()) {
                        Object propertyValue = docState.get(propertyName);
                        if (propertyValue instanceof String) {
                            props.set(propertyName, ValueType.STRING, (String) propertyValue);
                        } else if (propertyValue instanceof Boolean) {
                            props.set(propertyName, ValueType.BOOLEAN, (Boolean) propertyValue);
                        }
                    }
                    DBObject content = (DBObject) element.get("content");
                    String _type = (String) content.get("type");
                    ContentType contentType = ContentType.forValue(_type);
                    if (contentType == null) {
                        contentType = ContentType.UNKNOWN;
                    }
                    state = new ElementState.Window(
                            contentType,
                            new PersistentApplicationState(id),
                            props.build());
                    contents.put(id, content);
                } else if ("body".equals(type)) {
                    state = new ElementState.Body();
                } else {
                    throw new AssertionError("Unknown type " + type);
                }
                childrenIds = ElementState.EMPTY_STRINGS;
            }
            NodeData<ElementState> node = new NodeData<ElementState>(parentId, id, name, state, childrenIds);
            nodes.put(id, node);
        }

        //
        this.rootId = doc.get("_id").toString();
        this.nodes = nodes;
        this.contents = contents;
        this.securityStore = securityStore;
    }

    MongoElementStore(String rootId, MongoSecurityStore securityStore) {

        //
        HashMap<String, NodeData<ElementState>> nodes = new HashMap<String, NodeData<ElementState>>();
        nodes.put(
                rootId,
                new NodeData<ElementState>(null, rootId, "", new ElementState.Container(
                        null, Properties.EMPTY, false
                ), ElementState.EMPTY_STRINGS));

        //
        this.rootId = rootId;
        this.nodes = nodes;
        this.contents = new HashMap<String, DBObject>();
        this.securityStore = securityStore;
    }

    DBObject assemble() {

        //
        DBObject elements = new BasicDBObject(nodes.size());
        for (NodeData<ElementState> node : nodes.values()) {
            DBObject element = new BasicDBObject();
            element.put("name", node.name);
            if (node.parentId != null) {
                element.put("parent", node.parentId);
            }
            setState(element, node.state);
            if (node.state instanceof ElementState.Container) {
                element.put("children", Tools.toList(node.iterator()));
            }
            elements.put(node.id, element);
        }

        // Assemble document
        DBObject root = new BasicDBObject();
        root.put("_id", new ObjectId(rootId));
        root.put("elements", elements);

        //
        return root;
    }

    private void setState(DBObject dom, ElementState state) {
        DBObject doc = new BasicDBObject();
        DBObject content;
        String type;
        if (state instanceof ElementState.Container) {
            type = "container";
            content = null;
            ElementState.Container containerState = (ElementState.Container) state;
            for (Property property : containerState.properties) {
                doc.put(property.getName(), property.getValue());
            }
        } else if (state instanceof ElementState.Window) {
            type = "window";
            ElementState.Window windowState = (ElementState.Window) state;
            for (Property property : windowState.properties) {
                doc.put(property.getName(), property.getValue());
            }
            ApplicationState instanceState = windowState.state;
            if (instanceState instanceof TransientApplicationState) {
                TransientApplicationState transientState = (TransientApplicationState) instanceState;
                content = new BasicDBObject();
                content.put("type", windowState.type.getValue());
                content.put("id", transientState.getContentId());
                Serializable state_ = ((TransientApplicationState) instanceState).getContentState();
                if (state_ != null) {
                    byte[] blob;
                    try {
                        blob = IOTools.serialize(state_);
                    } catch (IOException e) {
                        throw new LayoutServiceException(LayoutError.INTERNAL_ERROR, e);
                    }
                    content.put("state", blob);
                }
            } else if (instanceState instanceof CloneApplicationState) {
                // CloneApplicationState cloneState = (CloneApplicationState) instanceState;
                // UIWindow customization = (UIWindow)session.findObjectById(cloneState.getStorageId());
                // window.customize(customization.getCustomization());
                throw new UnsupportedOperationException("Not supported yet");
            } else if (instanceState instanceof PersistentApplicationState) {
                PersistentApplicationState<?> persistentApplicationState = (PersistentApplicationState<?>) instanceState;
                content = contents.get(persistentApplicationState.getStorageId());
            } else {
                throw new IllegalArgumentException("Cannot save application with state " + instanceState);
            }
        } else if (state instanceof ElementState.Body) {
            type = "body";
            content = null;
        } else {
            throw new UnsupportedOperationException("element " + state.getClass().getName() + " not supported");
        }

        //
        dom.put("type", type);
        dom.put("state", doc);
        if (content != null) {
            dom.put("content", content);
        }
    }

    @Override
    public NodeData<ElementState> loadNode(String nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public NodeData<ElementState>[] createNode(String parentId, String previousId, String name, ElementState state) {
        String id = UUID.randomUUID().toString();
        NodeData<ElementState> parent = nodes.get(parentId);
        NodeData<ElementState> node = new NodeData<ElementState>(parentId, id, name, state, ElementState.EMPTY_STRINGS);
        List<String> childrenIds = Tools.toList(parent.iterator());
        int index = previousId != null ? childrenIds.indexOf(previousId) + 1 : 0;
        childrenIds.add(index, id);
        nodes.put(parentId, parent = new NodeData<ElementState>(parent.parentId, parent.id, parent.name, parent.state, childrenIds.toArray(new String[childrenIds.size()])));
        nodes.put(id, node);
        return new NodeData[]{parent, node};
    }

    @Override
    public NodeData<ElementState> destroyNode(String targetId) {
        NodeData<ElementState> target = nodes.remove(targetId);
        String parentId = target.parentId;
        NodeData<ElementState> parent = nodes.get(parentId);
        List<String> children = Tools.toList(parent.iterator());
        children.remove(targetId);
        nodes.put(parentId, parent = parent.withChildren(children));
        securityStore.savePermission(targetId, null);
        return parent;
    }

    @Override
    public NodeData<ElementState> updateNode(String targetId, ElementState state) {
        NodeData<ElementState> target = nodes.get(targetId);
        nodes.put(targetId, target = target.withState(state));
        return target;
    }

    @Override
    public NodeData<ElementState>[] moveNode(String targetId, String fromId, String toId, String previousId) {
        NodeData<ElementState> target = nodes.get(targetId);
        NodeData<ElementState> from = nodes.get(fromId);
        NodeData<ElementState> to = nodes.get(toId);
        if (fromId.equals(toId)) {
            List<String> children = Tools.toList(from.iterator());
            children.remove(targetId);
            int index = previousId != null ? children.indexOf(previousId) + 1: 0;
            children.add(index, targetId);
            nodes.put(fromId, from = to = from.withChildren(children));
        } else {
            List<String> fromChildren = Tools.toList(from.iterator());
            List<String> toChildren = Tools.toList(to.iterator());
            fromChildren.remove(targetId);
            int index = previousId != null ? toChildren.indexOf(previousId) + 1: 0;
            toChildren.add(index, targetId);
            nodes.put(targetId, target = target.withParent(toId));
            nodes.put(fromId, from = from.withChildren(fromChildren));
            nodes.put(toId, to = to.withChildren(toChildren));
        }
        return new NodeData[]{target,from,to};
    }

    @Override
    public NodeData<ElementState>[] renameNode(String targetId, String parentId, String name) {
        // We  don't support it as it's not necessary (yet)
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
    }
}
