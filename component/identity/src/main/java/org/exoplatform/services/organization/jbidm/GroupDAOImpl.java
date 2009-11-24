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

package org.exoplatform.services.organization.jbidm;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.jboss.identity.idm.api.Attribute;
import org.jboss.identity.idm.api.IdentitySession;
import org.jboss.identity.idm.impl.api.SimpleAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupDAOImpl implements GroupHandler
{

   public static final String GROUP_LABEL = "label";

   public static final String GROUP_DESCRIPTION = "description";

   private JBossIDMService service_;

   private List<GroupEventListener> listeners_;

   private JBossIDMOrganizationServiceImpl orgService;

   public GroupDAOImpl(JBossIDMOrganizationServiceImpl orgService, JBossIDMService service)
   {
      service_ = service;
      this.orgService = orgService;
      listeners_ = new ArrayList<GroupEventListener>();
   }

   public void addGroupEventListener(GroupEventListener listener)
   {
      listeners_.add(listener);
   }

   final public Group createGroupInstance()
   {
      return new GroupImpl();
   }

   public void createGroup(Group group, boolean broadcast) throws Exception
   {
      addChild(null, group, broadcast);
   }

   public void addChild(Group parent, Group child, boolean broadcast) throws Exception
   {
      org.jboss.identity.idm.api.Group parentGroup = null;

      if (parent != null)
      {
         parentGroup =
            getIdentitySession().getPersistenceManager().findGroup(parent.getGroupName(), orgService.getExoGroupType());
         ((GroupImpl)child).setId(parent.getId() + "/" + child.getGroupName());

      }
      else
      {
         ((GroupImpl)child).setId("/" + child.getGroupName());
      }

      if (broadcast)
      {
         preSave(child, true);
      }

      org.jboss.identity.idm.api.Group childGroup = persistGroup(child);

      if (parentGroup != null)
      {
         getIdentitySession().getRelationshipManager().associateGroups(parentGroup, childGroup);
         ((GroupImpl)child).setParentId(parent.getId());

      }
      else
      {
         getIdentitySession().getRelationshipManager().associateGroups(getRootGroup(), childGroup);
      }

      if (broadcast)
      {
         postSave(child, true);
      }

   }

   public void saveGroup(Group group, boolean broadcast) throws Exception
   {
      if (broadcast)
      {
         preSave(group, false);
      }
      persistGroup(group);
      if (broadcast)
      {
         postSave(group, false);
      }
   }

   public Group removeGroup(Group group, boolean broadcast) throws Exception
   {
      if (broadcast)
      {
         preDelete(group);
      }

      org.jboss.identity.idm.api.Group jbidGroup =
         getIdentitySession().getPersistenceManager().findGroup(group.getGroupName(), orgService.getExoGroupType());

      if (jbidGroup == null)
      {
         return group;
      }

      //      MembershipDAOImpl.removeMembershipEntriesOfGroup(group, getIdentitySession());

      Collection<org.jboss.identity.idm.api.Group> oneLevelChilds =
         getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, orgService.getExoGroupType(),
            true, false);

      Collection<org.jboss.identity.idm.api.Group> allChilds =
         getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, orgService.getExoGroupType(),
            true, true);

      getIdentitySession().getRelationshipManager().disassociateGroups(jbidGroup, oneLevelChilds);

      for (org.jboss.identity.idm.api.Group child : allChilds)
      {
         //TODO: impl force in IDM
         getIdentitySession().getPersistenceManager().removeGroup(child, true);
      }

      getIdentitySession().getPersistenceManager().removeGroup(jbidGroup, true);

      if (broadcast)
      {
         postDelete(group);
      }
      return group;
   }

   public Collection findGroupByMembership(String userName, String membershipType) throws Exception
   {
      Collection<org.jboss.identity.idm.api.Role> allRoles =
         getIdentitySession().getRoleManager().findRoles(userName, membershipType);

      Set<Group> exoGroups = new HashSet<Group>();

      for (org.jboss.identity.idm.api.Role role : allRoles)
      {
         exoGroups.add(convertGroup(role.getGroup()));

      }

      return exoGroups;
   }

   //
   public Group findGroupById(String groupId) throws Exception
   {

      org.jboss.identity.idm.api.Group jbidGroup = orgService.getJBIDMGroup(groupId);

      if (jbidGroup == null)
      {
         return null;
      }

      return convertGroup(jbidGroup);
   }

   public Collection findGroups(Group parent) throws Exception
   {
      org.jboss.identity.idm.api.Group jbidGroup = null;

      if (parent == null)
      {
         jbidGroup = getRootGroup();
      }
      else
      {
         jbidGroup =
            getIdentitySession().getPersistenceManager().findGroup(parent.getGroupName(), orgService.getExoGroupType());
      }

      if (jbidGroup == null)
      {
         return Collections.emptyList();
      }

      Collection<org.jboss.identity.idm.api.Group> allGroups =
         getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, orgService.getExoGroupType(),
            true, false);

      List<Group> exoGroups = new LinkedList<Group>();

      for (org.jboss.identity.idm.api.Group group : allGroups)
      {
         exoGroups.add(convertGroup(group));

      }

      return exoGroups;

   }

   public Collection findGroupsOfUser(String user) throws Exception
   {

      if (user == null)
      {
         // julien : integration bug
         // need to look at that later
         //
         // Caused by: java.lang.IllegalArgumentException: User name cannot be null
         // at org.jboss.identity.idm.impl.api.session.managers.AbstractManager.checkNotNullArgument(AbstractManager.java:267)
         //  at org.jboss.identity.idm.impl.api.session.managers.RelationshipManagerImpl.findRelatedGroups(RelationshipManagerImpl.java:753)
         // at org.exoplatform.services.organization.jbidm.GroupDAOImpl.findGroupsOfUser(GroupDAOImpl.java:225)
         // at org.exoplatform.organization.webui.component.GroupManagement.isMemberOfGroup(GroupManagement.java:72)
         // at org.exoplatform.organization.webui.component.GroupManagement.isAdministrator(GroupManagement.java:125)
         // at org.exoplatform.organization.webui.component.UIGroupExplorer.<init>(UIGroupExplorer.java:57)
         return Collections.emptyList();
      }

      Collection<org.jboss.identity.idm.api.Group> allGroups =
         getIdentitySession().getRelationshipManager().findRelatedGroups(user, orgService.getExoGroupType(), null);

      List<Group> exoGroups = new LinkedList<Group>();

      for (org.jboss.identity.idm.api.Group group : allGroups)
      {
         exoGroups.add(convertGroup(group));

      }

      return exoGroups;
   }

   public Collection getAllGroups() throws Exception
   {
      Collection<org.jboss.identity.idm.api.Group> allGroups =
         getIdentitySession().getPersistenceManager().findGroup(orgService.getExoGroupType());

      List<Group> exoGroups = new LinkedList<Group>();

      for (org.jboss.identity.idm.api.Group group : allGroups)
      {
         if (!orgService.getExoGroupType().equals(orgService.getExoRootGroupType())
            || !group.getName().equals(orgService.getExoRootGroupName()))
         {
            exoGroups.add(convertGroup(group));
         }

      }

      return exoGroups;
   }

   private void preSave(Group group, boolean isNew) throws Exception
   {
      for (GroupEventListener listener : listeners_)
      {
         listener.preSave(group, isNew);
      }
   }

   private void postSave(Group group, boolean isNew) throws Exception
   {
      for (GroupEventListener listener : listeners_)
      {
         listener.postSave(group, isNew);
      }
   }

   private void preDelete(Group group) throws Exception
   {
      for (GroupEventListener listener : listeners_)
      {
         listener.preDelete(group);
      }
   }

   private void postDelete(Group group) throws Exception
   {
      for (GroupEventListener listener : listeners_)
      {
         listener.postDelete(group);
      }
   }

   public Group getGroup(String groupName) throws Exception
   {
      org.jboss.identity.idm.api.Group jbidGroup =
         getIdentitySession().getPersistenceManager().findGroup(groupName, orgService.getExoGroupType());

      if (jbidGroup == null)
      {
         return null;
      }

      return convertGroup(jbidGroup);

   }

   private Group convertGroup(org.jboss.identity.idm.api.Group jbidGroup) throws Exception
   {
      Map<String, Attribute> attrs = getIdentitySession().getAttributesManager().getAttributes(jbidGroup);

      GroupImpl exoGroup = new GroupImpl(jbidGroup.getName());

      if (attrs.containsKey(GROUP_DESCRIPTION))
      {
         exoGroup.setDescription(attrs.get(GROUP_DESCRIPTION).getValue().toString());
      }
      if (attrs.containsKey(GROUP_LABEL))
      {
         exoGroup.setLabel(attrs.get(GROUP_LABEL).getValue().toString());
      }

      // Resolve full ID
      String id = getGroupId(jbidGroup.getName());

      exoGroup.setId(id);

      if (id.length() == jbidGroup.getName().length() + 1)
      {
         exoGroup.setParentId(null);
      }
      else
      {
         exoGroup.setParentId(id.substring(0, id.length() - jbidGroup.getName().length() - 1));
      }

      return exoGroup;
   }

   private String getGroupId(String groupName) throws Exception
   {
      if (groupName.equals(orgService.getExoRootGroupName()))
      {
         return "";
      }

      org.jboss.identity.idm.api.Group jbidGroup =
         getIdentitySession().getPersistenceManager().findGroup(groupName, orgService.getExoGroupType());

      Collection<org.jboss.identity.idm.api.Group> parents =
         getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, orgService.getExoGroupType(),
            false, false);

      if (parents.size() > 1)
      {
         throw new IllegalStateException("Group has more than one parent: " + groupName);
      }

      if (parents.size() == 0)
      {
         //As there is special root group this shouldn't happen:
         throw new IllegalStateException("Group present that is not connected to the root: " + groupName);

         // This group is at the root
         //return "/" + groupName;
      }

      String parentGroupId = getGroupId(((org.jboss.identity.idm.api.Group)parents.iterator().next()).getName());

      return parentGroupId + "/" + groupName;

   }

   private org.jboss.identity.idm.api.Group persistGroup(Group exoGroup) throws Exception
   {

      org.jboss.identity.idm.api.Group jbidGroup =
         getIdentitySession().getPersistenceManager().findGroup(exoGroup.getGroupName(), orgService.getExoGroupType());

      if (jbidGroup == null)
      {
         jbidGroup =
            getIdentitySession().getPersistenceManager().createGroup(exoGroup.getGroupName(),
               orgService.getExoGroupType());
      }

      String description = exoGroup.getDescription();
      String label = exoGroup.getLabel();

      List<Attribute> attrsList = new ArrayList<Attribute>();
      if (description != null)
      {
         attrsList.add(new SimpleAttribute(GROUP_DESCRIPTION, description));
      }

      if (label != null)
      {
         attrsList.add(new SimpleAttribute(GROUP_LABEL, label));
      }

      if (attrsList.size() > 0)
      {
         Attribute[] attrs = new Attribute[attrsList.size()];

         attrs = attrsList.toArray(attrs);

         getIdentitySession().getAttributesManager().addAttributes(jbidGroup, attrs);

      }

      return jbidGroup;
   }

   private IdentitySession getIdentitySession() throws Exception
   {
      return service_.getIdentitySession();
   }

   private org.jboss.identity.idm.api.Group getRootGroup() throws Exception
   {
      org.jboss.identity.idm.api.Group rootGroup =
         getIdentitySession().getPersistenceManager().findGroup(orgService.getExoRootGroupName(),
            orgService.getExoRootGroupType());

      if (rootGroup == null)
      {
         rootGroup =
            getIdentitySession().getPersistenceManager().createGroup(orgService.getExoRootGroupName(),
               orgService.getExoRootGroupType());
      }

      return rootGroup;
   }
}
