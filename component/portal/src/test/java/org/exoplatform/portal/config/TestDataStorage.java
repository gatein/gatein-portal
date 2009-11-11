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

import static org.exoplatform.portal.pom.config.Utils.split;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.ModelChange;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.exoplatform.test.BasicTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov
 * 13, 2007
 */
public class TestDataStorage extends BasicTestCase
{

   private final String testPortal = "testPortal";

   private final String testPage = "portal::classic::testPage";

   private final String testPortletPreferences = "portal#classic:/web/BannerPortlet/testPortletPreferences";

   DataStorage storage_;

   private POMSessionManager mgr;

   public TestDataStorage(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      if (storage_ != null)
         return;
      PortalContainer container = PortalContainer.getInstance();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      mgr.closeSession(false);
   }

   public void testPortalConfigCreate() throws Exception
   {
      PortalConfig portalConfig = new PortalConfig();
      portalConfig.setName(testPortal);
      portalConfig.setLocale("en");
      portalConfig.setAccessPermissions(new String[]{UserACL.EVERYONE});

      PortalConfig returnConfig = storage_.getPortalConfig(portalConfig.getName());
      if (returnConfig != null)
         storage_.remove(returnConfig);

      storage_.create(portalConfig);
      returnConfig = storage_.getPortalConfig(portalConfig.getName());
      assertNotNull(returnConfig);
      assertEquals(portalConfig.getName(), returnConfig.getName());
      assertEquals(portalConfig.getCreator(), returnConfig.getCreator());
   }

   public void testPortalConfigSave() throws Exception
   {
      testPortalConfigCreate();

      //
      PortalConfig portalConfig = storage_.getPortalConfig(testPortal);

      assertNotNull(portalConfig);

      String newLocale = "vietnam";
      portalConfig.setLocale(newLocale);
      storage_.save(portalConfig);
      portalConfig = storage_.getPortalConfig(testPortal);
      assertNotNull(portalConfig);
      assertEquals(newLocale, portalConfig.getLocale());
   }

   public void testPortalConfigRemove() throws Exception
   {
      testPortalConfigSave();

      //
      PortalConfig portalConfig = storage_.getPortalConfig(testPortal);
      assertNotNull(portalConfig);

      storage_.remove(portalConfig);
      assertNull(storage_.getPortalConfig(testPortal));
   }

   public void testPageConfigCreate() throws Exception
   {
      testPortalConfigRemove();

      //
      createPageConfig(PortalConfig.PORTAL_TYPE, "portalone");
      createPageConfig(PortalConfig.USER_TYPE, "exoadmin");
      createPageConfig(PortalConfig.GROUP_TYPE, "portal/admin");
   }

   private void createPageConfig(String ownerType, String ownerId) throws Exception
   {
      Page page = new Page();
      page.setName("testPage");
      page.setOwnerId("classic");
      page.setOwnerType("portal");

      try
      {
         storage_.create(page);
      }
      catch (Exception e)
      {
         page = storage_.getPage(page.getPageId());
      }

      Page returnPage = storage_.getPage(page.getPageId());
      assertNotNull(returnPage);
      assertEquals(page.getPageId(), returnPage.getPageId());
      assertEquals(page.getOwnerType(), returnPage.getOwnerType());
      assertEquals(page.getOwnerId(), returnPage.getOwnerId());
      assertEquals(page.getChildren().size(), returnPage.getChildren().size());
   }

   public void testPageConfigSave() throws Exception
   {
      testPageConfigCreate();

      //
      Page page = storage_.getPage(testPage);
      assertNotNull(page);

      //
      PortalConfig portalConfig = new PortalConfig();
      portalConfig.setName("customers");
      portalConfig.setLocale("en");
      portalConfig.setAccessPermissions(new String[]{UserACL.EVERYONE});
      storage_.create(portalConfig);

      page.setTitle("New Page Title");
      page.setOwnerId("customers");
      storage_.save(page);

      Page returnPage = storage_.getPage(page.getPageId());
      assertEquals("New Page Title", returnPage.getTitle());
      assertEquals(page.getPageId(), returnPage.getPageId());
      assertEquals(page.getOwnerType(), returnPage.getOwnerType());
      assertEquals(page.getOwnerId(), returnPage.getOwnerId());
      assertEquals(page.getChildren().size(), returnPage.getChildren().size());
   }

   public void testPageConfigRemove() throws Exception
   {
      testPageConfigSave();

      //
      Page page = storage_.getPage(testPage);
      assertNotNull(page);

      storage_.remove(page);

      page = storage_.getPage(testPage);
      assertNull(page);
   }

