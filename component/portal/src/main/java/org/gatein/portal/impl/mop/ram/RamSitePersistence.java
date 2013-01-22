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

import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SitePersistence;
import org.gatein.portal.mop.site.SiteState;
import org.gatein.portal.mop.site.SiteType;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
public class RamSitePersistence implements SitePersistence {

    /** . */
    private Store store;

    public RamSitePersistence(RamPersistence store) {
        this.store = store.store;
    }

    @Override
    public SiteData loadSite(SiteKey key) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getName());
        if (site != null) {
            Node entry = current.getNode(site);
            String layout = current.getChild(site, "layout");
            return new SiteData(key, site, layout, (SiteState)entry.getState());
        } else {
            return null;
        }
    }

    @Override
    public boolean saveSite(SiteKey key, SiteState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getName());
        if (site == null) {
            site = current.addChild(type, key.getName(), state);
            current.addChild(site, "pages", "");
            current.addChild(site, "layout", RamLayoutPersistence.INITIAL);
            return true;
        } else {
            Node entry = current.getNode(site);
            SiteData data = (SiteData)entry;
            current.update(data.id, state);
            return false;
        }
    }

    @Override
    public boolean destroySite(SiteKey key) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, key.getTypeName());
        String site = current.getChild(type, key.getTypeName());
        if (site != null) {
            current.remove(site);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Collection<SiteKey> findSites(SiteType siteType) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String root = current.getRoot();
        String type = current.getChild(root, siteType.getName());
        List<String> sites = current.getChildren(type);
        ArrayList<SiteKey> keys = new ArrayList<SiteKey>(sites.size());
        for (String site : sites) {
            Node entry = current.getNode(site);
            keys.add(((SiteData)entry.getState()).key);
        }
        return keys;
    }
}
