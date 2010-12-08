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

      List<ApplicationCategory> returnCategories = service_.getApplicationCategories(username1);
      assertEquals(2, returnCategories.size());
      assertEquals(2, returnCategories.get(0).getApplications().size());
      assertEquals(2, returnCategories.get(1).getApplications().size());
   }
   
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
}
