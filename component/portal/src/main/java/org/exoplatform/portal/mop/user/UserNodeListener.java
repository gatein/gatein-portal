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

package org.exoplatform.portal.mop.user;

import org.exoplatform.portal.mop.hierarchy.NodeChangeListener;
import org.exoplatform.portal.mop.hierarchy.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;

/**
 * This listener takes care of invalidating the cached state of the UserNode listener when it is updated against the navigation
 * service.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class UserNodeListener implements NodeChangeListener<NodeContext<UserNode, NodeState>, NodeState> {

    /** . */
    private NodeChangeListener<UserNode, NodeState> next;

    UserNodeListener(NodeChangeListener<UserNode, NodeState> next) {
        if (next != null) {
            this.next = next;
        }
    }

    public void onAdd(NodeContext<UserNode, NodeState> target, NodeContext<UserNode, NodeState> parent, NodeContext<UserNode, NodeState> previous) {
        if (next != null) {
            next.onAdd(unwrap(target), unwrap(parent), unwrap(previous));
        }
    }

    public void onCreate(NodeContext<UserNode, NodeState> target, NodeContext<UserNode, NodeState> parent, NodeContext<UserNode, NodeState> previous, String name, NodeState state) {
        if (next != null) {
            next.onCreate(unwrap(target), unwrap(parent), unwrap(previous), name, state);
        }
    }

    public void onRemove(NodeContext<UserNode, NodeState> target, NodeContext<UserNode, NodeState> parent) {
        if (next != null) {
            next.onRemove(unwrap(target), unwrap(parent));
        }
    }

    public void onDestroy(NodeContext<UserNode, NodeState> target, NodeContext<UserNode, NodeState> parent) {
        if (next != null) {
            next.onDestroy(unwrap(target), unwrap(parent));
        }
    }

    public void onRename(NodeContext<UserNode, NodeState> target, NodeContext<UserNode, NodeState> parent, String name) {
        UserNode unwrappedTarget = unwrap(target);
        unwrappedTarget.resolvedLabel = null;
        unwrappedTarget.encodedResolvedLabel = null;
        unwrappedTarget.uri = null;
        if (next != null) {
            next.onRename(unwrappedTarget, unwrap(parent), name);
        }
    }

    public void onUpdate(NodeContext<UserNode, NodeState> target, NodeState state) {
        UserNode unwrappedTarget = unwrap(target);
        unwrappedTarget.resolvedLabel = null;
        unwrappedTarget.encodedResolvedLabel = null;
        if (next != null) {
            next.onUpdate(unwrappedTarget, state);
        }
    }

    public void onMove(NodeContext<UserNode, NodeState> target, NodeContext<UserNode, NodeState> from, NodeContext<UserNode, NodeState> to,
            NodeContext<UserNode, NodeState> previous) {
        UserNode unwrappedTarget = unwrap(target);
        unwrappedTarget.uri = null;
        if (next != null) {
            next.onMove(unwrappedTarget, unwrap(from), unwrap(to), unwrap(previous));
        }
    }

    private UserNode unwrap(NodeContext<UserNode, NodeState> context) {
        return context != null ? context.getNode() : null;
    }
}
