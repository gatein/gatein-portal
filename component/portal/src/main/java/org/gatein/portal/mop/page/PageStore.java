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

package org.gatein.portal.mop.page;

import java.util.Collection;
import java.util.List;

import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface PageStore {

    PageData loadPage(PageKey key);

    boolean savePage(PageKey key, PageState state);

    boolean destroyPage(PageKey key);

    PageData clonePage(PageKey src, PageKey dst);

    List<PageKey> findPageKeys(SiteKey siteKey);

    Collection<PageData> findPages(
            int from,
            int to,
            SiteType siteType,
            String siteName,
            String pageName,
            String pageTitle);

    void clear();
}
