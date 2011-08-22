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

import junit.framework.AssertionFailedError;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageBody;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.pom.config.POMDataStorage;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.config.cache.DataCache;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.gatein.common.util.Tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestUserPortalConfigService extends AbstractConfigTest
{

   /** . */
   private UserPortalConfigService userPortalConfigSer_;

   /** . */
   private OrganizationService orgService_;

   /** . */
   private DataStorage storage_;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private Authenticator authenticator;

   /** . */
   private ListenerService listenerService;

   /** . */
   private LinkedList<Event> events;

   /** . */
   private boolean registered;

   /** . */
   private POMDataStorage mopStorage;

   public TestUserPortalConfigService(String name)
   {
      super(name);

      //
      registered = false;
   }

   @Override
   protected void setUp() throws Exception
   {
      Listener listener = new Listener()
      {
         @Override
         public void onEvent(Event event) throws Exception
         {
            events.add(event);
         }
      };

      PortalContainer container = getContainer();
      userPortalConfigSer_ =
         (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      orgService_ = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      authenticator = (Authenticator)container.getComponentInstanceOfType(Authenticator.class);
      listenerService = (ListenerService)container.getComponentInstanceOfType(ListenerService.class);
      events = new LinkedList<Event>();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mopStorage = (POMDataStorage)container.getComponentInstanceOfType(POMDataStorage.class);

      // Register only once for all unit tests
      if (!registered)
      {
         // I'm using this due to crappy design of
         // org.exoplatform.services.listener.ListenerService
         listenerService.addListener(DataStorage.PAGE_CREATED, listener);
         listenerService.addListener(DataStorage.PAGE_REMOVED, listener);
         listenerService.addListener(DataStorage.PAGE_UPDATED, listener);
         listenerService.addListener(EventType.NAVIGATION_CREATED, listener);
         listenerService.addListener(EventType.NAVIGATION_DESTROYED, listener);
         listenerService.addListener(EventType.NAVIGATION_UPDATED, listener);
      }
   }

   private static Map<String, UserNavigation> toMap(UserPortal cfg)
   {
      return toMap(cfg.getNavigations());
   }

   private static Map<String, UserNavigation> toMap(List<UserNavigation> navigations)
   {
      Map<String, UserNavigation> map = new HashMap<String, UserNavigation>();
      for (UserNavigation nav : navigations)
      {
         map.put(nav.getKey().getType().getName() + "::" + nav.getKey().getName(), nav);
      }
      return map;
   }

   public void testUpdatePortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", "root");
            assertNotNull(userPortalCfg);
            PortalConfig portalCfg = userPortalCfg.getPortalConfig();
            assertNotNull(portalCfg);
            assertEquals(PortalConfig.PORTAL_TYPE, portalCfg.getType());
            assertEquals("classic", portalCfg.getName());
            assertEquals("en", portalCfg.getLocale());
            portalCfg.setLocale("fr");

            storage_.save(portalCfg);

            userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", "root");
            portalCfg = userPortalCfg.getPortalConfig();
            assertEquals("fr", portalCfg.getLocale());
         }
      }.execute("root");
   }

   public void testRootGetUserPortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", "root");
            assertNotNull(userPortalCfg);
            PortalConfig portalCfg = userPortalCfg.getPortalConfig();
            assertNotNull(portalCfg);
            assertEquals(PortalConfig.PORTAL_TYPE, portalCfg.getType());
            assertEquals("classic", portalCfg.getName());
            UserPortal userPortal = userPortalCfg.getUserPortal();
            assertNotNull(userPortal.getNavigations());
            Map<String, UserNavigation> navigations = toMap(userPortal);
            assertEquals("expected to have 5 navigations instead of " + navigations, 5, navigations.size());
            assertTrue(navigations.containsKey("portal::classic"));
            assertTrue(navigations.containsKey("group::/platform/administrators"));
            assertTrue(navigations.containsKey("group::/platform/users"));
            assertTrue(navigations.containsKey("group::/organization/management/executive-board"));
            assertTrue(navigations.containsKey("user::root"));
         }
      }.execute("root");
   }

   public void testJohnGetUserPortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", "john");
            assertNotNull(userPortalCfg);
            PortalConfig portalCfg = userPortalCfg.getPortalConfig();
            assertNotNull(portalCfg);
            assertEquals(PortalConfig.PORTAL_TYPE, portalCfg.getType());
            assertEquals("classic", portalCfg.getName());
            UserPortal userPortal = userPortalCfg.getUserPortal();
            assertNotNull(userPortal.getNavigations());
            Map<String, UserNavigation> navigations = toMap(userPortal);
            assertEquals("expected to have 5 navigations instead of " + navigations, 5, navigations.size());
            assertTrue(navigations.containsKey("portal::classic"));
            assertTrue(navigations.containsKey("group::/platform/administrators"));
            assertTrue(navigations.containsKey("group::/platform/users"));
            assertTrue(navigations.containsKey("user::john"));
         }
      }.execute("john");
   }

   public void testMaryGetUserPortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", "mary");
            assertNotNull(userPortalCfg);
            PortalConfig portalCfg = userPortalCfg.getPortalConfig();
            assertNotNull(portalCfg);
            assertEquals(PortalConfig.PORTAL_TYPE, portalCfg.getType());
            assertEquals("classic", portalCfg.getName());
            UserPortal userPortal = userPortalCfg.getUserPortal();
            assertNotNull(userPortal.getNavigations());
            Map<String, UserNavigation> navigations = toMap(userPortal);
            assertEquals(3, navigations.size());
            assertTrue(navigations.containsKey("portal::classic"));
            assertTrue(navigations.containsKey("group::/platform/users"));
            assertTrue(navigations.containsKey("user::mary"));
         }
      }.execute("mary");
   }

   public void testGuestGetUserPortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", null);
            assertNotNull(userPortalCfg);
            PortalConfig portalCfg = userPortalCfg.getPortalConfig();
            assertNotNull(portalCfg);
            assertEquals(PortalConfig.PORTAL_TYPE, portalCfg.getType());
            assertEquals("classic", portalCfg.getName());
            UserPortal userPortal = userPortalCfg.getUserPortal();
            assertNotNull(userPortal.getNavigations());
            Map<String, UserNavigation> navigations = toMap(userPortal);
            assertEquals("" + navigations, 1, navigations.size());
            assertTrue(navigations.containsKey("portal::classic"));
         }
      }.execute(null);
   }

   public void testNavigationOrder()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", "root");
            UserPortal userPortal = userPortalCfg.getUserPortal();
            List<UserNavigation> navigations = userPortal.getNavigations();
            assertEquals("expected to have 5 navigations instead of " + navigations, 5, navigations.size());
            assertEquals("classic", navigations.get(0).getKey().getName()); // 1
            assertEquals("/platform/administrators", navigations.get(1).getKey().getName()); // 2
            assertEquals("root", navigations.get(2).getKey().getName()); // 3
            assertEquals("/organization/management/executive-board", navigations.get(3).getKey().getName()); // 5
            assertEquals("/platform/users", navigations.get(4).getKey().getName()); // 8
         }
      }.execute("root");
   }

   public void testCreateUserPortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            userPortalConfigSer_.createUserPortalConfig(PortalConfig.PORTAL_TYPE, "jazz", "test");
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("jazz", "root");
            assertNotNull(userPortalCfg);
            PortalConfig portalCfg = userPortalCfg.getPortalConfig();
            assertNotNull(portalCfg);
            assertEquals(PortalConfig.PORTAL_TYPE, portalCfg.getType());
            assertEquals("jazz", portalCfg.getName());
            UserPortal userPortal = userPortalCfg.getUserPortal();
            assertNotNull(userPortal.getNavigations());
            Map<String, UserNavigation> navigations = toMap(userPortal);
            assertEquals("expected to have 5 navigations instead of " + navigations, 5, navigations.size());
            assertTrue(navigations.containsKey("portal::jazz"));
            assertTrue(navigations.containsKey("group::/platform/administrators"));
            assertTrue(navigations.containsKey("group::/organization/management/executive-board"));
            assertTrue(navigations.containsKey("group::/platform/users"));
            assertTrue(navigations.containsKey("user::root"));

            queryPage();
         }

         private void queryPage()
         {
            Query<Page> query = new Query<Page>("portal", null, null, null, Page.class);
            try
            {
               storage_.find(query);
            }
            catch (Exception ex)
            {
               assertTrue("Exception while querying pages with new portal", false);
            }
         }

      }.execute("root");
   }

   public void testRemoveUserPortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            userPortalConfigSer_.createUserPortalConfig(PortalConfig.PORTAL_TYPE, "jazz", "test");
            UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("jazz", "root");
            assertNotNull(userPortalCfg);
            saveMOP();
            userPortalConfigSer_.removeUserPortalConfig("jazz");
            saveMOP();
            assertNull(userPortalConfigSer_.getUserPortalConfig("jazz", "root"));
         }
      }.execute("root");
   }

   public void testRootGetMakableNavigations()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Set<String> navigations = new HashSet<String>(userPortalConfigSer_.getMakableNavigations("root", false));
            Set<String> expectedNavigations = Tools.toSet(
               "/platform/users",
               "/platform",
               "/platform/guests",
               "/platform/administrators",
               "/organization",
               "/organization/management",
               "/organization/management/executive-board");
            assertEquals(expectedNavigations, navigations);
         }
      }.execute(null);
   }

   public void testJohnGetMakableNavigations()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Set<String> navigations = new HashSet<String>(userPortalConfigSer_.getMakableNavigations("john", false));
            Set<String> expectedNavigations = Tools.toSet("/organization/management/executive-board");
            assertEquals(expectedNavigations, navigations);
         }
      }.execute(null);
   }

   public void testMaryGetMakableNavigations()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Set<String> navigations = new HashSet<String>(userPortalConfigSer_.getMakableNavigations("mary", false));
            Set<String> expectedNavigations = Collections.emptySet();
            assertEquals(expectedNavigations, navigations);
         }
      }.execute(null);
   }

   public void testRootGetPage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            assertEquals("group::/platform/administrators::newAccount", userPortalConfigSer_.getPage(
               "group::/platform/administrators::newAccount", null).getPageId());
            assertEquals("group::/organization/management/executive-board::newStaff", userPortalConfigSer_.getPage(
               "group::/organization/management/executive-board::newStaff", null).getPageId());
         }
      }.execute("root");
   }

   public void testJohnGetPage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            assertEquals(null, userPortalConfigSer_.getPage("group::/platform/administrators::newAccount", null));
            assertEquals("group::/organization/management/executive-board::newStaff", userPortalConfigSer_.getPage(
               "group::/organization/management/executive-board::newStaff", null).getPageId());
         }
      }.execute("john");
   }

   public void testMaryGetPage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            assertEquals(null, userPortalConfigSer_.getPage("group::/platform/administrators::newAccount", null));
            assertEquals(null, userPortalConfigSer_.getPage(
               "group::/organization/management/executive-board::newStaff", null));
         }
      }.execute("mary");
   }

   public void testAnonymousGetPage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            assertEquals(null, userPortalConfigSer_.getPage("group::/platform/administrators::newAccount", null));
            assertEquals(null, userPortalConfigSer_.getPage(
               "group::/organization/management/executive-board::newStaff", null));
         }
      }.execute(null);
   }

   public void testRemovePage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Page page = new Page();
            page.setOwnerType("group");
            page.setOwnerId("/platform/administrators");
            page.setName("newAccount");
            assertTrue(events.isEmpty());
            storage_.remove(page);
            assertEquals(1, events.size());
            Event event = events.removeFirst();
            assertEquals(DataStorage.PAGE_REMOVED, event.getEventName());
            Page p = ((Page)event.getData());
            assertEquals("group", p.getOwnerType());
            assertEquals("/platform/administrators", p.getOwnerId());
            assertEquals("newAccount", p.getName());
            assertEquals(null, userPortalConfigSer_.getPage("group::/platform/administrators::newAccount"));
         }
      }.execute(null);
   }

   public void testCreatePage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Page page = new Page();
            page.setOwnerType("group");
            page.setOwnerId("/platform/administrators");
            page.setName("whatever");
            assertTrue(events.isEmpty());
            storage_.create(page);
            assertEquals(1, events.size());
            Event event = events.removeFirst();
            assertEquals(DataStorage.PAGE_CREATED, event.getEventName());
            Page p = ((Page)event.getData());
            assertEquals("group", p.getOwnerType());
            assertEquals("/platform/administrators", p.getOwnerId());
            assertEquals("whatever", p.getName());
            assertNotNull(userPortalConfigSer_.getPage("group::/platform/administrators::whatever"));
         }
      }.execute(null);
   }

   // Julien : see who added that and find out is test is relevant or not

   public void testClonePage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Page page = new Page();
            page.setOwnerType("group");
            page.setOwnerId("/platform/administrators");
            page.setName("whatever");
            page.setTitle("testTitle");
            storage_.create(page);

            String newName = "newPage";
            Page newPage = storage_.clonePage(page.getPageId(), page.getOwnerType(), page.getOwnerId(), newName);
            
            assertEquals(newName, newPage.getName());
            assertEquals(page.getTitle(), newPage.getTitle());
         }
      }.execute(null);
   }

   public void testUpdatePage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Page page = new Page();
            page.setOwnerType("group");
            page.setOwnerId("/platform/administrators");
            page.setName("newAccount");
            page.setShowMaxWindow(true);
            page.setTitle("newAccount title");
            assertTrue(events.isEmpty());
            storage_.create(page);
            assertEquals(1, events.size());
            Event event = events.removeFirst();
            assertEquals(DataStorage.PAGE_CREATED, event.getEventName());
            Page p = ((Page)event.getData());
            assertEquals("group", p.getOwnerType());
            assertEquals("/platform/administrators", p.getOwnerId());
            assertEquals("newAccount", p.getName());
            assertEquals("newAccount title", p.getTitle());
            assertTrue(p.isShowMaxWindow());

            p.setShowMaxWindow(false);
            storage_.save(p);
            p = userPortalConfigSer_.getPage("group::/platform/administrators::newAccount");
            assertFalse(p.isShowMaxWindow());
            p.setShowMaxWindow(true);
            storage_.save(p);
            p = userPortalConfigSer_.getPage("group::/platform/administrators::newAccount");
            assertTrue(p.isShowMaxWindow());
            p.setShowMaxWindow(false);
            storage_.save(p);
            p = userPortalConfigSer_.getPage("group::/platform/administrators::newAccount");
            assertFalse(p.isShowMaxWindow());
            p.setShowMaxWindow(true);
            storage_.save(p);
            p = userPortalConfigSer_.getPage("group::/platform/administrators::newAccount");
            assertTrue(p.isShowMaxWindow());

            Page p2 = userPortalConfigSer_.getPage("group::/platform/administrators::newAccount");
            assertEquals("group", p2.getOwnerType());
            assertEquals("/platform/administrators", p2.getOwnerId());
            assertEquals("newAccount", p2.getName());
            //            assertFalse(p2.isShowMaxWindow());
            p2.setTitle("newAccount title 1");
            p2.setShowMaxWindow(true);
            storage_.save(p2);

            Page p3 = userPortalConfigSer_.getPage("group::/platform/administrators::newAccount");
            assertEquals("newAccount title 1", p3.getTitle());
            //            assertTrue(p3.isShowMaxWindow());

         }
      }.execute(null);
   }
   public void testRenewPage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Page clone = storage_.clonePage("portal::test::test4", "portal", "test", "test5");
            assertNotNull(clone);
            assertEquals("portal", clone.getOwnerType());
            assertEquals("test", clone.getOwnerId());
            assertEquals("test5", clone.getName());

            //
            Application<Portlet> app = (Application<Portlet>)clone.getChildren().get(0);
            Portlet prefs2 = storage_.load(app.getState(), ApplicationType.PORTLET);
            assertEquals(new PortletBuilder().add("template",
               "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl").build(), prefs2);

            // Update prefs of original page
            PortletPreferences prefs = new PortletPreferences();
            prefs.setWindowId("portal#test:/web/BannerPortlet/banner");
            storage_.save(prefs);

            //
            prefs2 = storage_.load(app.getState(), ApplicationType.PORTLET);
            assertEquals(new PortletBuilder().add("template",
               "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl").build(), prefs2);
         }
      }.execute(null);
   }

   public void testCreateFromTemplate()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            Page clone = userPortalConfigSer_.createPageTemplate("dashboard", "portal", "test");
            assertNotNull(clone);
            assertEquals("portal", clone.getOwnerType());
            assertEquals("test", clone.getOwnerId());

            //
            assertEquals(1, clone.getChildren().size());

            //
            Application<Portlet> app = (Application<Portlet>)clone.getChildren().get(0);
            assertEquals("Dashboard", app.getTitle());
            assertNotNull(app.getState());
            assertEquals("dashboard/DashboardPortlet", storage_.getId(app.getState()));
            // assertEquals("portal", app.getInstanceState().getOwnerType());
            // assertEquals("test", app.getInstanceState().getOwnerId());
            Portlet prefs2 = storage_.load(app.getState(), ApplicationType.PORTLET);
            assertNull(prefs2);
         }
      }.execute(null);
   }

   public void testOverwriteUserLayout()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            mgr.clearCache();

            PortalConfig cfg = storage_.getPortalConfig(PortalConfig.USER_TYPE, "overwritelayout");
            assertNotNull(cfg);

            Container container = cfg.getPortalLayout();
            assertNotNull(container);
            assertEquals(2, container.getChildren().size());
            assertTrue(container.getChildren().get(0) instanceof PageBody);
            assertTrue(((Application)container.getChildren().get(1)).getType() == ApplicationType.PORTLET);
            Application<Portlet> pa = (Application<Portlet>)container.getChildren().get(1);
            ApplicationState<Portlet> state = pa.getState();
            assertEquals("overwrite_application_ref/overwrite_portlet_ref", storage_.getId(state));
         }
      }.execute(null);
   }

   public void testUserTemplate()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            assertNull(storage_.getPortalConfig(PortalConfig.USER_TYPE, "user"));
            assertNull(storage_.getPortalConfig(PortalConfig.USER_TYPE, "julien"));

            //
            UserHandler userHandler = orgService_.getUserHandler();
            User user = userHandler.createUserInstance("julien");
            user.setPassword("default");
            user.setFirstName("default");
            user.setLastName("default");
            user.setEmail("exo@exoportal.org");
            userHandler.createUser(user, true);

            //
            PortalConfig cfg = storage_.getPortalConfig(PortalConfig.USER_TYPE, "julien");
            assertNotNull(cfg);
            Container container = cfg.getPortalLayout();
            assertNotNull(container);
            assertEquals(2, container.getChildren().size());
            assertTrue(container.getChildren().get(0) instanceof PageBody);
            assertTrue(((Application)container.getChildren().get(1)).getType() == ApplicationType.PORTLET);
            Application<Portlet> pa = (Application<Portlet>)container.getChildren().get(1);
            ApplicationState state = pa.getState();
            assertEquals("foo/bar", storage_.getId(pa.getState()));
         }
      }.execute(null);
   }

   public void testGroupTemplate()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            String groupName = "groupTest";
            assertNull(storage_.getPortalConfig(PortalConfig.GROUP_TYPE, groupName));

            //
            GroupHandler groupHandler = orgService_.getGroupHandler();
            Group group = groupHandler.createGroupInstance();
            group.setGroupName(groupName);
            group.setDescription("this is a group for test");
            groupHandler.addChild(null, group, true);

            //
            PortalConfig cfg = storage_.getPortalConfig(PortalConfig.GROUP_TYPE, "/" + groupName);
            assertNotNull(cfg);
            Container container = cfg.getPortalLayout();
            assertNotNull(container);
            assertEquals(4, container.getChildren().size());
            assertTrue(container.getChildren().get(2) instanceof PageBody);
            assertTrue(((Application)container.getChildren().get(1)).getType() == ApplicationType.PORTLET);
            
            groupHandler.removeGroup(group, true);
         }
      }.execute(null);
   }
   
   public void testCacheUserPortalConfig()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            mgr.clearCache();
            DataCache cache = mgr.getDecorator(DataCache.class);
            long readCount0 = cache.getReadCount();
            userPortalConfigSer_.getUserPortalConfig("classic", null);
            long readCount1 = cache.getReadCount();
            assertTrue(readCount1 > readCount0);
            userPortalConfigSer_.getUserPortalConfig("classic", null);
            long readCount2 = cache.getReadCount();
            assertEquals(readCount1, readCount2);
         }
      }.execute(null);
   }

   public void testCachePage()
   {
      new UnitTest()
      {
         public void execute() throws Exception
         {
            mgr.clearCache();
            DataCache cache = mgr.getDecorator(DataCache.class);
            long readCount0 = cache.getReadCount();
            userPortalConfigSer_.getPage("portal::test::test1");
            long readCount1 = cache.getReadCount();
            assertTrue(readCount1 > readCount0);
            userPortalConfigSer_.getPage("portal::test::test1");
            long readCount2 = cache.getReadCount();
            assertEquals(readCount1, readCount2);
         }
      }.execute(null);
   }

   private abstract class UnitTest
   {

      /** . */
      private POMSession mopSession;

      protected final void execute(String userId)
      {
         Throwable failure = null;

         //
         begin();

         //
         ConversationState conversationState = null;
         if (userId != null)
         {
            try
            {
               conversationState = new ConversationState(authenticator.createIdentity(userId));
            }
            catch (Exception e)
            {
               failure = e;
            }
         }

         //
         if (failure == null)
         {
            // Clear cache for test
            mgr.clearCache();

            //
            mopSession = mgr.openSession();

            //
            ConversationState.setCurrent(conversationState);
            try
            {
               execute();
            }
            catch (Exception e)
            {
               failure = e;
            }
            finally
            {
               ConversationState.setCurrent(null);
               mopSession.close(false);
               end();
            }
         }

         // Report error as a junit assertion failure
         if (failure != null)
         {
            AssertionFailedError err = new AssertionFailedError();
            err.initCause(failure);
            throw err;
         }
      }

      protected final void saveMOP()
      {
         mopSession.save();
      }

      protected abstract void execute() throws Exception;

   }
}
