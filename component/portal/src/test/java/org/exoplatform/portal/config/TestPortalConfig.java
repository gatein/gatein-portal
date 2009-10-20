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
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.test.BasicTestCase;

/**
 * Author : TrongTT
 */
public class TestPortalConfig extends BasicTestCase
{

   private DataStorage storage;

   private POMSessionManager mgr;

   private OrganizationService org;

   public TestPortalConfig(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();

      PortalContainer container = PortalContainer.getInstance();
      org = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      storage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      mgr.openSession();
      ((ComponentRequestLifecycle)org).startRequest(container);
   }

   protected void tearDown() throws Exception
   {
      mgr.closeSession(false);
      storage = null;
      PortalContainer container = PortalContainer.getInstance();
      ((ComponentRequestLifecycle)org).endRequest(container);
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
      assertTrue(pConfig.getPortalLayout().getChildren() == null || pConfig.getPortalLayout().getChildren().size() < 1);
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
}
