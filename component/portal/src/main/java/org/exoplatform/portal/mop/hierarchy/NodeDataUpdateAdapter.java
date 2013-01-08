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

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
class NodeDataUpdateAdapter<S extends Serializable> extends TreeUpdateAdapter<NodeData<S>, S> {

    static <S extends Serializable> NodeDataUpdateAdapter<S> create(NodePersistence<S> persistence) {
        return new NodeDataUpdateAdapter<S>(persistence);
    }

    /** . */
    private final NodePersistence<S> persistence;

    NodeDataUpdateAdapter(NodePersistence<S> persistence) {
        this.persistence = persistence;
    }

    public String getHandle(NodeData<S> node) {
        return node.id;
    }

    public String[] getChildren(NodeData<S> node) {
        return node.children;
    }

    public NodeData<S> getDescendant(NodeData<S> node, String handle) {
        NodeData<S> data = persistence.getNode(handle);
        NodeData<S> current = data;
        while (current != null) {
            if (node.id.equals(current.id)) {
                return data;
            } else {
                if (current.parentId != null) {
                    current = persistence.getNode(current.parentId);
                } else {
                    current = null;
                }
            }
        }
        return null;
    }

    NodeData<S> getData(NodeData<S> node) {
        return node;
    }

    S getState(NodeData<S> node) {
        return null;
    }

    String getName(NodeData<S> node) {
        return null;
    }
}
