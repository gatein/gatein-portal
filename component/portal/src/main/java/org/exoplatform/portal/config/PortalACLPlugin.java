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

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Nov 19, 2008  
 */
public class PortalACLPlugin extends BaseComponentPlugin
{
   private List<String> portalCreationRoles = null;

   private String superUser;

   public PortalACLPlugin(InitParams initParams)
   {
      ValuesParam roles2 = initParams.getValuesParam("portal.creation.roles");
      if (roles2 != null)
      {
         portalCreationRoles = roles2.getValues();
      }
      ValueParam role3 = initParams.getValueParam("super.user");
      if (role3 != null)
      {
         superUser = role3.getValue();
      }
   }

   public List<String> getPortalCreationRoles()
   {
      return portalCreationRoles;
   }

   public String getSuperUser()
   {
      return superUser;
   }

}
