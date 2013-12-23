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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteState;
import org.gatein.portal.mop.site.SiteStore;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author Julien Viet
 */
public class MongoSiteStore implements SiteStore {

    /** . */
    private final MongoStore store;

    public MongoSiteStore(MongoStore store) {
        this.store = store;
    }

    private DBCollection getSites() {
        DB db = store.getDB();
        return db.getCollection("sites");
    }

    private static BasicDBObject getKey(SiteKey siteKey) {
        BasicDBObject key = new BasicDBObject();
        key.put("type", siteKey.getTypeName());
        key.put("name", siteKey.getName());
        return key;
    }

    @Override
    public SiteData loadSite(SiteKey siteKey) {
        DBCollection sites = getSites();
        DBObject key = getKey(siteKey);
        DBObject doc = sites.findOne(key);
        if (doc != null) {
            SiteState siteState = new SiteState(
                    (String) doc.get("locale"),
                    (String) doc.get("label"),
                    (String) doc.get("description"),
                    (List<String>) doc.get("access-permissions"),
                    Utils.firstElement((List<String>) doc.get("edit-permissions")),
                    Collections.<String, String>emptyMap(),
                    (String) doc.get("skin")
            );
            ObjectId id = (ObjectId) doc.get("_id");
            return new SiteData(siteKey, id.toString(), id.toString(), siteState);
        } else {
            return null;
        }
    }

    @Override
    public boolean saveSite(SiteKey siteKey, SiteState siteState) {
        DBCollection sites = getSites();
        BasicDBObject key = getKey(siteKey);
        DBObject doc = sites.findOne(key);
        boolean created;
        if (doc != null) {
            created = false;
        } else {
            created = true;
            doc = new BasicDBObject(key);
        }
        doc.put("locale", siteState.getLocale());
        doc.put("label", siteState.getLabel());
        doc.put("description", siteState.getDescription());
        doc.put("access-permissions", siteState.getAccessPermissions());
        doc.put("edit-permissions", Utils.safeList(siteState.getEditPermission()));
        doc.put("skin", siteState.getSkin());
        doc.put("properties", siteState.getProperties());
        sites.save(doc);
        return created;
    }

    @Override
    public boolean destroySite(SiteKey siteKey) {
        DBCollection sites = getSites();
        DBObject key = getKey(siteKey);
        DBObject doc = sites.findOne(key);
        if (doc != null) {
            sites.remove(key);
            ObjectId id = (ObjectId) doc.get("_id");
            store.getLayoutStore().destroy(id.toString());
            store.getSecurityStore().savePermission(id.toString(), null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Collection<SiteKey> findSites(SiteType type) {
        throw new UnsupportedOperationException("todo");
    }
}