   public void testNavigationCreate() throws Exception
   {
      testPageConfigRemove();

      //
      PortalConfig portalConfig = new PortalConfig();
      portalConfig.setName(testPortal);
      portalConfig.setLocale("en");
      portalConfig.setAccessPermissions(new String[]{UserACL.EVERYONE});
      storage_.save(portalConfig);

      //
      PageNavigation pageNavi = new PageNavigation();
      pageNavi.setOwnerId(testPortal);
      pageNavi.setOwnerType("portal");
      storage_.create(pageNavi);
   }

   public void testNavigationSave() throws Exception
   {
      testNavigationCreate();

      //
      PageNavigation pageNavi = storage_.getPageNavigation("portal", testPortal);
      assertNotNull(pageNavi);

      String newModifier = "trong.tran";
      pageNavi.setModifier(newModifier);
      storage_.save(pageNavi);

      PageNavigation newPageNavi = storage_.getPageNavigation(pageNavi.getOwnerType(), pageNavi.getOwnerId());
      assertEquals(newModifier, newPageNavi.getModifier());
   }

   public void testNavigationRemove() throws Exception
   {
      testNavigationSave();

      //
      PageNavigation pageNavi = storage_.getPageNavigation("portal", testPortal);
      assertNotNull(pageNavi);

      storage_.remove(pageNavi);

      pageNavi = storage_.getPageNavigation("portal", testPortal);
      // assertNull(pageNavi);
   }

   public void testPortletPreferencesCreate() throws Exception
   {
      testNavigationRemove();

      //
      ArrayList<Preference> prefs = new ArrayList<Preference>();
      for (int i = 0; i < 5; i++)
      {
         Preference pref = new Preference();
         pref.setName("name" + i);
         pref.addValue("value" + i);
         prefs.add(pref);
      }

      PortletPreferences portletPreferences = new PortletPreferences();
      portletPreferences.setWindowId(testPortletPreferences);
      portletPreferences.setPreferences(prefs);

      storage_.save(portletPreferences);

      PortletPreferences portletPref = storage_.getPortletPreferences(testPortletPreferences);
      assertEquals(portletPref.getWindowId(), testPortletPreferences);
   }

   public void testPortletPreferencesSave() throws Exception
   {
      testPortletPreferencesCreate();

      //
      PortletPreferences portletPref = storage_.getPortletPreferences(testPortletPreferences);
      assertNotNull(portletPref);

      List<Preference> prefs = portletPref.getPreferences();
      assertNotNull(prefs);
      assertEquals(5, prefs.size());
   }

   public void testWindowScopedPortletPreferences() throws Exception
   {
      Page page = new Page();
      page.setPageId("portal::test::foo");
      TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>("web/BannerPortlet", new PortletBuilder().add("template", "bar").build());
      Application<Portlet> app = Application.createPortletApplication();
      app.setState(state);
      page.getChildren().add(app);
      storage_.save(page);
      page = storage_.getPage(page.getPageId());
      app = (Application<Portlet>)page.getChildren().get(0);
      assertEquals("web/BannerPortlet", storage_.getId(app.getState()));
   }

