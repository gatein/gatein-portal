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
import java.util.ArrayList;

import org.exoplatform.portal.mop.Utils;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
class NodeContextUpdateAdapter<N, S extends Serializable> extends TreeUpdateAdapter<NodeContext<N, S>, S> {

    /** . */
    private static final NodeContextUpdateAdapter<?, ?> _instance = new NodeContextUpdateAdapter();

    static <N, S extends Serializable> NodeContextUpdateAdapter<N, S> create() {
        @SuppressWarnings("unchecked")
        NodeContextUpdateAdapter<N, S> instance = (NodeContextUpdateAdapter<N, S>) _instance;
        return instance;
    }

    public String getHandle(NodeContext<N, S> node) {
        return node.handle;
    }

    public String[] getChildren(NodeContext<N, S> node) {
        if (node.getFirst() != null) {
            ArrayList<String> tmp = new ArrayList<String>();
            for (NodeContext<N, S> current = node.getFirst(); current != null; current = current.getNext()) {
                tmp.add(current.handle);
            }
            return tmp.toArray(new String[tmp.size()]);
        } else {
            return Utils.EMPTY_STRING_ARRAY;
        }
    }

    public NodeContext<N, S> getDescendant(NodeContext<N, S> node, String handle) {
        return node.getDescendant(handle);
    }

    NodeData<S> getData(NodeContext<N, S> node) {
        return node.data;
    }

    S getState(NodeContext<N, S> node) {
        return node.state;
    }

    String getName(NodeContext<N, S> node) {
        return node.name;
    }
}
