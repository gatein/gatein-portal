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

package org.gatein.portal.impl.mop.ram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * An in memory lighweight and fast tree store used for the ram persistence.
 *
 * Todo : add failure detection in merge
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Store {

    /** . */
    private static final NodeImpl TOMBSTONE = new NodeImpl();

    /** . */
    final Store origin;

    /** . */
    final HashMap<String, NodeImpl> nodes;

    /** . */
    final String rootId;

    public Store() {

        //
        NodeImpl root = new NodeImpl();
        HashMap<String, NodeImpl> nodes = new HashMap<String, NodeImpl>();
        nodes.put(root.id, root);

        //
        this.rootId = root.id;
        this.origin = null;
        this.nodes = nodes;
    }

    public Store(Store origin) {
        this.origin = origin;
        this.rootId = origin.rootId;
        this.nodes = new HashMap<String, NodeImpl>();
    }

    public final <C extends Store> C open() throws IllegalStateException {
        if (origin != null) {
            throw new IllegalStateException("Already open");
        }
        return (C)create();
    }

    protected Store create() {
        return new Store(this);
    }

    public final Store merge() throws IllegalStateException {
        if (origin == null) {
            throw new IllegalStateException("Not open");
        }
        for (Map.Entry<String, NodeImpl> entry : nodes.entrySet()) {
            NodeImpl node = entry.getValue();
            if (node == TOMBSTONE) {
                origin.nodes.remove(entry.getKey());
            } else {
                origin.nodes.put(node.id, node);
            }
        }
        nodes.clear();
        return origin;
    }

    public final String getRoot() {
        return rootId;
    }

    public final boolean contains(String nodeId) {
        return getNode(nodeId) != null;
    }

    public final Node getNode(String nodeId) {
        return peek(nodeId);
    }

    public final void update(String nodeId, Serializable state) {
        if (state == null) {
            throw new NullPointerException("No null state");
        }
        NodeImpl node = peek(nodeId);
        nodes.put(nodeId, new NodeImpl(node, state));
    }

    public final List<String> getChildren(String parentId) throws NoSuchElementException {
        NodeImpl parent = peek(parentId);
        if (parent == null) {
            throw new NoSuchElementException("No such node");
        } else {
            return parent.children;
        }
    }

    public final String getChild(String parentId, String name) throws NoSuchElementException {
        List<String> children = null;
        try {
            children = getChildren(parentId);
            for (int i = children.size() - 1;i >= 0;i--) {
                String childId = children.get(i);
                NodeImpl child = nodes.get(childId);
                if (child == null) {
                    child = origin.nodes.get(childId);
                }
                if (child.name.equals(name)) {
                    return childId;
                }
            }
        } catch (NoSuchElementException ignore) {
            //
        }
        return null;
    }

    public final String getParent(String childId) throws NoSuchElementException {
        NodeImpl child = peek(childId);
        if (child == null) {
            throw new NoSuchElementException("No such node");
        } else {
            return child.parentId;
        }
    }

    /**
     * Add a child to the parent to the last position.
     *
     * @param parentId the parent identifier
     * @param name the child name
     * @param state the child state
     * @return the child identifier
     * @throws NullPointerException if the <code>parentId</code>, the <code>name</code> or the <code>state</code> argument are null
     * @throws NoSuchElementException if the parent does not exist
     * @throws IllegalStateException if the store is not writable
     * @throws IllegalArgumentException if the name already exist among the children
     */
    public final String addChild(String parentId, String name, Serializable state) throws
            NullPointerException,
            NoSuchElementException,
            IllegalStateException,
            IllegalArgumentException {
        if (origin == null) {
            throw new IllegalStateException("No previous");
        }
        NodeImpl parent = peek(parentId);
        if (parent == null) {
            throw new NoSuchElementException();
        }
        int size = parent.children.size();
        NodeImpl previous = size == 0 ? null : peek(parent.children.get(size - 1));
        return addChild(parent, previous, name, state).id;
    }

    /**
     * Add a child to the parent at the specified index. If the index is null, it means the child should be added
     * as the first child.
     *
     * @param parentId the parent identifier
     * @param previousId the previous identifier
     * @param name the child name
     * @param state the child state
     * @return the child identifier
     * @throws NullPointerException if the <code>parentId</code>, the <code>name</code> or the <code>state</code> argument are null
     * @throws NoSuchElementException if the parent does not exist
     * @throws IllegalStateException if the store is not writable
     * @throws IllegalArgumentException if the name already exist among the children or if the previous is not a child of the parent
     * @throws IndexOutOfBoundsException if the index is not valid
     */
    public final String addChild(String parentId, String previousId, String name, Serializable state) throws
            NullPointerException,
            NoSuchElementException,
            IllegalStateException,
            IllegalArgumentException,
            IndexOutOfBoundsException {
        if (origin == null) {
            throw new IllegalStateException("No previous");
        }
        NodeImpl parent = peek(parentId);
        if (parent == null) {
            throw new NoSuchElementException("Parent does not exist");
        }
        NodeImpl previous;
        if (previousId != null) {
            previous = peek(previousId);
            if (previous == null) {
                throw new NoSuchElementException("Previous does not exist");
            }
        } else {
            previous = null;
        }
        return addChild(parent, previous, name, state).id;
    }

    private NodeImpl addChild(NodeImpl parent, NodeImpl previous, String name, Serializable state) {
        if (state == null) {
            throw new NullPointerException();
        }
        for (int i = parent.children.size() - 1;i >= 0;i--) {
            String childId = parent.children.get(i);
            NodeImpl child = peek(childId);
            if (child.name.equals(name)) {
                throw new IllegalArgumentException("Duplicate child name " + name);
            }
        }
        int index;
        if (previous != null) {
            if ((index = parent.children.indexOf(previous.id) + 1) == 0) {
                throw new IllegalArgumentException("Previous is not a child of the parent");
            }
        } else {
            index = 0;
        }
        if (index < 0 || index > parent.children.size()) {
            throw new IndexOutOfBoundsException("Index value " + index + " does not belong to [0," + parent.children.size() + "]");
        }
        parent = get(parent.id);
        NodeImpl child = new NodeImpl(parent.id, name, state);
        nodes.put(child.id, child);
        parent.children.add(index, child.id);
        return child;
    }

    /**
     * Add a sibling after a node.
     *
     * @param previousId the previous identifier
     * @param name the child name
     * @param state the child state
     * @return the sibling identifier
     * @throws NullPointerException if the <code>previousId</code>, the <code>name</code> or the <code>state</code> argument are null
     * @throws NoSuchElementException if the previous does not exist
     * @throws IllegalStateException if the store is not writable
     * @throws IllegalArgumentException if the name already exist among the siblings or if the previous is the root node
     */
    public final String addSibling(String previousId, String name, Serializable state) throws
            NullPointerException,
            NoSuchElementException,
            IllegalStateException,
            IllegalArgumentException {
        if (origin == null) {
            throw new IllegalStateException("No previous");
        }
        if (state == null) {
            throw new NullPointerException();
        }
        NodeImpl previous = peek(previousId);
        if (previous == null) {
            throw new NoSuchElementException();
        }
        String parentId = previous.parentId;
        if (parentId == null) {
            throw new IllegalArgumentException("Cannot add a sibling to the root element");
        }
        NodeImpl parent = peek(parentId);
        return addChild(parent, previous, name, state).id;
    }

    /**
     * Renames a node to a new name.
     *
     * @param nodeId the node to rename
     * @param name the new name
     * @throws NoSuchElementException when the node does not exist
     * @throws IllegalStateException when the store is not writable
     * @throws IllegalArgumentException when the new name already exist or the node to rename is the root node
     */
    public final void rename(String nodeId, String name) throws NoSuchElementException, IllegalStateException, IllegalArgumentException {
        if (origin == null) {
            throw new IllegalStateException("No writable");
        }
        if (nodeId.equals(rootId)) {
            throw new IllegalArgumentException("Cannot rename root");
        }
        NodeImpl node = peek(nodeId);
        if (node == null) {
            throw new NoSuchElementException();
        }
        NodeImpl parent = peek(node.parentId);
        for (String childId : parent.children) {
            if (!childId.equals(nodeId)) {
                NodeImpl child = peek(childId);
                if (child.name.equals(name)) {
                    throw new IllegalArgumentException("Name already used");
                }
            }
        }
        nodes.put(nodeId, new NodeImpl(node, name));
    }

    /**
     * Moves a node to a new parent.
     *
     * @param nodeId the node to move
     * @param parentId the next parent id
     * @param previousId the previous node or null it should be moved first
     * @throws NullPointerException if the <code>nodeId</code> or <code>parentId</code> argument is null
     * @throws NoSuchElementException if the node to move or the parent do not exist
     * @throws IllegalArgumentException if the move operation creates a name conflict under the new parent or if the
     *                                  new parent is part of the subtree defined by the moved node or if the
     *                                  previous is not null and is not a child of the parent
     * @throws IllegalStateException if the store is not writable
     */
    public final void move(String nodeId, String parentId, String previousId) throws
            NullPointerException,
            NoSuchElementException,
            IllegalArgumentException,
            IllegalStateException {
        if (origin == null) {
            throw new IllegalStateException("No writable");
        }
        if (nodeId == null) {
            throw new NullPointerException("No null node id accepted");
        }
        if (parentId == null) {
            throw new NullPointerException("No null parent id accepted");
        }
        if (nodeId.equals(rootId)) {
            throw new IllegalArgumentException("Cannot move root");
        }
        NodeImpl node = peek(nodeId);
        if (node == null) {
            throw new NoSuchElementException();
        }
        NodeImpl nextParent = peek(parentId);
        if (nextParent == null) {
            throw new NoSuchElementException();
        }
        if (previousId != null && !nextParent.children.contains(previousId)) {
            throw new IllegalArgumentException("The previous node is not a child of the parent");
        }
        for (NodeImpl current = nextParent;!current.id.equals(rootId);current = peek(current.parentId)) {
            if (current == node) {
                throw new IllegalArgumentException("Node cannot be moved in its subtree");
            }
        }
        nextParent = get(parentId);
        NodeImpl currParent = get(node.parentId);
        currParent.children.remove(node.id);
        int index = previousId != null ? nextParent.children.indexOf(previousId) + 1: 0;
        nextParent.children.add(index, nodeId);
        nodes.put(nodeId, new NodeImpl(parentId, node));
    }

    public final String clone(String nodeId, String parentId, String name) {
        if (origin == null) {
            throw new IllegalStateException("No writable");
        }
        if (nodeId == null) {
            throw new NullPointerException("No null node id accepted");
        }
        if (parentId == null) {
            throw new NullPointerException("No null parent id accepted");
        }
        NodeImpl parent = peek(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("The parent does not exist");
        }
        NodeImpl node = peek(nodeId);
        if (node == null) {
            throw new NoSuchElementException("The node does not exist");
        }
        for (NodeImpl current = parent;current.parentId != null;current = peek(current.parentId)) {
            if (current.id.equals(nodeId)) {
                throw new IllegalArgumentException("The parent cannot be part of the cloned subtree");
            }
        }
        return clone(node, parent, null, name).id;
    }

    private NodeImpl clone(NodeImpl node, NodeImpl parent, NodeImpl previous, String name) {
        NodeImpl clone = addChild(parent, previous, name, node.state);
        NodeImpl previousChild = null;
        for (String childId : node.children) {
            NodeImpl child = peek(childId);
            previousChild = clone(child, clone, previousChild, child.name);
        }
        return clone;
    }


    public final String remove(String childId) throws NoSuchElementException, IllegalStateException, IllegalArgumentException {
        if (origin == null) {
            throw new IllegalStateException("No previous");
        }
        if (childId.equals(rootId)) {
            throw new IllegalArgumentException("Cannot remove root node");
        }
        return remove_(childId).id;
    }

    /**
     * Removes a node recursively.
     *
     * @param nodeId the node to remove
     * @return the parent of the removed node
     * @throws NoSuchElementException if the node does not exist
     */
    private NodeImpl remove_(String nodeId) throws NoSuchElementException, IllegalArgumentException {
        NodeImpl node = nodes.get(nodeId);
        if (node == TOMBSTONE) {
            throw new UnsupportedOperationException("todo");
        } else if (node != null) {
            while (node.children.size() > 0) {
                node = remove_(node.children.iterator().next());
            }
            NodeImpl parent = get(node.parentId);
            parent.children.remove(node.id);
            nodes.remove(node.id);
            return parent;
        } else {
            node = origin.nodes.get(nodeId);
            if (node == null) {
                throw new NoSuchElementException();
            } else {
                while (node.children.size() > 0) {
                    node = remove_(node.children.iterator().next());
                }
                NodeImpl parent = get(node.parentId);
                parent.children.remove(node.id);
                nodes.put(node.id, TOMBSTONE);
                return parent;
            }
        }
    }

    private NodeImpl get(String nodeId) {
        NodeImpl node = nodes.get(nodeId);
        if (node == TOMBSTONE) {
            throw new UnsupportedOperationException("todo");
        } else if (node == null) {
            node = origin.nodes.get(nodeId);
            if (node != null) {
                nodes.put(nodeId, node = new NodeImpl(node));
            }
        }
        return node;
    }

    private NodeImpl peek(String nodeId) {
        NodeImpl node = nodes.get(nodeId);
        if (node == TOMBSTONE) {
            return null;
        } else if (node == null) {
            if (origin != null) {
                node = origin.nodes.get(nodeId);
            }
        }
        return node;
    }


    private static class NodeImpl implements Node {

        /** . */
        private final String parentId;

        /** . */
        private final String id;

        /** . */
        private final String name;

        /** . */
        private final ArrayList<String> children;

        /** . */
        private final Serializable state;

        public NodeImpl() {
            this.parentId =  null;
            this.id = UUID.randomUUID().toString();
            this.name = "";
            this.children = new ArrayList<String>();
            this.state = "";
        }

        public NodeImpl(String parentId, String name, Serializable state) {
            if (parentId == null) {
                throw new AssertionError();
            }
            this.parentId = parentId;
            this.name = name;
            this.id = UUID.randomUUID().toString();
            this.children = new ArrayList<String>();
            this.state = state;
        }

        public NodeImpl(NodeImpl that) {
            this.parentId = that.parentId;
            this.id = that.id;
            this.name = that.name;
            this.children = new ArrayList<String>(that.children);
            this.state = that.state;
        }

        public NodeImpl(NodeImpl that, Serializable state) {
            this.parentId = that.parentId;
            this.id = that.id;
            this.name = that.name;
            this.children = new ArrayList<String>(that.children);
            this.state = state;
        }

        public NodeImpl(NodeImpl that, String name) {
            this.parentId = that.parentId;
            this.id = that.id;
            this.name = name;
            this.children = new ArrayList<String>(that.children);
            this.state = that.state;
        }

        public NodeImpl(String parentId, NodeImpl that) {
            this.parentId = parentId;
            this.id = that.id;
            this.name = that.name;
            this.children = new ArrayList<String>(that.children);
            this.state = that.state;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Serializable getState() {
            return state;
        }
    }
}
