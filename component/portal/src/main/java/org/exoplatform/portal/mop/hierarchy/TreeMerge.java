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

import org.exoplatform.portal.mop.navigation.NavigationError;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeMerge<N, S extends Serializable> extends NodeChangeListener.Base<NodeContext<N, S>, S> {

    /** . */
    private final TreeContext<N, S> merged;

    /** . */
    private final NodeChangeListener<NodeContext<N, S>, S> next;

    TreeMerge(TreeContext<N, S> merged, NodeChangeListener<NodeContext<N, S>, S> next) {
        this.merged = merged;
        this.next = next;
    }

    public void onCreate(NodeContext<N, S> target, NodeContext<N, S> _parent, NodeContext<N, S> _previous, String name, S state)
            throws NavigationServiceException {
        String parentHandle = _parent.handle;
        NodeContext<N, S> parent = merged.getNode(parentHandle);
        if (parent == null) {
            throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
        }

        //
        NodeContext<N, S> previous;
        if (_previous != null) {
            previous = merged.getNode(_previous.handle);
            if (previous == null) {
                throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
            }
        } else {
            previous = null;
        }

        //
        NodeContext<N, S> added = parent.get(name);
        if (added != null) {
            throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_ADDED_NODE);
        }

        //
        NodeContext<N, S> source = merged.create(target.handle, name, target.getState());

        //
        next.onCreate(source, parent, previous, name, state);
    }

    public void onDestroy(NodeContext<N, S> target, NodeContext<N, S> _parent) {
        NodeContext<N, S> removed = merged.getNode(target.handle);

        //
        if (removed != null) {
            NodeContext<N, S> parent = merged.getNode(_parent.handle);
            next.onDestroy(removed, parent);
        }
    }

    public void onRename(NodeContext<N, S> target, NodeContext<N, S> _parent, String _name) throws NavigationServiceException {
        //
        String renamedHandle = target.handle;
        NodeContext<N, S> renamed = merged.getNode(renamedHandle);
        if (renamed == null) {
            throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
        }

        //
        NodeContext<N, S> parent = renamed.getParent();
        if (parent.get(_name) != null) {
            throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME);
        }

        //
        next.onRename(renamed, parent, _name);
    }

    public void onUpdate(NodeContext<N, S> target, S state) throws NavigationServiceException {
        String updatedHandle = target.handle;
        NodeContext<N, S> navigation = merged.getNode(updatedHandle);
        if (navigation == null) {
            throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
        }

        //
        next.onUpdate(navigation, state);
    }

    public void onMove(NodeContext<N, S> target, NodeContext<N, S> _from, NodeContext<N, S> _to, NodeContext<N, S> _previous)
            throws NavigationServiceException {
        String srcHandle = _from.handle;
        NodeContext<N, S> src = merged.getNode(srcHandle);
        if (src == null) {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
        }

        //
        String dstHandle = _to.handle;
        NodeContext<N, S> dst = merged.getNode(dstHandle);
        if (dst == null) {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
        }

        //
        String movedHandle = target.handle;
        NodeContext<N, S> moved = merged.getNode(movedHandle);
        if (moved == null) {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
        }

        //
        NodeContext<N, S> previous;
        if (_previous != null) {
            previous = merged.getNode(_previous.handle);
            if (previous == null) {
                throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
            }
        } else {
            previous = null;
        }

        //
        if (src != moved.getParent()) {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE);
        }

        //
        if (src != dst) {
            String name = moved.getName();
            NodeContext<N, S> existing = dst.get(name);
            if (existing != null) {
                throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_DUPLICATE_NAME);
            }
        }

        //
        next.onMove(moved, src, dst, previous);
    }
}
