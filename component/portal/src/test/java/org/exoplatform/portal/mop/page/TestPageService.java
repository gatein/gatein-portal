package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TestPageService extends AbstractTestPageService
{

   public void testLoad()
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "load_page").getRootPage().addChild("pages");
      sync(true);

      //
      SiteKey site = SiteKey.portal("load_page");

      // Read twice (to load and check and the get from cache and check)
      assertNull(service.loadPage(site.page("foo")));
      assertNull(service.loadPage(site.page("foo")));

      //
      Page foo = mgr.getPOMService().getModel().getWorkspace().
         getSite(ObjectType.PORTAL_SITE, "load_page").
         getRootPage().
         getChild("pages").
         addChild("foo");
      Described fooDescribed = foo.adapt(Described.class);
      fooDescribed.setName("foo_name");
      fooDescribed.setDescription("foo_description");
      ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
      fooResource.setAccessPermissions(Arrays.asList("foo_access_permission"));
      fooResource.setEditPermission("foo_edit_permission");
      Attributes fooAttrs = foo.getAttributes();
      fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
      fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);
      sync(true);

      //
      service.clearCache();
      PageContext page = service.loadPage(site.page("foo"));
      assertNotNull(page);
      assertNull(page.state);
      assertNotNull(page.data);
      PageState state = page.getState();
      assertEquals("foo_name", state.getName());
      assertEquals("foo_description", state.getDescription());
      assertEquals(Arrays.asList("foo_access_permission"), state.getAccessPermissions());
      assertEquals("foo_edit_permission", state.getEditPermission());
      assertEquals("foo_factory_id", state.getFactoryId());
      assertEquals(true, state.getShowMaxWindow());
   }

   public void testCreate()
   {
      mgr.getPOMService().getModel().getWorkspace().
         addSite(ObjectType.PORTAL_SITE, "create_page").
         getRootPage().
         addChild("pages");
      sync(true);

      //
      SiteKey site = SiteKey.portal("create_page");

      //
      PageContext page = new PageContext(site.page("foo"), new PageState(
         "foo_name",
         "foo_description",
         true,
         "foo_factory_id",
         Arrays.asList("foo_access_permission"),
         "foo_edit_permission"
      ));
      assertTrue(service.savePage(page));
      sync(true);

      //
      Page foo = mgr.getPOMService().getModel().getWorkspace().
         getSite(ObjectType.PORTAL_SITE, "create_page").
         getRootPage().
         getChild("pages").
         getChild("foo");
      assertNotNull(foo);
      Described fooDescribed = foo.adapt(Described.class);
      assertEquals("foo_name", fooDescribed.getName());
      assertEquals("foo_description", fooDescribed.getDescription());
      ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
      assertEquals(Arrays.asList("foo_access_permission"), fooResource.getAccessPermissions());
      assertEquals("foo_edit_permission", fooResource.getEditPermission());
      Attributes fooAttrs = foo.getAttributes();
      assertEquals("foo_factory_id", fooAttrs.getValue(MappedAttributes.FACTORY_ID));
      assertEquals(Boolean.TRUE, fooAttrs.getValue(MappedAttributes.SHOW_MAX_WINDOW));
   }

   public void testUpdate()
   {
      Page foo = mgr.getPOMService().getModel().getWorkspace().
         addSite(ObjectType.PORTAL_SITE, "update_page").
         getRootPage().
         addChild("pages").
         addChild("foo");
      Described fooDescribed = foo.adapt(Described.class);
      fooDescribed.setName("foo_name");
      fooDescribed.setDescription("foo_description");
      ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
      fooResource.setAccessPermissions(Arrays.asList("foo_access_permission"));
      fooResource.setEditPermission("foo_edit_permission");
      Attributes fooAttrs = foo.getAttributes();
      fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
      fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);
      sync(true);

      //
      SiteKey site = SiteKey.portal("update_page");

      //
      PageContext page = new PageContext(site.page("foo"), new PageState(
         "foo_name_2",
         "foo_description_2",
         false,
         "foo_factory_id_2",
         Arrays.asList("foo_access_permission_2"),
         "foo_edit_permission_2"
      ));
      assertFalse(service.savePage(page));
      sync(true);

      //
      foo = mgr.getPOMService().getModel().getWorkspace().
         getSite(ObjectType.PORTAL_SITE, "update_page").
         getRootPage().
         getChild("pages").
         getChild("foo");
      assertNotNull(foo);
      fooDescribed = foo.adapt(Described.class);
      assertEquals("foo_name_2", fooDescribed.getName());
      assertEquals("foo_description_2", fooDescribed.getDescription());
      fooResource = foo.adapt(ProtectedResource.class);
      assertEquals(Arrays.asList("foo_access_permission_2"), fooResource.getAccessPermissions());
      assertEquals("foo_edit_permission_2", fooResource.getEditPermission());
      fooAttrs = foo.getAttributes();
      assertEquals("foo_factory_id_2", fooAttrs.getValue(MappedAttributes.FACTORY_ID));
      assertEquals(Boolean.FALSE, fooAttrs.getValue(MappedAttributes.SHOW_MAX_WINDOW));
   }

   public void testDestroy()
   {
      mgr.getPOMService().getModel().getWorkspace().
         addSite(ObjectType.PORTAL_SITE, "destroy_page").
         getRootPage().
         addChild("pages");
      sync(true);

      //
      SiteKey site = SiteKey.portal("destroy_page");

      //
      assertFalse(service.destroyPage(site.page("foo")));

      //
      Page foo = mgr.getPOMService().getModel().getWorkspace().
         getSite(ObjectType.PORTAL_SITE, "destroy_page").
         getRootPage().
         getChild("pages").
         addChild("foo");
      Described fooDescribed = foo.adapt(Described.class);
      fooDescribed.setName("foo_name");
      fooDescribed.setDescription("foo_description");
      ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
      fooResource.setAccessPermissions(Arrays.asList("foo_access_permission"));
      fooResource.setEditPermission("foo_edit_permission");
      Attributes fooAttrs = foo.getAttributes();
      fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
      fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);
      sync(true);

      //
      assertTrue(service.destroyPage(site.page("foo")));
      assertNull(service.loadPage(CLASSIC_FOO));
      sync(true);

      //
      foo = mgr.getPOMService().getModel().getWorkspace().
         getSite(ObjectType.PORTAL_SITE, "destroy_page").
         getRootPage().
         getChild("pages").
         getChild("foo");
      assertNull(foo);
   }

   public void testFind() throws Exception
   {
      Page pages = mgr.getPOMService().getModel().getWorkspace().
         addSite(ObjectType.PORTAL_SITE, "find_pages").
         getRootPage().
         addChild("pages");
      pages.addChild("foo");
      pages.addChild("bar");
      sync(true);

      //
      QueryResult<PageContext> result = service.findPages(0, 10, SiteType.PORTAL, "find_pages", null, null);
      assertEquals(2, result.getSize());
      assertEquals(2, result.getHits());
   }

   public void testClone() throws Exception
   {
      Page foo = mgr.getPOMService().getModel().getWorkspace().
         addSite(ObjectType.PORTAL_SITE, "clone_page").
         getRootPage().
         addChild("pages").
         addChild("foo");
      Described fooDescribed = foo.adapt(Described.class);
      fooDescribed.setName("foo_name");
      fooDescribed.setDescription("foo_description");
      ProtectedResource fooResource = foo.adapt(ProtectedResource.class);
      fooResource.setAccessPermissions(Arrays.asList("foo_access_permission"));
      fooResource.setEditPermission("foo_edit_permission");
      Attributes fooAttrs = foo.getAttributes();
      fooAttrs.setValue(MappedAttributes.FACTORY_ID, "foo_factory_id");
      fooAttrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, true);
      sync(true);

      //
      SiteKey site = SiteKey.portal("clone_page");

      //
      PageContext bar = service.clone(site.page("foo"), site.page("bar"));
      assertNotNull(bar);
      assertNull(bar.state);
      assertNotNull(bar.data);
      PageState state = bar.getState();
      assertEquals("foo_name", state.getName());
      assertEquals("foo_description", state.getDescription());
      assertEquals(Arrays.asList("foo_access_permission"), state.getAccessPermissions());
      assertEquals("foo_edit_permission", state.getEditPermission());
      assertEquals("foo_factory_id", state.getFactoryId());
      assertEquals(true, state.getShowMaxWindow());

      //
