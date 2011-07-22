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

package org.exoplatform.services.organization;

import junit.framework.Assert;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

import java.util.Collection;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov
 * 13, 2007
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/services/organization/TestOrganization-configuration.xml")
})
public class TestOrganization extends AbstractKernelTest
{

	private static final String GROUP_1 = "testOrganization_group1";
	private static final String GROUP_2 = "testOrganization_group2";
	private static final String GROUP_3 = "testOrganization_group3";
	
	private static final String USER_1 = "testOrganization_user1";
	private static final String USER_2 = "testOrganization_user2";
	private static final String USER_3 = "testOrganization_user3";
	private static final String DEFAULT_PASSWORD = "defaultpassword";
	private static final String DESCRIPTION = " Description";
	
   private OrganizationService organizationService;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = getContainer();
      organizationService = (OrganizationService)container.getComponentInstance(OrganizationService.class);
      
      createGroup(null, GROUP_1);
      createGroup(GROUP_1, GROUP_2);
      createGroup(GROUP_1, GROUP_3);
      
      createUser(USER_1, GROUP_1);
      createUser(USER_2, GROUP_2);
      createUser(USER_3, GROUP_1);
   }

   @Override
   protected void tearDown() throws Exception
   {
      deleteGroup(GROUP_1);
      deleteGroup(GROUP_2);
      deleteGroup(GROUP_3);

      deleteUser(USER_1);
      deleteUser(USER_2);
      deleteUser(USER_3);

      end();
      super.tearDown();
   }

   public void testFindGroupNotFound() throws Exception {
	   GroupHandler groupHander = organizationService.getGroupHandler();
	   Group group = groupHander.findGroupById(GROUP_1 + "NOTFOUND");
	   assertNull(group);
   }
   
   public void testFindGroupFromRoot() throws Exception
   {
      GroupHandler handler = organizationService.getGroupHandler();
      Collection allGroups = handler.findGroups(null);
      Assert.assertTrue(allGroups.size() > 0);
   }

   public void testFindGroupById() throws Exception
   {
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group group = groupHandler.findGroupById(GROUP_1);
      assertNotNull(group);
      assertEquals(GROUP_1, group.getGroupName());
      assertEquals(GROUP_1 + DESCRIPTION, group.getDescription());
      
      group = groupHandler.findGroupById(GROUP_3);
      assertNotNull(group);
      assertEquals(GROUP_3, group.getGroupName());
   }
   
   public void testFindGroupOfUser() {
	   GroupHandler groupHandler = organizationService.getGroupHandler();
	   try {
		Collection<Group> groups = groupHandler.findGroupsOfUser(USER_1);
		assertNotNull(groups);
		assertTrue(groups.size() >= 1);
	} catch (Exception e) {
		fail();
	}
   }

   public void testFindUserByGroup() throws Exception
   {
	  GroupHandler groupHandler = organizationService.getGroupHandler();
	  Group group = groupHandler.findGroupById(GROUP_1);
      UserHandler uHandler = organizationService.getUserHandler();
      PageList users = uHandler.findUsersByGroup("/platform/administrators");
      Assert.assertTrue(users.getAvailable() > 0);

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
      Assert.assertNotNull(user);
      Assert.assertTrue(uHandler.authenticate("root", "gtn"));
      
      // Test changing password
      user.setPassword("newPassword");
      uHandler.saveUser(user, false);
      user = uHandler.findUserByName("root");
      Assert.assertNotNull(user);
      Assert.assertTrue(uHandler.authenticate("root", "newPassword"));

      // Reset to default password
      user.setPassword("gtn");
      uHandler.saveUser(user, false);

   }

   private void createGroup(String parent, String name)
   {
      GroupHandler groupHandler = organizationService.getGroupHandler();
      try
      {
         Group parentGroup = null;
         if (parent != null)
         {
            parentGroup = groupHandler.findGroupById(parent);
         }
         Group newGroup = groupHandler.createGroupInstance();
         newGroup.setGroupName(name);
         newGroup.setDescription(name + DESCRIPTION);
         newGroup.setLabel(name);
         if (parentGroup != null)
         {
            groupHandler.addChild(parentGroup, newGroup, true);
         }
         groupHandler.saveGroup(newGroup, true);
      }
      
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Error on create group [" + name + "] " + e.getMessage());
      }
   }
   private void deleteGroup(String name)
   {
      GroupHandler groupHandler = organizationService.getGroupHandler();
      try
      {
         Group group = groupHandler.findGroupById(name);
         groupHandler.removeGroup(group, true);
      }
      catch (Exception e)
      {
      }
   }

   private void createUser(String username, String... groups)
   {
      UserHandler userHandler = organizationService.getUserHandler();
      User user = userHandler.createUserInstance(username);
      user.setPassword(DEFAULT_PASSWORD);
      user.setFirstName("default");
      user.setLastName("default");
      user.setEmail("exo@exoportal.org");
      user.setOrganizationId(groups[0]);
      try
      {
         userHandler.createUser(user, true);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Error on create user: " + e.getMessage());
      }
   }

   private void deleteUser(String username)
   {
      UserHandler userHandler = organizationService.getUserHandler();
      try
      {
         userHandler.removeUser(username, true);
      }
      catch (Exception e)
      {

      }
   }
}
