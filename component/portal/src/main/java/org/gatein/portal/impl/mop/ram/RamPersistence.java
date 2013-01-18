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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.navigation.NavigationData;
import org.gatein.portal.mop.navigation.NavigationPersistence;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SitePersistence;
import org.gatein.portal.mop.site.SiteState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class RamPersistence implements SitePersistence, NavigationPersistence {

    /** . */
    private final Store store = new Store();

    public RamPersistence() {
        Store init = store.open();
        String root = init.getRoot();
        init.addChild(root, SiteType.PORTAL.getName(), SiteType.PORTAL);
        init.addChild(root, SiteType.GROUP.getName(), SiteType.GROUP);
        init.addChild(root, SiteType.USER.getName(), SiteType.USER);
        init.merge();
    }

    // SitePersistence *********************************************************************************************************

    @Override
    public SiteData loadSite(SiteKey key) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getName());
        if (site != null) {
            Node entry = current.getNode(site);
            return (SiteData)entry.getState();
        } else {
            return null;
        }
    }

    @Override
    public boolean saveSite(SiteKey key, SiteState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getName());
        if (site == null) {
            site = current.addChild(type, key.getName(), "");
            current.update(site, new SiteData(key, site, UUID.randomUUID().toString(), state));
            return true;
        } else {
            Node entry = current.getNode(site);
            SiteData data = (SiteData)entry;
            current.update(data.id, new SiteData(key, data.id, data.layoutId, state));
            return false;
        }
    }

    @Override
    public boolean destroySite(SiteKey key) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getTypeName());
        if (site != null) {
            current.remove(site);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Collection<SiteKey> findSites(SiteType siteType) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, siteType.getName());
        List<String> sites = current.getChildren(type);
        ArrayList<SiteKey> keys = new ArrayList<SiteKey>(sites.size());
        for (String site : sites) {
            Node entry = current.getNode(site);
            keys.add(((SiteData)entry.getState()).key);
        }
        return keys;
    }

    // NavigationPersistence ***************************************************************************************************


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
            current.addChild(navigation, "root", NodeState.INITIAL);
        }
    }

    @Override
    public boolean destroyNavigation(NavigationData data) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        SiteKey key = data.key;
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
        String nodeId;
        if (previousId == null) {
            nodeId = current.addChild(parentId, name, state);
        } else {
            nodeId = current.addSibling(previousId, name, state);
        }
        return new NodeData[]{
                getNode(current, parentId),
                getNode(current, nodeId)
        };
    }

    @Override
    public NodeData<NodeState> destroyNode(String targetId) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String parent = current.remove(targetId);
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
        current.move(targetId, toId, previousId);
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
        String parent = current.getParent(targetId);
        return new NodeData[]{
                getNode(current, targetId),
                getNode(current, parent)
        };
    }

    @Override
    public void close() {
    }

    private NodeData<NodeState> getNode(Store current, String nodeId) {
        String parent = current.getParent(nodeId);
        Node entry = current.getNode(parent);
        String parentId = entry.getState() instanceof NodeState ? parent : null;
        List<String> children = current.getChildren(nodeId);
        Node thisE = current.getNode(nodeId);
        return new NodeData<NodeState>(
                parentId,
                nodeId,
                thisE.getName(),
                (NodeState)thisE.getState(),
                children.toArray(new String[children.size()]));
    }
}
