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

import java.io.IOException;
import java.io.Serializable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.gatein.common.io.IOTools;
import org.gatein.portal.mop.customization.ContentType;
import org.gatein.portal.mop.customization.CustomizationData;
import org.gatein.portal.mop.customization.CustomizationError;
import org.gatein.portal.mop.customization.CustomizationServiceException;
import org.gatein.portal.mop.customization.CustomizationStore;

/**
 * @author Julien Viet
 */
public class MongoCustomizationStore implements CustomizationStore {

    /** . */
    private final MongoLayoutStore layoutStore;

    public MongoCustomizationStore(MongoLayoutStore layoutStore) {
        this.layoutStore = layoutStore;
    }

    @Override
    public <S extends Serializable> CustomizationData<S> loadCustomization(String id) {
        DBCollection layouts = layoutStore.getLayouts();
        DBObject layout = layouts.findOne(new BasicDBObject("elements." + id, new BasicDBObject("$exists", "true")));
        if (layout != null) {
            DBObject element = (DBObject) ((DBObject)layout.get("elements")).get(id);
            DBObject content = (DBObject) element.get("content");
            String contentId = (String) content.get("id");
            String contentTypeValue = (String) content.get("type");
            byte[] contentStateValue = (byte[]) content.get("state");
            ContentType<S> contentType = ContentType.forValue(contentTypeValue);
            S contentState;
            if (contentStateValue != null) {
                try {
                    contentState = (S) IOTools.unserialize(contentStateValue, Thread.currentThread().getContextClassLoader());
                } catch (Exception e) {
                    throw new CustomizationServiceException(CustomizationError.INTERNAL_ERROR, e);
                }
            } else {
                contentState = null;
            }
            return new CustomizationData<S>(id, contentType, contentId, contentState);
        } else {
            return null;
        }
    }

    @Override
    public String cloneCustomization(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Serializable> S saveCustomization(String id, S state) {
        DBCollection layouts = layoutStore.getLayouts();
        DBObject layout = layouts.findOne(new BasicDBObject("elements." + id, new BasicDBObject("$exists", "true")));
        byte[] blob;
        try {
            blob = IOTools.serialize(state);
        } catch (IOException e) {
            throw new CustomizationServiceException(CustomizationError.INTERNAL_ERROR, e);
        }
        layouts.update(
                new BasicDBObject("_id", layout.get("_id")),
                new BasicDBObject("$set", new BasicDBObject("elements." + id + ".state", blob)));
        return state;
    }
}