   public void testPageMerge() throws Exception
   {
      Page page = storage_.getPage("portal::test::test4");

      String app1Id = page.getChildren().get(0).getStorageId();
      Container container = (Container)page.getChildren().get(1);
      String containerId = container.getStorageId();
      String app2Id = container.getChildren().get(0).getStorageId();
      String app3Id = container.getChildren().get(1).getStorageId();

      // Add an application
      Application<Portlet> groovyApp = create("portal#test:/web/GroovyPortlet/groovyportlet");
      ((Container)page.getChildren().get(1)).getChildren().add(1, groovyApp);

      // Save
      List<ModelChange> changes = storage_.save(page);
      assertEquals(6, changes.size());
      ModelChange.Update c0 = (ModelChange.Update)changes.get(0);
//      assertSame(page, c0.getObject());
      ModelChange.Update c1 = (ModelChange.Update)changes.get(1);
//      assertSame(page.getChildren().get(0), c1.getObject());
      ModelChange.Update c2 = (ModelChange.Update)changes.get(2);
//      assertSame(page.getChildren().get(1), c2.getObject());
      ModelChange.Update c3 = (ModelChange.Update)changes.get(3);
//      assertSame(container.getChildren().get(0), c3.getObject());
      ModelChange.Create c4 = (ModelChange.Create)changes.get(4);
//      assertSame(container.getChildren().get(1), c4.getObject());
      ModelChange.Update c5 = (ModelChange.Update)changes.get(5);
//      assertSame(container.getChildren().get(2), c5.getObject());

      // Check it is existing at the correct location
      // and also that the ids are still the same
      page = storage_.getPage("portal::test::test4");
      assertEquals(2, page.getChildren().size());
      //    assertEquals(PortletState.create("portal#test:/web/BannerPortlet/banner"), ((Application)page.getChildren().get(0)).getInstanceState());
      assertEquals(app1Id, page.getChildren().get(0).getStorageId());
      container = (Container)page.getChildren().get(1);
      assertEquals(3, container.getChildren().size());
      assertEquals(containerId, container.getStorageId());
      //    assertEquals(PortletState.create("portal#test:/web/BannerPortlet/banner"), ((Application)container.getChildren().get(0)).getInstanceState());
      assertEquals(app2Id, container.getChildren().get(0).getStorageId());
      //    assertEquals(PortletState.create("portal#test:/web/GroovyPortlet/groovyportlet"), ((Application)container.getChildren().get(1)).getInstanceState());
      assertNotNull(container.getChildren().get(0).getStorageId());
      //    assertEquals(PortletState.create("portal#test:/web/FooterPortlet/footer"), ((Application)container.getChildren().get(2)).getInstanceState());
      assertEquals(app3Id, container.getChildren().get(2).getStorageId());

      // Now remove the element
      container.getChildren().remove(1);
      storage_.save(page);

      // Check it is removed
      // and also that the ids are still the same
      page = storage_.getPage("portal::test::test4");
      assertEquals(2, page.getChildren().size());
      //    assertEquals(PortletState.create("portal#test:/web/BannerPortlet/banner"), ((Application)page.getChildren().get(0)).getInstanceState());
      assertEquals(app1Id, page.getChildren().get(0).getStorageId());
      container = (Container)page.getChildren().get(1);
      assertEquals(2, container.getChildren().size());
      assertEquals(containerId, container.getStorageId());
      //    assertEquals(PortletState.create("portal#test:/web/BannerPortlet/banner"), ((Application)container.getChildren().get(0)).getInstanceState());
      assertEquals(app2Id, container.getChildren().get(0).getStorageId());
      //    assertEquals(PortletState.create("portal#test:/web/FooterPortlet/footer"), ((Application)container.getChildren().get(1)).getInstanceState());
      assertEquals(app3Id, container.getChildren().get(1).getStorageId());
   }

   public void testClone() throws Exception
   {
      storage_.clonePage("portal::test::test4", "portal", "test", "_test4");

      // Get cloned page
      Page clone = storage_.getPage("portal::test::_test4");
      assertEquals(2, clone.getChildren().size());
      Application<Portlet> banner1 = (Application<Portlet>)clone.getChildren().get(0);
      ApplicationState<Portlet> instanceId = banner1.getState();

      // Check instance id format
      assertEquals("web/BannerPortlet", storage_.getId(banner1.getState()));

      // Check state
      Portlet pagePrefs = storage_.load(instanceId);
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
      pagePrefs = storage_.load(instanceId);
      assertEquals(new PortletBuilder().add("template", "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl")
         .build(), pagePrefs);

      // Update page prefs
      pagePrefs.setValue("template", "foo");
      storage_.save(instanceId, pagePrefs);

      // Check that page prefs have changed
      pagePrefs = storage_.load(instanceId);
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
   }

   public void _testDashboard() throws Exception
   {
      Page page = new Page();
      page.setPageId("portal::test::foo");
      page.getChildren().add(new Dashboard());
      storage_.save(page);

      //
      page = storage_.getPage("portal::test::foo");
      assertEquals(1, page.getChildren().size());
      Application<Portlet> dashboardPortlet = (Application<Portlet>)page.getChildren().get(0);
      String dashboardId = dashboardPortlet.getStorageId();
      assertNotNull(dashboardId);
      assertNotNull(dashboardPortlet.getStorageName());
      assertEquals("dashboard/DashboardPortlet", storage_.getId(dashboardPortlet.getState()));

      // Configures the dashboard
      Dashboard dashboard = new Dashboard(dashboardId);
      TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>("foo/bar");
      Application<Portlet> app = Application.createPortletApplication();
      app.setState(state);
      dashboard.getChildren().add(app);

      // Attempt to save a dashboard with a portlet on it
      storage_.saveDashboard(dashboard);

      // Test that load page does not load the children
      page = storage_.getPage("portal::test::foo");
      assertEquals(1, page.getChildren().size());
      assertTrue(((Application)page.getChildren().get(0)).getType() == ApplicationType.PORTLET);

      // Now check we have the state on the dashboard
      dashboard = storage_.loadDashboard(dashboardId);
      assertEquals(1, dashboard.getChildren().size());
      app = (Application<Portlet>)dashboard.getChildren().get(0);
      assertEquals("foo/bar", storage_.getId(app.getState()));
   }

