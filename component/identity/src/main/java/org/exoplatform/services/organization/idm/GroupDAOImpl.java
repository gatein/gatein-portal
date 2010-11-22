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

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;
import org.picketlink.idm.api.Attribute;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.impl.api.SimpleAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class GroupDAOImpl implements GroupHandler
{

   private static Logger log = LoggerFactory.getLogger(GroupDAOImpl.class);

   public static final String GROUP_LABEL = "label";

   public static final String GROUP_DESCRIPTION = "description";

   private PicketLinkIDMService service_;

   private List<GroupEventListener> listeners_;

   private PicketLinkIDMOrganizationServiceImpl orgService;

   public GroupDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService service)
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
      return new ExtGroup();
   }

   public void createGroup(Group group, boolean broadcast) throws Exception
   {
      addChild(null, group, broadcast);
   }

   public void addChild(Group parent, Group child, boolean broadcast) throws Exception
   {
      org.picketlink.idm.api.Group parentGroup = null;

      String childPLGroupName = getPLIDMGroupName(child.getGroupName());

      if (parent != null)
      {

         String parentPLGroupName = getPLIDMGroupName(parent.getGroupName());
         try
         {
            parentGroup =
               getIdentitySession().getPersistenceManager().
                  findGroup(parentPLGroupName, orgService.getConfiguration().getGroupType(parent.getParentId()));
         }
         catch (Exception e)
         {
            log.info("Cannot obtain group: " + parentPLGroupName, e);

         }

         ((ExtGroup)child).setId(parent.getId() + "/" + child.getGroupName());

      }
      else
      {
         ((ExtGroup)child).setId("/" + child.getGroupName());
      }

      if (broadcast)
      {
         preSave(child, true);
      }

      if (parentGroup != null)
      {
         ((ExtGroup)child).setParentId(parent.getId());
      }
      org.picketlink.idm.api.Group childGroup = persistGroup(child);

      try
      {
         if (parentGroup != null)
         {
            getIdentitySession().getRelationshipManager().associateGroups(parentGroup, childGroup);

         }
         else
         {
            getIdentitySession().getRelationshipManager().associateGroups(getRootGroup(), childGroup);
         }
      }
      catch (Exception e)
      {
         log.info("Cannot associate groups: ", e);
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

      org.picketlink.idm.api.Group jbidGroup = null;

      String plGroupName = getPLIDMGroupName(group.getGroupName());

      try
      {
         jbidGroup =
            getIdentitySession().getPersistenceManager().
               findGroup(plGroupName, orgService.getConfiguration().getGroupType(group.getParentId()));
      }
      catch (Exception e)
      {
         log.info("Cannot obtain group: " + plGroupName + "; ", e);
      }

      if (jbidGroup == null)
      {
         return group;
      }

      //      MembershipDAOImpl.removeMembershipEntriesOfGroup(group, getIdentitySession());

      try
      {
         Collection<org.picketlink.idm.api.Group> oneLevelChilds =
            getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, null, true, false);

         Collection<org.picketlink.idm.api.Group> allChilds =
            getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, null, true, true);

         getIdentitySession().getRelationshipManager().disassociateGroups(jbidGroup, oneLevelChilds);

         for (org.picketlink.idm.api.Group child : allChilds)
         {
            //TODO: impl force in IDM
            getIdentitySession().getPersistenceManager().removeGroup(child, true);
         }
      }
      catch (Exception e)
      {
         log.info("Cannot clear group relationships: " + plGroupName + "; ", e);
      }

      try
      {
         getIdentitySession().getPersistenceManager().removeGroup(jbidGroup, true);
      }
      catch (Exception e)
      {
         log.info("Cannot remove group: " + plGroupName + "; ", e);
      }

      if (broadcast)
      {
         postDelete(group);
      }
      return group;
   }

   public Collection findGroupByMembership(String userName, String membershipType) throws Exception
   {
      Collection<org.picketlink.idm.api.Role> allRoles = new HashSet();

      try
      {
         allRoles = getIdentitySession().getRoleManager().findRoles(userName, membershipType);
      }
      catch (Exception e)
      {
         log.info("Identity operation error: ", e);
      }

      Set<Group> exoGroups = new HashSet<Group>();

      MembershipDAOImpl mmm = (MembershipDAOImpl)orgService.getMembershipHandler();

      for (org.picketlink.idm.api.Role role : allRoles)
      {
         if (mmm.isCreateMembership(role.getRoleType().getName()))                            
         {
            exoGroups.add(convertGroup(role.getGroup()));
         }
      }

      if (mmm.isAssociationMapped() && mmm.getAssociationMapping().equals(membershipType))
      {
         Collection<org.picketlink.idm.api.Group> groups = new HashSet();

         try
         {
            groups = getIdentitySession().getRelationshipManager().findAssociatedGroups(userName, null);
         }
         catch (Exception e)
         {
            log.info("Identity operation error: ", e);
         }

         for (org.picketlink.idm.api.Group group : groups)
         {
            exoGroups.add(convertGroup(group));
         }

      }

      // UI has hardcoded casts to List
      return new LinkedList<Group>(exoGroups);
   }

   //
   public Group findGroupById(String groupId) throws Exception
   {

      org.picketlink.idm.api.Group jbidGroup = orgService.getJBIDMGroup(groupId);

      if (jbidGroup == null)
      {
         return null;
      }

      return convertGroup(jbidGroup);
   }

   public Collection findGroups(Group parent) throws Exception
   {
      org.picketlink.idm.api.Group jbidGroup = null;

      if (parent == null)
      {
         jbidGroup = getRootGroup();
      }
      else
      {
         try
         {
            String plGroupName = getPLIDMGroupName(parent.getGroupName());

            jbidGroup =
               getIdentitySession().getPersistenceManager().
                  findGroup(plGroupName, orgService.getConfiguration().getGroupType(parent.getParentId()));
         }
         catch (Exception e)
         {
            //TODO:
            log.info("Identity operation error: ", e);

         }
      }

      if (jbidGroup == null)
      {
         return Collections.emptyList();
      }

      String parentId = parent == null ? null : parent.getParentId();

      Set<org.picketlink.idm.api.Group> plGroups = new HashSet<org.picketlink.idm.api.Group>();


      try
      {
         plGroups.addAll(getIdentitySession().getRelationshipManager().
               findAssociatedGroups(jbidGroup, null, true, false));
      }
      catch (Exception e)
      {
         //TODO:
         log.info("Identity operation error: ", e);

      }

      // Get members of all types mapped below the parent group id path.
      if (orgService.getConfiguration().isForceMembershipOfMappedTypes())
      {

         String id = parent != null ? parent.getId() : "/";

         for (String type : orgService.getConfiguration().getTypes(id))
         {
            try
            {
               plGroups
                  .addAll(getIdentitySession().getPersistenceManager().findGroup(type));
            }
            catch (Exception e)
            {
               //TODO:
               log.info("Identity operation error: ", e);
            }
         }
      }

      Set<Group> exoGroups = new HashSet<Group>();

      org.picketlink.idm.api.Group root = getRootGroup();

      for (org.picketlink.idm.api.Group group : plGroups)
      {
         if (!group.equals(root))
         {
            Group g = convertGroup(group);

            // If membership of mapped types is forced then we need to exclude those that are not direct child
            if (orgService.getConfiguration().isForceMembershipOfMappedTypes())
            {
               String id = g.getParentId();
               if ((parent == null && id == null)
                  || (parent != null && id != null && id.equals(parent.getId()))
                  || (parent == null && id != null && id.equals("/")))
               {
                  exoGroups.add(g);
                  continue;
               }
            }
            else
            {
               exoGroups.add(g);
            }
         }
      }


      // UI has hardcoded casts to List
      List results = new LinkedList<Group>(exoGroups);

      if (orgService.getConfiguration().isSortGroups())
      {
         Collections.sort(results);
      }

      return results;

   }

   public Collection findGroupsOfUser(String user) throws Exception
   {

      if (user == null)
      {
         // julien : integration bug
         // need to look at that later
         //
         // Caused by: java.lang.IllegalArgumentException: User name cannot be null
         // at org.picketlink.idm.impl.api.session.managers.AbstractManager.checkNotNullArgument(AbstractManager.java:267)
         //  at org.picketlink.idm.impl.api.session.managers.RelationshipManagerImpl.findRelatedGroups(RelationshipManagerImpl.java:753)
         // at org.exoplatform.services.organization.idm.GroupDAOImpl.findGroupsOfUser(GroupDAOImpl.java:225)
         // at org.exoplatform.organization.webui.component.GroupManagement.isMemberOfGroup(GroupManagement.java:72)
         // at org.exoplatform.organization.webui.component.GroupManagement.isAdministrator(GroupManagement.java:125)
         // at org.exoplatform.organization.webui.component.UIGroupExplorer.<init>(UIGroupExplorer.java:57)
         return Collections.emptyList();
      }

      Collection<org.picketlink.idm.api.Group> allGroups = new HashSet();

      try
      {
         allGroups = getIdentitySession().getRelationshipManager().findRelatedGroups(user, null, null);
      }
      catch (Exception e)
      {
         //TODO:
         log.info("Identity operation error: ", e);

      }

      List<Group> exoGroups = new LinkedList<Group>();

      for (org.picketlink.idm.api.Group group : allGroups)
      {
         exoGroups.add(convertGroup(group));

      }

      return exoGroups;
   }

   public Collection getAllGroups() throws Exception
   {

      Set<org.picketlink.idm.api.Group> plGroups = new HashSet<org.picketlink.idm.api.Group>();

      try
      {
         plGroups
            .addAll(getIdentitySession().getRelationshipManager().findAssociatedGroups(getRootGroup(), null, true, true));
      }
      catch (Exception e)
      {
         //TODO:
         log.info("Identity operation error: ", e);

      }

      // Check for all type groups mapped as part of the group tree but not connected with the root group by association
      if (orgService.getConfiguration().isForceMembershipOfMappedTypes())
      {
         for (String type : orgService.getConfiguration().getAllTypes())
         {
            try
            {
               plGroups
                  .addAll(getIdentitySession().getPersistenceManager().findGroup(type));
            }
            catch (Exception e)
            {
               //TODO:
               log.info("Identity operation error: ", e);
            }
         }
      }

      Set<Group> exoGroups = new HashSet<Group>();

      org.picketlink.idm.api.Group root = getRootGroup();

      for (org.picketlink.idm.api.Group group : plGroups)
      {
         if (!group.equals(root))
         {
            exoGroups.add(convertGroup(group));
         }
      }

      // UI has hardcoded casts to List
      return new LinkedList<Group>(exoGroups);
      
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


   protected Group convertGroup(org.picketlink.idm.api.Group jbidGroup) throws Exception
   {
      Map<String, Attribute> attrs = new HashMap();

      try
      {
         attrs = getIdentitySession().getAttributesManager().getAttributes(jbidGroup);
      }
      catch (Exception e)
      {
         //TODO:
         log.info("Identity operation error: ", e);
      }

      String gtnGroupName = getGtnGroupName(jbidGroup.getName());

      ExtGroup exoGroup = new ExtGroup(gtnGroupName);

      if (attrs.containsKey(GROUP_DESCRIPTION) && attrs.get(GROUP_DESCRIPTION).getValue() != null)
      {
         exoGroup.setDescription(attrs.get(GROUP_DESCRIPTION).getValue().toString());
      }
      if (attrs.containsKey(GROUP_LABEL) && attrs.get(GROUP_LABEL).getValue() != null)
      {
         exoGroup.setLabel(attrs.get(GROUP_LABEL).getValue().toString());
      }
      // UI requires that group has label
      else
      {
         exoGroup.setLabel(gtnGroupName);
      }

      // Resolve full ID
      String id = getGroupId(jbidGroup);

      exoGroup.setId(id);

      // child of root
      if (id.length() == gtnGroupName.length() + 1)
      {
         exoGroup.setParentId(null);
      }
      else if (!id.equals("") && !id.equals("/"))
      {

         exoGroup.setParentId(id.substring(0, id.lastIndexOf("/")));
      }

      return exoGroup;
   }

   private String getGroupId(org.picketlink.idm.api.Group jbidGroup) throws Exception
   {
      if (jbidGroup.equals(getRootGroup()))
      {
         return "";
      }

      Collection<org.picketlink.idm.api.Group> parents = new HashSet();

      String gtnGroupName = getGtnGroupName(jbidGroup.getName());

      try
      {
         parents = getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, null, false, false);
      }
      catch (Exception e)
      {
         //TODO:
         log.info("Identity operation error: ", e);
      }


      if (parents.size() == 0 || parents.size() > 1)
      {

         if (parents.size() > 1)
         {
            log.info("PLIDM Group has more than one parent: " + jbidGroup.getName() + "; Will try to use parent path " +
               "defined by type mappings or just place it under root /");
         }


         String id = orgService.getConfiguration().getParentId(jbidGroup.getGroupType());



         if (id != null && orgService.getConfiguration().isForceMembershipOfMappedTypes())
         {
            if (id.endsWith("/*"))
            {
               id = id.substring(0, id.length() - 2);
            }

            return id + "/" + gtnGroupName;
         }


         // All groups not connected to the root should be just below the root
         return "/" + gtnGroupName;

         //TODO: make it configurable
         // throw new IllegalStateException("Group present that is not connected to the root: " + jbidGroup.getName());

      }

      String parentGroupId = getGroupId(((org.picketlink.idm.api.Group)parents.iterator().next()));

      return parentGroupId + "/" + gtnGroupName;

   }

   private org.picketlink.idm.api.Group persistGroup(Group exoGroup) throws Exception
   {

      org.picketlink.idm.api.Group jbidGroup = null;

      String plGroupName = getPLIDMGroupName(exoGroup.getGroupName());

      try
      {
         jbidGroup = getIdentitySession().getPersistenceManager().
               findGroup(plGroupName, orgService.getConfiguration().getGroupType(exoGroup.getParentId()));
      }
      catch (Exception e)
      {
         //TODO:
         log.info("Identity operation error: ", e);
      }

      if (jbidGroup == null)
      {
         try
         {
            jbidGroup =
               getIdentitySession().getPersistenceManager().
                  createGroup(plGroupName, orgService.getConfiguration().getGroupType(exoGroup.getParentId()));
         }
         catch (Exception e)
         {
            //TODO:
            log.info("Identity operation error: ", e);
         }
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

         try
         {
            getIdentitySession().getAttributesManager().updateAttributes(jbidGroup, attrs);
         }
         catch (Exception e)
         {
            //TODO:
            log.info("Identity operation error: ", e);
         }

      }

      return jbidGroup;
   }

   private IdentitySession getIdentitySession() throws Exception
   {
      return service_.getIdentitySession();
   }

   private org.picketlink.idm.api.Group getRootGroup() throws Exception
   {
      org.picketlink.idm.api.Group rootGroup =  null;
      try
      {
         rootGroup = getIdentitySession().getPersistenceManager().
            findGroup(orgService.getConfiguration().getRootGroupName(), orgService.getConfiguration().getGroupType("/"));
      }
      catch (Exception e)
      {
         //TODO:
         log.info("Identity operation error: ", e);
      }

      if (rootGroup == null)
      {
         try
         {
            rootGroup =
               getIdentitySession().getPersistenceManager().
                  createGroup(
                     orgService.getConfiguration().getRootGroupName(),
                     orgService.getConfiguration().getGroupType("/"));
         }
         catch (Exception e)
         {
            //TODO:
            log.info("Identity operation error: ", e);
         }
      }

      return rootGroup;
   }

   public String getPLIDMGroupName(String gtnGroupName)
   {
      return orgService.getConfiguration().getPLIDMGroupName(gtnGroupName);
   }

   public String getGtnGroupName(String plidmGroupName)
   {
      return orgService.getConfiguration().getGtnGroupName(plidmGroupName);
   }



}
