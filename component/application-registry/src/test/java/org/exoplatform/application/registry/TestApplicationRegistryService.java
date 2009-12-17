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

package org.exoplatform.application.registry;

import org.exoplatform.application.AbstractApplicationRegistryTest;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tung Pham
 *          thanhtungty@gmail.com
 * Nov 27, 2007  
 */
public class TestApplicationRegistryService extends AbstractApplicationRegistryTest
{

   protected static String demo = "demo";

   protected static String Group1 = "Group1";

   protected static String Group2 = "Group2";

   protected static String username1 = "userName_1";

   protected static String username2 = "userName_2";

   protected static String memtype1 = "MembershipType_1";

   protected static String memtype2 = "MembershipType_2";

   protected Group group1, group2, groupDefault;

   protected MembershipType mType1, mType2, mTypeDefault;

   protected User user1, user2, userDefault;

   protected ApplicationRegistryService service_;

   protected OrganizationService orgService;

/*
   protected int initialCats;

   protected int initialApps;
*/

   protected ChromatticManager chromatticManager;

   @Override
   protected void setUp() throws Exception
   {
      PortalContainer portalContainer = PortalContainer.getInstance();
      chromatticManager = (ChromatticManager)portalContainer.getComponentInstanceOfType(ChromatticManager.class);
      service_ = (ApplicationRegistryService)portalContainer.getComponentInstanceOfType(ApplicationRegistryService.class);
      orgService = (OrganizationService)portalContainer.getComponentInstanceOfType(OrganizationService.class);
      begin();
   }

   @Override
   protected void tearDown() throws Exception
   {
      chromatticManager.getSynchronization().setSaveOnClose(false);
      end();
   }

   public void testApplicationCategory() throws Exception
   {
      //Add new ApplicationRegistry
      String categoryName = "Category1";
      String categoryDes = "Description for category 1";
      ApplicationCategory category1 = createAppCategory(categoryName, categoryDes);
      service_.save(category1);

      int numberOfCats = service_.getApplicationCategories().size();
      assertEquals(1, numberOfCats);

      ApplicationCategory returnedCategory1 = service_.getApplicationCategory(categoryName);
      assertNotNull(returnedCategory1);
      assertEquals(category1.getName(), returnedCategory1.getName());
      assertEquals(categoryName, returnedCategory1.getName());

      //Update the ApplicationRegistry
      String newDescription = "New description for category 1";
      category1.setDescription(newDescription);
      service_.save(category1);

      numberOfCats = service_.getApplicationCategories().size();
      assertEquals(1, numberOfCats);
      returnedCategory1 = service_.getApplicationCategory(categoryName);
      assertEquals(newDescription, returnedCategory1.getDescription());

      //Remove the ApplicationRegistry
      service_.remove(category1);
      numberOfCats = service_.getApplicationCategories().size();
      assertEquals(0, numberOfCats);

      returnedCategory1 = service_.getApplicationCategory(categoryName);
      assertNull(returnedCategory1);
   }

