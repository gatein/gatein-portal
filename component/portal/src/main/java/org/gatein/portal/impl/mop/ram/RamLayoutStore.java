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

package org.gatein.portal.impl.mop.ram;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.hierarchy.NodeStore;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutStore;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
public class RamLayoutStore implements LayoutStore, NodeStore<ElementState> {

    /** . */
    static final ElementState.Container INITIAL = new ElementState.Container(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false
    );
    /** . */
    private Store store;

    public RamLayoutStore(RamStore persistence) {
        this.store = persistence.store;
    }

    private NodeData<ElementState> getNode(Store current, String nodeId) {
        String parent;
        try {
            parent = current.getParent(nodeId);
        } catch (NoSuchElementException e) {
            return null;
        }
        Node parentNode = current.getNode(parent);
        String parentId = parentNode.getState() instanceof ElementState ? parent : null;
        List<String> children = current.getChildren(nodeId);
        Node element = current.getNode(nodeId);
        ElementState state = (ElementState) element.getState();
        if (state instanceof ElementState.Window) {
            ElementState.Window windowState = (ElementState.Window) state;
            state = new ElementState.Window(
                    windowState.type,
                    new PersistentApplicationState(nodeId),
                    windowState.properties
            );
        }
        return new NodeData<ElementState>(
                parentId,
                nodeId,
                element.getName(),
                state,
                children.toArray(new String[children.size()]));
    }

    @Override
    public NodeStore<ElementState> begin(String rootId, boolean write) {
        return this;
    }

    @Override
    public void end(NodeStore<ElementState> store) {
        // Do nothing
    }

    @Override
    public NodeData<ElementState> loadNode(String nodeId) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        return getNode(current, nodeId);
    }

    @Override
    public NodeData<ElementState>[] createNode(String parentId, String previousId, String name, ElementState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String added = current.addChild(parentId, previousId, name, state);
        return new NodeData[]{
                getNode(current, parentId),
                getNode(current, added),
        };
    }

    @Override
    public NodeData<ElementState> destroyNode(String targetId) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String parent = current.getParent(targetId);
        current.remove(targetId);
        return getNode(current, parent);
    }

    @Override
    public NodeData<ElementState> updateNode(String targetId, ElementState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        if (state instanceof ElementState.Window<?>) {
            Node customization = current.getNode(targetId);
            ElementState.Window windowState = (ElementState.Window) customization.getState();
            state = ((ElementState.Window) state).builder().state(windowState.state).build();
        }
        current.update(targetId, state);
        return getNode(current, targetId);
    }

    @Override
    public NodeData<ElementState>[] moveNode(String targetId, String fromId, String toId, String previousId) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        current.move(targetId, toId, previousId);
        return new NodeData[] {
                getNode(current, targetId),
                getNode(current, fromId),
                getNode(current, toId)
        };
    }

    @Override
    public NodeData<ElementState>[] renameNode(String targetId, String parentId, String name) {
        // We  don't support it as it's not necessary (yet)
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        // Nothing to do
    }
}
