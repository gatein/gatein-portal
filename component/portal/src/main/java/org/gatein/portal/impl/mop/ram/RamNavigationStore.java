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

import java.util.ArrayList;
import java.util.List;

import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.navigation.NavigationData;
import org.gatein.portal.mop.navigation.NavigationStore;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
public class RamNavigationStore implements NavigationStore {

    /** . */
    private Store store;

    public RamNavigationStore(RamStore persistence) {
        this.store = persistence.store;
    }

    @Override
    public List<NavigationData> loadNavigations(SiteType siteType) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, siteType.getName());
        List<String> sites = current.getChildren(type);
        ArrayList<NavigationData> navigations = new ArrayList<NavigationData>(sites.size());
        for (String site : sites) {
            Node siteEntry = current.getNode(site);
            String navigation = current.getChild(site, "navigation");
            if (navigation != null) {
                Node entry = current.getNode(navigation);
                NavigationState state = (NavigationState) entry.getState();
                String node = current.getChild(navigation, "root");
                SiteKey key = siteType.key(siteEntry.getName());
                NavigationData data = new NavigationData(key, state, node);
                navigations.add(data);
            }
        }
        return navigations;
    }

    @Override
    public NavigationData loadNavigationData(SiteKey key) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getName());
        if (site != null) {
            String navigation = current.getChild(site, "navigation");
            if (navigation != null) {
                Node entry = current.getNode(navigation);
                NavigationState state = (NavigationState) entry.getState();
                String node = current.getChild(navigation, "root");
                return new NavigationData(key, state, node);
            }
        }
        return null;
    }

    @Override
    public void saveNavigation(SiteKey key, NavigationState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getName());
        String navigation = current.getChild(site, "navigation");
        if (navigation != null) {
            current.update(navigation, state);
        } else {
            navigation = current.addChild(site, "navigation", state);
            String node = current.addChild(navigation, "root", NodeState.INITIAL);
            current.addChild(node, "children", "not-yet-used");
        }
    }

    @Override
    public boolean destroyNavigation(SiteKey key) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getName());
        String navigation = current.getChild(site, "navigation");
        if (navigation != null) {
            current.remove(navigation);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        // Nothing to do
    }

    @Override
    public NodeData<NodeState> loadNode(String nodeId) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        if (current.contains(nodeId)) {
            return getNode(current, nodeId);
        } else {
            return null;
        }
    }

    @Override
    public NodeData<NodeState>[] createNode(String parentId, String previousId, String name, NodeState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String container = current.getChild(parentId, "children");
        String nodeId = current.addChild(container, previousId, name, state);
        current.addChild(nodeId, null, "children", "not-yet-used");
        return new NodeData[]{
                getNode(current, parentId),
                getNode(current, nodeId)
        };
    }

    @Override
    public NodeData<NodeState> destroyNode(String targetId) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String container = current.remove(targetId);
        String parent = current.getParent(container);
        return getNode(current, parent);
    }

    @Override
    public NodeData<NodeState> updateNode(String targetId, NodeState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        current.update(targetId, state);
        return getNode(current, targetId);
    }

    @Override
    public NodeData<NodeState>[] moveNode(String targetId, String fromId, String toId, String previousId) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String toContainer = current.getChild(toId, "children");
        current.move(targetId, toContainer, previousId);
        return new NodeData[]{
                getNode(current, targetId),
                getNode(current, fromId),
                getNode(current, toId)
        };
    }

    @Override
    public NodeData<NodeState>[] renameNode(String targetId, String parentId, String name) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        current.rename(targetId, name);
        String container = current.getParent(targetId);
        String parent = current.getParent(container);
        return new NodeData[]{
                getNode(current, targetId),
                getNode(current, parent)
        };
    }

    @Override
    public void flush() {
    }

    private NodeData<NodeState> getNode(Store current, String nodeId) {
        String parent = current.getParent(nodeId);
        Node entry = current.getNode(parent);
        String parentId;
        if (entry.getState() instanceof NavigationState) {
            parentId = null;
        } else {
            parentId = current.getParent(parent);
        }
        String container = current.getChild(nodeId, "children");
        List<String> children = current.getChildren(container);
        Node thisE = current.getNode(nodeId);
        return new NodeData<NodeState>(
                parentId,
                nodeId,
                thisE.getName(),
                (NodeState)thisE.getState(),
                children.toArray(new String[children.size()]));
    }
}
