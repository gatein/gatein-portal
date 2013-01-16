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

package org.exoplatform.portal.mop.navigation;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.exoplatform.portal.mop.Described;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.Visible;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.link.Link;
import org.gatein.mop.api.workspace.link.PageLink;
import org.gatein.portal.mop.navigation.NavigationData;
import org.gatein.portal.mop.navigation.NavigationError;
import org.gatein.portal.mop.navigation.NavigationPersistence;
import org.gatein.portal.mop.navigation.NavigationServiceException;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;

import static org.exoplatform.portal.mop.Utils.objectType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class MopPersistence implements NavigationPersistence {

    /** . */
    private POMSession session;

    /** . */
    DataCache cache;

    /** The handles to evict. */
    final Set<String> toEvict;

    MopPersistence(POMSession session, DataCache cache) {

        cache.persistence = this;

        //
        this.session = session;
        this.cache = cache;
        this.toEvict = new HashSet<String>();
    }

    public List<NavigationData> loadNavigations(SiteType type) {
        ObjectType<Site> objectType = objectType(type);
        Collection<Site> sites = session.getWorkspace().getSites(objectType);
        List<NavigationData> navigations = new LinkedList<NavigationData>();
        for (Site site : sites) {
            Navigation navigation = site.getRootNavigation().getChild("default");
            if (navigation != null) {
                navigations.add(createData(navigation));
            }
        }
        return navigations;
    }

    private NavigationData createData(Navigation navigation) {
        Site site = navigation.getSite();
        ObjectType<? extends Site> objectType = site.getObjectType();
        SiteKey key = new SiteKey(Utils.siteType(objectType), site.getName());
        NavigationState navigationState = new NavigationState(navigation.getAttributes().getValue(MappedAttributes.PRIORITY, 1));
        return new NavigationData(key, navigationState, navigation.getObjectId());
    }

    public void saveNavigation(SiteKey key, NavigationState state) {
        ObjectType<Site> objectType = objectType(key.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, key.getName());
        if (site == null) {
            throw new NavigationServiceException(NavigationError.NAVIGATION_NO_SITE);
        }
        Navigation rootNode = site.getRootNavigation();
        Navigation defaultNode = rootNode.getChild("default");
        if (defaultNode == null) {
            defaultNode = rootNode.addChild("default");
        }
        if (state != null) {
            Integer priority = state.getPriority();
            defaultNode.getAttributes().setValue(MappedAttributes.PRIORITY, priority);
        }
        cache.removeNavigation(key);
    }

    public boolean destroyNavigation(NavigationData data) {
        ObjectType<Site> objectType = objectType(data.key.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, data.key.getName());
        if (site == null) {
            throw new NavigationServiceException(NavigationError.NAVIGATION_NO_SITE);
        }
        Navigation rootNode = site.getRootNavigation();
        Navigation defaultNode = rootNode.getChild("default");
        if (defaultNode != null) {
            defaultNode.destroy();
            cache.removeNavigation(data.key);
            String rootId = data.rootId;
            if (rootId != null) {
                cache.removeNodes(Collections.singleton(rootId));
            }
            return true;
        } else {
            return false;
        }
    }

    public NodeData<NodeState> getNode(String nodeId) {
        NodeData<NodeState> data;
        if (session.isModified()) {
            data = loadNode(nodeId);
        } else {
            data = cache.getNode(nodeId);
        }
        return data;
    }

    public NavigationData getNavigationData(SiteKey key) {
        NavigationData data;
        if (session.isModified()) {
            data = loadNavigation(key);
        } else {
            data = cache.getNavigation(key);
        }

        //
        return data;
    }

    NodeData<NodeState> loadNode(String nodeId) {
        Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
        if (navigation != null) {
            return create(navigation);
        } else {
            return null;
        }
    }

    private NodeData<NodeState> create(Navigation navigation) {
        String[] children;
        List<Navigation> _children = navigation.getChildren();
        if (_children == null) {
            children = Utils.EMPTY_STRING_ARRAY;
        } else {
            children = new String[_children.size()];
            int index = 0;
            for (Navigation child : _children) {
                children[index++] = child.getObjectId();
            }
        }

        //
        String label = null;
        if (navigation.isAdapted(Described.class)) {
            Described described = navigation.adapt(Described.class);
            label = described.getName();
        }

        //
        Visibility visibility = Visibility.DISPLAYED;
        Date startPublicationDate = null;
        Date endPublicationDate = null;
        if (navigation.isAdapted(Visible.class)) {
            Visible visible = navigation.adapt(Visible.class);
            visibility = visible.getVisibility();
            startPublicationDate = visible.getStartPublicationDate();
            endPublicationDate = visible.getEndPublicationDate();
        }

        //
        PageKey pageRef = null;
        Link link = navigation.getLink();
        if (link instanceof PageLink) {
            PageLink pageLink = (PageLink) link;
            org.gatein.mop.api.workspace.Page target = pageLink.getPage();
            if (target != null) {
                Site site = target.getSite();
                pageRef = Utils.siteType(site.getObjectType()).key(site.getName()).page(target.getName());
            }
        }

        //
        Attributes attrs = navigation.getAttributes();

        //
        NodeState state = new NodeState(label, attrs.getValue(MappedAttributes.ICON),
                startPublicationDate != null ? startPublicationDate.getTime() : -1,
                endPublicationDate != null ? endPublicationDate.getTime() : -1, visibility, pageRef);

        //
        String parentId;
        Navigation parent = navigation.getParent();
        if (parent != null) {
            parentId = parent.getObjectId();
        } else {
            parentId = null;
        }

        //
        return new NodeData<NodeState>(
                parentId,
                navigation.getObjectId(),
                navigation.getName(),
                state,
                children
        );
    }

    protected final NavigationData loadNavigation(SiteKey key) {
        Workspace workspace = session.getWorkspace();
        ObjectType<Site> objectType = objectType(key.getType());
        Site site = workspace.getSite(objectType, key.getName());
        if (site != null) {
            Navigation navigation = site.getRootNavigation().getChild("default");
            if (navigation != null) {
                return createData(navigation);
            } else {
                return NavigationData.EMPTY;
            }
        } else {
            return NavigationData.EMPTY;
        }
    }

    public NodeData<NodeState>[] createNode(String parentId, String previousId, String name, NodeState state) {
        Navigation parent = session.findObjectById(ObjectType.NAVIGATION, parentId);
        int index = 0;
        if (previousId != null) {
            Navigation previous = session.findObjectById(ObjectType.NAVIGATION, previousId);
            index = previous.getIndex() + 1;
        }
        Navigation target = parent.addChild(index, name);
        set(state, target);
        toEvict.add(parentId);
        return new NodeData[]{create(parent),create(target)};
    }

    public NodeData<NodeState> destroyNode(String targetId) {
        Navigation target = session.findObjectById(ObjectType.NAVIGATION, targetId);
        Navigation parent = target.getParent();
        target.destroy();
        toEvict.add(targetId);
        toEvict.add(parent.getObjectId());
        return create(parent);
    }

    public NodeData<NodeState> updateNode(String targetId, NodeState state) {
        Navigation target = session.findObjectById(ObjectType.NAVIGATION, targetId);
        set(state, target);
        toEvict.add(targetId);
        return create(target);
    }

    private static void set(NodeState state, Navigation target) {
        Workspace workspace = target.getSite().getWorkspace();
        PageKey reference = state.getPageRef();
        if (reference != null) {
            ObjectType<? extends Site> siteType = Utils.objectType(reference.getSite().getType());
            Site site = workspace.getSite(siteType, reference.getSite().getName());
            org.gatein.mop.api.workspace.Page page = site.getRootPage().getChild("pages").getChild(reference.getName());
            PageLink link = target.linkTo(ObjectType.PAGE_LINK);
            link.setPage(page);
        } else {
            PageLink link = target.linkTo(ObjectType.PAGE_LINK);
            link.setPage(null);
        }
        Described described = target.adapt(Described.class);
        described.setName(state.getLabel());
        Visible visible = target.adapt(Visible.class);
        visible.setVisibility(state.getVisibility());
        visible.setStartPublicationDate(state.getStartPublicationDate());
        visible.setEndPublicationDate(state.getEndPublicationDate());
        Attributes attrs = target.getAttributes();
        attrs.setValue(MappedAttributes.ICON, state.getIcon());
    }

    public NodeData<NodeState>[] moveNode(String targetId, String fromId, String toId, String previousId) {
        Navigation target = session.findObjectById(ObjectType.NAVIGATION, targetId);
        Navigation from = session.findObjectById(ObjectType.NAVIGATION, fromId);
        Navigation to = session.findObjectById(ObjectType.NAVIGATION, toId);
        int index;
        if (previousId != null) {
            Navigation previousNav = session.findObjectById(ObjectType.NAVIGATION, previousId);
            index = previousNav.getIndex() + 1;
        } else {
            index = 0;
        }
        to.getChildren().add(index, target);
        toEvict.add(targetId);
        toEvict.add(fromId);
        toEvict.add(toId);
        return new NodeData[]{create(target),create(from),create(to)};
    }

    public NodeData<NodeState>[] rename(String targetId, String parentId, String name) {
        Navigation target = session.findObjectById(ObjectType.NAVIGATION, targetId);
        Navigation parent = session.findObjectById(ObjectType.NAVIGATION, parentId);
        target.setName(name);
        toEvict.add(targetId);
        toEvict.add(parentId);
        return new NodeData[]{create(target),create(parent)};
    }

    @Override
    public void close() {
        if (toEvict.size() > 0) {
            cache.removeNodes(toEvict);
        }
    }

    public void clear() {
        cache.clear();
    }
}
