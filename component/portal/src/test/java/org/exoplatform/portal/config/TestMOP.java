/*
 * Copyright (C) 2011 eXo Platform SAS.
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
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.link.Link;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov
 * 13, 2007
 */
public class TestMOP extends AbstractConfigTest
{

   /** . */
   private UserPortalConfigService portalConfigService;

   /** . */
   private DataStorage storage;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   /** . */
   private NavigationService navService;

   public TestMOP(String name)
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
      navService = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      session = mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }

   public void testLoadLegacyGroupWithNormalizedName() throws Exception
   {
      SiteKey key = SiteKey.group("/test/legacy");
      NavigationContext nav = navService.loadNavigation(key);
      assertNotNull(nav);
      NodeContext<?> root = navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      NodeContext<?> node = root.get(0);
      assertEquals("group::/test/legacy::register", node.getState().getPageRef());

      Page page = storage.getPage("group::/test/legacy::register");
      assertNotNull(page);
      assertEquals("group::/test/legacy::register", page.getPageId());
      assertEquals("/test/legacy", page.getOwnerId());
      Application app = (Application)page.getChildren().get(0);
      //    assertEquals("group#/test/legacy:/web/IFramePortlet/blog", app.getInstanceState().getWeakReference());

      PortletPreferences prefs = storage.getPortletPreferences("group#/test/legacy:/web/IFramePortlet/blog");
      assertNotNull(prefs);
      assertEquals("group#/test/legacy:/web/IFramePortlet/blog", prefs.getWindowId());
   }

   public void testLoadGroupWithNormalizedName() throws Exception
   {
      SiteKey key = SiteKey.group("/test/normalized");
      NavigationContext nav = navService.loadNavigation(key);
      assertNotNull(nav);
      NodeContext<?> root = navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      NodeContext<?> node = root.get(0);
      assertEquals("group::/test/normalized::register", node.getState().getPageRef());

      Page page = storage.getPage("group::/test/normalized::register");
      assertNotNull(page);
      assertEquals("group::/test/normalized::register", page.getPageId());
      assertEquals("/test/normalized", page.getOwnerId());
      Application app = (Application)page.getChildren().get(0);
      //    assertEquals("group#/test/normalized:/exoadmin/AccountPortlet/Account", app.getInstanceState().getWeakReference());

      PortletPreferences prefs =
         storage.getPortletPreferences("group#/test/normalized:/exoadmin/AccountPortlet/Account");
      assertNotNull(prefs);
      assertEquals("group#/test/normalized:/exoadmin/AccountPortlet/Account", prefs.getWindowId());
   }

   public void testLoadNavigation() throws Exception
   {
      SiteKey key = SiteKey.portal("test");
      NavigationContext nav = navService.loadNavigation(key);
      assertNotNull(nav);

      //
      assertEquals(1, (int) nav.getState().getPriority());

      //
      NodeContext<?> root = navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      assertEquals(5, root.getNodeCount());

      //
      NodeContext<?> nodeNavigation = root.get(0);
      assertEquals(0, nodeNavigation.getNodeCount());
      assertEquals("node_name", nodeNavigation.getName());
      assertEquals("node_label", nodeNavigation.getState().getLabel());
      assertEquals("node_icon", nodeNavigation.getState().getIcon());
      GregorianCalendar start = new GregorianCalendar(2000, 2, 21, 1, 33, 0);
      start.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(start.getTime().getTime(), nodeNavigation.getState().getStartPublicationTime());
      GregorianCalendar end = new GregorianCalendar(2009, 2, 21, 1, 33, 0);
      end.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(end.getTime().getTime(), nodeNavigation.getState().getEndPublicationTime());
      assertEquals(Visibility.TEMPORAL, nodeNavigation.getState().getVisibility());
   }

   public void testLoadPortal() throws Exception
   {
      PortalConfig portal = storage.getPortalConfig("test");
      assertNotNull(portal);

      assertEquals("test", portal.getName());
      assertEquals("en", portal.getLocale());
      assertTrue(Arrays.equals(new String[]{"test_access_permissions"}, portal.getAccessPermissions()));
      assertEquals("test_edit_permission", portal.getEditPermission());
      assertEquals("test_skin", portal.getSkin());
      assertEquals("test_prop_value", portal.getProperty("prop_key"));
      assertNull(portal.getLabel());
      assertNull(portal.getDescription());
   }

   public void testLoadPageWithoutPageId() throws Exception
   {
      Page page = storage.getPage("portal::test::test2");
      assertNotNull(page);
      assertEquals("portal::test::test2", page.getPageId());
      assertEquals("test", page.getOwnerId());
      assertEquals("portal", page.getOwnerType());
      assertEquals("test2", page.getName());
   }

   public void testLoadPage() throws Exception
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
         new HashSet<String>(Arrays.asList("group::/test/legacy::register",
            "group::/test/normalized::register"));
      assertEquals(expectedIds, ids);
   }
