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
      ApplicationRegistryService service = getApplicationComponent(ApplicationRegistryService.class);
      String remoteUser = Util.getPortalRequestContext().getRemoteUser();
      if (remoteUser == null || remoteUser.equals(""))
         return;
      UserACL userACL = Util.getUIPortalApplication().getApplicationComponent(UserACL.class);

      PortletComparator portletComparator = new PortletComparator();
      categories = service.getApplicationCategories(remoteUser);

      Iterator<ApplicationCategory> cateItr = categories.iterator();
      while (cateItr.hasNext())
      {
         ApplicationCategory cate = cateItr.next();
         List<Application> applications = cate.getApplications();
         boolean hasPermission = false;
         List<String> accessPermission = cate.getAccessPermissions();
         if (accessPermission == null)
         {
            accessPermission = new ArrayList<String>();
         }
         if (accessPermission.size() == 0)
         {
            accessPermission.add(null);
         }
         for (String permssion : accessPermission)
         {
            hasPermission = userACL.hasPermission(permssion);
            if (hasPermission)
               break;
         }

         if (!hasPermission || applications.size() < 1)
            cateItr.remove();
         else
            Collections.sort(applications, portletComparator);
      }
      if (categories.size() > 0)
         setSelectedCategory(categories.get(0).getName());

      Collections.sort(categories, new PortletCategoryComparator());
   }

   public Application getApplication(String id) throws Exception
   {
      for (ApplicationCategory category : categories)
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
      for (ApplicationCategory category : categories)
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
      return categories;
   }

   static class PortletCategoryComparator implements Comparator<ApplicationCategory>
   {
      public int compare(ApplicationCategory cat1, ApplicationCategory cat2)
      {
         return cat1.getDisplayName().compareToIgnoreCase(cat2.getDisplayName());
      }
   }

   static class PortletComparator implements Comparator<Application>
   {
      public int compare(Application p1, Application p2)
      {
         return p1.getDisplayName().compareToIgnoreCase(p2.getDisplayName());
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