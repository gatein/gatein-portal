package org.exoplatform.portal.mop.page;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedContainer;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TestPageService extends AbstractTestPageService {

    public void testLoad() {
        mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "load_page").getRootPage()
                .addChild("pages");
        sync(true);

        //
        SiteKey site = SiteKey.portal("load_page");

        // Read twice (to load and check and the get from cache and check)
        assertNull(service.loadPage(site.page("foo")));
        assertNull(service.loadPage(site.page("foo")));

        //
        Page foo = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "load_page").getRootPage()
                .getChild("pages").addChild("foo");
        Described fooDescribed = foo.adapt(Described.class);
        fooDescribed.setName("foo_name");
        fooDescribed.setDescription("foo_description");
        ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
        fooResource.setAccessPermissions(Collections.singletonList("foo_access_permission"));
        fooResource.setEditPermission("foo_edit_permission");
        Attributes fooAttrs = foo.getAttributes();
        fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
        fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);

        ProtectedContainer pc = foo.adapt(ProtectedContainer.class);
        pc.setMoveAppsPermissions(Collections.singletonList("foo-move-apps-permissions"));
        pc.setMoveContainersPermissions(Collections.singletonList("foo-move-containers-permissions"));

        sync(true);

        //
        service.clearCache();
        PageContext page = service.loadPage(site.page("foo"));
        assertNotNull(page);
        assertNull(page.state);
        assertNotNull(page.data);
        PageState state = page.getState();
        assertEquals("foo_name", state.getDisplayName());
        assertEquals("foo_description", state.getDescription());
        assertEquals(Collections.singletonList("foo_access_permission"), state.getAccessPermissions());
        assertEquals("foo_edit_permission", state.getEditPermission());
        assertEquals("foo_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());

        assertEquals(Collections.singletonList("foo-move-apps-permissions"), state.getMoveAppsPermissions());
        assertEquals(Collections.singletonList("foo-move-containers-permissions"), state.getMoveContainersPermissions());
    }

    public void testLoadPages() {
        mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "load_pages").getRootPage()
                .addChild("pages");
        sync(true);

        //
        SiteKey site = SiteKey.portal("load_pages");

        // Read twice (to load and check and the get from cache and check)
        assertNotNull(service.loadPages(site));
        assertNotNull(service.loadPages(site));
        assertEquals(0, service.loadPages(site).size());

        //
        Page foo = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "load_pages").getRootPage()
                .getChild("pages").addChild("foo");
        Described fooDescribed = foo.adapt(Described.class);
        fooDescribed.setName("foo_name");
        fooDescribed.setDescription("foo_description");
        ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
        fooResource.setAccessPermissions(Collections.singletonList("foo_access_permission"));
        fooResource.setEditPermission("foo_edit_permission");
        Attributes fooAttrs = foo.getAttributes();
        fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
        fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);

        ProtectedContainer pc = foo.adapt(ProtectedContainer.class);
        pc.setMoveAppsPermissions(Collections.singletonList("foo-move-apps-permissions"));
        pc.setMoveContainersPermissions(Collections.singletonList("foo-move-containers-permissions"));

        Page bar = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "load_pages").getRootPage()
                .getChild("pages").addChild("bar");
        Described barDescribed = bar.adapt(Described.class);
        barDescribed.setName("bar_name");
        barDescribed.setDescription("bar_description");
        ProtectedResource barResource = bar.adapt(ProtectedResource.class);
        barResource.setAccessPermissions(Collections.singletonList("bar_access_permission"));
        barResource.setEditPermission("bar_edit_permission");
        Attributes barAttrs = bar.getAttributes();
        barAttrs.setValue(MappedAttributes.FACTORY_ID, "bar_factory_id");
        barAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);

        pc = bar.adapt(ProtectedContainer.class);
        pc.setMoveAppsPermissions(Collections.singletonList("bar-move-apps-permissions"));
        pc.setMoveContainersPermissions(Collections.singletonList("bar-move-containers-permissions"));

        sync(true);

        //
        service.clearCache();

        List<PageContext> pages = service.loadPages(site);
        assertNotNull(pages);
        assertEquals(2, pages.size());

        Iterator<PageContext> iterator = pages.iterator();
        PageContext page = iterator.next();
        assertNotNull(page);
        assertNull(page.state);
        assertNotNull(page.data);
        PageState state = page.getState();
        assertEquals("foo_name", state.getDisplayName());
        assertEquals("foo_description", state.getDescription());
        assertEquals(Collections.singletonList("foo_access_permission"), state.getAccessPermissions());
        assertEquals("foo_edit_permission", state.getEditPermission());
        assertEquals("foo_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());
        assertEquals(Collections.singletonList("foo-move-apps-permissions"), state.getMoveAppsPermissions());
        assertEquals(Collections.singletonList("foo-move-containers-permissions"), state.getMoveContainersPermissions());


        page = iterator.next();
        assertNotNull(page);
        assertNull(page.state);
        assertNotNull(page.data);
        state = page.getState();
        assertEquals("bar_name", state.getDisplayName());
        assertEquals("bar_description", state.getDescription());
        assertEquals(Collections.singletonList("bar_access_permission"), state.getAccessPermissions());
        assertEquals("bar_edit_permission", state.getEditPermission());
        assertEquals("bar_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());
        assertEquals(Collections.singletonList("bar-move-apps-permissions"), state.getMoveAppsPermissions());
        assertEquals(Collections.singletonList("bar-move-containers-permissions"), state.getMoveContainersPermissions());

    }

    public void testCreate() {
        mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "create_page").getRootPage()
                .addChild("pages");
        sync(true);

        //
        SiteKey site = SiteKey.portal("create_page");

        //
        PageContext page = new PageContext(site.page("foo"), new PageState("foo_name", "foo_description", true,
                "foo_factory_id", Collections.singletonList("foo_access_permission"), "foo_edit_permission",
                Collections.singletonList("foo-move-apps-permissions"), Collections.singletonList("foo-move-containers-permissions")));
        assertTrue(service.savePage(page));
        sync(true);

        //
        Page foo = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "create_page").getRootPage()
                .getChild("pages").getChild("foo");
        assertNotNull(foo);
        Described fooDescribed = foo.adapt(Described.class);
        assertEquals("foo_name", fooDescribed.getName());
        assertEquals("foo_description", fooDescribed.getDescription());
        ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
        assertEquals(Collections.singletonList("foo_access_permission"), fooResource.getAccessPermissions());
        assertEquals("foo_edit_permission", fooResource.getEditPermission());
        Attributes fooAttrs = foo.getAttributes();
        assertEquals("foo_factory_id", fooAttrs.getValue(MappedAttributes.FACTORY_ID));
        assertEquals(Boolean.TRUE, fooAttrs.getValue(MappedAttributes.SHOW_MAX_WINDOW));
        ProtectedContainer pc = foo.adapt(ProtectedContainer.class);
        assertEquals(Collections.singletonList("foo-move-apps-permissions"), pc.getMoveAppsPermissions());
        assertEquals(Collections.singletonList("foo-move-containers-permissions"), pc.getMoveContainersPermissions());
    }

    public void testUpdate() {
        Page foo = mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "update_page").getRootPage()
                .addChild("pages").addChild("foo");
        Described fooDescribed = foo.adapt(Described.class);
        fooDescribed.setName("foo_name");
        fooDescribed.setDescription("foo_description");
        ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
        fooResource.setAccessPermissions(Collections.singletonList("foo_access_permission"));
        fooResource.setEditPermission("foo_edit_permission");
        Attributes fooAttrs = foo.getAttributes();
        fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
        fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);
        ProtectedContainer pc = foo.adapt(ProtectedContainer.class);
        pc.setMoveAppsPermissions(Collections.singletonList("foo-move-apps-permissions"));
        pc.setMoveContainersPermissions(Collections.singletonList("foo-move-containers-permissions"));
        sync(true);

        //
        SiteKey site = SiteKey.portal("update_page");

        //
        PageContext page = new PageContext(site.page("foo"), new PageState("foo_name_2", "foo_description_2", false,
                "foo_factory_id_2", Arrays.asList("foo_access_permission_2", "foo_2_access_permission_2"),
                "foo_edit_permission_2", Collections.singletonList("foo-move-apps-permissions-2"),
                Collections.singletonList("foo-move-containers-permissions-2")));
        assertFalse(service.savePage(page));
        sync(true);

        //
        foo = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "update_page").getRootPage()
                .getChild("pages").getChild("foo");
        assertNotNull(foo);
        fooDescribed = foo.adapt(Described.class);
        assertEquals("foo_name_2", fooDescribed.getName());
        assertEquals("foo_description_2", fooDescribed.getDescription());
        fooResource = foo.adapt(ProtectedResource.class);
        assertEquals(Arrays.asList("foo_access_permission_2", "foo_2_access_permission_2"), fooResource.getAccessPermissions());
        assertEquals("foo_edit_permission_2", fooResource.getEditPermission());
        fooAttrs = foo.getAttributes();
        assertEquals("foo_factory_id_2", fooAttrs.getValue(MappedAttributes.FACTORY_ID));
        assertEquals(Boolean.FALSE, fooAttrs.getValue(MappedAttributes.SHOW_MAX_WINDOW));
        pc = foo.adapt(ProtectedContainer.class);
        assertEquals(Collections.singletonList("foo-move-apps-permissions-2"), pc.getMoveAppsPermissions());
        assertEquals(Collections.singletonList("foo-move-containers-permissions-2"), pc.getMoveContainersPermissions());

    }

    public void testDestroy() {
        mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "destroy_page").getRootPage()
                .addChild("pages");
        sync(true);

        //
        SiteKey site = SiteKey.portal("destroy_page");

        //
        assertFalse(service.destroyPage(site.page("foo")));

        //
        Page foo = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "destroy_page").getRootPage()
                .getChild("pages").addChild("foo");
        Described fooDescribed = foo.adapt(Described.class);
        fooDescribed.setName("foo_name");
        fooDescribed.setDescription("foo_description");
        ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
        fooResource.setAccessPermissions(Collections.singletonList("foo_access_permission"));
        fooResource.setEditPermission("foo_edit_permission");
        Attributes fooAttrs = foo.getAttributes();
        fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
        fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);

        ProtectedContainer pc = foo.adapt(ProtectedContainer.class);
        pc.setMoveAppsPermissions(Collections.singletonList("foo-move-apps-permissions"));
        pc.setMoveContainersPermissions(Collections.singletonList("foo-move-containers-permissions"));
        sync(true);

        //
        assertTrue(service.destroyPage(site.page("foo")));
        assertNull(service.loadPage(CLASSIC_FOO));
        sync(true);

        //
        foo = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "destroy_page").getRootPage()
                .getChild("pages").getChild("foo");
        assertNull(foo);
    }

    public void testFind() throws Exception {
        Page pages = mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "find_pages").getRootPage()
                .addChild("pages");
        pages.addChild("foo");
        pages.addChild("bar");
        sync(true);

        //
        QueryResult<PageContext> result = service.findPages(0, 10, SiteType.PORTAL, "find_pages", null, null);
        assertEquals(2, result.getSize());
    }

    public void testFindOrder() throws Exception {
        Page pages = mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "find_pages_order").getRootPage()
                .addChild("pages");
        pages.addChild("page01").adapt(Described.class).setName("page01");
        pages.addChild("page02").adapt(Described.class).setName("page02");
        pages.addChild("page03").adapt(Described.class).setName("page03");
        pages.addChild("page04").adapt(Described.class).setName("page04");
        pages.addChild("page05").adapt(Described.class).setName("page05");
        pages.addChild("page06").adapt(Described.class).setName("page06");
        pages.addChild("page07").adapt(Described.class).setName("page07");
        pages.addChild("page08").adapt(Described.class).setName("page08");
        pages.addChild("page09").adapt(Described.class).setName("page09");
        pages.addChild("page10").adapt(Described.class).setName("page10");
        pages.addChild("page11").adapt(Described.class).setName("page11");
        pages.addChild("page12").adapt(Described.class).setName("page12");
        sync(true);

        QueryResult<PageContext> result = service.findPages(0, 10, SiteType.PORTAL, "find_pages_order", null, null);
        Iterator<PageContext> iterator = result.iterator();

        assertEquals("portal::find_pages_order::page01", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page02", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page03", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page04", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page05", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page06", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page07", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page08", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page09", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page10", iterator.next().getKey().format());

        result = service.findPages(10, 12, SiteType.PORTAL, "find_pages_order", null, null);
        iterator = result.iterator();

        assertEquals("portal::find_pages_order::page11", iterator.next().getKey().format());
        assertEquals("portal::find_pages_order::page12", iterator.next().getKey().format());
    }

    public void testClone() throws Exception {
        Page foo = mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "clone_page").getRootPage()
                .addChild("pages").addChild("foo");
        Described fooDescribed = foo.adapt(Described.class);
        fooDescribed.setName("foo_name");
        fooDescribed.setDescription("foo_description");
        ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
        fooResource.setAccessPermissions(Collections.singletonList("foo_access_permission"));
        fooResource.setEditPermission("foo_edit_permission");
        Attributes fooAttrs = foo.getAttributes();
        fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
        fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);
        ProtectedContainer pc = foo.adapt(ProtectedContainer.class);
        pc.setMoveAppsPermissions(Collections.singletonList("foo-move-apps-permissions"));
        pc.setMoveContainersPermissions(Collections.singletonList("foo-move-containers-permissions"));
        sync(true);


        //
        SiteKey site = SiteKey.portal("clone_page");

        //
        PageContext bar = service.clone(site.page("foo"), site.page("bar"));
        assertNotNull(bar);
        assertNull(bar.state);
        assertNotNull(bar.data);
        PageState state = bar.getState();
        assertEquals("foo_name", state.getDisplayName());
        assertEquals("foo_description", state.getDescription());
        assertEquals(Collections.singletonList("foo_access_permission"), state.getAccessPermissions());
        assertEquals("foo_edit_permission", state.getEditPermission());
        assertEquals("foo_factory_id", state.getFactoryId());
        assertEquals(true, state.getShowMaxWindow());
        assertEquals(Collections.singletonList("foo-move-apps-permissions"), state.getMoveAppsPermissions());
        assertEquals(Collections.singletonList("foo-move-containers-permissions"), state.getMoveContainersPermissions());

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
         * // Page srcPage = storage_.getPage("portal::test::test4"); srcPage.setEditPermission("Administrator");
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
        assertNull(service.loadPage(SiteKey.portal("foo").page("homepage")));
    }

    public void testCreateWithoutSite() {
        PageContext page = new PageContext(SiteKey.portal("foo").page("homepage"), new PageState("foo", "Foo", false,
                "factory-id", Collections.singletonList("*:/platform/administrators"), "Everyone",
                Collections.singletonList("foo_add_application_permission"), Collections.singletonList("foo_add_container_permission")));
        try {
            service.savePage(page);
            fail();
        } catch (PageServiceException e) {
            assertEquals(PageError.NO_SITE, e.getError());
        }
    }

    public void testDestroyWithoutSite() {
        try {
            service.destroyPage(SiteKey.portal("foo").page("homepage"));
            fail();
        } catch (PageServiceException e) {
            assertEquals(PageError.NO_SITE, e.getError());
        }
    }
}
