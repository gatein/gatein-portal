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

package org.exoplatform.services.organization.idm;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.impl.UserImpl;
import org.gatein.common.logging.LogLevel;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySearchCriteria;
import org.picketlink.idm.api.Role;
import org.picketlink.idm.api.SortOrder;
import org.picketlink.idm.api.query.UserQuery;
import org.picketlink.idm.impl.api.IdentitySearchCriteriaImpl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/*
* @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
*/
public class IDMMembershipListAccess implements ListAccess<Membership>, Serializable
{
   private static Logger log = LoggerFactory.getLogger(IDMMembershipListAccess.class);

   private final Group group;
   
   private final org.picketlink.idm.api.User user;

   private int size = -1;

   public IDMMembershipListAccess(Group group)
   {
      this.group = group;
      this.user = null;
   }

   public IDMMembershipListAccess(org.picketlink.idm.api.User user)
   {
      this.group = null;
      this.user = user;
   }

   public Membership[] load(int index, int length) throws Exception, IllegalArgumentException
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "load",
            new Object[]{
               "index", index,
               "length", length
            }
         );
      }

      IdentitySearchCriteria crit = new IdentitySearchCriteriaImpl().page(index, length);
      crit.sort(SortOrder.ASCENDING);


      List<Role> roles = null;

      if (group != null)
      {
         roles = new LinkedList<Role>(getIDMService().getIdentitySession().getRoleManager().findRoles(group, null, crit));
      }
      else if (user != null)
      {
         roles = new LinkedList<Role>(getIDMService().getIdentitySession().getRoleManager().findRoles(user, null, crit));
      }

      Membership[] memberships = new Membership[roles.size()];

      for (int i = 0; i < roles.size(); i++)
      {
         
         Role role = roles.get(i);
         
         org.exoplatform.services.organization.Group exoGroup = 
            ((GroupDAOImpl)getOrganizationService().getGroupHandler()).convertGroup(role.getGroup());
         
         MembershipImpl memb = new MembershipImpl();
         memb.setGroupId(exoGroup.getId());
         memb.setUserName(role.getUser().getId());
         memb.setMembershipType(role.getRoleType().getName());
         
         memberships[i] = memb;
      }

      if (log.isTraceEnabled())
      {
        Tools.logMethodOut(
            log,
            LogLevel.TRACE,
            "load",
            memberships
         );
      }

      return memberships;
   }

   public int getSize() throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "getSize",
            null
         );
      }

      int result = 0;

      if (size < 0)
      {
         if (group != null && user == null)
         {
            result = getIDMService().getIdentitySession().getRoleManager().getRolesCount(group, null, null);
         }
         else if (group == null && user != null)
         {
            result = getIDMService().getIdentitySession().getRoleManager().getRolesCount(user, null, null);
         }

         size = result;
      }
      else
      {
         result = size;
      }

      if (log.isTraceEnabled())
      {
         Tools.logMethodOut(
            log,
            LogLevel.TRACE,
            "getSize",
            result
         );
      }

      return result;

   }

   PicketLinkIDMService getIDMService()
   {
      return (PicketLinkIDMService)
         PortalContainer.getInstance().getComponentInstanceOfType(PicketLinkIDMService.class);
   }

   PicketLinkIDMOrganizationServiceImpl getOrganizationService()
   {
      return (PicketLinkIDMOrganizationServiceImpl)
         PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
   }
}
