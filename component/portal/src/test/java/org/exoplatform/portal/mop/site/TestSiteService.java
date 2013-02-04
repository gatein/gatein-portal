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

    private static final SiteKey FOO_SITE = SiteKey.portal("foo_site");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteService = context.getSiteService();
    }

    @Test
    public void testLoadSite() {
        SiteState state = new SiteState(
                "en",
                "foo_site",
                "foo_site_description",
                Arrays.asList("Everyone"),
                "/platform/administrators",
                null,
                "foo_skin");
        getSitePersistence().saveSite(FOO_SITE, state);
        sync(true);

        assertNotNull(siteService.loadSite(FOO_SITE));
        getSitePersistence().destroySite(FOO_SITE);
        sync(true);
    }

    @Test
    public void testCreateSite() {
        assertNull(getSitePersistence().loadSite(FOO_SITE));
        SiteState state = new SiteState(
                "en",
                "foo_site",
                "foo_site_description",
                Arrays.asList("Everyone"),
                "/platform/administrators",
                null,
                "foo_skin");
        siteService.saveSite(new SiteContext(FOO_SITE, state));
        sync(true);

        assertNotNull(getSitePersistence().loadSite(FOO_SITE));
        getSitePersistence().destroySite(FOO_SITE);
        sync(true);
    }

    @Test
    public void testDestroySite() {
        assertNull(getSitePersistence().loadSite(FOO_SITE));
        SiteState state = new SiteState(
                "en",
                "foo_site",
                "foo_site_description",
                Arrays.asList("Everyone"),
                "/platform/administrators",
                null,
                "foo_skin");
        siteService.saveSite(new SiteContext(FOO_SITE, state));
        sync(true);

        assertNotNull(getSitePersistence().loadSite(FOO_SITE));
        siteService.destroySite(FOO_SITE);
        sync(true);

        assertNull(getSitePersistence().loadSite(FOO_SITE));
    }
}
