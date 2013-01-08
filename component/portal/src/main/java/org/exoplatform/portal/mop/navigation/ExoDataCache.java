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

import java.io.Serializable;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.scope.ScopedKey;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.hierarchy.NodeData;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

/**
 * An implementation using the cache service.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExoDataCache extends DataCache {

    /** . */
    protected ExoCache<ScopedKey<?>, Serializable> cache;

    /** . */
    protected FutureExoCache<ScopedKey<?>, Serializable, Void> objects;

    /** . */
    private Loader<ScopedKey<?>, Serializable, Void> navigationLoader = new Loader<ScopedKey<?>, Serializable, Void>() {
        public Serializable retrieve(Void session, ScopedKey<?> scopedKey) throws Exception {
            Object key = scopedKey.getKey();
            if (key instanceof SiteKey) {
                NavigationData data = persistence.loadNavigation((SiteKey) key);
                return data == NavigationData.EMPTY ? null : data;
            } else {
                return persistence.loadNode((String) key);
            }
        }
    };

    public ExoDataCache(CacheService cacheService) {
        this.cache = cacheService.getCacheInstance(NavigationService.class.getSimpleName());
        this.objects = new FutureExoCache<ScopedKey<?>, Serializable, Void>(navigationLoader, cache);
    }

    @Override
    protected void removeNodes(Iterable<String> keys) {
        for (String key : keys) {
            cache.remove(ScopedKey.create(key));
        }
    }

    @Override
    protected NodeData<NodeState> getNode(String key) {
        return (NodeData<NodeState>) objects.get(null, ScopedKey.create(key));
    }

    @Override
    protected void removeNavigation(SiteKey key) {
        cache.remove(ScopedKey.create(key));
    }

    @Override
    protected NavigationData getNavigation(SiteKey key) {
        return (NavigationData) objects.get(null, ScopedKey.create(key));
    }

    @Override
    protected void clear() {
        cache.clearCache();
    }
}
