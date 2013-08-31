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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.exoplatform.commons.utils.I18N;
import org.gatein.portal.impl.mop.ram.Node;
import org.gatein.portal.impl.mop.ram.RamDescriptionStore;
import org.gatein.portal.mop.description.DescriptionState;
import org.gatein.portal.mop.description.DescriptionStore;

/**
 * @author Julien Viet
 */
class MongoDescriptionStore implements DescriptionStore {

    /** . */
    private final MongoStore store;

    MongoDescriptionStore(MongoStore store) {
        this.store = store;
    }

    private DBCollection getCollection() {
        return store.getDB().getCollection("descriptions");
    }

    @Override
    public DescriptionState loadDescription(String id, Locale locale, boolean resolve) {
        DBCollection collection = getCollection();
        DBObject doc = collection.findOne(new BasicDBObject("_id", new ObjectId(id)));
        if (doc != null) {
            for (Locale l = locale; l != null; l = I18N.getParent(l)) {
                DBObject entry = (DBObject) doc.get(I18N.toJavaIdentifier(l));
                if (entry != null) {
                    return new DescriptionState(
                            (String) entry.get("name"),
                            (String) entry.get("description"));
                }

                if (!resolve) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public void saveDescription(String id, Locale locale, DescriptionState description) {
        String op;
        DBObject descriptionDoc;
        if (description != null) {
            op = "$set";
            descriptionDoc = make(description);
        } else {
            op = "$unset";
            descriptionDoc = null;
        }
        BasicDBObject update = new BasicDBObject();
        update.put(I18N.toJavaIdentifier(locale), descriptionDoc);
        getCollection().update(new BasicDBObject("_id", new ObjectId(id)), new BasicDBObject(op, update), true, false);
    }

    @Override
    public Map<Locale, DescriptionState> loadDescriptions(String id) {
        DBCollection collection = getCollection();
        DBObject doc = collection.findOne(new BasicDBObject("_id", new ObjectId(id)));
        if (doc != null) {
            Set<String> keys = doc.keySet();
            HashMap<Locale, DescriptionState> state = new HashMap<Locale, DescriptionState>(keys.size());
            for (String key : keys) {
                if (!key.equals("_id")) {
                    Locale locale = I18N.parseJavaIdentifier(key);
                    DBObject a = (DBObject) doc.get(key);
                    DescriptionState d = new DescriptionState(
                            (String) a.get("name"),
                            (String) a.get("description"));
                    state.put(locale, d);
                }
            }
            return state;
        }
        return null;
    }

    private DBObject make(DescriptionState state) {
        DBObject doc = new BasicDBObject();
        doc.put("name", state.getName());
        doc.put("description", state.getDescription());
        return doc;
    }

    @Override
    public void saveDescriptions(String id, Map<Locale, DescriptionState> descriptions) {
        BasicDBObject doc = new BasicDBObject("_id", new ObjectId(id));
        if (descriptions != null && descriptions.size() > 0) {
            for (Map.Entry<Locale, DescriptionState> entry : descriptions.entrySet()) {
                doc.put(I18N.toJavaIdentifier(entry.getKey()), make(entry.getValue()));
            }
            getCollection().save(doc);
        } else {
            getCollection().remove(doc);
        }
    }
}
