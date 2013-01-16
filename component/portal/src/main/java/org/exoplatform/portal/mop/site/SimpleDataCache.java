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

package org.exoplatform.portal.mop.site;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.commons.serialization.MarshalledObject;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;
import org.exoplatform.portal.pom.config.POMSession;

/**
 * A simple implementation for unit testing purpose.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleDataCache extends DataCache {

    /** . */
    protected Map<SiteKey, SiteData> siteCache;

    /** . */
    protected Map<SiteType, MarshalledObject<ArrayList<SiteKey>>> sitesCache;

    public SimpleDataCache() {
        this.siteCache = new ConcurrentHashMap<SiteKey, SiteData>();
        this.sitesCache = new ConcurrentHashMap<SiteType, MarshalledObject<ArrayList<SiteKey>>>();
    }

    @Override
    protected SiteData getSite(POMSession session, SiteKey key) {
        SiteData site = siteCache.get(key);
        if (site == null) {
            site = loadSite(session, key);
            if (site != null) {
                siteCache.put(key, site);
                return site;
            } else {
                return null;
            }
        } else {
            return site;
        }
    }

    @Override
    protected void removeSite(POMSession session, SiteKey key) {
        siteCache.remove(key);
    }

    @Override
    protected void putSite(SiteData data) {
        siteCache.put(data.key, data);
    }

    @Override
    protected void clear() {
        siteCache.clear();
    }

    @Override
    protected ArrayList<SiteKey> getSites(POMSession session, SiteType key) {
        MarshalledObject<ArrayList<SiteKey>> marshalled = sitesCache.get(key);
        return marshalled != null ? marshalled.unmarshall() : null;
    }

    @Override
    protected void putSites(POMSession session, SiteType key, ArrayList<SiteKey> sites) {
        if (!session.isModified()) {
            session.putInCache(key, sites);
        }
    }

    @Override
    protected void removeSites(POMSession session, SiteType key) {
        session.scheduleForEviction(key);
    }
}