   public void testAppCategoryGetByAccessUser() throws Exception
   {
      String officeCategoryName = "Office";
      ApplicationCategory officeCategory = createAppCategory(officeCategoryName, "None");
      service_.save(officeCategory);
      String[] officeApps = {"MSOffice", "OpenOffice"};
      Application msApp = createApplication(officeApps[0], officeCategoryName);
      ArrayList<String> pers = new ArrayList<String>();
      pers.add("member:/users");
      msApp.setAccessPermissions(pers);
      service_.save(officeCategory, msApp);
      Application openApp = createApplication(officeApps[1], officeCategoryName);
      service_.save(officeCategory, openApp);

      String gameCategoryName = "Game";
      ApplicationCategory gameCategory = createAppCategory(gameCategoryName, "None");
      service_.save(gameCategory);
      String[] gameApps = {"HaftLife", "Chess"};
      Application haftlifeApp = createApplication(gameApps[0], gameCategoryName);
      pers = new ArrayList<String>();
      pers.add("member:/portal/admin");
      haftlifeApp.setAccessPermissions(pers);
      service_.save(gameCategory, haftlifeApp);
      Application chessApp = createApplication(gameApps[1], gameCategoryName);
      chessApp.setAccessPermissions(pers);
      service_.save(gameCategory, chessApp);

      List<ApplicationCategory> returnCategorys = service_.getApplicationCategories(username1);
      for (ApplicationCategory cate : returnCategorys)
      {
         System.out.println("\n\n\ncateName: " + cate.getName());
         List<Application> apps = service_.getApplications(cate);
         for (Application app : apps)
         {
            System.out.println("\nappName: " + app.getApplicationName() + "---" + app.getAccessPermissions());
         }
      }
      assertEquals(2, returnCategorys.size());
   }
/*

   void assertApplicationOperation(ApplicationRegistryService service) throws Exception
   {
      assertApplicationSave(service);
      assertApplicationUpdate(service);
      assertApplicationRemove(service);
   }

   void assertApplicationSave(ApplicationRegistryService service) throws Exception
   {
      String categoryName = "Office";
      String appType = "TypeOne";
      String appGroup = "GroupOne";
      String[] appNames = {"OpenOffice_org", "MS_Office"};

      ApplicationCategory appCategory = createAppCategory(categoryName, "None");
      service.save(appCategory);

      for (String appName : appNames)
      {
         Application app = createApplication(appName, appType, appGroup);
         app.setCategoryName(categoryName);
         service.save(appCategory, app);
      }

      List<Application> apps = service.getApplications(appCategory);
      assertEquals(2, apps.size());

      for (String appName : appNames)
      {
         String appId = categoryName + "/" + appName;

         Application app = service.getApplication(appId);
         assertEquals(appName, app.getApplicationName());
      }
      //    service.clearAllRegistries() ;
   }

   void assertApplicationUpdate(ApplicationRegistryService service) throws Exception
   {
      String categoryName = "Office";
      String appType = "TypeOne";
      String appGroup = "GroupOne";
      String[] appNames = {"OpenOffice_org", "MS_Office"};

      ApplicationCategory appCategory = createAppCategory(categoryName, "None");
      service.save(appCategory);

      // Save apps with description
      for (String appName : appNames)
      {
         String oldDesciption = "This is: " + appName;
         Application app = createApplication(appName, appType, appGroup);
         app.setCategoryName(categoryName);
         app.setDescription(oldDesciption);
         service.save(appCategory, app);
      }

      for (String appName : appNames)
      {
         String appId = categoryName + "/" + appName;
         String oldDesciption = "This is: " + appName;

         Application app = service.getApplication(appId);
         assertEquals(oldDesciption, app.getDescription());
      }

      // Update apps with new description: use save() method
      List<Application> apps = service.getApplications(appCategory);
      for (Application app : apps)
      {
         String newDesciption = "This is: " + app.getApplicationName() + " suite.";
         app.setDescription(newDesciption);
         service.save(appCategory, app);

      }

      for (String appName : appNames)
      {
         String appId = categoryName + "/" + appName;

         Application app = service.getApplication(appId);
         String newDesciption = "This is: " + app.getApplicationName() + " suite.";
         assertEquals(newDesciption, app.getDescription());
      }

      // Update apps with new description: use update() method
      for (String appName : appNames)
      {
         String appId = categoryName + "/" + appName;
         String newDesciption = "This is new : " + appName + " suite.";

         Application app = service.getApplication(appId);
         app.setDescription(newDesciption);
         service.update(app);
      }

      for (String appName : appNames)
      {
         String appId = categoryName + "/" + appName;
         String newDesciption = "This is new : " + appName + " suite.";
         Application app = service.getApplication(appId);
         assertEquals(newDesciption, app.getDescription());
      }

      List<Application> apps2 = service.getApplications(appCategory);
      assertEquals(2, apps2.size());

      //    service.clearAllRegistries() ;
   }

   void assertApplicationRemove(ApplicationRegistryService service) throws Exception
   {
      String categoryName = "Office";
      String appType = "TestType";
      String appGroup = "TestGroup";
      String[] appNames = {"OpenOffice_org", "MS_Office"};

      ApplicationCategory appCategory = createAppCategory(categoryName, "None");
      service.save(appCategory);

      for (String appName : appNames)
      {
         Application app = createApplication(appName, appType, appGroup);
         app.setCategoryName(categoryName);
         service.save(appCategory, app);
      }

      List<Application> apps = service.getApplications(appCategory);
      assertEquals(2, apps.size());

      for (Application app : apps)
      {
         service.remove(app);
      }

      List<Application> apps2 = service.getApplications(appCategory);
      assertEquals(0, apps2.size());
      //    service.clearAllRegistries() ;
   }
*/
   private ApplicationCategory createAppCategory(String categoryName, String categoryDes)
   {
      ApplicationCategory category = new ApplicationCategory();
      category.setName(categoryName);
      category.setDisplayName(categoryName);
      category.setDescription(categoryDes);
      return category;
   }
   private Application createApplication(String appName, String appGroup)
   {
      Application app = new Application();
      app.setContentId(appName);
      app.setApplicationName(appName);
      app.setDisplayName(appName);
      app.setType(ApplicationType.PORTLET);
      return app;
   }

/*
   private void prepareOrganizationData() throws Exception
   {
      groupDefault = orgService.getGroupHandler().findGroupById("/platform/users");
      if (group1 == null)
      {
         group1 = createGroup(orgService, Group1);
      }
      if (group2 == null)
      {
         group2 = createGroup(orgService, Group2);
      }

      mTypeDefault = orgService.getMembershipTypeHandler().findMembershipType("member");
      if (mType1 == null)
      {
         mType1 = createMembershipType(orgService, memtype1);
      }
      if (mType2 == null)
      {
         mType2 = createMembershipType(orgService, memtype2);
      }

      if (user1 == null)
      {
         user1 = createUser(orgService, username1);
         createDataUser(orgService, user1);
      }
      if (user2 == null)
      {
         user2 = createUser(orgService, username2);
         createDataUser(orgService, user2);
      }

      userDefault = orgService.getUserHandler().findUserByName(demo);
   }

   private Group createGroup(OrganizationService orgService, String groupName) throws Exception
   {
      Group savedGroup = orgService.getGroupHandler().findGroupById("/" + groupName);
      if (savedGroup != null)
         return savedGroup;
      Group groupParent = orgService.getGroupHandler().createGroupInstance();
      groupParent.setGroupName(groupName);
      groupParent.setDescription("This is description");
      orgService.getGroupHandler().addChild(null, groupParent, true);
      return groupParent;
   }

   private MembershipType createMembershipType(OrganizationService orgService, String name) throws Exception
   {
      MembershipType savedMt = orgService.getMembershipTypeHandler().findMembershipType(name);
      if (savedMt != null)
         return savedMt;
      MembershipType mt = orgService.getMembershipTypeHandler().createMembershipTypeInstance();
      mt.setName(name);
      mt.setDescription("This is a test");
      mt.setOwner("exo");
      orgService.getMembershipTypeHandler().createMembershipType(mt, true);
      return mt;
   }

   @SuppressWarnings("deprecation")
   private User createUser(OrganizationService orgService, String userName) throws Exception
   {
      User savedUser = orgService.getUserHandler().findUserByName(userName);
      if (savedUser != null)
         return savedUser;
      User user = orgService.getUserHandler().createUserInstance(userName);
      user.setPassword("default");
      user.setFirstName("default");
      user.setLastName("default");
      user.setEmail("exo@exoportal.org");
      orgService.getUserHandler().createUser(user, true);
      return user;
   }

   private User createDataUser(OrganizationService orgService, User u) throws Exception
   {
      UserProfile up = orgService.getUserProfileHandler().findUserProfileByName(u.getUserName());
      up.getUserInfoMap().put("user.gender", "male");
      orgService.getUserProfileHandler().saveUserProfile(up, true);
      return u;
   }
*/
}
