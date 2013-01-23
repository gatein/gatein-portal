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
import java.util.List;

import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageError;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageStore;
import org.gatein.portal.mop.page.PageServiceException;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
public class RamPageStore implements PageStore {

    /** . */
    private Store store;

    public RamPageStore(RamStore persistence) {
        this.store = persistence.store;
    }

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
                current.addChild(page, "layout", RamLayoutStore.INITIAL);
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
}
