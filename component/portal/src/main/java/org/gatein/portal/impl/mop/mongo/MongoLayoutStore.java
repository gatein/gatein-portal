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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.gatein.portal.mop.hierarchy.NodeStore;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutStore;

/**
 * @author Julien Viet
 */
public class MongoLayoutStore implements LayoutStore {

    /** . */
    private final MongoStore store;

    public MongoLayoutStore(MongoStore store) {
        this.store = store;
    }

    DBCollection getLayouts() {
        return store.getDB().getCollection("layouts");
    }

    @Override
    public NodeStore<ElementState> begin(String rootId, boolean write) {
        DBObject doc = getLayouts().findOne(new BasicDBObject("_id", new ObjectId(rootId)));
        if (doc != null) {
            return new MongoElementStore(doc, store.getSecurityStore());
        } else {
            return new MongoElementStore(rootId, store.getSecurityStore());
        }
    }

    @Override
    public void end(NodeStore<ElementState> store) {
        DBObject doc = ((MongoElementStore) store).assemble();
        getLayouts().save(doc);
    }

    public void destroy(String rootId) {
        BasicDBObject key = new BasicDBObject("_id", new ObjectId(rootId));
        int n = getLayouts().remove(key).getN();
        System.out.println(n);
    }
}
