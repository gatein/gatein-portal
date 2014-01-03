/*
 * Copyright (C) 2013 eXo Platform SAS.
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

import org.gatein.portal.mop.permission.SecurityState;
import org.gatein.portal.mop.permission.SecurityStore;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoSecurityStore implements SecurityStore {

    public static String ACCESS_PERMISSION = "accessPermission";
    
    public static String EDIT_PERMISSION = "editPermission";
    
    public static String SECURITY_STATE = "permissions";
    
    /** . */
    private final MongoStore store;

    public MongoSecurityStore(MongoStore store) {
        this.store = store;
    }

    private DBCollection getCollection() {
        return store.getDB().getCollection(SECURITY_STATE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SecurityState loadPermission(String id) {
        DBCollection collection = getCollection();
        DBObject doc = collection.findOne(make(id, null));
        if (doc != null) {
            BasicDBList mongoList = (BasicDBList)doc.get(ACCESS_PERMISSION);
            BasicDBList mongoEditList = (BasicDBList)doc.get(EDIT_PERMISSION);
            String[] accessList = mongoList == null ? null : mongoList.toArray(new String[mongoList.size()]);
            String[] editList = mongoEditList == null ? null : mongoEditList.toArray(new String[mongoEditList.size()]);
            return new SecurityState(accessList, editList);
        }
        return null;
    }

    @Override
    public void savePermission(String id, SecurityState state) {
        DBObject doc = make(id, state);
        if (state != null) {
            getCollection().save(doc);
        } else {
            getCollection().remove(doc);
        }
    }
    
    private DBObject make(String id, SecurityState state) {
        DBObject doc = new BasicDBObject("_id", id);
        if (state != null) {
            doc.put(ACCESS_PERMISSION, state.getAccessPermission());
            doc.put(EDIT_PERMISSION, state.getEditPermissions());
        }
        return doc;
    }
}