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

package org.exoplatform.portal.mop.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.exoplatform.portal.tree.diff.HierarchyAdapter;
import org.exoplatform.portal.tree.diff.HierarchyChangeIterator;
import org.exoplatform.portal.tree.diff.HierarchyChangeType;
import org.exoplatform.portal.tree.diff.HierarchyDiff;
import org.exoplatform.portal.tree.diff.ListAdapter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeDiff<L, N, S extends Serializable> {

    /** . */
    private static final Comparator<String> COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    /** . */
    private final N node;

    /** . */
    private final NodeAdapter<L, N, S> adapter;

    /** . */
    private final NodeContext<N, S> context;

    TreeDiff(N node, NodeContext<N, S> context, NodeAdapter<L, N, S> adapter) {
        this.node = node;
        this.context = context;
        this.adapter = adapter;
    }

    public void perform() {
        NodeContextHierarchyAdapter nc = new NodeContextHierarchyAdapter();
        HierarchyDiff<List<String>, NodeContext<N, S>, L, N, String> diff = HierarchyDiff.create(nc, nc, adapter, adapter, COMPARATOR);
        LinkedList<NodeContext<N, S>> previousStack = new LinkedList<NodeContext<N, S>>();
        LinkedList<NodeContext<N, S>> parentStack = new LinkedList<NodeContext<N, S>>();
        HierarchyChangeIterator<List<String>, NodeContext<N, S>, L, N, String> i = diff.iterator(context, node);
        while (i.hasNext()) {
            HierarchyChangeType type = i.next();
            switch (type) {
                case ADDED: {
                    S state = adapter.getState(i.getDestination());
                    NodeContext<N, S> parent = parentStack.peekLast();
                    NodeContext<N, S> previous = previousStack.peekLast();
                    NodeContext<N, S> added;
                    String name = UUID.randomUUID().toString();
                    if (previous != null) {
                        added = parent.add(previous.getIndex() + 1, name, state);
                    } else {
                        added = parent.add(0, name, state);
                    }
                    adapter.setHandle(i.getDestination(), added.getHandle());
                    previousStack.set(previousStack.size() - 1, added);
                    break;
                }
                case REMOVED:
                    i.getSource().removeNode();
                    break;
                case MOVED_OUT:
                    break;
                case MOVED_IN: {
                    NodeContext<N, S> moved = i.getSource();
                    N cd = i.getDestination();
                    N parent = adapter.getParent(cd);
                    String handle = adapter.getHandle(parent);
                    NodeContext<N, S> parent2 = context.getDescendant(handle);
                    N pre = adapter.getPrevious(parent, cd);
                    if (pre != null) {
                        String preHandle = adapter.getHandle(pre);
                        NodeContext<N, S> foo = context.getDescendant(preHandle);
                        parent2.add(foo.getIndex() + 1, moved);
                    } else {
                        parent2.add(0, moved);
                    }
                    previousStack.set(previousStack.size() - 1, moved);
                    break;
                }
                case KEEP:
                    S s = adapter.getState(i.getDestination());
                    i.getSource().setState(s);
                    previousStack.set(previousStack.size() - 1, i.getSource());
                    break;
                case ENTER:
                    NodeContext<N, S> parent = i.getSource();
                    if (parent == null) {
                        // This is a trick : if the parent is null -> a node was added
                        // and this node should/must be the previous node
                        parentStack.addLast(previousStack.peekLast());
                    } else {
                        parentStack.addLast(parent);
                    }
                    previousStack.addLast(null);
                    break;
                case LEAVE:
                    parentStack.removeLast();
                    previousStack.removeLast();
                    break;
            }
        }
    }

    class NodeContextHierarchyAdapter implements HierarchyAdapter<List<String>, NodeContext<N, S>, String>, ListAdapter<List<String>, String> {

        @Override
        public String getHandle(NodeContext<N, S> node) {
            return node.getId();
        }

        @Override
        public List<String> getChildren(NodeContext<N, S> node) {
            ArrayList<String> ret = new ArrayList<String>(node.getSize());
            for (NodeContext<N, S> child : node) {
                ret.add(child.getId());
            }
            return ret;
        }

        @Override
        public NodeContext<N, S> getDescendant(NodeContext<N, S> node, String handle) {
            return node.getDescendant(handle);
        }

        @Override
        public int size(List<String> list) {
            return list.size();
        }

        @Override
        public Iterator<String> iterator(List<String> list, boolean reverse) {
            if (reverse) {
                final ListIterator<String> i = list.listIterator(list.size());
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return i.hasPrevious();
                    }
                    @Override
                    public String next() {
                        return i.previous();
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            } else {
                return list.iterator();
            }
        }
    }
}
