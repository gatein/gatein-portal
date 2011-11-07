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

package org.exoplatform.portal.webui.portal;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Jun 11, 2009  
 */
@ComponentConfig(template = "system:/groovy/portal/webui/application/UIApplicationList.gtmpl", events = {@EventConfig(listeners = UIApplicationList.SelectCategoryActionListener.class)})
public class UIApplicationList extends UIContainer
{
   private List<ApplicationCategory> categories;

   private ApplicationCategory selectedCategory;

   public UIApplicationList() throws Exception
   {
   }

   public Application getApplication(String id) throws Exception
   {
      for (ApplicationCategory category : getCategories())
      {
         List<Application> items = category.getApplications();
         for (Application item : items)
         {
            if (item.getId().equals(id))
               return item;
         }
      }
      return null;
   }

   public ApplicationCategory getSelectedCategory()
   {
      return selectedCategory;
   }

   public void setSelectedCategory(String categoryName)
   {
      for (ApplicationCategory category : getCategories())
      {
         if (category.getName().equals(categoryName))
         {
            selectedCategory = category;
         }
      }
   }

   public List<Application> getApplications()
   {
      if (selectedCategory == null)
         return null;
      List<Application> apps = selectedCategory.getApplications();

      //Correct IconURL of Gadget
      GadgetRegistryService gadgetService = getApplicationComponent(GadgetRegistryService.class);
      for (Application app : apps)
      {
         if (ApplicationType.GADGET.equals(app.getType()))
         {
            try
            {
               Gadget gadget;
               gadget = gadgetService.getGadget(app.getApplicationName());
               if (gadget != null)
                  app.setIconURL(gadget.getThumbnail());
            }
            catch (Exception e)
            {
            }
         }
      }
      return apps;
   }

   public List<ApplicationCategory> getCategories()
   {
      try
      {
         //TODO: Handle concurrent requests associated with current session
         if (categories == null)
         {
            initAllCategories();
         }
         return categories;
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   private void initAllCategories() throws Exception
   {
      String remoteUser = Util.getPortalRequestContext().getRemoteUser();
      if (remoteUser == null || remoteUser.equals(""))
      { return; }

      ApplicationRegistryService service = getApplicationComponent(ApplicationRegistryService.class);
      UserACL userACL = Util.getUIPortalApplication().getApplicationComponent(UserACL.class);

      final Comparator<Application> appComparator = new Comparator<Application>()
      {
         public int compare(Application p_1, Application p_2)
         {
            return p_1.getDisplayName().compareToIgnoreCase(p_2.getDisplayName());
         }
      };
      final Comparator<ApplicationCategory> cateComparator = new Comparator<ApplicationCategory>()
      {
         public int compare(ApplicationCategory p_1, ApplicationCategory p_2)
         {
            return p_1.getDisplayName(true).compareToIgnoreCase(p_2.getDisplayName(true));
         }
      };

      List<ApplicationCategory> allCategories = service.getApplicationCategories(remoteUser);
      categories = new ArrayList<ApplicationCategory>();

      for (ApplicationCategory category : allCategories)
      {
         List<Application> apps = category.getApplications();
         List<String> accessPermission = category.getAccessPermissions();
         if(accessPermission == null)
         {
            continue;
         }

         accessCheck:
         for (String p : accessPermission)
         {
            if (userACL.hasPermission(p))
            {
               if (apps.size() > 0)
               {
                  Collections.sort(apps, appComparator);
               }
               categories.add(category);
            }
            break accessCheck;
         }
      }

      if (categories.size() > 0)
      {
         Collections.sort(categories, cateComparator);
         selectedCategory = categories.get(0);
      }
   }

   static public class SelectCategoryActionListener extends EventListener<UIApplicationList>
   {
      public void execute(Event<UIApplicationList> event) throws Exception
      {
         String category = event.getRequestContext().getRequestParameter(OBJECTID);
         UIApplicationList uiApplicationList = event.getSource();
         uiApplicationList.setSelectedCategory(category);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiApplicationList);
      }

   }
}