/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.hierarchy;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.exoplatform.portal.mop.navigation.NavigationServiceException;


/**
 * <p>
 * The context of a tree, that performs:
 * <ul>
 * <li>holding the list of pending changes</li>
 * <li>keep a reference to the {@link NodeModel}</li>
 * <li>hold a sequence for providing id for transient contexts</li>
 * <li>hold the root context</li>
 * </ul>
 * </p>
 *
 * <p>
 * The class implements the {@link Scope.Visitor} and defines a scope describing the actual content of the context tree.
 * </p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeContext<N, S extends Serializable> implements Scope.Visitor<S>, NodeChangeListener<NodeContext<N, S>, S> {

    /** . */
    private NodeChangeQueue<NodeContext<N, S>, S> changes;

    /** . */
    final NodeModel<N, S> model;

    /** . */
    boolean editMode;

    /** . */
    int sequence;

    /** . */
    final NodeContext<N, S> root;

    TreeContext(NodeModel<N, S> model, NodeContext<N, S> root) {
        this.model = model;
        this.editMode = false;
        this.sequence = 0;
        this.root = root;
    }

    public NodeChangeQueue<NodeContext<N, S>, S> getChanges() {
        return changes;
    }

    // Improve that method if we can
    Scope.Visitor<S> origin() {

        final Map<String, Boolean> map = new HashMap<String, Boolean>();

        //
        populate(map, root);

        //
        if (changes != null) {
            ListIterator<NodeChange<NodeContext<N, S>, S>> it = changes.listIterator(changes.size());
            while (it.hasPrevious()) {
                NodeChange<NodeContext<N, S>, S> change = it.previous();
                if (change instanceof NodeChange.Created<?, ?>) {
                    NodeChange.Created<NodeContext<N, S>, S> created = (NodeChange.Created<NodeContext<N, S>, S>) change;
                    map.remove(created.target.handle);
                } else if (change instanceof NodeChange.Destroyed<?, ?>) {
                    NodeChange.Destroyed<NodeContext<N, S>, S> destroyed = (NodeChange.Destroyed<NodeContext<N, S>, S>) change;
                    map.put(destroyed.target.handle, Boolean.TRUE);
                }
            }
        }

        //
        return new Scope.Visitor<S>() {
            public VisitMode enter(int depth, String id, String name, S state) {
                return map.containsKey(id) ? VisitMode.ALL_CHILDREN : VisitMode.NO_CHILDREN;
            }

            public void leave(int depth, String id, String name, S state) {
            }
        };
    }

    private void populate(Map<String, Boolean> map, NodeContext<N, S> ctx) {
        if (ctx.isExpanded()) {
            map.put(ctx.handle, Boolean.TRUE);
            for (NodeContext<N, S> current = ctx.getFirst(); current != null; current = current.getNext()) {
                populate(map, current);
            }
        }
    }

    void addChange(NodeChange<NodeContext<N, S>, S> change) {
        if (editMode) {
            throw new AssertionError();
        }
        if (changes == null) {
            changes = new NodeChangeQueue<NodeContext<N, S>, S>();
        }

        //
        if (change.target.tree != this) {
            // Normally should be done for all arguments depending on the change type
            throw new AssertionError("Ensure we are not mixing badly things");
        }

        // Perform state modification here
        if (change instanceof NodeChange.Renamed<?, ?>) {
            NodeChange.Renamed<NodeContext<N, S>, S> renamed = (NodeChange.Renamed<NodeContext<N, S>, S>) change;
            renamed.target.name = renamed.name;
        } else if (change instanceof NodeChange.Created<?, ?>) {
            NodeChange.Created<NodeContext<N, S>, S> added = (NodeChange.Created<NodeContext<N, S>, S>) change;
            if (added.previous != null) {
                added.previous.insertAfter(added.target);
            } else {
                added.parent.insertAt(0, added.target);
            }
        } else if (change instanceof NodeChange.Moved<?, ?>) {
            NodeChange.Moved<NodeContext<N, S>, S> moved = (NodeChange.Moved<NodeContext<N, S>, S>) change;
            if (moved.previous != null) {
                moved.previous.insertAfter(moved.target);
            } else {
                moved.to.insertAt(0, moved.target);
            }
        } else if (change instanceof NodeChange.Destroyed<?, ?>) {
            NodeChange.Destroyed<NodeContext<N, S>, S> removed = (NodeChange.Destroyed<NodeContext<N, S>, S>) change;
            removed.target.remove();
        } else if (change instanceof NodeChange.Updated<?, ?>) {
            NodeChange.Updated<NodeContext<N, S>, S> updated = (NodeChange.Updated<NodeContext<N, S>, S>) change;
            updated.target.state = updated.state;
        }

        //
        changes.addLast(change);
    }

    boolean hasChanges() {
        return changes != null && changes.size() > 0;
    }

    List<NodeChange<NodeContext<N, S>, S>> peekChanges() {
        if (hasChanges()) {
            return changes;
        } else {
            return Collections.emptyList();
        }
    }

    List<NodeChange<NodeContext<N, S>, S>> popChanges() {
        if (hasChanges()) {
            LinkedList<NodeChange<NodeContext<N, S>, S>> tmp = changes;
            changes = null;
            return tmp;
        } else {
            return Collections.emptyList();
        }
    }

    NodeContext<N, S> getNode(String handle) {
        return root.getDescendant(handle);
    }

    NodeContext<N, S> create(String handle, String name, S state) {
        return new NodeContext<N, S>(this, handle, name, state, true);
    }

    // Scope.Visitor implementation -------------------------------------------------------------------------------------

    public VisitMode enter(int depth, String id, String name, S state) {
        NodeContext<N, S> descendant = root.getDescendant(id);
        if (descendant != null) {
            return descendant.isExpanded() ? VisitMode.ALL_CHILDREN : VisitMode.NO_CHILDREN;
        } else {
            return VisitMode.NO_CHILDREN;
        }
    }

    public void leave(int depth, String id, String name, S state) {
    }

    //

    public void onCreate(NodeContext<N, S> target, NodeContext<N, S> parent, NodeContext<N, S> previous, String name, S state)
            throws NavigationServiceException {
        addChange(new NodeChange.Created<NodeContext<N, S>, S>(parent, previous, target, name, state));
    }

    public void onDestroy(NodeContext<N, S> target, NodeContext<N, S> parent) {
        addChange(new NodeChange.Destroyed<NodeContext<N, S>, S>(parent, target));
    }

    public void onRename(NodeContext<N, S> target, NodeContext<N, S> parent, String name) throws NavigationServiceException {
        addChange(new NodeChange.Renamed<NodeContext<N, S>, S>(parent, target, name));
    }

    public void onUpdate(NodeContext<N, S> target, S state) throws NavigationServiceException {
        addChange(new NodeChange.Updated<NodeContext<N, S>, S>(target, state));
    }

    public void onMove(NodeContext<N, S> target, NodeContext<N, S> from, NodeContext<N, S> to, NodeContext<N, S> previous)
            throws NavigationServiceException {
        addChange(new NodeChange.Moved<NodeContext<N, S>, S>(from, to, previous, target));
    }

    public void onAdd(NodeContext<N, S> target, NodeContext<N, S> parent, NodeContext<N, S> previous) {
        throw new UnsupportedOperationException();
    }

    public void onRemove(NodeContext<N, S> target, NodeContext<N, S> parent) {
        throw new UnsupportedOperationException();
    }
}
