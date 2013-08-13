/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.gatein.portal.mop.hierarchy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An immutable node data class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NodeData<S extends Serializable> implements Serializable, Iterable<String> {

    /** . */
    public final String parentId;

    /** . */
    public final String id;

    /** . */
    public final String name;

    /** . */
    public final S state;

    /** . */
    final String[] children;

    NodeData(NodeContext<?, S> context) {
        int size = 0;
        for (NodeContext<?, S> current = context.getFirst(); current != null; current = current.getNext()) {
            size++;
        }
        String[] children = new String[size];
        for (NodeContext<?, S> current = context.getFirst(); current != null; current = current.getNext()) {
            children[children.length - size--] = current.handle;
        }
        String parentId = context.getParent() != null ? context.getParent().handle : null;
        String id = context.handle;
        String name = context.getName();
        S state = context.getState();

        //
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.state = state;
        this.children = children;
    }

    public NodeData(String parentId, String id, String name, S state, String[] children) {
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.state = state;
        this.children = children;
    }

    /**
     * Create a new node but with a new parent.
     *
     * @param parentId the new parent id
     * @return the newly created node
     */
    public NodeData<S> withParent(String parentId) {
        return new NodeData<S>(parentId, id, name, state, children);
    }

    /**
     * Create a new node but with a new set of children.
     *
     * @param children the new children
     * @return the newly created node
     */
    public NodeData<S> withChildren(Collection<String> children) {
        return new NodeData<S>(parentId, id, name, state, children.toArray(new String[children.size()]));
    }

    /**
     * Create a new node but with a new parent.
     *
     * @param state the new state
     * @return the newly created node
     */
    public NodeData<S> withState(S state) {
        return new NodeData<S>(parentId, id, name, state, children);
    }

    public Iterator<String> iterator() {
        return iterator(false);
    }

    public Iterator<String> iterator(boolean reverse) {
        if (reverse) {
            return new Iterator<String>() {
                int index = children.length;

                public boolean hasNext() {
                    return index > 0;
                }

                public String next() {
                    if (index > 0) {
                        return children[--index];
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return new Iterator<String>() {
                int index = 0;

                public boolean hasNext() {
                    return index < children.length;
                }

                public String next() {
                    if (index < children.length) {
                        return children[index++];
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public S getState() {
        return state;
    }

    public String getLastChild() {
        return children.length > 0 ? children[children.length - 1] : null;
    }

    @Override
    public String toString() {
        return "NodeData[id=" + id + ",name=" + name + ",state=" + state + ",children=" + Arrays.asList(children) + "]";
    }
}
