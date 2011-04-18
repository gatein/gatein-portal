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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.model.*;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.AssertionFailedError;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov
 * 13, 2007
 */
public class TestDataStorage extends AbstractPortalTest
{

   /** . */
   private static final String CLASSIC_HOME = "portal::classic::homepage";

   /** . */
   private static final String CLASSIC_TEST = "portal::classic::testPage";

   /** . */
   private final String testPortletPreferences = "portal#classic:/web/BannerPortlet/testPortletPreferences";

   /** . */
   private DataStorage storage_;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   /** . */
   private EventQueue events;

   /** . */
   private ListenerService listenerService;

   public TestDataStorage(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = PortalContainer.getInstance();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = mgr.openSession();
      listenerService = (ListenerService)container.getComponentInstanceOfType(ListenerService.class);

      //
      if (events == null)
      {
         events = new EventQueue();
         listenerService.addListener(DataStorage.PAGE_CREATED, events);
         listenerService.addListener(DataStorage.PAGE_REMOVED, events);
         listenerService.addListener(DataStorage.PAGE_UPDATED, events);
         listenerService.addListener(DataStorage.NAVIGATION_CREATED, events);
         listenerService.addListener(DataStorage.NAVIGATION_REMOVED, events);
         listenerService.addListener(DataStorage.NAVIGATION_UPDATED, events);
         listenerService.addListener(DataStorage.PORTAL_CONFIG_CREATED, events);
         listenerService.addListener(DataStorage.PORTAL_CONFIG_UPDATED, events);
         listenerService.addListener(DataStorage.PORTAL_CONFIG_REMOVED, events);
      }
      else
      {
         events.clear();
      }
   }

   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }

   public void testCreatePortal() throws Exception
   {
      String label = "portal_foo";
      String description = "This is new portal for testing";
      PortalConfig portal = new PortalConfig();
      portal.setType("portal");
      portal.setName("foo");
      portal.setLocale("en");
      portal.setLabel(label);
      portal.setDescription(description);
      portal.setAccessPermissions(new String[]{UserACL.EVERYONE});

      //
      events.clear();
      storage_.create(portal);
      events.assertSize(1);
      portal = storage_.getPortalConfig(portal.getName());
      assertNotNull(portal);
      assertEquals("portal", portal.getType());
      assertEquals("foo", portal.getName());
      assertEquals(label, portal.getLabel());
      assertEquals(description, portal.getDescription());
   }

   public void testPortalConfigSave() throws Exception
   {
      PortalConfig portal = storage_.getPortalConfig("portal", "test");
      assertNotNull(portal);

      //
      events.clear();
      portal.setLocale("vietnam");
      storage_.save(portal);
      events.assertSize(1);

      //
      portal = storage_.getPortalConfig("portal", "test");
      assertNotNull(portal);
      assertEquals("vietnam", portal.getLocale());
   }

   public void testPortalConfigRemove() throws Exception
   {
      PortalConfig portal = storage_.getPortalConfig("portal", "test");
      assertNotNull(portal);

      events.clear();
      storage_.remove(portal);
      events.assertSize(1);
      assertNull(storage_.getPortalConfig("portal", "test"));
      
      try
      {
         //Trying to remove non existing a portal config
         storage_.remove(portal);
         fail("was expecting a NoSuchDataException");
      }
      catch (NoSuchDataException e)
      {

      }
   }

   public void testCreatePage() throws Exception
   {
      Page page = new Page();
      page.setTitle("MyTitle");
      page.setOwnerType(PortalConfig.PORTAL_TYPE);
      page.setOwnerId("test");
      page.setName("foo");

      //
      events.clear();
      storage_.create(page);
      events.assertSize(1);

      //
      Page page2 = storage_.getPage(page.getPageId());

      //
      assertNotNull(page2);
      assertEquals("portal::test::foo", page2.getPageId());
      assertEquals("portal", page2.getOwnerType());
      assertEquals("test", page2.getOwnerId());
      assertEquals("foo", page2.getName());
      assertEquals("MyTitle", page2.getTitle());
      assertEquals(0, page2.getChildren().size());
   }

   public void testSavePage() throws Exception
   {
      Page page = new Page();
      page.setTitle("MyTitle");
      page.setOwnerType(PortalConfig.PORTAL_TYPE);
      page.setOwnerId("test");
      page.setName("foo");
      page.setShowMaxWindow(false);

      //
      events.clear();
      storage_.create(page);
      events.assertSize(1);

      //
      Page page2 = storage_.getPage(page.getPageId());
      page2.setTitle("MyTitle2");
      page2.setShowMaxWindow(true);
      events.clear();
      storage_.save(page2);
      events.assertSize(1);

      page2 = storage_.getPage(page.getPageId());
      assertNotNull(page2);
      assertEquals("portal::test::foo", page2.getPageId());
      assertEquals("portal", page2.getOwnerType());
      assertEquals("test", page2.getOwnerId());
      assertEquals("foo", page2.getName());
      assertEquals("MyTitle2", page2.getTitle());
      assertEquals(0, page2.getChildren().size());
      assertEquals(true, page2.isShowMaxWindow());
   }
   
   public void testRenameNode() throws Exception
   {
      //Create node
      PageNode pageNode = new PageNode();
      String name = "MyPageNode";
      pageNode.setName(name);
      pageNode.setUri(name);
      pageNode.setLabel(name);
      
      //add node to page navigation
      String ownerId = "root";
      String ownerType = "user";
      PageNavigation nav = storage_.getPageNavigation(ownerType, ownerId);
      assertNotNull(nav);
      assertEquals(ownerId, nav.getOwnerId());
      assertEquals(ownerType, nav.getOwnerType());
      nav.addNode(pageNode);
      storage_.save(nav);
      
      //Rename node
      PageNavigation nav2 = storage_.getPageNavigation(ownerType, ownerId);
      assertNotNull(nav2);
      assertEquals(ownerId, nav2.getOwnerId());
      assertEquals(ownerType, nav2.getOwnerType());
      
      PageNode pageNode2 = nav2.getNode(name);
      assertNotNull(pageNode2);
      assertEquals(name, pageNode2.getName());
      assertEquals(name, pageNode2.getLabel());
      assertEquals(name, pageNode2.getUri());

      String newName = "NewMyPageNode";
      pageNode2.setName(newName);
      pageNode2.setUri(newName);
      pageNode2.setLabel(newName);
      storage_.save(nav2);
      
      //Get and compare
      PageNavigation nav3 = storage_.getPageNavigation(ownerType, ownerId);
      assertNotNull(nav3);
      assertEquals(ownerId, nav3.getOwnerId());
      assertEquals(ownerType, nav3.getOwnerType());
      
      PageNode pageNode3 = nav3.getNode(newName);
      assertNotNull(pageNode3);
      assertEquals(newName, pageNode3.getName());
      assertEquals(newName, pageNode3.getLabel());
      assertEquals(newName, pageNode3.getUri());
   }
   
   public void testChangingPortletThemeInPage() throws Exception {
      Page page;
      Application<?> app;
      
      page = storage_.getPage("portal::classic::homepage");
      app = (Application<?>)page.getChildren().get(0);
      assertEquals(1, page.getChildren().size());
      app.setTheme("Theme1");
      storage_.save(page);
      
      page = storage_.getPage("portal::classic::homepage");
      app = (Application<?>)page.getChildren().get(0);
      assertEquals("Theme1", app.getTheme());
      app.setTheme("Theme2");
      storage_.save(page);
      
      page = storage_.getPage("portal::classic::homepage");
      app = (Application<?>)page.getChildren().get(0);
      assertEquals("Theme2", app.getTheme());
   }

   public void testPageRemove() throws Exception
   {
      Page page = storage_.getPage("portal::test::test1");
      assertNotNull(page);

      //
      events.clear();
      storage_.remove(page);
      events.assertSize(1);

      //
      page = storage_.getPage(CLASSIC_TEST);
      assertNull(page);
   }

   public void testWindowMove1() throws Exception
   {
      Page page = storage_.getPage("portal::test::test4");
      Application<?> a1 = (Application<?>)page.getChildren().get(0);
      Container a2 = (Container)page.getChildren().get(1);
      Application<?> a3 = (Application<?>)a2.getChildren().get(0);
      Application<?> a4 = (Application<?>)a2.getChildren().remove(1);
      page.getChildren().add(1, a4);
      List<ModelChange> changes = storage_.save(page);

      //
      page = storage_.getPage("portal::test::test4");
      assertEquals(3, page.getChildren().size());
      Application<?> c1 = (Application<?>)page.getChildren().get(0);
      assertEquals(a1.getStorageId(), c1.getStorageId());
      Application<?> c2 = (Application<?>)page.getChildren().get(1);
      assertEquals(a4.getStorageId(), c2.getStorageId());
      Container c3 = (Container)page.getChildren().get(2);
      assertEquals(a2.getStorageId(), c3.getStorageId());
      assertEquals(1, c3.getChildren().size());
      Application<?> c4 = (Application<?>)c3.getChildren().get(0);
      assertEquals(a3.getStorageId(), c4.getStorageId());

      //
      assertEquals(6, changes.size());
      ModelChange.Update ch1 = (ModelChange.Update)changes.get(0);
      assertEquals(page.getStorageId(), ch1.getObject().getStorageId());
      ModelChange.Update ch2 = (ModelChange.Update)changes.get(1);
      assertEquals(a1.getStorageId(), ch2.getObject().getStorageId());
      ModelChange.Move ch3 = (ModelChange.Move)changes.get(2);
//      assertEquals(a2.getStorageId(), ch3.getSrcId());
//      assertEquals(page.getStorageId(), ch3.getDstId());
      assertEquals(a4.getStorageId(), ch3.getId());
      ModelChange.Update ch4 = (ModelChange.Update)changes.get(3);
      assertEquals(a4.getStorageId(), ch4.getObject().getStorageId());
      ModelChange.Update ch5 = (ModelChange.Update)changes.get(4);
      assertEquals(a2.getStorageId(), ch5.getObject().getStorageId());
      ModelChange.Update ch6 = (ModelChange.Update)changes.get(5);
      assertEquals(a3.getStorageId(), ch6.getObject().getStorageId());
   }

   public void testWindowMove2() throws Exception
   {
      Page page = storage_.getPage("portal::test::test3");
      Container container = new Container();
      Application application = (Application)page.getChildren().remove(0);
      container.getChildren().add(application);
      page.getChildren().add(container);
      
      //
      storage_.save(page);

      //
      Page page2 = storage_.getPage("portal::test::test3");

      //
      assertEquals(1, page2.getChildren().size());
      Container container2 = (Container)page2.getChildren().get(0);
      assertEquals(1, page2.getChildren().size());
      Application application2 = (Application)container2.getChildren().get(0);
      assertEquals(application2.getStorageId(), application.getStorageId());
   }

   // Need to make window move 3 unit test

   public void testCreateNavigation() throws Exception
   {
      PortalConfig portal = new PortalConfig();
      portal.setName("foo");
      portal.setLocale("en");
      portal.setAccessPermissions(new String[]{UserACL.EVERYONE});
      events.clear();
      storage_.create(portal);
      events.assertSize(1);

      //
      PageNavigation navigation = new PageNavigation();
      navigation.setOwnerId("foo");
      navigation.setOwnerType("portal");
      events.clear();
      storage_.create(navigation);
      events.assertSize(1);
   }

   public void testSaveNavigation() throws Exception
   {
      PageNavigation pageNavi = storage_.getPageNavigation("portal", "test");
      assertNotNull(pageNavi);

      //
      events.clear();
      storage_.save(pageNavi);
      events.assertSize(1);

      //
      PageNavigation newPageNavi = storage_.getPageNavigation(pageNavi.getOwnerType(), pageNavi.getOwnerId());
   }

   /**
    * Test that setting a page reference to null will actually remove the page reference from the PageNode
    * @throws Exception
    */
   public void testNullPageReferenceDeletes() throws Exception
   {
      // create portal
      PortalConfig portal = new PortalConfig();
      portal.setName("foo");
      portal.setLocale("en");
      portal.setAccessPermissions(new String[]{UserACL.EVERYONE});
      storage_.create(portal);
      
      // create page
      Page page = new Page();
      page.setOwnerType(PortalConfig.PORTAL_TYPE);
      page.setOwnerId("test");
      page.setName("foo");
      storage_.create(page);
      
      //create a page node and add page
      PageNode pageNode = new PageNode();
      pageNode.setName("testPage");
      pageNode.setPageReference(page.getPageId());
      pageNode.build();
      
      // create a new page navigation and add node
      PageNavigation navigation = new PageNavigation();
      navigation.setOwnerId("foo");
      navigation.setOwnerType("portal");
      navigation.addNode(pageNode);
      storage_.create(navigation);
      
      // get the page reference from the created page and check that it exists
      PageNavigation pageNavigationWithPageReference = storage_.getPageNavigation("portal", navigation.getOwnerId());
      assertNotNull("Expected page reference should not be null.", pageNavigationWithPageReference.getNodes().get(0).getPageReference());
      
      // set the page reference to null and save.
      ArrayList<PageNode> nodes = navigation.getNodes();
      nodes.get(0).setPageReference(null);
      navigation.setNodes(nodes);
      storage_.save(navigation);
      
      // check that setting the page reference to null actually removes the page reference
      PageNavigation pageNavigationWithoutPageReference = storage_.getPageNavigation("portal", navigation.getOwnerId());
      assertNull("Expected page reference should be null.", pageNavigationWithoutPageReference.getNodes().get(0).getPageReference());
   }
   
   public void testRemoveNavigation() throws Exception
   {
      PageNavigation navigation = storage_.getPageNavigation("portal", "test");
      assertNotNull(navigation);

      //
      events.clear();
      storage_.remove(navigation);
      events.assertSize(1);

      //
      navigation = storage_.getPageNavigation("portal", "test");
      assertNull(navigation);
   }

   public void testNavigationOrder() throws Exception
   {
      PortalConfig portal = new PortalConfig("portal");
      portal.setName("test_nav");
      storage_.create(portal);

      //
      PageNavigation nav = new PageNavigation();
      nav.setOwnerType("portal");
      nav.setOwnerId("test_nav");
      PageNode node1 = new PageNode();
      node1.setName("n1");
      PageNode node2 = new PageNode();
      node2.setName("n2");
      PageNode node3 = new PageNode();
      node3.setName("n3");
      nav.addNode(node1);
      nav.addNode(node2);
      nav.addNode(node3);

      //
      storage_.save(nav);

      //
      nav = storage_.getPageNavigation("portal", "test_nav");
      assertEquals(3, nav.getNodes().size());
      assertEquals("n1", nav.getNodes().get(0).getName());
      assertEquals("n2", nav.getNodes().get(1).getName());
      assertEquals("n3", nav.getNodes().get(2).getName());

      //
      nav.getNodes().add(0, nav.getNodes().remove(1));
      storage_.save(nav);

      //
      nav = storage_.getPageNavigation("portal", "test_nav");
      assertEquals(3, nav.getNodes().size());
      assertEquals("n2", nav.getNodes().get(0).getName());
      assertEquals("n1", nav.getNodes().get(1).getName());
      assertEquals("n3", nav.getNodes().get(2).getName());
   }

   public void testCreatePortletPreferences() throws Exception
   {
      ArrayList<Preference> prefs = new ArrayList<Preference>();
      for (int i = 0; i < 5; i++)
      {
         Preference pref = new Preference();
         pref.setName("name" + i);
         pref.addValue("value" + i);
         prefs.add(pref);
      }

      //
      PortletPreferences portletPreferences = new PortletPreferences();
      portletPreferences.setWindowId(testPortletPreferences);
      portletPreferences.setPreferences(prefs);

      //
      storage_.save(portletPreferences);

      //
      PortletPreferences portletPref = storage_.getPortletPreferences(testPortletPreferences);
      assertEquals(portletPref.getWindowId(), testPortletPreferences);
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
      assertSame(page.getStorageId(), c0.getObject().getStorageId());
      ModelChange.Update c1 = (ModelChange.Update)changes.get(1);
      assertSame(page.getChildren().get(0).getStorageId(), c1.getObject().getStorageId());
      ModelChange.Update c2 = (ModelChange.Update)changes.get(2);
      assertSame(page.getChildren().get(1).getStorageId(), c2.getObject().getStorageId());
      ModelChange.Update c3 = (ModelChange.Update)changes.get(3);
      assertSame(container.getChildren().get(0).getStorageId(), c3.getObject().getStorageId());
      ModelChange.Create c4 = (ModelChange.Create)changes.get(4);
      assertSame(container.getChildren().get(1).getStorageId(), c4.getObject().getStorageId());
      ModelChange.Update c5 = (ModelChange.Update)changes.get(5);
      assertSame(container.getChildren().get(2).getStorageId(), c5.getObject().getStorageId());

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
   }

   public void testDashboard() throws Exception
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
      dashboard.setAccessPermissions(new String[]{"perm1","perm2"});
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
      assertEquals(Arrays.asList("perm1","perm2"), Arrays.asList(dashboard.getAccessPermissions()));
      assertEquals(1, dashboard.getChildren().size());
      app = (Application<Portlet>)dashboard.getChildren().get(0);
      assertEquals("foo/bar", storage_.getId(app.getState()));
   }

   public void testDashboardLayout() throws Exception
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

   public void testDashboardSecurity() throws Exception
   {
      Page page = new Page();
      page.setPageId("portal::test::foo");
      Application<Portlet> app = Application.createPortletApplication();
      app.setAccessPermissions(new String[]{"perm1","perm2"});
      app.setState(new TransientApplicationState<Portlet>("dashboard/DashboardPortlet"));
      page.getChildren().add(app);
      storage_.save(page);
      page = storage_.getPage("portal::test::foo");
      String id = page.getChildren().get(0).getStorageId();

      // Load the dashboard itself
      Dashboard dashboard = storage_.loadDashboard(id);
      assertEquals(Arrays.asList("perm1","perm2"), Arrays.asList(dashboard.getAccessPermissions()));

      // Modify the dashboard permission
      dashboard.setAccessPermissions(new String[]{"perm3"});
      storage_.saveDashboard(dashboard);

      // Load application and check
      page = storage_.getPage("portal::test::foo");
      app = (Application<Portlet>)page.getChildren().get(0);
      assertEquals(Arrays.asList("perm3"), Arrays.asList(app.getAccessPermissions()));
   }

   public void testDashboardMoveRight() throws Exception
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

   public void testDashboardMoveLeft() throws Exception
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
   
   public void testRemoveAndFindPage() throws Exception
   {
      Page page = storage_.getPage(CLASSIC_HOME);
      assertNotNull(page);
      storage_.remove(page);

      // This will trigger a save
      Query<Page> query = new Query<Page>(null, null, null, null, Page.class);
      LazyPageList<Page> list = storage_.find(query);
      assertNotNull(list);

      // We check is now seen as removed
      assertNull(storage_.getPage(CLASSIC_HOME));
   }

   public void testGetAllPortalNames() throws Exception
   {
      final List<String> names = storage_.getAllPortalNames();

      // Create new portal
      storage_.create(new PortalConfig("portal", "testGetAllPortalNames"));

      // Test during tx we see the good names
      List<String> transientNames = storage_.getAllPortalNames();
      assertTrue(transientNames.containsAll(names));
      transientNames.removeAll(names);
      assertEquals(Collections.singletonList("testGetAllPortalNames"), transientNames);

      // Test we have not seen anything yet outside of tx
      final CountDownLatch addSync = new CountDownLatch(1);
      final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
      new Thread()
      {
         @Override
         public void run()
         {
            begin();
            try
            {
               List<String> isolatedNames = storage_.getAllPortalNames();
               assertEquals(new HashSet<String>(names), new HashSet<String>(isolatedNames));
            }
            catch (Throwable t)
            {
               error.set(t);
            }
            finally
            {
               addSync.countDown();
               end();
            }
         }
      }.start();

      //
      addSync.await();
      if (error.get() != null)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(error.get());
         throw afe;
      }

      // Now commit tx
      session.close(true);
      end(true);

      // We test we observe the change
      begin();
      session = mgr.openSession();
      List<String> afterNames = storage_.getAllPortalNames();
      assertTrue(afterNames.containsAll(names));
      afterNames.removeAll(names);
      assertEquals(Collections.singletonList("testGetAllPortalNames"), afterNames);

      // Then we remove the newly created portal
      storage_.remove(new PortalConfig("portal", "testGetAllPortalNames"));

      // Test we are syeing the transient change
      transientNames.clear();
      transientNames = storage_.getAllPortalNames();
      assertEquals(names, transientNames);

      // Test we have not seen anything yet outside of tx
      error.set(null);
      final CountDownLatch removeSync = new CountDownLatch(1);
      new Thread()
      {
         public void run()
         {
            begin();
            try
            {
               List<String> isolatedNames = storage_.getAllPortalNames();
               assertTrue(isolatedNames.containsAll(names));
               isolatedNames.removeAll(names);
               assertEquals(Collections.singletonList("testGetAllPortalNames"), isolatedNames);
            }
            catch (Throwable t)
            {
               error.set(t);
            }
            finally
            {
               removeSync.countDown();
               end();
            }
         }
      }.start();

      //
      removeSync.await();
      if (error.get() != null)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(error.get());
         throw afe;
      }

      //
      session.close(true);
      end(true);

      // Now test it is still removed
      begin();
      session = mgr.openSession();
      afterNames = storage_.getAllPortalNames();
      assertEquals(new HashSet<String>(names), new HashSet<String>(afterNames));
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
