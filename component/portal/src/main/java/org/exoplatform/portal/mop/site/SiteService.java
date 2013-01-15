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

import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface SiteService {

    /**
     * Find and returns a site, if no such site exist, null is returned.
     *
     * @param key the site key
     * @return the matching site
     * @throws NullPointerException if the key is null
     * @throws SiteServiceException anything that would prevent the operation to succeed
     */
    SiteContext loadSite(SiteKey key) throws NullPointerException, SiteServiceException;

    /**
     * Create, update a site. When the site state is not null, the site will be created or updated depending on whether or not
     * the site already exists.
     *
     * @param site the site
     *
     * @return true if the site does not already exist otherwise false.
     *
     * @throws NullPointerException if the key is null
     * @throws SiteServiceException anything that would prevent the operation to succeed
     *
     */
    boolean saveSite(SiteContext site) throws NullPointerException, SiteServiceException;

    /**
     * Destroy a site.
     *
     * @param key the site key
     * @return true when the site was destroyed
     * @throws NullPointerException if the site key is null
     * @throws SiteServiceException anything that would prevent the operation to succeed
     */
    boolean destroySite(SiteKey key) throws NullPointerException, SiteServiceException;

    /**
     * Query the site service to find sites that match the <code>siteType</code> criteria.
     *
     * @param siteType the site type
     * @return the query result
     * @throws NullPointerException if the site type is null
     * @throws IllegalArgumentException if the site is equals to {@link SiteType#USER}
     * @throws SiteServiceException anything that would prevent the operation to succeed
     */
    QueryResult<SiteKey> findSites(SiteType siteType) throws NullPointerException, IllegalArgumentException, SiteServiceException;
}
