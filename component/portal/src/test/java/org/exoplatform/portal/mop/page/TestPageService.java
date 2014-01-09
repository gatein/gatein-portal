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

package org.exoplatform.portal.mop.page;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.portal.mop.QueryResult;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageError;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageServiceException;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TestPageService extends AbstractMopServiceTest {

    /** . */
    static final SiteKey CLASSIC = SiteKey.portal("classic");

    /** . */
    static final PageKey CLASSIC_FOO = CLASSIC.page("foo");

    public void testLoad() {
        SiteData site = createSite(SiteType.PORTAL, "load_page");
        sync(true);

        // Read twice (to load and check and the get from cache and check)
        assertNull(getPageService().loadPage(site.key.page("foo")));
        assertNull(getPageService().loadPage(site.key.page("foo")));

        //
        PageState state = new PageState(
                "foo_name",
                "foo_description",
                true,
                "foo_factory_id");
        createPage(site, "foo", state);
        sync(true);

        //
        getPageService().clear();
        PageContext page = getPageService().loadPage(site.key.page("foo"));
        assertNotNull(page);
        assertNull(page.getState(true));
        assertNotNull(page.getData());
        state = page.getState();
        assertEquals("foo_name", state.getDisplayName());
        assertEquals("foo_description", state.getDescription());
        assertEquals("foo_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());
    }

    public void testLoadPages() {
        SiteData site = createSite(SiteType.PORTAL, "load_pages");
        sync(true);

        // Read twice (to load and check and the get from cache and check)
        assertNotNull(getPageService().loadPages(site.key));
        assertNotNull(getPageService().loadPages(site.key));
        assertEquals(0, getPageService().loadPages(site.key).size());

        //
        PageState fooState = new PageState(
                "foo_name",
                "foo_description",
                true,
                "foo_factory_id");
        createPage(site, "foo", fooState);
        PageState barState = new PageState(
                "bar_name",
                "bar_description",
                true,
                "bar_factory_id");
        createPage(site, "bar", barState);
        sync(true);

        //
        getPageService().clear();

        List<PageContext> pages = getPageService().loadPages(site.key);
        assertNotNull(pages);
        assertEquals(2, pages.size());

        Iterator<PageContext> iterator = pages.iterator();
        PageContext page = iterator.next();
        assertNotNull(page);
        assertNull(page.getState(true));
        assertNotNull(page.getData());
        PageState state = page.getState();
        assertEquals("foo_name", state.getDisplayName());
        assertEquals("foo_description", state.getDescription());
        assertEquals("foo_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());

        page = iterator.next();
        assertNotNull(page);
        assertNull(page.getState(true));
        assertNotNull(page.getData());
        state = page.getState();
        assertEquals("bar_name", state.getDisplayName());
        assertEquals("bar_description", state.getDescription());
        assertEquals("bar_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());
    }

    public void testCreate() {
        createSite(SiteType.PORTAL, "create_page");
        sync(true);

        //
        SiteKey site = SiteKey.portal("create_page");

        //
        PageContext pageCtx = new PageContext(site.page("foo"), new PageState("foo_name", "foo_description", true,
                "foo_factory_id"));
        assertTrue(getPageService().savePage(pageCtx));
        sync(true);

        //
        PageState state = getPage(site.page("foo")).state;
        assertEquals("foo_name", state.getDisplayName());
        assertEquals("foo_description", state.getDescription());
        assertEquals("foo_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());
    }

    public void testUpdate() {
        SiteData site = createSite(SiteType.PORTAL, "update_page");
        PageState state = new PageState(
                "foo_name",
                "foo_description",
                true,
                "foo_factory_id");
        createPage(site, "foo", state);
        sync(true);

        //
        PageContext pageCtx = new PageContext(site.key.page("foo"), new PageState("foo_name_2", "foo_description_2", false,
                "foo_factory_id_2"));
        assertFalse(getPageService().savePage(pageCtx));
        sync(true);

        //
        state = getPage(site.key, "foo").state;
        assertEquals("foo_name_2", state.getDisplayName());
        assertEquals("foo_description_2", state.getDescription());
        assertEquals("foo_factory_id_2", state.getFactoryId());
        assertEquals(false, state.getShowMaxWindow());
    }

    public void testDestroy() {
        SiteData site = createSite(SiteType.PORTAL, "destroy_page");
        sync(true);

        //
        PageKey pageKey = site.key.page("foo");
        assertFalse(getPageService().destroyPage(pageKey));

        //
        PageState state = new PageState(
                "foo_name",
                "foo_description",
                true,
                "foo_factory_id");
        createPage(site, "foo", state);
        sync(true);

        //
        assertTrue(getPageService().destroyPage(pageKey));
        assertNull(getPageService().loadPage(CLASSIC_FOO));
        sync(true);

        //
        assertNull(getPage(pageKey));
    }

    public void testFind() throws Exception {
        SiteData site = createSite(SiteType.PORTAL, "find_pages");
        PageState state = new PageState(
                "name",
                "description",
                true,
                "factory_id");
        createPage(site, "foo", state);
        createPage(site, "bar", state);
        sync(true);

        //
        QueryResult<PageContext> result = getPageService().findPages(0, 10, SiteType.PORTAL, "find_pages", null, null);
        assertEquals(2, result.getSize());
    }

    public void testClone() throws Exception {
        SiteData site = createSite(SiteType.PORTAL, "clone_page");
        PageState state = new PageState(
                "foo_name",
                "foo_description",
                true,
                "foo_factory_id");
        createPage(site, "foo", state);
        sync(true);

        //
        PageContext bar = getPageService().clone(site.key.page("foo"), site.key.page("bar"));
        assertNotNull(bar);
        assertNull(bar.getState(true));
        assertNotNull(bar.getData());
        state = bar.getState();
        assertEquals("foo_name", state.getDisplayName());
        assertEquals("foo_description", state.getDescription());
        assertEquals("foo_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());

        //
        // DataStorage dataStorage = (DataStorage)PortalContainer.getInstance().getComponentInstanceOfType(DataStorage.class);

        /*
         *
         *
         * // Check instance id format
         *
         * // Check state Portlet pagePrefs = storage_.load(instanceId, ApplicationType.PORTLET); assertEquals(new
         * PortletBuilder().add("template", "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl") .build(), pagePrefs);
         *
         * // Now save the cloned page storage_.save(clone);
         *
         * // Get cloned page clone = storage_.getPage("portal::test::_test4"); assertEquals(2, clone.getChildren().size());
         * banner1 = (Application<Portlet>)clone.getChildren().get(0); instanceId = banner1.getState();
         *
         * // Check instance id format assertEquals("web/BannerPortlet", storage_.getId(banner1.getState()));
         *
         * // Update site prefs PortletPreferences sitePrefs = new PortletPreferences();
         * sitePrefs.setWindowId("portal#test:/web/BannerPortlet/banner"); sitePrefs.setPreferences(new
         * ArrayList<Preference>(Collections.singleton(new Preference())));
         * sitePrefs.getPreferences().get(0).setName("template"); sitePrefs.getPreferences().get(0).getValues().add("bar");
         * storage_.save(sitePrefs);
         *
         * // Check that page prefs have not changed pagePrefs = storage_.load(instanceId, ApplicationType.PORTLET);
         * assertEquals(new PortletBuilder().add("template", "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl")
         * .build(), pagePrefs);
         *
         * // Update page prefs pagePrefs.setValue("template", "foo"); storage_.save(instanceId, pagePrefs);
         *
         * // Check that page prefs have changed pagePrefs = storage_.load(instanceId, ApplicationType.PORTLET);
         * assertEquals(new PortletBuilder().add("template", "foo").build(), pagePrefs);
         *
         * // Check that site prefs have not changed sitePrefs =
         * storage_.getPortletPreferences("portal#test:/web/BannerPortlet/banner"); assertEquals("bar",
         * sitePrefs.getPreferences().get(0).getValues().get(0));
         *
         * // Now check the container Container container = (Container)clone.getChildren().get(1); assertEquals(2,
         * container.getChildren().size());
         *
         * // Application banner2 = (Application)container.getChildren().get(0); // assertEquals(banner2.getInstanceId(),
         * banner1.getInstanceId());
         *
         * // Page srcPage = storage_.getPage("portal::test::test4"); srcPage.setEditPermissions("Administrator");
         * Application<Portlet>portlet = (Application<Portlet>)srcPage.getChildren().get(0);
         * portlet.setDescription("NewPortlet");
         *
         * ArrayList<ModelObject> modelObject = srcPage.getChildren(); modelObject.set(0, portlet);
         *
         * srcPage.setChildren(modelObject);
         *
         * storage_.save(srcPage); Page dstPage = storage_.clonePage(srcPage.getPageId(), srcPage.getOwnerType(),
         * srcPage.getOwnerId(), "_PageTest1234"); Application<Portlet>portlet1 =
         * (Application<Portlet>)dstPage.getChildren().get(0); // Check src's edit permission and dst's edit permission
         * assertEquals(srcPage.getEditPermission(), dstPage.getEditPermission());
         *
         * // Check src's children and dst's children assertEquals(portlet.getDescription(), portlet1.getDescription());
         */
    }

    public void testLoadWithoutSite() {
        assertNull(getPageService().loadPage(SiteKey.portal("foo").page("homepage")));
    }

    public void testCreateWithoutSite() {
        PageContext page = new PageContext(SiteKey.portal("foo").page("homepage"), new PageState("foo", "Foo", false, "factory-id"));
        try {
            getPageService().savePage(page);
            fail();
        } catch (PageServiceException e) {
            assertEquals(PageError.NO_SITE, e.getError());
        }
    }

    public void testDestroyWithoutSite() {
        try {
            getPageService().destroyPage(SiteKey.portal("foo").page("homepage"));
            fail();
        } catch (PageServiceException e) {
            assertEquals(PageError.NO_SITE, e.getError());
        }
    }
}
