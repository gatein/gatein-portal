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
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

import java.util.Comparator;
import java.util.List;

/**
 * Author : TrongTT
 */
public class TestPortalConfig extends AbstractPortalTest
{

   private DataStorage storage;

   private POMSessionManager mgr;

   private OrganizationService org;

   /** . */
   private POMSession session;

   public TestPortalConfig(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = PortalContainer.getInstance();
      org = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
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

   public void testSiteLayout() throws Exception
   {
      PortalConfig pConfig = storage.getPortalConfig(PortalConfig.PORTAL_TYPE, "classic");
      assertNotNull(pConfig);
      assertNotNull("The Group layout of " + pConfig.getName() + " is null", pConfig.getPortalLayout());

      pConfig = storage.getPortalConfig(PortalConfig.GROUP_TYPE, "/platform/administrators");
      assertNotNull(pConfig);
      assertNotNull("The Group layout of " + pConfig.getName() + " is null", pConfig.getPortalLayout());
      assertTrue(pConfig.getPortalLayout().getChildren() != null && pConfig.getPortalLayout().getChildren().size() > 1);
      pConfig.getPortalLayout().getChildren().clear();
      storage.save(pConfig);

      pConfig = storage.getPortalConfig(PortalConfig.GROUP_TYPE, "/platform/administrators");
      assertNotNull(pConfig);
      assertNotNull("The Group layout of " + pConfig.getName() + " is null", pConfig.getPortalLayout());
      assertTrue(pConfig.getPortalLayout().getChildren() != null && pConfig.getPortalLayout().getChildren().size() == 0);

      pConfig = storage.getPortalConfig(PortalConfig.USER_TYPE, "root");
      assertNotNull(pConfig);
      assertNotNull("The User layout of " + pConfig.getName() + " is null", pConfig.getPortalLayout());

      pConfig = storage.getPortalConfig(PortalConfig.USER_TYPE, "mary");
      assertNotNull(pConfig);
      assertNotNull("The User layout of " + pConfig.getName() + " is null", pConfig.getPortalLayout());
   }

   public void testGroupLayout() throws Exception
   {
      GroupHandler groupHandler = org.getGroupHandler();
      Group group = groupHandler.findGroupById("groupTest");
      assertNull(group);

      group = groupHandler.createGroupInstance();
      group.setGroupName("groupTest");
      group.setLabel("group label");

      groupHandler.addChild(null, group, true);

      group = groupHandler.findGroupById("/groupTest");
      assertNotNull(group);

      PortalConfig pConfig = storage.getPortalConfig(PortalConfig.GROUP_TYPE, "/groupTest");
      assertNotNull("the Group's PortalConfig is not null", pConfig);
      assertTrue(pConfig.getPortalLayout().getChildren() == null || pConfig.getPortalLayout().getChildren().size() == 4);
      
      /**
       * We need to remove the /groupTest from the groupHandler as the
       * handler is shared between the tests and can cause other tests
       * to fail.
       * TODO: make the tests fully independent
       */
      groupHandler.removeGroup(group, false);
      group = groupHandler.findGroupById("/groupTest");
      assertNull(group);
   }
   
   public void testGroupNavigation() throws Exception
   {
      GroupHandler groupHandler = org.getGroupHandler();
      Group group = groupHandler.createGroupInstance();
      group.setGroupName("testGroupNavigation");
      group.setLabel("testGroupNavigation");
      
      groupHandler.addChild(null, group, true);
      
      PageNavigation pageNavigation = new PageNavigation();
      pageNavigation.setOwnerId(group.getId());
      pageNavigation.setOwnerType(PortalConfig.GROUP_TYPE);
      storage.create(pageNavigation);
      
      pageNavigation = storage.getPageNavigation(PortalConfig.GROUP_TYPE, group.getId());
      assertNotNull(pageNavigation);
      
      // Remove group
      groupHandler.removeGroup(group, true);
      
      // Group navigations is removed after remove group 
      pageNavigation = storage.getPageNavigation(PortalConfig.GROUP_TYPE, group.getId());
      assertNull(pageNavigation);
   }

   public void testUserLayout() throws Exception
   {
      UserHandler userHandler = org.getUserHandler();
      User user = userHandler.findUserByName("testing");
      assertNull(user);

      user = userHandler.createUserInstance("testing");
      user.setEmail("testing@gmaild.com");
      user.setFirstName("test firstname");
      user.setLastName("test lastname");
      user.setPassword("123456");

      userHandler.createUser(user, true);

      user = userHandler.findUserByName("testing");
      assertNotNull(user);

      PortalConfig pConfig = storage.getPortalConfig(PortalConfig.USER_TYPE, "testing");
      assertNotNull("the User's PortalConfig is not null", pConfig);
   }

   public void testGetAllOrder() throws Exception
   {
      // Query with comparator to make sure returned list is ordered
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.GROUP_TYPE, null, PageNavigation.class);
      Comparator<PageNavigation> sortComparator = new Comparator<PageNavigation>()
      {
         public int compare(PageNavigation pconfig1, PageNavigation pconfig2)
         {
            return pconfig1.getOwnerId().compareTo(pconfig2.getOwnerId());
         }
      };

      // First query
      List<PageNavigation> navis = storage.find(query, sortComparator).getAll();
      storage.save(navis.get(0)); // Modify
      // Second query
      List<PageNavigation> navis2 = storage.find(query, sortComparator).getAll();
      for (int i = 0; i < navis.size(); i++)
      {
         assertEquals(true, navis.get(i).getOwnerId().equals(navis2.get(i).getOwnerId()));
      }
   }
}
