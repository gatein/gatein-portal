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

import org.exoplatform.commons.utils.ListenerStack;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.Role;
import org.picketlink.idm.api.RoleType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.naming.InvalidNameException;

/**
 */
public class MembershipDAOImpl implements MembershipHandler
{

   private PicketLinkIDMService service_;

   private List listeners_;

   private PicketLinkIDMOrganizationServiceImpl orgService;

   public MembershipDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService service)
   {
      service_ = service;
      listeners_ = new ListenerStack(5);
      this.orgService = orgService;
   }

   public void addMembershipEventListener(MembershipEventListener listener)
   {
      listeners_.add(listener);
   }

   final public Membership createMembershipInstance()
   {
      return new MembershipImpl();
   }

   public void createMembership(Membership m, boolean broadcast) throws Exception
   {

      if (broadcast)
      {
         preSave(m, true);
      }

      saveMembership(m, false);

      if (broadcast)
      {
         postSave(m, true);
      }

   }

   public void linkMembership(User user, Group g, MembershipType mt, boolean broadcast) throws Exception
   {
      if (g == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because group is null");
      }

      if (mt == null)
      {
         throw new InvalidNameException("Can not create membership record for " + user.getUserName()
            + " because membership type is null");
      }

      if (getIdentitySession().getRoleManager().getRoleType(mt.getName()) == null)
      {
         getIdentitySession().getRoleManager().createRoleType(mt.getName());
      }

      String groupId =
         getIdentitySession().getPersistenceManager().createGroupKey(g.getGroupName(), orgService.getGtnGroupType());

      if (getIdentitySession().getRoleManager().hasRole(user.getUserName(), groupId, mt.getName()))
      {
         return;
      }

      MembershipImpl membership = new MembershipImpl();
      membership.setMembershipType(mt.getName());
      membership.setUserName(user.getUserName());
      membership.setGroupId(g.getId());

      if (broadcast)
      {
         preSave(membership, true);
      }

      getIdentitySession().getRoleManager().createRole(mt.getName(), user.getUserName(), groupId);

      if (broadcast)
      {
         postSave(membership, true);
      }

   }

   public void saveMembership(Membership m, boolean broadcast) throws Exception
   {
      String groupId =
         getIdentitySession().getPersistenceManager().createGroupKey(getGroupNameFromId(m.getGroupId()),
            orgService.getGtnGroupType());

      if (getIdentitySession().getRoleManager().hasRole(m.getUserName(), groupId, m.getMembershipType()))
      {
         return;
      }

      if (broadcast)
      {
         preSave(m, false);
      }

      getIdentitySession().getRoleManager().createRole(m.getMembershipType(), m.getUserName(), groupId);

      if (broadcast)
      {
         postSave(m, false);
      }
   }

   public Membership removeMembership(String id, boolean broadcast) throws Exception
   {

      Membership m = new MembershipImpl(id);

      String groupId =
         getIdentitySession().getPersistenceManager().createGroupKey(getGroupNameFromId(m.getGroupId()),
            orgService.getGtnGroupType());

      if (!getIdentitySession().getRoleManager().hasRole(m.getUserName(), groupId, m.getMembershipType()))
      {
         return m;
      }

      if (broadcast)
      {
         preDelete(m);
      }

      getIdentitySession().getRoleManager().removeRole(m.getMembershipType(), m.getUserName(), groupId);

      if (broadcast)
      {
         postDelete(m);
      }
      return m;
   }

   public Collection removeMembershipByUser(String userName, boolean broadcast) throws Exception
   {

      Collection<Role> roles = getIdentitySession().getRoleManager().findRoles(userName, null);

      //TODO: Exo UI has hardcoded casts to List
      List<Membership> memberships = new LinkedList<Membership>();

      for (Role role : roles)
      {
         MembershipImpl m = new MembershipImpl();
         Group g = ((GroupDAOImpl)orgService.getGroupHandler()).getGroup(role.getGroup().getName());
         m.setGroupId(g.getId());
         m.setUserName(role.getUser().getId());
         m.setMembershipType(role.getRoleType().getName());
         memberships.add(m);

         if (broadcast)
         {
            preDelete(m);
         }

         getIdentitySession().getRoleManager().removeRole(role);

         if (broadcast)
         {
            postDelete(m);
         }

      }

      return memberships;

   }

   public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception
   {
      String gid =
         getIdentitySession().getPersistenceManager().createGroupKey(getGroupNameFromId(groupId),
            orgService.getGtnGroupType());

      Role role = getIdentitySession().getRoleManager().getRole(type, userName, gid);

      if (role == null)
      {
         return null;
      }

      MembershipImpl m = new MembershipImpl();
      m.setGroupId(groupId);
      m.setUserName(userName);
      m.setMembershipType(type);

      return m;
   }

   public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception
   {
      if (userName == null)
      {
         // julien fix : if user name is null, need to check if we do need to return a special group
         return Collections.emptyList();
      }

      String gid =
         getIdentitySession().getPersistenceManager().createGroupKey(getGroupNameFromId(groupId),
            orgService.getGtnGroupType());

      Collection<RoleType> roleTypes = getIdentitySession().getRoleManager().findRoleTypes(userName, gid, null);

      //TODO: Exo UI has hardcoded casts to List
      List<Membership> memberships = new LinkedList<Membership>();

      for (RoleType roleType : roleTypes)
      {
         MembershipImpl m = new MembershipImpl();
         m.setGroupId(groupId);
         m.setUserName(userName);
         m.setMembershipType(roleType.getName());
         memberships.add(m);
      }

      return memberships;
   }

   public Collection findMembershipsByUser(String userName) throws Exception
   {
      Collection<Role> roles = getIdentitySession().getRoleManager().findRoles(userName, null);

      //TODO: Exo UI has hardcoded casts to List
      List<Membership> memberships = new LinkedList<Membership>();

      for (Role role : roles)
      {
         MembershipImpl m = new MembershipImpl();
         Group g = ((GroupDAOImpl)orgService.getGroupHandler()).getGroup(role.getGroup().getName());
         m.setGroupId(g.getId());
         m.setUserName(role.getUser().getId());
         m.setMembershipType(role.getRoleType().getName());
         memberships.add(m);
      }

      return memberships;
   }

   static void removeMembershipEntriesOfGroup(PicketLinkIDMOrganizationServiceImpl orgService, Group group,
      IdentitySession session) throws Exception
   {
      String gid = session.getPersistenceManager().createGroupKey(group.getGroupName(), orgService.getGtnGroupType());

      Collection<Role> roles = session.getRoleManager().findRoles(gid, null);

      for (Role role : roles)
      {
         session.getRoleManager().removeRole(role);
      }
   }

   public Collection findMembershipsByGroup(Group group) throws Exception
   {
      return findMembershipsByGroupId(group.getId());
   }

   public Collection findMembershipsByGroupId(String groupId) throws Exception
   {
      String gid =
         getIdentitySession().getPersistenceManager().createGroupKey(getGroupNameFromId(groupId),
            orgService.getGtnGroupType());

      Collection<Role> roles = getIdentitySession().getRoleManager().findRoles(gid, null);

      //TODO: Exo UI has hardcoded casts to List
      List<Membership> memberships = new LinkedList<Membership>();

      for (Role role : roles)
      {
         MembershipImpl m = new MembershipImpl();
         Group g = ((GroupDAOImpl)orgService.getGroupHandler()).getGroup(role.getGroup().getName());
         m.setGroupId(g.getId());
         m.setUserName(role.getUser().getId());
         m.setMembershipType(role.getRoleType().getName());
         memberships.add(m);
      }

      return memberships;

   }

   public Membership findMembership(String id) throws Exception
   {
      Membership m = new MembershipImpl(id);

      String groupId =
         getIdentitySession().getPersistenceManager().createGroupKey(getGroupNameFromId(m.getGroupId()),
            orgService.getGtnGroupType());

      if (getIdentitySession().getRoleManager().hasRole(m.getUserName(), groupId, m.getMembershipType()))
      {
         return m;
      }

      return null;
   }

   private void preSave(Membership membership, boolean isNew) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.preSave(membership, isNew);
      }
   }

   private void postSave(Membership membership, boolean isNew) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.postSave(membership, isNew);
      }
   }

   private void preDelete(Membership membership) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.preDelete(membership);
      }
   }

   private void postDelete(Membership membership) throws Exception
   {
      for (int i = 0; i < listeners_.size(); i++)
      {
         MembershipEventListener listener = (MembershipEventListener)listeners_.get(i);
         listener.postDelete(membership);
      }
   }

   private IdentitySession getIdentitySession() throws Exception
   {
      return service_.getIdentitySession();
   }

   private String getGroupNameFromId(String groupId)
   {
      String[] ids = groupId.split("/");

      return ids[ids.length - 1];
   }
}
