/**
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.portal.config.model.PortalConfig;

import java.util.Set;

/**
 * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */

public class SiteConfigTemplates
{
   private String location;
   
   private Set<String> portalTemplates;
   
   private Set<String> groupTemplates;
   
   private Set<String> userTemplates;
   
   /**
    * @return the location
    */
   public String getLocation()
   {
      return location;
   }

   /**
    * @param location the locationPath to set
    */
   public void setLocation(String locationPath)
   {
      this.location = locationPath;
   }

   /**
    * @return the type
    */
   public Set<String> getTemplates(String type)
   {
      if (type.equals(PortalConfig.PORTAL_TYPE))
      {
         return portalTemplates;
      }
      else if (type.equals(PortalConfig.GROUP_TYPE))
      {
         return groupTemplates;
      }
      else
      {
         return userTemplates;
      }
   }
}
