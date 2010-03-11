/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.config;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov
 * 13, 2007
 */
public class TestLoadedPOM extends AbstractPortalTest
{

   /** . */
   private UserPortalConfigService portalConfigService;

   /** . */
   private DataStorage storage;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   public TestLoadedPOM(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = getContainer();
      portalConfigService = (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      storage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }

   public void testLegacyGroupWithNormalizedName() throws Exception
   {
      PageNavigation nav = storage.getPageNavigation("group::/platform/test/legacy");
      assertNotNull(nav);
      assertEquals("/platform/test/legacy", nav.getOwnerId());
      PageNode node = nav.getNodes().get(0);
      assertEquals("group::/platform/test/legacy::register", node.getPageReference());

      Page page = storage.getPage("group::/platform/test/legacy::register");
      assertNotNull(page);
      assertEquals("group::/platform/test/legacy::register", page.getPageId());
      assertEquals("/platform/test/legacy", page.getOwnerId());
      Application app = (Application)page.getChildren().get(0);
      //    assertEquals("group#/platform/test/legacy:/web/IFramePortlet/blog", app.getInstanceState().getWeakReference());

      PortletPreferences prefs = storage.getPortletPreferences("group#/platform/test/legacy:/web/IFramePortlet/blog");
      assertNotNull(prefs);
      assertEquals("group#/platform/test/legacy:/web/IFramePortlet/blog", prefs.getWindowId());
   }

   public void testGroupWithNormalizedName() throws Exception
   {
      PageNavigation nav = storage.getPageNavigation("group::/platform/test/normalized");
      assertNotNull(nav);
      assertEquals("/platform/test/normalized", nav.getOwnerId());
      PageNode node = nav.getNodes().get(0);
      assertEquals("group::/platform/test/normalized::register", node.getPageReference());

      Page page = storage.getPage("group::/platform/test/normalized::register");
      assertNotNull(page);
      assertEquals("group::/platform/test/normalized::register", page.getPageId());
      assertEquals("/platform/test/normalized", page.getOwnerId());
      Application app = (Application)page.getChildren().get(0);
      //    assertEquals("group#/platform/test/normalized:/exoadmin/AccountPortlet/Account", app.getInstanceState().getWeakReference());

      PortletPreferences prefs =
         storage.getPortletPreferences("group#/platform/test/normalized:/exoadmin/AccountPortlet/Account");
      assertNotNull(prefs);
      assertEquals("group#/platform/test/normalized:/exoadmin/AccountPortlet/Account", prefs.getWindowId());
   }

   public void testNavigation() throws Exception
   {
      PageNavigation nav = storage.getPageNavigation("portal::test");
      assertNotNull(nav);

      //
      assertEquals(1, nav.getPriority());

      //
      assertEquals(2, nav.getNodes().size());

      //
      PageNode nodeNavigation = nav.getNodes().get(0);
      assertEquals(0, nodeNavigation.getChildren().size());
      assertEquals("node_name", nodeNavigation.getName());
      assertEquals("node_uri", nodeNavigation.getUri());
      assertEquals("node_label", nodeNavigation.getLabel());
      assertEquals("node_icon", nodeNavigation.getIcon());
      GregorianCalendar start = new GregorianCalendar(2000, 2, 21, 1, 33, 0);
      start.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(start.getTime(), nodeNavigation.getStartPublicationDate());
      GregorianCalendar end = new GregorianCalendar(2009, 2, 21, 1, 33, 0);
      end.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(end.getTime(), nodeNavigation.getEndPublicationDate());
      assertEquals(true, nodeNavigation.isShowPublicationDate());
      assertEquals(true, nodeNavigation.isVisible());
   }

   public void testPortal() throws Exception
   {
      PortalConfig portal = storage.getPortalConfig("test");
      assertNotNull(portal);

      assertEquals("test", portal.getName());
      assertEquals("en", portal.getLocale());
      assertTrue(Arrays.equals(new String[]{"test_access_permissions"}, portal.getAccessPermissions()));
      assertEquals("test_edit_permission", portal.getEditPermission());
      assertEquals("test_skin", portal.getSkin());
      assertEquals("test_prop_value", portal.getProperty("prop_key"));
   }

   public void testPageWithoutPageId() throws Exception
   {
      Page page = storage.getPage("portal::test::test2");
      assertNotNull(page);
      assertEquals("portal::test::test2", page.getPageId());
      assertEquals("test", page.getOwnerId());
      assertEquals("portal", page.getOwnerType());
      assertEquals("test2", page.getName());
   }

   public void testPage() throws Exception
   {
      Page page = storage.getPage("portal::test::test1");
      assertNotNull(page);

      //
      assertEquals("test_title", page.getTitle());
      assertEquals("test_factory_id", page.getFactoryId());
      assertTrue(Arrays.equals(new String[]{"test_access_permissions"}, page.getAccessPermissions()));
      assertEquals("test_edit_permission", page.getEditPermission());
      assertEquals(true, page.isShowMaxWindow());

      //
      List<ModelObject> children = page.getChildren();
      assertEquals(2, children.size());

      //
      Container container1 = (Container)children.get(0);
      assertEquals("container_1", container1.getName());
      assertEquals("container_1_title", container1.getTitle());
      assertEquals("container_1_icon", container1.getIcon());
      assertEquals("container_1_template", container1.getTemplate());
      assertTrue(Arrays.equals(new String[]{"container_1_access_permissions"}, container1.getAccessPermissions()));
      assertEquals("container_1_factory_id", container1.getFactoryId());
      assertEquals("container_1_description", container1.getDescription());
      assertEquals("container_1_width", container1.getWidth());
      assertEquals("container_1_height", container1.getHeight());

      //
      Application application1 = (Application)children.get(1);
      assertEquals("application_1_theme", application1.getTheme());
      assertEquals("application_1_title", application1.getTitle());
      assertTrue(Arrays.equals(new String[]{"application_1_access_permissions"}, application1.getAccessPermissions()));
      assertEquals(true, application1.getShowInfoBar());
      assertEquals(true, application1.getShowApplicationState());
      assertEquals(true, application1.getShowApplicationMode());
      assertEquals("application_1_description", application1.getDescription());
      assertEquals("application_1_icon", application1.getIcon());
      assertEquals("application_1_width", application1.getWidth());
      assertEquals("application_1_height", application1.getHeight());
      assertEquals("application_1_prop_value", application1.getProperties().get("prop_key"));
      //    assertEquals("portal#test:/web/BannerPortlet/banner", application1.getInstanceState().getWeakReference());
   }

/*
   public void testFindPageByTitle() throws Exception
   {
      Query<Page> query = new Query<Page>(null, null, null, "TestTitle", Page.class);
      List<Page> list = storage.find(query).getAll();
      assertEquals("Expected two result instead of " + list, 2, list.size());
      Set<String> ids = new HashSet<String>(Arrays.asList(list.get(0).getPageId(), list.get(1).getPageId()));
      HashSet<String> expectedIds =
         new HashSet<String>(Arrays.asList("group::/platform/test/legacy::register",
            "group::/platform/test/normalized::register"));
      assertEquals(expectedIds, ids);
   }
*/

   public void testFindNavigation() throws Exception
   {
      Query<PageNavigation> query = new Query<PageNavigation>("group", null, null, null, PageNavigation.class);
      List<PageNavigation> list = storage.find(query).getAll();
      assertEquals("Expected 6 results instead of " + list, 6, list.size());
      Set<String> names = new HashSet<String>();
      for (PageNavigation navigation : list)
      {
         assertEquals("group", navigation.getOwnerType());
         names.add(navigation.getOwnerId());
      }
      HashSet<String> expectedNames =
         new HashSet<String>(Arrays.asList("/platform/test/legacy", "/platform/test/normalized",
            "/platform/administrators", "/platform/guests", "/platform/users",
            "/organization/management/executive-board"));
      assertEquals(expectedNames, names);
   }

/*
   public void testFindPageByName() throws Exception
   {
      Query<Page> query = new Query<Page>("portal", "test", null, null, Page.class);
      List<Page> list = storage.find(query).getAll();
      assertEquals("Expected 4 results instead of " + list, 4, list.size());
      Set<String> names = new HashSet<String>();
      for (Page page : list)
      {
         assertEquals("portal", page.getOwnerType());
         assertEquals("test", page.getOwnerId());
         names.add(page.getName());
      }
      HashSet<String> expectedNames = new HashSet<String>(Arrays.asList("test1", "test2", "test3", "test4"));
      assertEquals(expectedNames, names);
   }
*/

   public void testAnonymousPreferencesSavePage() throws Exception
   {
      Page page = storage.getPage("portal::test::test3");

      // Save it again
      storage.save(page);

      //
      page = storage.getPage("portal::test::test3");

      //
      Application app = (Application)page.getChildren().get(0);
      //    String instanceId = app.getInstanceState().getWeakReference();

      // Check instance id
      //    String[] chunks = Mapper.parseWindowId(instanceId);
      //    assertEquals("portal", chunks[0]);
      //    assertEquals("test", chunks[1]);
      //    assertEquals("web", chunks[2]);
      //    assertEquals("BannerPortlet", chunks[3]);
      //    assertEquals("banner2", chunks[4]);

      // Check state
      //    assertNull(storage.getPortletPreferences(instanceId));
   }

   public void testAnonymousPreference() throws Exception
   {
      Page page = storage.getPage("portal::test::test3");
      Application app = (Application)page.getChildren().get(0);
      //    String instanceId = app.getInstanceState().getWeakReference();

      // Check instance id
      //    String[] chunks = Mapper.parseWindowId(instanceId);
      //    assertEquals("portal", chunks[0]);
      //    assertEquals("test", chunks[1]);
      //    assertEquals("web", chunks[2]);
      //    assertEquals("BannerPortlet", chunks[3]);
      //    assertEquals("banner2", chunks[4]);

      // Check initial state
      //    assertNull(storage.getPortletPreferences(instanceId));

      // Save state
      //    PortletPreferences prefs = new PortletPreferences();
      //    prefs.setWindowId(instanceId);
      //    prefs.setPreferences(new ArrayList<Preference>());
      //    Preference pref = new Preference();
      //    pref.setName("foo");
      //    pref.setValues(new ArrayList<String>(Arrays.asList("foo1")));
      //    pref.setReadOnly(false);
      //    prefs.getPreferences().add(pref);
      //    storage.save(prefs);

      // Now save the page
      //    storage.save(page);

      // Check we have the same instance id
      //    page = storage.getPage(page.getPageId());
      //    app = (Application)page.getChildren().get(0);
      //    assertEquals(instanceId, app.getInstanceState().getWeakReference());

      // Now check state
      //    prefs = storage.getPortletPreferences(app.getInstanceState().getWeakReference());
      //    assertEquals(1, prefs.getPreferences().size());
      //    assertEquals("foo", prefs.getPreferences().get(0).getName());
      //    assertEquals(1, prefs.getPreferences().get(0).getValues().size());
      //    assertEquals("foo1", prefs.getPreferences().get(0).getValues().get(0));
   }

   public void testSitePreferences() throws Exception
   {
      //    Page page = storage.getPage("portal::test::test4");
      //    Application app = (Application)page.getChildren().get(0);
      //    String instanceId = app.getInstanceState().getWeakReference();

      // Check instance id
      //    String[] chunks = Mapper.parseWindowId(instanceId);
      //    assertEquals("portal", chunks[0]);
      //    assertEquals("test", chunks[1]);
      //    assertEquals("web", chunks[2]);
      //    assertEquals("BannerPortlet", chunks[3]);
      //    assertEquals("banner", chunks[4]);

      // Check initial state
      //    PortletPreferences prefs = storage.getPortletPreferences(instanceId);
      //    assertEquals(1, prefs.getPreferences().size());
      //    assertEquals("template", prefs.getPreferences().get(0).getName());
      //    assertEquals(1, prefs.getPreferences().get(0).getValues().size());
      //    assertEquals("par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl", prefs.getPreferences().get(0).getValues().get(0));
      //
      // Save state
      //    prefs.getPreferences().get(0).setValues(new ArrayList<String>(Arrays.asList("foo")));
      //    storage.save(prefs);

      // Now save the page
      //    storage.save(page);

      // Check we have the same instance id
      //    page = storage.getPage(page.getPageId());
      //    app = (Application)page.getChildren().get(0);
      //    assertEquals(instanceId, app.getInstanceState().getWeakReference());

      // Now check state
      //    prefs = storage.getPortletPreferences(instanceId);
      //    assertEquals(1, prefs.getPreferences().size());
      //    assertEquals("template", prefs.getPreferences().get(0).getName());
      //    assertEquals(1, prefs.getPreferences().get(0).getValues().size());
      //    assertEquals("foo", prefs.getPreferences().get(0).getValues().get(0));
   }
}