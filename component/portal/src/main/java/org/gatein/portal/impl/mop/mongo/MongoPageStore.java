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
package org.gatein.portal.impl.mop.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageError;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageServiceException;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.page.PageStore;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author Julien Viet
 */
public class MongoPageStore implements PageStore {

    /** . */
    private final MongoStore store;

    public MongoPageStore(MongoStore store) {
        this.store = store;
    }

    private DBCollection getPages() {
        return store.getDB().getCollection("pages");
    }

    private static BasicDBObject getKey(PageKey pageKey) {
        return getKey(pageKey.getSite().getType(), pageKey.getSite().getName(), pageKey.getName());
    }

    private static BasicDBObject getKey(SiteType siteType, String siteName, String name) {
        BasicDBObject key = new BasicDBObject();
        if (siteType != null) {
            key.put("site_type", siteType.getName());
        }
        if (siteName != null) {
            key.put("site_name", siteName);
        }
        if (name != null) {
            key.put("name", name);
        }
        return key;
    }

    @Override
    public PageData loadPage(PageKey pageKey) {
        BasicDBObject key = getKey(pageKey);
        DBCollection pages = getPages();
        DBObject doc = pages.findOne(key);
        if (doc != null) {
            return getPage(doc);
        } else {
            return null;
        }
    }

    private PageData getPage(DBObject doc) {
        ObjectId id = (ObjectId) doc.get("_id");
        SiteType type = SiteType.valueOf(((String) doc.get("site_type")).toUpperCase());
        SiteKey siteKey = new SiteKey(type, (String) doc.get("site_name"));
        PageKey pageKey = new PageKey(siteKey, (String) doc.get("name"));
        PageState pageState = new PageState(
                (String) doc.get("display_name"),
                (String) doc.get("description"),
                (Boolean) doc.get("show_max_window"),
                (String) doc.get("factory_id"),
                (List<String>) doc.get("access-permissions"),
                Utils.firstElement((List<String>) doc.get("edit-permissions"))
        );
        return new PageData(pageKey, id.toString(), id.toString(), pageState);
    }

    @Override
    public boolean savePage(PageKey pageKey, PageState pageState) {

        // Check site existence first
        if (store.getSiteStore().loadSite(pageKey.getSite()) == null) {
            throw new PageServiceException(PageError.NO_SITE);
        }

        //
        BasicDBObject key = getKey(pageKey);
        DBCollection pages = getPages();
        DBObject doc = pages.findOne(key);
        boolean created;
        if (doc != null) {
            created = false;
        } else {
            created = true;
            doc = new BasicDBObject(key);
        }
        doc.put("display_name", pageState.getDisplayName());
        doc.put("description", pageState.getDescription());
        doc.put("access-permissions", pageState.getAccessPermissions());
        doc.put("edit-permissions", Utils.safeList(pageState.getEditPermission()));
        doc.put("factory_id", pageState.getFactoryId());
        doc.put("show_max_window", pageState.getShowMaxWindow());
        pages.save(doc);
        return created;
    }

    @Override
    public boolean destroyPage(PageKey pageKey) {

        // Check site existence first
        if (store.getSiteStore().loadSite(pageKey.getSite()) == null) {
            throw new PageServiceException(PageError.NO_SITE);
        }

        //
        DBCollection pages = getPages();
        BasicDBObject key = getKey(pageKey);
        DBObject doc = pages.findOne(key);
        if (doc != null) {
            ObjectId id = (ObjectId) doc.get("_id");
            pages.remove(key);
            store.getSecurityStore().savePermission(id.toString(), null);
            store.getLayoutStore().destroy(id.toString());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PageData clonePage(PageKey src, PageKey dst) {
        PageData srcPage;
        try {
            srcPage = loadPage(src);
        } catch (PageServiceException e) {
            throw PageError.cloneNoSrcSite(src);
        }
        if (srcPage == null) {
            throw PageError.cloneNoSrcPage(src);
        } else {
            PageData dstPage;
            try {
                dstPage = loadPage(dst);
            } catch (PageServiceException e) {
                throw PageError.cloneNoDstSite(dst);
            }
            if (dstPage != null) {
                throw PageError.cloneDstAlreadyExists(dst);
            } else {
                savePage(dst, srcPage.state);
                return loadPage(dst);
            }
        }
    }

    @Override
    public List<PageKey> findPageKeys(SiteKey siteKey) {
        DBObject key = getKey(siteKey.getType(), siteKey.getName(), null);
        DBCursor cursor = getPages().find(key);
        ArrayList<PageKey> pageKeys = new ArrayList<PageKey>(cursor.size());
        for (DBObject doc : cursor) {
            PageKey pageKey = siteKey.page((String) doc.get("name"));
            pageKeys.add(pageKey);
        }
        return pageKeys;
    }

    @Override
    public Collection<PageData> findPages(int from, int to, SiteType siteType, String siteName, String pageName, String pageTitle) {
        DBObject key = getKey(siteType, siteName, pageName);
        DBCursor cursor = getPages().find(key).skip(from).limit(to - from);
        ArrayList<PageData> pages = new ArrayList<PageData>(cursor.size());
        for (DBObject doc : cursor) {
            PageData page = getPage(doc);
            pages.add(page);
        }
        return pages;
    }

    @Override
    public void clear() {
    }
}
