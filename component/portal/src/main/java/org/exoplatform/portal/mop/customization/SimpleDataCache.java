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

package org.exoplatform.portal.mop.customization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.portal.mop.customization.CustomizationData;

/**
 * A simple implementation for unit testing purpose.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleDataCache extends DataCache {

    /** . */
    protected Map<String, CustomizationData> siteCache;

    public SimpleDataCache() {
        this.siteCache = new ConcurrentHashMap<String, CustomizationData>();
    }

    @Override
    protected CustomizationData getCustomization(POMSession session, String key) {
        CustomizationData site = siteCache.get(key);
        if (site == null) {
            site = loadCustomization(session, key);
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
    protected void removeCustomization(POMSession session, String key) {
        siteCache.remove(key);
    }

/*
    @Override
    protected void putCustomization(CustomizationData data) {
        siteCache.put(data.id, data);
    }
*/

    @Override
    protected void clear() {
        siteCache.clear();
    }
}
