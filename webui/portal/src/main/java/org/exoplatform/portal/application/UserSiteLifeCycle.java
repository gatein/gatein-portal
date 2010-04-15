/*
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

package org.exoplatform.portal.application;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserSiteLifeCycle implements ApplicationLifecycle<PortalRequestContext>
{

   /** . */
   private final Logger log = LoggerFactory.getLogger(UserSiteLifeCycle.class);

   public void onInit(Application app) throws Exception
   {
   }

   public void onStartRequest(Application app, PortalRequestContext context) throws Exception
   {
      String userName = context.getRemoteUser();
      if (userName != null)
      {
         DataStorage storage = (DataStorage)PortalContainer.getComponent(DataStorage.class);
         PortalConfig portalConfig = storage.getPortalConfig("user", userName);

         //
         if (portalConfig == null)
         {
            log.debug("About to create user site for user " + userName);
            UserPortalConfigService configService = (UserPortalConfigService)PortalContainer.getComponent(UserPortalConfigService.class);
            configService.createUserSite(userName);
         }
      }
   }
   
   public void onFailRequest(Application app, PortalRequestContext context, RequestFailure failureType) throws Exception
   {
   }

   public void onEndRequest(Application app, PortalRequestContext context) throws Exception
   {
   }

   public void onDestroy(Application app) throws Exception
   {
   }
}
