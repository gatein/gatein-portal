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

package org.gatein.portal.mop.site;

import java.util.Collection;

import org.gatein.portal.mop.QueryResult;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SiteServiceImpl implements SiteService {

    /** . */
    final SiteStore store;

    public SiteServiceImpl(SiteStore store) {
        this.store = store;
    }

    @Override
    public SiteContext loadSite(SiteKey key) throws NullPointerException, SiteServiceException {
        if (key == null) {
            throw new NullPointerException();
        }

        //
        SiteData data = store.loadSite(key);
        return data != null && data != SiteData.EMPTY ? new SiteContext(data) : null;
    }

    @Override
    public boolean saveSite(SiteContext context) throws NullPointerException, SiteServiceException {
        if (context == null) {
            throw new NullPointerException();
        }

        //
        boolean created = store.saveSite(context.key, context.state);
        context.data = store.loadSite(context.key);
        context.state = null;
        return created;
    }

    @Override
    public boolean destroySite(SiteKey key) throws NullPointerException, SiteServiceException {
        if (key == null) {
            throw new NullPointerException("No null page argument");
        }

        //
        return store.destroySite(key);
    }

    @Override
    public QueryResult<SiteKey> findSites(SiteType siteType) throws SiteServiceException {
        if (siteType == null) {
            throw new NullPointerException("No null site type accepted");
        }
        if (siteType == SiteType.USER) {
            throw new IllegalArgumentException("No site type user accepted");
        }

        //
        Collection<SiteKey> sites = store.findSites(siteType);
        return new QueryResult<SiteKey>(0, sites.size(), sites);
    }
}
