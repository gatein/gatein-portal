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
package org.exoplatform.portal.pom.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.mop.hierarchy.ModelAdapter;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.layout.ElementState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONContainerAdapter implements ModelAdapter<JSONObject, ElementState> {
    private final JSONObject root;
    private final NodeContext<JSONObject, ElementState> context;

    public JSONContainerAdapter(JSONObject root, NodeContext<JSONObject, ElementState> context) {
        this.root = root;
        this.context = context;

        this.addEmptyContainer(this.root, this.context);
    }

    @Override
    public String getId(JSONObject node) {
        NodeContext<JSONObject, ElementState> nodeContext = null;
        if (node == root) {
            nodeContext = this.context;
        } else {
            try {
                nodeContext = this.find(node.getString("id"), this.context);
            } catch (JSONException ex) {
            }
        }

        return nodeContext != null ? nodeContext.getId() : null;
    }

    @Override
    public String getName(JSONObject node) {
        String name = null;
        try {
            name = node.getString("id");
        } catch (JSONException ex) {}

        if (name == null) {
            // For now we generate a name
            // however the name should be fully provided by the node (possibly randomly generated)
            return UUID.randomUUID().toString();
        }
        return name;
    }

    @Override
    public ElementState getState(JSONObject node) {
        NodeContext<JSONObject, ElementState> nodeContext = null;
        if (node == root) {
            nodeContext = this.context;
        } else {
            try {
                nodeContext = this.find(node.getString("id"), this.context);
            } catch (JSONException ex) {
            }
        }

        return this.createState(node, nodeContext);
    }

    @Override
    public JSONObject getParent(JSONObject node) {
        return this.getParent(this.root, node);
    }

    @Override
    public JSONObject getPrevious(JSONObject parent, JSONObject node) {
        try {
            JSONArray children = parent.getJSONArray("children");
            JSONObject prev = null;
            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.getJSONObject(i);
                if (child.equals(node)) {
                    return prev;
                } else {
                    prev = child;
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterator<JSONObject> getChildren(JSONObject node, final boolean reverse) {
        try {
            if (isContainer(node)) {
                final JSONArray children = node.getJSONArray("children");
                final List<JSONObject> list = new ArrayList<JSONObject>();
                for (int i = 0; i < children.length(); i++) {
                    try {
                        JSONObject child = children.getJSONObject(i);
                        list.add(child);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                if (reverse) {
                    final ListIterator<JSONObject> iterator = list.listIterator(list.size());
                    return new Iterator<JSONObject>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasPrevious();
                        }

                        @Override
                        public JSONObject next() {
                            return iterator.previous();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                } else {
                    return list.iterator();
                }
            } else {
                return Collections.EMPTY_LIST.iterator();
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public int size(JSONObject node) {
        try {
            if (isContainer(node)) {
                return node.getJSONArray("children").length();
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void addEmptyContainer(JSONObject rootNode, NodeContext<JSONObject, ElementState> rootContext) {
        try {
            JSONArray newChildren = new JSONArray();
            JSONArray children = rootNode.getJSONArray("children");
            int childrenLength = children.length();
            for (NodeContext<JSONObject, ElementState> sel : rootContext) {
                String name = sel.getName();
                JSONObject found = null;
                for (int i = 0; i < childrenLength; i++) {
                    if(children.get(i) == JSONObject.NULL) {
                        continue;
                    }
                    JSONObject child = children.getJSONObject(i);
                    if (name.equalsIgnoreCase(child.getString("id"))) {
                        found = child;
                        children.put(i, JSONObject.NULL);
                        break;
                    }
                }
                if (found == null) {
                    found = createEmptyContainer(sel);
                }
                newChildren.put(found);
            }

            for (int i = 0; i < children.length(); i++) {
                if(children.get(i) != JSONObject.NULL) {
                    newChildren.put(children.getJSONObject(i));
                }
            }

            rootNode.put("children", newChildren);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private JSONObject createEmptyContainer(NodeContext<JSONObject, ElementState> nodeContext) {
        try {
            JSONObject node = new JSONObject();
            node.put("id", nodeContext.getName());
            node.put("type", "container");
            node.put("children", new JSONArray());

            return node;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private JSONObject getParent(JSONObject rootNode, JSONObject node) {
        try {
            JSONArray children = rootNode.getJSONArray("children");
            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.getJSONObject(i);
                if (child.equals(node)) {
                    return rootNode;
                } else if (isContainer(child)) {
                    JSONObject parent = getParent(child, node);
                    if (parent != null) {
                        return parent;
                    }
                }
            }
        } catch (JSONException ex) {

        }
        return null;
    }

    private ElementState createState(JSONObject json, NodeContext<JSONObject, ElementState> nodeContext) {
        if (nodeContext != null) {
            //TODO: need clone to new context?
            return nodeContext.getState();
        }
        try {
            String type = json.getString("type");

            if (isContainer(json)) {
                //Create new container
                return new ElementState.Container(null, json.getString("id"), null, null, null, null, null, null, null, false);

            } else if ("application".equalsIgnoreCase(type)) {
                //create new window
                String contentId = json.getString("contentId");
                TransientApplicationState state = new TransientApplicationState(contentId);
                String contentTypeValue = json.getString("contentType");
                ContentType<?> contentType = ContentType.forValue(contentTypeValue);
                return new ElementState.Window(contentType, state, null, null, null, false, false, false, null, null, null, null);

            } else {
                throw new UnsupportedOperationException();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private NodeContext<JSONObject, ElementState> find(String name, NodeContext<JSONObject, ElementState> root) {
        if (root.getName().equals(name)) {
            return root;
        } else {
            for (NodeContext<JSONObject, ElementState> child : root) {
                NodeContext<JSONObject, ElementState> tmp = this.find(name, child);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        return null;
    }

    private boolean isContainer(JSONObject node) {
        try {
            String type = node.getString("type");
            return "container".equalsIgnoreCase(type) || "layout".equalsIgnoreCase(type);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
