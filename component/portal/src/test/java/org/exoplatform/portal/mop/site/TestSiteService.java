/*
 * Copyright (C) 2013 eXo Platform SAS.
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

import java.util.Arrays;
import java.util.Collections;

import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.portal.mop.site.SiteContext;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.mop.site.SiteState;
import org.junit.Test;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 1/31/13
 */
public class TestSiteService extends AbstractMopServiceTest {

    private SiteService siteService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteService = context.getSiteService();
    }

    public void testLoadSite() {
        SiteKey key = SiteKey.portal("load_site");
        assertNull(getSitePersistence().loadSite(key));
        SiteState state = new SiteState(
                "en",
                "load_site",
                "load_site_description",
                Arrays.asList("Everyone"),
                Arrays.asList("/platform/administrators"),
                Collections.<String, String>emptyMap(),
                "load_site_skin");
        getSitePersistence().saveSite(key, state);
        sync(true);
        SiteContext site = siteService.loadSite(key);
        assertNotNull(site);
        assertNotNull(site.getId());
        assertEquals(key, site.getKey());
        assertEquals(state, site.getState());
        getSitePersistence().destroySite(key);
        sync(true);
    }

    public void testCreateSite() {
        SiteKey key = SiteKey.portal("create_site");
        assertNull(getSitePersistence().loadSite(key));
        SiteState state = new SiteState(
                "en",
                "create_site",
                "create_site_description",
                Arrays.asList("Everyone"),
                Arrays.asList("/platform/administrators"),
                null,
                "create_site_skin");
        siteService.saveSite(new SiteContext(key, state));
        sync(true);

        assertNotNull(getSitePersistence().loadSite(key));
        getSitePersistence().destroySite(key);
        sync(true);
    }

    public void testDestroySite() {
        SiteKey key = SiteKey.portal("destroy_site");
        assertNull(getSitePersistence().loadSite(key));
        SiteState state = new SiteState(
                "en",
                "destroy_site",
                "destroy_site_description",
                Arrays.asList("Everyone"),
                Arrays.asList("/platform/administrators"),
                null,
                "destroy_site_skin");
        siteService.saveSite(new SiteContext(key, state));
        sync(true);

        assertNotNull(getSitePersistence().loadSite(key));
        siteService.destroySite(key);
        sync(true);

        assertNull(getSitePersistence().loadSite(key));
    }
}
