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

package org.gatein.portal.mop.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ModelNode<M extends ModelNode<M, S>, S extends Serializable> {

    /** . */
    protected final NodeContext<M, S> context;

    public ModelNode(NodeContext<M, S> context) {
        this.context = context;
    }

    public String getId() {
        return context.getId();
    }

    public String getHandle() {
        return context.getHandle();
    }

    public String getName() {
        return context.getName();
    }

    public void setName(String name) {
        context.setName(name);
    }

    public NodeContext<M, S> getContext() {
        return context;
    }

    public S getState() {
        return context.getState();
    }

    public void setState(S state) {
        context.setState(state);
    }

    public M getParent() {
        return context.getParentNode();
    }

    public Collection<M> getChildren() {
        return context.getNodes();
    }

    public M getChild(String childName) {
        return context.getNode(childName);
    }

    public M getChild(int childIndex) {
        return context.getNode(childIndex);
    }

    public void addChild(M child) {
        context.add(null, child.context);
    }

    public void addChild(int index, M child) {
        context.add(index, child.context);
    }

    public M addChild(String childName, S state) {
        return context.add(null, childName, state).getNode();
    }

    public M addChild(int index, String childName, S state) {
        return context.add(index, childName, state).getNode();
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

    public void filter(NodeFilter<S> filter) {
        context.filter(filter);
    }

    public void assertConsistent() {
        if (context.isExpanded()) {
            List<String> a = new ArrayList<String>();
            for (NodeContext<M, S> b = context.getFirst(); b != null; b = b.getNext()) {
                Assert.assertNotNull(b.getData());
                a.add(b.getId());
            }
            List<String> b = Arrays.asList(context.data.children);
            Assert.assertEquals(a, b);
            for (NodeContext<M, S> c = context.getFirst(); c != null; c = c.getNext()) {
                c.getNode().assertConsistent();
            }
        }
    }

    public void assertEquals(M node) {

        // First check state
        if (context.data != null) {
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
        List<M> nodes1 = new ArrayList<M>();
        for (NodeContext<M, S> current = context.getFirst(); current != null; current = current.getNext()) {
            nodes1.add(current.getNode());
        }

        //
        List<M> nodes2 = new ArrayList<M>();
        for (NodeContext<M, S> current = node.context.getFirst(); current != null; current = current.getNext()) {
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

    @Override
    public String toString() {
        return toString(1);
    }

    public String toString(int depth) {
        return context.toString(depth, new StringBuilder("Node[")).append("]").toString();
    }
}
