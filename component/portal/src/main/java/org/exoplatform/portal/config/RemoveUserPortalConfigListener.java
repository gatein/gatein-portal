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
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

/**
 * Created by The eXo Platform SARL
 * Author : Tung.Pham
 *          tung.pham@exoplatform.com
 * Aug 1, 2007  
 */
public class RemoveUserPortalConfigListener extends Listener<UserHandler, User>
{

   @Override
   public void onEvent(Event<UserHandler, User> event) throws Exception
   {
      User user = event.getData();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserPortalConfigService portalConfigService =
         (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      String userName = user.getUserName();
      portalConfigService.removeUserPortalConfig("user", userName);
   }
}
