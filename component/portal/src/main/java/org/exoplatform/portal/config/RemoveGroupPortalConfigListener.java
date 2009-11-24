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
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;

/**
 * Created by The eXo Platform SARL
 * Author : Tung.Pham
 *          tung.pham@exoplatform.com
 * Jul 31, 2007  
 */
public class RemoveGroupPortalConfigListener extends Listener<GroupHandler, Group>
{

   @Override
   public void onEvent(Event<GroupHandler, Group> event) throws Exception
   {
      Group group = event.getData();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserPortalConfigService portalConfigService =
         (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      String groupId = group.getId().substring(1);
      portalConfigService.removeUserPortalConfig("group", groupId);
   }

}