*/

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

   public void testLoadAnonymousPreferencesSavePage() throws Exception
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

   public void testLoadAnonymousPreference() throws Exception
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

   public void testLoadSitePreferences() throws Exception
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

   public void testSaveNavigation() throws Exception
   {
      Site portal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
      assertNotNull(portal);

      //
      Navigation rootNavigation = portal.getRootNavigation();
      assertNotNull(rootNavigation);

      //
      Navigation defaultNav = rootNavigation.getChild("default");

      //
      Attributes defaultAttrs = defaultNav.getAttributes();
      assertEquals(1, (int)defaultAttrs.getInteger("priority"));

      //
      Collection<? extends Navigation> childrenNavigations = defaultNav.getChildren();
      assertNotNull(childrenNavigations);
      assertEquals(5, childrenNavigations.size());
      Iterator<? extends Navigation> i = childrenNavigations.iterator();

      //
      assertTrue(i.hasNext());
      Navigation nodeNavigation = i.next();
      assertNotNull(nodeNavigation);
      assertEquals(0, nodeNavigation.getChildren().size());
      assertEquals("node_name", nodeNavigation.getName());
      Described nodeDescribed = nodeNavigation.adapt(Described.class);
      assertEquals("node_label", nodeDescribed.getName());
      Attributes nodeAttrs = nodeNavigation.getAttributes();
      assertEquals("node_icon", nodeAttrs.getString("icon"));

      //
      assertTrue(nodeNavigation.isAdapted(Visible.class));
      Visible visible = nodeNavigation.adapt(Visible.class);
      GregorianCalendar start = new GregorianCalendar(2000, 2, 21, 1, 33, 0);
      start.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(start.getTime(), visible.getStartPublicationDate());
      GregorianCalendar end = new GregorianCalendar(2009, 2, 21, 1, 33, 0);
      end.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(end.getTime(), visible.getEndPublicationDate());
      assertEquals(Visibility.TEMPORAL, visible.getVisibility());

      //
      Link link = nodeNavigation.getLink();
      assertNotNull(link);
   }

   public void testSavePortal() throws Exception
   {
      Site portal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
      assertNotNull(portal);

      //
      assertTrue(portal.isAdapted(ProtectedResource.class));
      ProtectedResource pr = portal.adapt(ProtectedResource.class);
      assertEquals(Arrays.asList("test_access_permissions"), pr.getAccessPermissions());
      assertEquals("test_edit_permission", pr.getEditPermission());

      //
      assertEquals("test", portal.getName());
      Attributes attrs = portal.getAttributes();
      assertEquals("en", attrs.getString("locale"));
      assertEquals("test_skin", attrs.getString("skin"));
      assertEquals("test_prop_value", attrs.getString("prop_key"));

      //
      org.gatein.mop.api.workspace.Page layout = portal.getRootNavigation().getTemplatized().getTemplate();
      assertNotNull(layout);
      assertSame(portal.getRootPage().getChild("templates").getChild("default"), layout);
   }

   public void testSavePageWithoutPageId() throws Exception
   {
      Site testPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
      org.gatein.mop.api.workspace.Page testRootPage = testPortal.getRootPage();
      org.gatein.mop.api.workspace.Page pages = testRootPage.getChild("pages");
      org.gatein.mop.api.workspace.Page testPage = pages.getChild("test2");
      assertNotNull(testPage);
   }

   public void testSavePage() throws Exception
   {
      Site testPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
      org.gatein.mop.api.workspace.Page testRootPage = testPortal.getRootPage();
      org.gatein.mop.api.workspace.Page pages = testRootPage.getChild("pages");
      org.gatein.mop.api.workspace.Page testPage = pages.getChild("test1");
      assertNotNull(testPage);

      //
      assertTrue(testPage.isAdapted(ProtectedResource.class));
      ProtectedResource pr = testPage.adapt(ProtectedResource.class);
      assertEquals(Arrays.asList("test_access_permissions"), pr.getAccessPermissions());
      assertEquals("test_edit_permission", pr.getEditPermission());

      //
      Described testPageDescribed = testPage.adapt(Described.class);
      assertEquals("test_title", testPageDescribed.getName());
      assertEquals(null, testPageDescribed.getDescription());

      //
      Attributes testPageAttrs = testPage.getAttributes();
      assertEquals("test_factory_id", testPageAttrs.getString("factory-id"));
      assertEquals(true, (boolean)testPageAttrs.getBoolean("show-max-window"));

      //
      UIContainer c = testPage.getRootComponent();
      assertNotNull(c);
      assertEquals(2, c.getComponents().size());
      Iterator<? extends UIComponent> it = c.getComponents().iterator();

      //
      UIContainer container1 = (UIContainer)it.next();
      assertTrue(container1.isAdapted(ProtectedResource.class));
      ProtectedResource container1PR = container1.adapt(ProtectedResource.class);
      assertEquals(Collections.singletonList("container_1_access_permissions"), container1PR.getAccessPermissions());
      Described container1Described = container1.adapt(Described.class);
      assertEquals("container_1_title", container1Described.getName());
      assertEquals("container_1_description", container1Described.getDescription());
      Attributes container1Attrs = container1.getAttributes();
      assertEquals("container_1", container1Attrs.getString("name"));
      assertEquals("container_1_icon", container1Attrs.getString("icon"));
      assertEquals("container_1_template", container1Attrs.getString("template"));
      assertEquals("container_1_factory_id", container1Attrs.getString("factory-id"));
      assertEquals("container_1_width", container1Attrs.getString("width"));
      assertEquals("container_1_height", container1Attrs.getString("height"));

      //
      UIWindow application1 = (UIWindow)it.next();
      assertTrue(application1.isAdapted(ProtectedResource.class));
      ProtectedResource application1PR = application1.adapt(ProtectedResource.class);
      assertEquals(Collections.singletonList("application_1_access_permissions"), application1PR.getAccessPermissions());
      Described application1Described = application1.adapt(Described.class);
      assertEquals("application_1_title", application1Described.getName());
      assertEquals("application_1_description", application1Described.getDescription());
      Attributes application1Attrs = application1.getAttributes();
      assertEquals("application_1_theme", application1Attrs.getString("theme"));
      assertEquals(true, (boolean)application1Attrs.getBoolean("showinfobar"));
      assertEquals(true, (boolean)application1Attrs.getBoolean("showmode"));
      assertEquals(true, (boolean)application1Attrs.getBoolean("showwindowstate"));
      assertEquals("application_1_icon", application1Attrs.getString("icon"));
      assertEquals("application_1_width", application1Attrs.getString("width"));
      assertEquals("application_1_height", application1Attrs.getString("height"));
      assertEquals("application_1_prop_value", application1Attrs.getString("prop_key"));

      //
      Customization<?> customization = application1.getCustomization();
      assertNotNull(customization);
      assertEquals("application/portlet", customization.getType().getMimeType());
      assertEquals("web/BannerPortlet", customization.getContentId());
      //    assertEquals("banner", customization.getName());
   }
}