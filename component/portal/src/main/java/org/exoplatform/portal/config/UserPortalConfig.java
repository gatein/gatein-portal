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

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;

import java.util.ArrayList;
import java.util.List;

public class UserPortalConfig
{

   private PortalConfig portal;

   private List<PageNavigation> navigations;
   
   /** Added by Minh Hoang TO */
   private PageNavigation selectedNavigation;

   public UserPortalConfig()
   {

   }

   public UserPortalConfig(PortalConfig portal, List<PageNavigation> navigations)
   {
      this.portal = portal;
      this.navigations = navigations;
   }

   public PortalConfig getPortalConfig()
   {
      return portal;
   }

   public void setPortal(PortalConfig portal)
   {
      this.portal = portal;
   }
   
   public void setSelectedNavigation(PageNavigation _selectedNavigation)
   {
      this.selectedNavigation = _selectedNavigation;
   }

   /** Fetch navigation (specified by ownerType, ownerId) from the list of all navigations and set it as selected navigation **/
   public void updateSelectedNavigation(String ownerType, String ownerId)
   {
      PageNavigation targetNavigation = null;
      for (PageNavigation nav : navigations)
      {
         if (nav.getOwnerType().equals(ownerType) && nav.getOwnerId().equals(ownerId))
         {
            targetNavigation = nav;
            break;
         }
      }

      if (targetNavigation != null)
      {
         this.setSelectedNavigation(targetNavigation);
      }
   }
   
   public PageNavigation getSelectedNavigation()
   {
      if(this.selectedNavigation != null)
      {
         return this.selectedNavigation;
      }
      return navigations.get(0);
   }
   
   public void setNavigations(List<PageNavigation> navs)
   {
      navigations = navs;
   }

   public List<PageNavigation> getNavigations()
   {
      return navigations;
   }

   public void addNavigation(PageNavigation nav)
   {
      if (navigations == null)
         navigations = new ArrayList<PageNavigation>();
      if (nav == null)
         return;
      navigations.add(nav);
   }
}