/*
* JBoss, a division of Red Hat
* Copyright 2010, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.idm.Config;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com
 * Oct 27, 2005
 */

public class TestConfigOrganizationService extends AbstractGateInTest
{

   static String Group1 = "Group1";

   static String Group2 = "Group2";

   static String Group3 = "Group3";

   static String Benj = "Benj";

   static String Tuan = "Tuan";

   OrganizationService service_;

   UserHandler userHandler_;

   UserProfileHandler profileHandler_;

   GroupHandler groupHandler_;

   MembershipTypeHandler mtHandler_;

   MembershipHandler membershipHandler_;

   boolean runtest = true;

   private static final String USER = "test";

   private static final List<String> USERS;

   private static final int USERS_LIST_SIZE = 15;

   private PortalContainer manager;

   static
   {
      USERS = new ArrayList<String>(USERS_LIST_SIZE);
      for (int i = 0; i < USERS_LIST_SIZE; i++)
         USERS.add(USER + "_" + i);
   }

   public TestConfigOrganizationService(String s)
   {
      super(s);
   }

   public void setUp() throws Exception
   {
      if (!runtest)
         return;

      manager = PortalContainer.getInstance();
      service_ = (OrganizationService)manager.getComponentInstanceOfType(OrganizationService.class);
      userHandler_ = service_.getUserHandler();
      profileHandler_ = service_.getUserProfileHandler();
      groupHandler_ = service_.getGroupHandler();
      mtHandler_ = service_.getMembershipTypeHandler();
      membershipHandler_ = service_.getMembershipHandler();

//      ((ComponentRequestLifecycle)service_).startRequest(manager);
      RequestLifeCycle.begin((ComponentRequestLifecycle)service_);

   }

   public void tearDown() throws Exception
   {
      Query query = new Query();
      query.setUserName(USER + "*");
      PageList users = userHandler_.findUsers(query);

      List<User> allUsers = users.getAll();

      for (int i = allUsers.size() - 1; i >= 0; i--)
      {
         String userName = allUsers.get(i).getUserName();
         userHandler_.removeUser(userName, true);
      }

//      ((ComponentRequestLifecycle)service_).endRequest(manager);
      RequestLifeCycle.end();
   }

   public void testSimple() throws Exception
   {
      assertTrue(true);
      Config config = ((PicketLinkIDMOrganizationServiceImpl)service_).getConfiguration();

      assertNotNull(config);
      assertNotNull(config.getGroupTypeMappings());
      assertNotNull(config.getGroupTypeMappings().keySet());

      assertEquals(config.getGroupTypeMappings().keySet().size(), 5);
      assertEquals(config.getGroupTypeMappings().get("/"), "root_type");

      assertEquals(config.getGroupType("/"), "root_type");
      assertEquals(config.getGroupType(null), "root_type");
      assertEquals(config.getGroupType("/platform"), "platform_type");
      assertEquals(config.getGroupType("/platform/administrators"), "platform_type");
      assertEquals(config.getGroupType("/platform/guests"), "platform_type");
      assertEquals(config.getGroupType("/platform/users"), "users_type");
      assertEquals(config.getGroupType("/platform/users/john"), "platform_type");
      assertEquals(config.getGroupType("/organization/acme/france/offices"), ".organization.acme.france.offices");
      assertEquals(config.getGroupType("/organization/acme/france/offices/paris"), ".organization.acme.france.offices.paris");
      assertEquals(config.getGroupType("/organization/acme/france"), "france_type");
      assertEquals(config.getGroupType("/organization/acme"), ".organization.acme");
      assertEquals(config.getGroupType("/foo/bar"), ".foo.bar");
      assertEquals(config.getGroupType("/foo"), ".foo");
      assertEquals(config.getGroupType("/toto"), "toto_type");
      assertEquals(config.getGroupType("/toto/lolo"), "toto_type");
      assertEquals(config.getGroupType("/toto/lolo/tutu"), "toto_type");


   }



}