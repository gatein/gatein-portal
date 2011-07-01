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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by The eXo Platform SARL 
 * Author : Nhu Dinh Thuan
 *    nhudinhthuan@exoplatform.com May 29, 2007
 */
public class GroupPortalConfigListener extends GroupEventListener
{

   /** . */
   private final UserPortalConfigService portalConfigService;

   /** . */
   private final OrganizationService orgService;

   public GroupPortalConfigListener(UserPortalConfigService portalConfigService, OrganizationService orgService)
   {
      this.portalConfigService = portalConfigService;
      this.orgService = orgService;
   }

   public void preDelete(Group group) throws Exception
   {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try
      {
         String groupId = group.getId().trim();

         // Remove all descendant navigations
         removeGroupNavigation(group);

         portalConfigService.removeUserPortalConfig(PortalConfig.GROUP_TYPE, groupId);
      }
      finally
      {
         RequestLifeCycle.end();
      }
   }

   @Override
   public void preSave(Group group, boolean isNew) throws Exception
   {
      RequestLifeCycle.begin(PortalContainer.getInstance());

      try
      {

         String groupId = group.getId();

         // Bug in hibernate org service implementation
         if (groupId == null)
         {
            groupId = "/" + group.getGroupName();
         }

         // Bug in JCR org service implementation
         if ("/administrators".equals(groupId))
         {
            groupId = "/platform/administrators";
         }
         else if ("/users".equals(groupId))
         {
            groupId = "/platform/users";
         }
         else if ("/guests".equals(groupId))
         {
            groupId = "/platform/guests";
         }
         else if ("/management".equals(groupId))
         {
            groupId = "/organization/management";
         }
         else if ("/executive-board".equals(groupId))
         {
            groupId = "/organization/management/executive-board";
         }
         else if ("/human-resources".equals(groupId))
         {
            groupId = "/organization/management/human-resources";
         }
         else if ("/communication".equals(groupId))
         {
            groupId = "/organization/communication";
         }
         else if ("/marketing".equals(groupId))
         {
            groupId = "/organization/communication/marketing";
         }
         else if ("/press-and-media".equals(groupId))
         {
            groupId = "/organization/communication/press-and-media";
         }
         else if ("/operations".equals(groupId))
         {
            groupId = "/organization/operations";
         }
         else if ("/sales".equals(groupId))
         {
            groupId = "/organization/operations/sales";
         }
         else if ("/finances".equals(groupId))
         {
            groupId = "/organization/operations/finances";
         }

         // Create the portal from the template
         portalConfigService.createGroupSite(groupId);
      }
      finally
      {
         RequestLifeCycle.end();
      }
   }

   private void removeGroupNavigation(Group group) throws Exception
   {
      GroupHandler groupHandler = orgService.getGroupHandler();
      Collection<String> descendantGroups = getDescendantGroups(group, groupHandler);
      Collection<String> deletedNavigationGroups = new ArrayList<String>();
      deletedNavigationGroups.addAll(descendantGroups);
      deletedNavigationGroups.add(group.getId());
      for (String childGroup : deletedNavigationGroups)
      {
         SiteKey key = SiteKey.group(childGroup);
         NavigationService navService = portalConfigService.getNavigationService();
         NavigationContext nav = navService.loadNavigation(key);
         if (nav != null)
         {
            navService.destroyNavigation(nav);
         }
      }
   }

   private Collection<String> getDescendantGroups(Group group, GroupHandler groupHandler) throws Exception
   {
      Collection<Group> groupCollection = groupHandler.findGroups(group);
      Collection<String> col = new ArrayList<String>();
      for (Group childGroup : groupCollection)
      {
         col.add(childGroup.getId());
         col.addAll(getDescendantGroups(childGroup, groupHandler));
      }
      return col;
   }
}
