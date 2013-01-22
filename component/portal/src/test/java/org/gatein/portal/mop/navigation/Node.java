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

package org.gatein.portal.mop.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import org.gatein.portal.mop.hierarchy.ModelNode;
import org.gatein.portal.mop.hierarchy.NodeChange;
import org.gatein.portal.mop.hierarchy.NodeChangeQueue;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeContextChangeAdapter;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.hierarchy.Scope;

/**
 * Represents a navigation node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Node extends ModelNode<Node, NodeState> {

    /** . */
    public static final NodeModel<Node, NodeState> MODEL = new NodeModel<Node, NodeState>() {
        public NodeContext<Node, NodeState> getContext(Node node) {
            return node.context;
        }

        public Node create(NodeContext<Node, NodeState> context) {
            return new Node(context);
        }
    };

    public Node(NodeContext<Node, NodeState> context) {
        super(context);
    }

    public Node addChild(String childName) {
        return addChild(childName, NodeState.INITIAL);
    }

    public Node addChild(int index, String childName) {
        return addChild(index, childName, NodeState.INITIAL);
    }

    public Iterator<NodeChange<Node, NodeState>> update(NavigationService service, Scope<NodeState> scope) throws NavigationServiceException {
        NodeChangeQueue<Node, NodeState> queue = new NodeChangeQueue<Node, NodeState>();
        service.updateNode(context, scope, new NodeContextChangeAdapter<Node, NodeState>(queue));
        return queue.iterator();
    }

    public Iterator<NodeChange<Node, NodeState>> rebase(NavigationService service, Scope<NodeState> scope) throws NavigationServiceException {
        NodeChangeQueue<Node, NodeState> queue = new NodeChangeQueue<Node, NodeState>();
        service.rebaseNode(context, scope, new NodeContextChangeAdapter<Node, NodeState>(queue));
        return queue.iterator();
    }

    public Iterator<NodeChange<Node, NodeState>> save(NavigationService service) throws NavigationServiceException {
        NodeChangeQueue<Node, NodeState> queue = new NodeChangeQueue<Node, NodeState>();
        service.saveNode(context, new NodeContextChangeAdapter<Node, NodeState>(queue));
        return queue.iterator();
    }
}
