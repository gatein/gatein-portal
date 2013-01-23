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

import org.exoplatform.portal.mop.site.MopStore;
import org.exoplatform.portal.mop.site.SimpleDataCache;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.portal.mop.QueryResult;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SiteServiceWrapper implements SiteService {

    /** . */
    private final SiteServiceImpl service;

    public SiteServiceWrapper(POMSessionManager manager) {
        this.service = new SiteServiceImpl(new MopStore(manager, new SimpleDataCache()));
    }

    @Override
    public SiteContext loadSite(SiteKey key) throws NullPointerException, SiteServiceException {
        return service.loadSite(key);
    }

    @Override
    public boolean saveSite(SiteContext site) throws NullPointerException, SiteServiceException {
        return service.saveSite(site);
    }

    @Override
    public boolean destroySite(SiteKey key) throws NullPointerException, SiteServiceException {
        return service.destroySite(key);
    }

    @Override
    public QueryResult<SiteKey> findSites(SiteType siteType) throws NullPointerException, IllegalArgumentException, SiteServiceException {
        return service.findSites(siteType);
    }
}
