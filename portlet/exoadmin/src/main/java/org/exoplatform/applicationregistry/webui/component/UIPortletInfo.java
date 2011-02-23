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

package org.exoplatform.applicationregistry.webui.component;

import java.util.ArrayList;
import java.util.List;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.applicationregistry.webui.component.UIPortletManagement.PortletExtra;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Sep 11, 2008  
 */
@ComponentConfig(template = "app:/groovy/applicationregistry/webui/component/UIPortletInfo.gtmpl", events = {
   @EventConfig(listeners = UIPortletInfo.ShowCategoriesActionListener.class)})
@Serialized
public class UIPortletInfo extends UIContainer
{
   private static String CATEGORY_ID = "PortletCategory";
   private PortletExtra portlet_;

   public UIPortletInfo() throws Exception
   {
      addChild(UICategorySelector.class, null, CATEGORY_ID);
   }
   
   public void setPortlet(PortletExtra portlet)
   {
      portlet_ = portlet;
   }

   public PortletExtra getPortlet()
   {
      return portlet_;
   }

   public static class ShowCategoriesActionListener extends EventListener<UIPortletInfo>
   {
      
      @Override
      public void execute(Event<UIPortletInfo> event) throws Exception
      {
         UIPortletInfo uiPortletInfo = event.getSource();
         PortletExtra portlet = uiPortletInfo.getPortlet();
         uiPortletInfo.removeChild(UICategorySelector.class);
         UICategorySelector selector = uiPortletInfo.addChild(UICategorySelector.class, null, CATEGORY_ID);
         Application app = new Application();
         app.setApplicationName(portlet.getName());
         app.setType(ApplicationType.PORTLET);
         app.setDisplayName(portlet.getDisplayName());
         app.setContentId(portlet.getId());
         app.setAccessPermissions(new ArrayList<String>());
         
         selector.setApplication(app);
         selector.setRendered(true);
      }
      
   }
   
   private String getCategorieNames() throws Exception
   {
      ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
      List<ApplicationCategory> allCategories = appRegService.getApplicationCategories();
      List<String> nameList = new ArrayList<String>();
      
      for (ApplicationCategory category : allCategories)
      {
         for (Application application : appRegService.getApplications(category))
         {
            if (application.getContentId().equals(portlet_.getId()))
            {
               nameList.add(category.getDisplayName());
            }
         }
      }
      StringBuffer names = new StringBuffer("");
      for (String name : nameList)
      {
         names.append(name);
         if (!name.equals(nameList.get(nameList.size() - 1)))
            names.append(", ");
      }
      return names.toString();
   }
}
