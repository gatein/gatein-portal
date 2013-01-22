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

package org.gatein.portal.mop.hierarchy;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NodeContextChangeAdapter<N, S extends Serializable> implements NodeChangeListener<NodeContext<N, S>, S> {

    /** . */
    private final NodeChangeListener<N, S> wrapped;

    public NodeContextChangeAdapter(NodeChangeListener<N, S> wrapped) {
        if (wrapped == null) {
            throw new NullPointerException();
        }
        this.wrapped = wrapped;
    }

    public void onAdd(NodeContext<N, S> target, NodeContext<N, S> parent, NodeContext<N, S> previous) {
        wrapped.onAdd(unwrap(target), unwrap(parent), unwrap(previous));
    }

    public void onCreate(NodeContext<N, S> target, NodeContext<N, S> parent, NodeContext<N, S> previous, String name, S state) {
        wrapped.onCreate(unwrap(target), unwrap(parent), unwrap(previous), name, state);
    }

    public void onRemove(NodeContext<N, S> target, NodeContext<N, S> parent) {
        wrapped.onRemove(unwrap(target), unwrap(parent));
    }

    public void onDestroy(NodeContext<N, S> target, NodeContext<N, S> parent) {
        wrapped.onDestroy(unwrap(target), unwrap(parent));
    }

    public void onRename(NodeContext<N, S> target, NodeContext<N, S> parent, String name) {
        wrapped.onRename(unwrap(target), unwrap(parent), name);
    }

    public void onUpdate(NodeContext<N, S> target, S state) {
        wrapped.onUpdate(unwrap(target), state);
    }

    public void onMove(NodeContext<N, S> target, NodeContext<N, S> from, NodeContext<N, S> to, NodeContext<N, S> previous) {
        wrapped.onMove(unwrap(target), unwrap(from), unwrap(to), unwrap(previous));
    }

    private N unwrap(NodeContext<N, S> context) {
        return context != null ? context.getNode() : null;
    }
}
