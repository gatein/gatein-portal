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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS May 29, 2007
 */
public class UserPortalConfigListener extends UserEventListener
{

   public void preDelete(User user) throws Exception
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserPortalConfigService portalConfigService =
         (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      String userName = user.getUserName();
      portalConfigService.removeUserPortalConfig("user", userName);
   }

   public void preSave(User user, boolean isNew) throws Exception
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      /*
       * TODO Call start method on RegistryService to allow ecm, ultimate can run
       * with JDK6. This is uncommon behavior. We need find other way to fix it I
       * hope that this issues will be fixed when we use the lastest version of
       * PicoContainer Comment by Hoa Pham.
       */
      RegistryService registryService = (RegistryService)container.getComponentInstanceOfType(RegistryService.class);
      registryService.start();
      UserPortalConfigService portalConfigService =
         (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      String userName = user.getUserName();

      // Create the portal from the template
      portalConfigService.createUserPortalConfig(PortalConfig.USER_TYPE, userName, "user");

      // Need to insert the corresponding user site if needed
      PortalConfig cfg = dataStorage.getPortalConfig(PortalConfig.USER_TYPE, userName);
      if (cfg == null)
      {
         cfg = new PortalConfig(PortalConfig.USER_TYPE);
         cfg.setPortalLayout(new Container());
         cfg.setName(userName);
         dataStorage.create(cfg);
      }

      // Create a blank navigation if needed
      PageNavigation navigation = dataStorage.getPageNavigation(PortalConfig.USER_TYPE, userName);
      if (navigation == null)
      {
         PageNavigation pageNav = new PageNavigation();
         pageNav.setOwnerType(PortalConfig.USER_TYPE);
         pageNav.setOwnerId(userName);
         pageNav.setPriority(5);
         pageNav.setNodes(new ArrayList<PageNode>());
         portalConfigService.create(pageNav);
      }
   }
}
