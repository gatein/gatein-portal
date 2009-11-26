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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.test.BasicTestCase;

import java.util.Collection;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov
 * 13, 2007
 */
public class TestOrganization extends BasicTestCase
{

   private OrganizationService organizationService;

   @Override
   protected void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      organizationService = (OrganizationService)container.getComponentInstance(OrganizationService.class);
      ((ComponentRequestLifecycle)organizationService).startRequest(container);
   }

   @Override
   protected void tearDown() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      organizationService = (OrganizationService)container.getComponentInstance(OrganizationService.class);
      ((ComponentRequestLifecycle)organizationService).endRequest(container);
   }

   public void testFindGroups() throws Exception
   {
      GroupHandler handler = organizationService.getGroupHandler();
      Collection allGroups = handler.findGroups(null);
      assertTrue(allGroups.size() > 0);
   }

   public void testFindGroupById() throws Exception
   {
      GroupHandler uHandler = organizationService.getGroupHandler();
      Collection group = uHandler.findGroupsOfUser("root");
   }

   public void testFindUserByGroup() throws Exception
   {
      UserHandler uHandler = organizationService.getUserHandler();
      PageList users = uHandler.findUsersByGroup("/platform/administrators");
      assertTrue(users.getAvailable() > 0);

      List iterator = users.getAll();
      for (Object test : iterator)
      {
         User a = (User)test;
         System.out.println(a.getUserName());
      }
   }

   public void testChangePassword() throws Exception
   {
      UserHandler uHandler = organizationService.getUserHandler();
      User user = uHandler.findUserByName("root");
      assertNotNull(user);
      assertTrue(uHandler.authenticate("root", "gtn"));
      
      // Test changing password
      user.setPassword("newPassword");
      uHandler.saveUser(user, false);
      user = uHandler.findUserByName("root");
      assertNotNull(user);
      assertTrue(uHandler.authenticate("root", "newPassword"));    

      // Reset to default password
      user.setPassword("gtn");
      uHandler.saveUser(user, false);

   }
}