   public void _testDashboardLayout() throws Exception
   {
      Application<Portlet> dashboardPortlet = Application.createPortletApplication();
      ApplicationState<Portlet> state = new TransientApplicationState<Portlet>("dashboard/DashboardPortlet");
      dashboardPortlet.setState(state);

      //
      Page page = new Page();
      page.setPageId("portal::test::foo");
      page.getChildren().add(dashboardPortlet);
      storage_.save(page);

      //
      page = storage_.getPage("portal::test::foo");
      String dashboardId = page.getChildren().get(0).getStorageId();

      //
      Dashboard dashboard = storage_.loadDashboard(dashboardId);
      assertEquals(3, dashboard.getChildren().size());

      // Now save the page with the dashboard
      storage_.save(page);

      //
      dashboard = storage_.loadDashboard(dashboardId);
      assertEquals(3, dashboard.getChildren().size());
   }

   public void _testDashboardMoveRight() throws Exception
   {
      Page page = new Page();
      page.setPageId("portal::test::foo");
      Application<Portlet> app = Application.createPortletApplication();
      app.setState(new TransientApplicationState<Portlet>("dashboard/DashboardPortlet"));
      page.getChildren().add(app);
      storage_.save(page);
      page = storage_.getPage("portal::test::foo");
      String id = page.getChildren().get(0).getStorageId();

      // Load the dashboard itself
      Dashboard dashboard = storage_.loadDashboard(id);

      // Put a gadget in one container
      Container row0 = (Container)dashboard.getChildren().get(0);
      Application<Gadget> gadgetApp = Application.createGadgetApplication();
      gadgetApp.setState(new TransientApplicationState<Gadget>("foo"));
      row0.getChildren().add(gadgetApp);

      // Save the dashboard
      storage_.saveDashboard(dashboard);

      // Load again the persisted version
      dashboard = storage_.loadDashboard(id);

      // Now move the gadget from one container to another to simulate a move
      row0 = (Container)dashboard.getChildren().get(0);
      Container row1 = (Container)dashboard.getChildren().get(1);
      row1.getChildren().add(row0.getChildren().remove(0));

      // Save  
      storage_.saveDashboard(dashboard);

      // Load again the persisted version and check the move was done in the storage
      dashboard = storage_.loadDashboard(id);
      row0 = (Container)dashboard.getChildren().get(0);
      row1 = (Container)dashboard.getChildren().get(1);
      assertEquals(0, row0.getChildren().size());
      assertEquals(1, row1.getChildren().size());
      gadgetApp = (Application<Gadget>)row1.getChildren().get(0);
      assertEquals("foo", storage_.getId(gadgetApp.getState()));
   }

   public void _testDashboardMoveLeft() throws Exception
   {
      Page page = new Page();
      page.setPageId("portal::test::foo");
      Application<Portlet> app = Application.createPortletApplication();
      app.setState(new TransientApplicationState<Portlet>("dashboard/DashboardPortlet"));
      page.getChildren().add(app);
      storage_.save(page);
      page = storage_.getPage("portal::test::foo");
      String id = page.getChildren().get(0).getStorageId();

      // Load the dashboard itself
      Dashboard dashboard = storage_.loadDashboard(id);

      // Put a gadget in one container
      Container row1 = (Container)dashboard.getChildren().get(1);
      Application<Gadget> gadgetApp = Application.createGadgetApplication();
      gadgetApp.setState(new TransientApplicationState<Gadget>("foo"));
      row1.getChildren().add(gadgetApp);

      // Save the dashboard
      storage_.saveDashboard(dashboard);

      // Load again the persisted version
      dashboard = storage_.loadDashboard(id);

      // Now move the gadget from one container to another to simulate a move
      row1 = (Container)dashboard.getChildren().get(1);
      Container row0 = (Container)dashboard.getChildren().get(0);
      row0.getChildren().add(row1.getChildren().remove(0));

      // Save
      storage_.saveDashboard(dashboard);

      // Load again the persisted version and check the move was done in the storage
      dashboard = storage_.loadDashboard(id);
      row0 = (Container)dashboard.getChildren().get(0);
      row1 = (Container)dashboard.getChildren().get(1);
      assertEquals(0, row1.getChildren().size());
      assertEquals(1, row0.getChildren().size());
      gadgetApp = (Application<Gadget>)row0.getChildren().get(0);
      assertEquals("foo", storage_.getId(gadgetApp.getState()));
   }

   private Application<Portlet> create(String instanceId)
   {
      int i0 = instanceId.indexOf("#");
      int i1 = instanceId.indexOf(":/", i0 + 1);
      String ownerType = instanceId.substring(0, i0);
      String ownerId = instanceId.substring(i0 + 1, i1);
      String persistenceid = instanceId.substring(i1 + 2);
      String[] persistenceChunks = split("/", persistenceid);
      TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>(
         persistenceChunks[0] + "/" + persistenceChunks[1],
         null,
         ownerType,
         ownerId,
         persistenceChunks[2]);
      Application<Portlet> portletApp = Application.createPortletApplication();
      portletApp.setState(state);
      return portletApp;
   }
}