//      DataStorage dataStorage = (DataStorage)PortalContainer.getInstance().getComponentInstanceOfType(DataStorage.class);


/*


      // Check instance id format

      // Check state
      Portlet pagePrefs = storage_.load(instanceId, ApplicationType.PORTLET);
      assertEquals(new PortletBuilder().add("template", "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl")
         .build(), pagePrefs);

      // Now save the cloned page
      storage_.save(clone);

      // Get cloned page
      clone = storage_.getPage("portal::test::_test4");
      assertEquals(2, clone.getChildren().size());
      banner1 = (Application<Portlet>)clone.getChildren().get(0);
      instanceId = banner1.getState();

      // Check instance id format
      assertEquals("web/BannerPortlet", storage_.getId(banner1.getState()));

      // Update site prefs
      PortletPreferences sitePrefs = new PortletPreferences();
      sitePrefs.setWindowId("portal#test:/web/BannerPortlet/banner");
      sitePrefs.setPreferences(new ArrayList<Preference>(Collections.singleton(new Preference())));
      sitePrefs.getPreferences().get(0).setName("template");
      sitePrefs.getPreferences().get(0).getValues().add("bar");
      storage_.save(sitePrefs);

      // Check that page prefs have not changed
      pagePrefs = storage_.load(instanceId, ApplicationType.PORTLET);
      assertEquals(new PortletBuilder().add("template", "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl")
         .build(), pagePrefs);

      // Update page prefs
      pagePrefs.setValue("template", "foo");
      storage_.save(instanceId, pagePrefs);

      // Check that page prefs have changed
      pagePrefs = storage_.load(instanceId, ApplicationType.PORTLET);
      assertEquals(new PortletBuilder().add("template", "foo").build(), pagePrefs);

      // Check that site prefs have not changed
      sitePrefs = storage_.getPortletPreferences("portal#test:/web/BannerPortlet/banner");
      assertEquals("bar", sitePrefs.getPreferences().get(0).getValues().get(0));

      // Now check the container
      Container container = (Container)clone.getChildren().get(1);
      assertEquals(2, container.getChildren().size());

      //
      Application banner2 = (Application)container.getChildren().get(0);
      // assertEquals(banner2.getInstanceId(), banner1.getInstanceId());

      //
      Page srcPage = storage_.getPage("portal::test::test4");
      srcPage.setEditPermission("Administrator");
      Application<Portlet>portlet = (Application<Portlet>)srcPage.getChildren().get(0);
      portlet.setDescription("NewPortlet");

      ArrayList<ModelObject> modelObject = srcPage.getChildren();
      modelObject.set(0, portlet);

      srcPage.setChildren(modelObject);

      storage_.save(srcPage);
      Page dstPage = storage_.clonePage(srcPage.getPageId(), srcPage.getOwnerType(), srcPage.getOwnerId(), "_PageTest1234");
      Application<Portlet>portlet1 = (Application<Portlet>)dstPage.getChildren().get(0);
      // Check src's edit permission and dst's edit permission
      assertEquals(srcPage.getEditPermission(), dstPage.getEditPermission());

      // Check src's children and dst's children
      assertEquals(portlet.getDescription(), portlet1.getDescription());
*/
   }

   public void testLoadWithoutSite()
   {
      assertNull(service.loadPage(SiteKey.portal("foo").page("homepage")));
   }

   public void testCreateWithoutSite()
   {
      PageContext page = new PageContext(SiteKey.portal("foo").page("homepage"), new PageState(
         "foo",
         "Foo",
         false,
         "factory-id",
         Arrays.asList("*:/platform/administrators"),
         "Everyone"
      ));
      try
      {
         service.savePage(page);
         fail();
      }
      catch (PageServiceException e)
      {
         assertEquals(PageError.NO_SITE, e.getError());
      }
   }

   public void testDestroyWithoutSite()
   {
      try
      {
         service.destroyPage(SiteKey.portal("foo").page("homepage"));
         fail();
      }
      catch (PageServiceException e)
      {
         assertEquals(PageError.NO_SITE, e.getError());
      }
   }
}
