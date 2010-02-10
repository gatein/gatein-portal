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

package org.exoplatform.webui.organization;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Jun 26, 2009  
 */
public class OrganizationUtils
{

   private static String cachedGroupLabel;
   
   static public String getGroupLabel(String groupId) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ExoContainer container = context.getApplication().getApplicationServiceContainer();
      OrganizationService orgService =
         (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      Group group = orgService.getGroupHandler().findGroupById(groupId);
      if(group == null){
         return cachedGroupLabel;
      }
      String label = group.getLabel();
      cachedGroupLabel = (label != null && label.trim().length() > 0 )? label : group.getGroupName();
      return cachedGroupLabel;
   }
   
   static public String getGroupDescription(String groupId) throws Exception {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ExoContainer container = context.getApplication().getApplicationServiceContainer();
      OrganizationService orgService =
         (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      String description = "";
      Group group = orgService.getGroupHandler().findGroupById(groupId);
      if (group != null) {
         description = group.getDescription();
      }
      return description;
   }

}
