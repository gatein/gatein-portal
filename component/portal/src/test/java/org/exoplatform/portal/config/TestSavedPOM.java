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
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.test.BasicTestCase;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.link.Link;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;

import java.util.*;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov
 * 13, 2007
 */
public class TestSavedPOM extends AbstractPortalTest
{

   /** . */
   private UserPortalConfigService portalConfigService;

   /** . */
   private DataStorage storage;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   public TestSavedPOM(String name)
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

   public void testNavigation() throws Exception
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
      assertEquals("navigation_creator", defaultAttrs.getString("creator"));
      assertEquals("navigation_modifier", defaultAttrs.getString("modifier"));

      //
      Collection<? extends Navigation> childrenNavigations = defaultNav.getChildren();
      assertNotNull(childrenNavigations);
      assertEquals(2, childrenNavigations.size());
      Iterator<? extends Navigation> i = childrenNavigations.iterator();

      //
      assertTrue(i.hasNext());
      Navigation nodeNavigation = i.next();
      assertNotNull(nodeNavigation);
      assertEquals(0, nodeNavigation.getChildren().size());
      assertEquals("node_name", nodeNavigation.getName());
      Attributes nodeAttrs = nodeNavigation.getAttributes();
      assertEquals("node_uri", nodeAttrs.getString("uri"));
      assertEquals("node_label", nodeAttrs.getString("label"));
      assertEquals("node_icon", nodeAttrs.getString("icon"));
      GregorianCalendar start = new GregorianCalendar(2000, 2, 21, 1, 33, 0);
      start.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(start.getTime(), nodeAttrs.getDate("start-publication-date"));
      GregorianCalendar end = new GregorianCalendar(2009, 2, 21, 1, 33, 0);
      end.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals(end.getTime(), nodeAttrs.getDate("end-publication-date"));
      assertEquals(true, (boolean)nodeAttrs.getBoolean("show-publication-date"));
      assertEquals(true, (boolean)nodeAttrs.getBoolean("visible"));

      //
      Link link = nodeNavigation.getLink();
      assertNotNull(link);
   }

   public void testPortal() throws Exception
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
      assertEquals("test_title", attrs.getString("title"));
      assertEquals("test_creator", attrs.getString("creator"));
      assertEquals("test_modifier", attrs.getString("modifier"));
      assertEquals("test_prop_value", attrs.getString("prop_key"));

      //
      Page layout = portal.getRootNavigation().getTemplate();
      assertNotNull(layout);
      assertSame(portal.getRootPage().getChild("templates").getChild("default"), layout);
   }

   public void testPageWithoutPageId() throws Exception
   {
      Site testPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
      Page testRootPage = testPortal.getRootPage();
      Page pages = testRootPage.getChild("pages");
      Page testPage = pages.getChild("test2");
      assertNotNull(testPage);
   }

   public void testPage() throws Exception
   {
      Site testPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
      Page testRootPage = testPortal.getRootPage();
      Page pages = testRootPage.getChild("pages");
      Page testPage = pages.getChild("test1");
      assertNotNull(testPage);

      //
      assertTrue(testPage.isAdapted(ProtectedResource.class));
      ProtectedResource pr = testPage.adapt(ProtectedResource.class);
      assertEquals(Arrays.asList("test_access_permissions"), pr.getAccessPermissions());
      assertEquals("test_edit_permission", pr.getEditPermission());

      //
      Attributes testPageAttrs = testPage.getAttributes();
      assertEquals("test_title", testPageAttrs.getString("title"));
      assertEquals("test_factory_id", testPageAttrs.getString("factory-id"));
      assertEquals(true, (boolean)testPageAttrs.getBoolean("show-max-window"));
      assertEquals("test_creator", testPageAttrs.getString("creator"));
      assertEquals("test_modifier", testPageAttrs.getString("modifier"));

      //
      UIContainer c = testPage.getRootComponent();
      assertNotNull(c);
      assertEquals(2, c.size());
      Iterator<? extends UIComponent> it = c.iterator();

      //
      UIContainer container1 = (UIContainer)it.next();
      assertTrue(container1.isAdapted(ProtectedResource.class));
      ProtectedResource container1PR = container1.adapt(ProtectedResource.class);
      assertEquals(Collections.singletonList("container_1_access_permissions"), container1PR.getAccessPermissions());
      Attributes container1Attrs = container1.getAttributes();
      assertEquals("container_1", container1Attrs.getString("name"));
      assertEquals("container_1_title", container1Attrs.getString("title"));
      assertEquals("container_1_icon", container1Attrs.getString("icon"));
      assertEquals("container_1_template", container1Attrs.getString("template"));
      assertEquals("container_1_factory_id", container1Attrs.getString("factory-id"));
      assertEquals("container_1_decorator", container1Attrs.getString("decorator"));
      assertEquals("container_1_description", container1Attrs.getString("description"));
      assertEquals("container_1_width", container1Attrs.getString("width"));
      assertEquals("container_1_height", container1Attrs.getString("height"));

      //
      UIWindow application1 = (UIWindow)it.next();
      assertTrue(application1.isAdapted(ProtectedResource.class));
      ProtectedResource application1PR = application1.adapt(ProtectedResource.class);
      assertEquals(Collections.singletonList("application_1_access_permissions"), application1PR.getAccessPermissions());
      Attributes application1Attrs = application1.getAttributes();
      assertEquals("application_1_theme", application1Attrs.getString("theme"));
      assertEquals("application_1_title", application1Attrs.getString("title"));
      assertEquals(true, (boolean)application1Attrs.getBoolean("show-info-bar"));
      assertEquals(true, (boolean)application1Attrs.getBoolean("show-state"));
      assertEquals(true, (boolean)application1Attrs.getBoolean("show-mode"));
      assertEquals("application_1_description", application1Attrs.getString("description"));
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