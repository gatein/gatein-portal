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

package org.exoplatform.portal.mop.hierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NodeState;

/**
 * Represents a navigation node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Node {

    /** . */
    public static final NodeModel<Node, NodeState> MODEL = new NodeModel<Node, NodeState>() {
        public NodeContext<Node, NodeState> getContext(Node node) {
            return node.context;
        }

        public Node create(NodeContext<Node, NodeState> context) {
            return new Node(context);
        }
    };

    /** . */
    final NodeContext<Node, NodeState> context;

    Node(NodeContext<Node, NodeState> context) {
        this.context = context;
    }

    public String getId() {
        return context.getId();
    }

    public String getHandle() {
        return context.handle;
    }

    public String getName() {
        return context.getName();
    }

    public void setName(String name) {
        context.setName(name);
    }

    public NodeContext<Node, NodeState> getContext() {
        return context;
    }

    public NodeState getState() {
        return context.getState();
    }

    public void setState(NodeState state) {
        context.setState(state);
    }

    public Node getParent() {
        return context.getParentNode();
    }

    public Collection<Node> getChildren() {
        return context.getNodes();
    }

    public Node getChild(String childName) {
        return context.getNode(childName);
    }

    public Node getChild(int childIndex) {
        return context.getNode(childIndex);
    }

    public void addChild(Node child) {
        context.add(null, child.context);
    }

    public void addChild(int index, Node child) {
        context.add(index, child.context);
    }

    public Node addChild(String childName) {
        return context.add(null, childName, NodeState.INITIAL).node;
    }

    public Node addChild(int index, String childName) {
        return context.add(index, childName, NodeState.INITIAL).node;
    }

    public boolean removeChild(String childName) {
        return context.removeNode(childName);
    }

    public int getNodeCount() {
        return context.getNodeCount();
    }

    public int getSize() {
        return context.getSize();
    }

    public void setHidden(boolean hidden) {
        context.setHidden(hidden);
    }

    public boolean isHidden() {
        return context.isHidden();
    }

    public void filter(NodeFilter<NodeState> filter) {
        context.filter(filter);
    }

    public void assertConsistent() {
        if (context.isExpanded()) {
            List<String> a = new ArrayList<String>();
            for (NodeContext<Node, NodeState> b = context.getFirst(); b != null; b = b.getNext()) {
                Assert.assertNotNull(b.data);
                a.add(b.data.getId());
            }
            List<String> b = Arrays.asList(context.data.children);
            Assert.assertEquals(a, b);
            for (NodeContext<Node, NodeState> c = context.getFirst(); c != null; c = c.getNext()) {
                c.getNode().assertConsistent();
            }
        }
    }

    public void assertEquals(Node node) {

        // First check state
        if (context.data != null) {
            Assert.assertNotNull(node.context.data);
            Assert.assertEquals(context.data.id, node.context.data.id);
            Assert.assertEquals(context.data.name, node.context.data.name);
            Assert.assertEquals(context.data.state, node.context.data.state);
            Assert.assertEquals(context.state, node.context.state);
        } else {
            Assert.assertNull(node.context.data);
            Assert.assertEquals(context.getName(), node.context.getName());
            Assert.assertEquals(context.state, node.context.state);
        }

        //
        List<Node> nodes1 = new ArrayList<Node>();
        for (NodeContext<Node, NodeState> current = context.getFirst(); current != null; current = current.getNext()) {
            nodes1.add(current.getNode());
        }

        //
        List<Node> nodes2 = new ArrayList<Node>();
        for (NodeContext<Node, NodeState> current = node.context.getFirst(); current != null; current = current.getNext()) {
            nodes2.add(current.getNode());
        }

        //
        Assert.assertEquals("Was expecting to have the same children for node " + toString(1) + " " + node.toString(1),
                nodes1.size(), nodes2.size());

        //
        for (int i = 0; i < nodes1.size(); i++) {
            nodes1.get(i).assertEquals(nodes2.get(i));
        }
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

    @Override
    public String toString() {
        return toString(1);
    }

    public String toString(int depth) {
        return context.toString(depth, new StringBuilder("Node[")).append("]").toString();
    }
}
