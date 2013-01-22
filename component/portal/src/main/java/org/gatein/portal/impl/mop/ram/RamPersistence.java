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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.utils.I18N;
import org.gatein.portal.mop.description.DescriptionPersistence;
import org.gatein.portal.mop.description.DescriptionState;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.hierarchy.NodePersistence;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.navigation.NavigationData;
import org.gatein.portal.mop.navigation.NavigationPersistence;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageError;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PagePersistence;
import org.gatein.portal.mop.page.PageServiceException;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SitePersistence;
import org.gatein.portal.mop.site.SiteState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class RamPersistence {

    /** . */
    private static final ElementState.Container INITIAL = new ElementState.Container(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Collections.<String>emptyList(),
            false
    );

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

    public PagePersistence getPagePersistence() {
        return page;
    }

    public DescriptionPersistence getDescriptionPersistence() {
        return description;
    }

    public NavigationPersistence getNavigationPersistence() {
        return navigation;
    }

    public SitePersistence getSitePersistence() {
        return site;
    }

    public NodePersistence<ElementState> getLayoutPersistence() {
        return layout;
    }

    private final SitePersistence site = new SitePersistence() {

        @Override
        public SiteData loadSite(SiteKey key) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String root = current.getRoot();
            String type = current.getChild(root, key.getTypeName());
            String site = current.getChild(type, key.getName());
            if (site != null) {
                Node entry = current.getNode(site);
                String layout = current.getChild(site, "layout");
                return new SiteData(key, site, layout, (SiteState)entry.getState());
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
                site = current.addChild(type, key.getName(), state);
                current.addChild(site, "pages", "");
                current.addChild(site, "layout", INITIAL);
                return true;
            } else {
                Node entry = current.getNode(site);
                SiteData data = (SiteData)entry;
                current.update(data.id, state);
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
    };

    /** . */
    private final NavigationPersistence navigation = new NavigationPersistence() {

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
        public void close() {
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
    };


    /** . */
    private final PagePersistence page = new PagePersistence() {

        @Override
        public PageData loadPage(PageKey key) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String root = current.getRoot();
            String type = current.getChild(root, key.getSite().getTypeName());
            String site = current.getChild(type, key.getSite().getName());
            if (site != null) {
                String pages = current.getChild(site, "pages");
                String page = current.getChild(pages, key.getName());
                if (page != null) {
                    return data(current, key, page);
                }
            }
            return null;
        }

        private PageData data(Store store, PageKey key, String page) {
            String layout = store.getChild(page, "layout");
            PageState state = (PageState) store.getNode(page).getState();
            return new PageData(key, page, layout, state);
        }

        @Override
        public boolean savePage(PageKey key, PageState state) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String root = current.getRoot();
            String type = current.getChild(root, key.getSite().getTypeName());
            String site = current.getChild(type, key.getSite().getName());
            if (site != null) {
                String pages = current.getChild(site, "pages");
                String page = current.getChild(pages, key.getName());
                if (page != null) {
                    current.update(page, state);
                    return false;
                } else {
                    page = current.addChild(pages, key.getName(), state);
                    current.addChild(page, "layout", "");
                    return true;
                }
            } else {
                throw new PageServiceException(PageError.NO_SITE);
            }
        }

        @Override
        public boolean destroyPage(PageKey key) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String root = current.getRoot();
            String type = current.getChild(root, key.getSite().getTypeName());
            String site = current.getChild(type, key.getSite().getName());
            if (site != null) {
                String pages = current.getChild(site, "pages");
                String page = current.getChild(pages, key.getName());
                if (current.contains(page)) {
                    current.remove(page);
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new PageServiceException(PageError.NO_SITE);
            }
        }

        @Override
        public PageData clonePage(PageKey src, PageKey dst) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String root = current.getRoot();
            String srcType = current.getChild(root, src.getSite().getTypeName());
            String srcSite = current.getChild(srcType, src.getSite().getName());
            if (srcSite == null) {
                throw new PageServiceException(PageError.CLONE_NO_SRC_SITE, "Could not clone page " + src.getName()
                        + "from non existing site of type " + src.site.getType() + " with id " + src.site.getName());
            }
            String srcPages = current.getChild(srcSite, "pages");
            String srcPage = current.getChild(srcPages, src.getName());
            if (srcPage == null) {
                throw new PageServiceException(PageError.CLONE_NO_SRC_PAGE, "Could not clone non existing page " + src.getName()
                        + " from site of type " + src.site.getType() + " with id " + src.site.getName());
            }
            String dstType = current.getChild(root, dst.getSite().getTypeName());
            String dstSite = current.getChild(dstType, dst.getSite().getName());
            if (dstSite == null) {
                throw new PageServiceException(PageError.CLONE_NO_DST_SITE, "Could not clone page " + dst.name
                        + "to non existing site of type " + dst.site.getType() + " with id " + dst.site.getName());
            }
            String dstPages = current.getChild(dstSite, "pages");
            String dstPage = current.getChild(dstPages, dst.getName());
            if (dstPage != null) {
                throw new PageServiceException(PageError.CLONE_DST_ALREADY_EXIST, "Could not clone page " + dst.name
                        + "to existing page " + dst.site.getType() + " with id " + dst.site.getName());
            }
            String clone = current.clone(srcPage, dstPages, dst.getName());
            return data(current, dst, clone);
        }

        @Override
        public List<PageKey> findPageKeys(SiteKey siteKey) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String root = current.getRoot();
            String type = current.getChild(root, siteKey.getTypeName());
            String site = current.getChild(type, siteKey.getName());
            if (site != null) {
                String pages = current.getChild(site, "pages");
                List<String> children = current.getChildren(pages);
                ArrayList<PageKey> keys = new ArrayList<PageKey>(children.size());
                for (String page : children) {
                    Node node = current.getNode(page);
                    keys.add(siteKey.page(node.getName()));
                }
                return keys;
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public Collection<PageData> findPages(int from, int to, SiteType siteType, String siteName, String pageName, String pageTitle) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String root = current.getRoot();
            String type = current.getChild(root, siteType.getName());
            List<String> sites = new ArrayList<String>();
            if (siteName != null) {
                String site = current.getChild(type, siteName);
                if (site != null) {
                    sites.add(site);
                }
            } else {
                sites = current.getChildren(type);
            }
            ArrayList<PageData> matches = new ArrayList<PageData>();
            for (String site : sites) {
                SiteKey siteKey = siteType.key(siteName);
                String pages = current.getChild(site, "pages");
                List<String> children = current.getChildren(pages);
                for (String page : children) {
                    Node node = current.getNode(page);
                    PageState state = (PageState) node.getState();
                    if (pageName == null || pageName.equals(node.getName())) {
                        if (pageTitle == null || pageTitle.equals(state.getDisplayName())) {
                            String layout = current.getChild(page, "layout");
                            matches.add(new PageData(siteKey.page(node.getName()), page, layout, state));
                        }
                    }
                }
            }
            return matches;
        }

        @Override
        public void clear() {
            // Nothing to do
        }
    };

    /** . */
    private NodePersistence<ElementState> layout = new NodePersistence<ElementState>() {

        private NodeData<ElementState> getNode(Store current, String nodeId) {
            String parent = current.getParent(nodeId);
            Node parentNode = current.getNode(parent);
            String parentId = parentNode.getState() instanceof ElementState ? parent : null;
            List<String> children = current.getChildren(nodeId);
            Node element = current.getNode(nodeId);
            return new NodeData<ElementState>(
                    parentId,
                    nodeId,
                    element.getName(),
                    (ElementState)element.getState(),
                    children.toArray(new String[children.size()]));
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
        public void close() {
            // Nothing to do
        }
    };

    /** Todo : rework description state that contains label ? . */
    private DescriptionPersistence description = new DescriptionPersistence() {

        private Locale parent(Locale locale) {
            if (locale.getVariant() != null && !locale.getVariant().isEmpty()) {
                return new Locale(locale.getLanguage(), locale.getCountry());
            } else if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
                return new Locale(locale.getLanguage());
            } else {
                return null;
            }
        }

        /**
         * todo : move this code to DescriptionService
         */
        private void validateLocale(Locale locale) {
            if (locale.getLanguage().length() != 2) {
                throw new IllegalArgumentException("Illegal locale");
            }
            if (locale.getCountry().length() != 0 && locale.getCountry().length() != 2) {
                throw new IllegalArgumentException("Illegal locale");
            }
            if (locale.getVariant().length() != 0 && locale.getVariant().length() != 2) {
                throw new IllegalArgumentException("Illegal locale");
            }
        }

        @Override
        public DescriptionState resolveDescription(String id, Locale locale) throws NullPointerException {
            return getDescription(id, locale, true);
        }

        @Override
        public DescriptionState getDescription(String id, Locale locale) {
            return getDescription(id, locale, false);
        }

        private DescriptionState getDescription(String id, Locale locale, boolean resolve) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            Node node = current.getNode(id);
            if (locale == null) {
                NodeState nodeState = (NodeState)node.getState();
                if (resolve || nodeState.getLabel() != null) {
                    return new DescriptionState(nodeState.getLabel(), null);
                }
            } else {
                for (Locale l = locale; l != null; l = parent(l)) {
                    String descriptions = current.getChild(id, "descriptions");
                    if (descriptions != null) {
                        String description = current.getChild(descriptions, l.toString());
                        if (description != null) {
                            Node descriptionNode = current.getNode(description);
                            return (DescriptionState) descriptionNode.getState();
                        }
                    }
                    if (!resolve) {
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        public void setDescription(String id, Locale locale, DescriptionState state) {
            validateLocale(locale);
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String descriptions = current.getChild(id, "descriptions");
            if (descriptions == null) {
                descriptions = current.addChild(id, "descriptions", "not-yet-used");
            }
            String description = current.getChild(descriptions, locale.toString());
            if (description == null) {
                current.addChild(descriptions, locale.toString(), state);
            } else {
                current.update(description, state);
            }
        }

        @Override
        public void setDescription(String id, DescriptionState description) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            NodeState state = (NodeState) current.getNode(id).getState();
            String label = description != null ? description.getName() : null;
            current.update(id, state.builder().label(label).build());
        }

        @Override
        public Map<Locale, DescriptionState> getDescriptions(String id) {
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String descriptions = current.getChild(id, "descriptions");
            if (descriptions == null) {
                return null;
            } else {
                HashMap<Locale, DescriptionState> states = new HashMap<Locale, DescriptionState>();
                for (String child : current.getChildren(descriptions)) {
                    Node node = current.getNode(child);
                    Locale locale = I18N.parseJavaIdentifier(node.getName());
                    DescriptionState state = (DescriptionState) node.getState();
                    states.put(locale, state);
                }
                return states;
            }
        }

        @Override
        public void setDescriptions(String id, Map<Locale, DescriptionState> states) {
            for (Locale locale : states.keySet()) {
                validateLocale(locale);
            }
            Tx tx = Tx.associate(store);
            Store current = tx.getContext();
            String descriptions = current.getChild(id, "descriptions");
            if (descriptions != null) {
                current.remove(descriptions);
            }
            if (states.size() > 0) {
                descriptions = current.addChild(id, "descriptions", "not-yet-used");
                for (Map.Entry<Locale, DescriptionState> state : states.entrySet()) {
                    current.addChild(descriptions, state.getKey().toString(), state.getValue());
                }
            }
        }
    };
}
