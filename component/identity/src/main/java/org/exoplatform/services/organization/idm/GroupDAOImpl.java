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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.gatein.common.logging.LogLevel;
import org.picketlink.idm.api.Attribute;
import org.picketlink.idm.api.Role;
import org.picketlink.idm.impl.api.SimpleAttribute;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class GroupDAOImpl extends AbstractDAOImpl implements GroupHandler {

    public static final String GROUP_LABEL = "label";

    public static final String GROUP_DESCRIPTION = "description";

    private List<GroupEventListener> listeners_;

    private static final String CYCLIC_ID = "org.gatein.portal.identity.LOOPED_GROUP_ID";

    public GroupDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService service) {
        super(orgService, service);
        listeners_ = new ArrayList<GroupEventListener>();
    }

    public void addGroupEventListener(GroupEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners_.add(listener);
    }

    public void removeGroupEventListener(GroupEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners_.remove(listener);
    }

    public final Group createGroupInstance() {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "createGroupInstance", null);
        }
        return new ExtGroup();
    }

    public void createGroup(Group group, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "createGroup", new Object[] { "broadcast", broadcast });
        }
        addChild(null, group, broadcast);
    }

    public void addChild(Group parent, Group child, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "addChild", new Object[] { "parent", parent, "child", child, "broadcast",
                    broadcast });
        }

        org.picketlink.idm.api.Group parentGroup = null;

        if (parent != null) {

            String parentPLGroupName = getPLIDMGroupName(parent.getGroupName());
            try {
                parentGroup = getIdentitySession().getPersistenceManager().findGroup(parentPLGroupName,
                        orgService.getConfiguration().getGroupType(parent.getParentId()));
            } catch (Exception e) {
                handleException("Cannot obtain group: " + parentPLGroupName, e);

            }
            if(parentGroup == null) {
                throw new Exception("Parent group is not exist");
            }

            ((ExtGroup) child).setId(parent.getId() + "/" + child.getGroupName());

        } else {
            ((ExtGroup) child).setId("/" + child.getGroupName());
        }

        if (broadcast) {
            preSave(child, true);
        }

        if (parentGroup != null) {
            ((ExtGroup) child).setParentId(parent.getId());
        }
        org.picketlink.idm.api.Group childGroup = persistGroup(child);

        try {
            if (parentGroup != null) {
                getIdentitySession().getRelationshipManager().associateGroups(parentGroup, childGroup);

            } else {
                getIdentitySession().getRelationshipManager().associateGroups(getRootGroup(), childGroup);
            }
        } catch (Exception e) {
            handleException("Cannot associate groups: ", e);
        }

        if (broadcast) {
            postSave(child, true);
        }

    }

    public void saveGroup(Group group, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "saveGroup", new Object[] { "group", group, "broadcast", broadcast });
        }

        if (broadcast) {
            preSave(group, false);
        }
        persistGroup(group);
        if (broadcast) {
            postSave(group, false);
        }
    }

    public Group removeGroup(Group group, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "removeGroup", new Object[] { "group", group, "broadcast", broadcast });
        }

        org.picketlink.idm.api.Group jbidGroup = null;

        String plGroupName = getPLIDMGroupName(group.getGroupName());

        try {

            orgService.flush();

            jbidGroup = getIdentitySession().getPersistenceManager().findGroup(plGroupName,
                    orgService.getConfiguration().getGroupType(group.getParentId()));
        } catch (Exception e) {
            handleException("Cannot obtain group: " + plGroupName + "; ", e);
        }

        if (jbidGroup == null) {
            //As test case suppose, api should throw exception here
            throw new Exception("Group is not exists");
            //return group;
        }

        // MembershipDAOImpl.removeMembershipEntriesOfGroup(group, getIdentitySession());

        //Check: Has this group got any child?
        Collection<org.picketlink.idm.api.Group> oneLevelChilds = null;
        orgService.flush();
        try {
            oneLevelChilds = getIdentitySession().getRelationshipManager()
                    .findAssociatedGroups(jbidGroup, null, true, false);
        } catch (Exception e) {
            handleException("Cannot clear group relationships: " + plGroupName + "; ", e);
        } finally {
            if(oneLevelChilds != null && oneLevelChilds.size() > 0) {
                throw new IllegalStateException("Group " + group.getGroupName() + " has at least one child group");
            }
        }

        //preDelete event should be raise here, when group will be really removed
        if (broadcast) {
            preDelete(group);
        }

        try {
            /*orgService.flush();

            Collection<org.picketlink.idm.api.Group> oneLevelChilds = getIdentitySession().getRelationshipManager()
                    .findAssociatedGroups(jbidGroup, null, true, false);

            Collection<org.picketlink.idm.api.Group> allChilds = getIdentitySession().getRelationshipManager()
                    .findAssociatedGroups(jbidGroup, null, true, true);

            getIdentitySession().getRelationshipManager().disassociateGroups(jbidGroup, oneLevelChilds);

            for (org.picketlink.idm.api.Group child : allChilds) {
                // TODO: impl force in IDM
                getIdentitySession().getPersistenceManager().removeGroup(child, true);
            }*/

            // Obtain parents

            Collection<org.picketlink.idm.api.Group> parents = getIdentitySession().getRelationshipManager()
                    .findAssociatedGroups(jbidGroup, null, false, false);

            // not possible to disassociate only one child...
            Set<org.picketlink.idm.api.Group> dummySet = new HashSet<org.picketlink.idm.api.Group>();
            dummySet.add(jbidGroup);

            for (org.picketlink.idm.api.Group parent : parents) {
                getIdentitySession().getRelationshipManager().disassociateGroups(parent, dummySet);
            }

        } catch (Exception e) {
            handleException("Cannot clear group relationships: " + plGroupName + "; ", e);
        }

        try {
            getIdentitySession().getPersistenceManager().removeGroup(jbidGroup, true);

        } catch (Exception e) {
            handleException("Cannot remove group: " + plGroupName + "; ", e);
        }

        if (broadcast) {
            postDelete(group);
        }
        return group;
    }

    public Collection<Group> findGroupByMembership(String userName, String membershipType) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findGroupsByMembership", new Object[] { "userName", membershipType });
        }

        Collection<Role> allRoles = new HashSet<Role>();

        try {
            orgService.flush();

            allRoles = getIdentitySession().getRoleManager().findRoles(userName, membershipType);
        } catch (Exception e) {
            handleException("Identity operation error: ", e);
        }

        Set<Group> exoGroups = new HashSet<Group>();

        MembershipDAOImpl mmm = (MembershipDAOImpl) orgService.getMembershipHandler();

        for (org.picketlink.idm.api.Role role : allRoles) {
            Group exoGroup = convertGroup(role.getGroup());
            if (mmm.isCreateMembership(role.getRoleType().getName(), exoGroup.getId())) {
                exoGroups.add(exoGroup);
            }
        }

        if (mmm.isAssociationMapped() && mmm.getAssociationMapping().equals(membershipType)) {
            Collection<org.picketlink.idm.api.Group> groups = new HashSet<org.picketlink.idm.api.Group>();

            try {
                orgService.flush();

                groups = getIdentitySession().getRelationshipManager().findAssociatedGroups(userName, null);
            } catch (Exception e) {
                handleException("Identity operation error: ", e);
            }

            for (org.picketlink.idm.api.Group group : groups) {
                exoGroups.add(convertGroup(group));
            }

        }

        // UI has hardcoded casts to List
        Collection<Group> result = new LinkedList<Group>(exoGroups);

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findGroupByMembership", result);
        }

        return result;
    }


//    @Override
//    public Collection<Group> resolveGroupByMembership(String userName, String membershipType) throws Exception {
//        if (log.isTraceEnabled()) {
//            Tools.logMethodIn(log, LogLevel.TRACE, "findGroupsByMembership", new Object[] { "userName", membershipType });
//        }
//
//        Collection<Role> roles = new HashSet<Role>();
//
//        try {
//            orgService.flush();
//
//            roles.addAll(getIdentitySession().getRoleManager().findRoles(userName, membershipType));
//
//            roles.addAll(getIdentitySession().getRoleManager().findRoles(userName, MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));
//        } catch (Exception e) {
//            handleException("Identity operation error: ", e);
//        }
//
//        Set<Group> exoGroups = new HashSet<Group>();
//
//        MembershipDAOImpl mmm = (MembershipDAOImpl) orgService.getMembershipHandler();
//
//        for (org.picketlink.idm.api.Role role : roles) {
//            Group exoGroup = convertGroup(role.getGroup());
//            if (mmm.isCreateMembership(role.getRoleType().getName(), exoGroup.getId())) {
//                exoGroups.add(exoGroup);
//            }
//        }
//
//        if (mmm.isAssociationMapped() && mmm.getAssociationMapping().equals(membershipType)) {
//            Collection<org.picketlink.idm.api.Group> groups = new HashSet<org.picketlink.idm.api.Group>();
//
//            try {
//                orgService.flush();
//
//                groups = getIdentitySession().getRelationshipManager().findAssociatedGroups(userName, null);
//            } catch (Exception e) {
//                handleException("Identity operation error: ", e);
//            }
//
//            for (org.picketlink.idm.api.Group group : groups) {
//                exoGroups.add(convertGroup(group));
//            }
//
//        }
//
//        // UI has hardcoded casts to List
//        Collection<Group> result = new LinkedList<Group>(exoGroups);
//
//        if (log.isTraceEnabled()) {
//            Tools.logMethodOut(log, LogLevel.TRACE, "findGroupByMembership", result);
//        }
//
//        return result;
//    }

    //
    public Group findGroupById(String groupId) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findGroupById", new Object[] { "groupId", groupId });
        }

        org.picketlink.idm.api.Group jbidGroup = orgService.getJBIDMGroup(groupId);

        if (jbidGroup == null) {
            if (log.isTraceEnabled()) {
                Tools.logMethodOut(log, LogLevel.TRACE, "findGroupById", null);
            }

            return null;
        }

        Group result = convertGroup(jbidGroup);

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findGroupById", result);
        }

        return result;

    }

    public Collection<Group> findGroups(Group parent) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findGroups", new Object[] { "parent", parent });
        }

        org.picketlink.idm.api.Group jbidGroup = null;

        if (parent == null) {
            jbidGroup = getRootGroup();
        } else {
            try {
                String plGroupName = getPLIDMGroupName(parent.getGroupName());

                jbidGroup = getIdentitySession().getPersistenceManager().findGroup(plGroupName,
                        orgService.getConfiguration().getGroupType(parent.getParentId()));
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }
        }

        if (jbidGroup == null) {
            return Collections.emptyList();
        }

        Set<org.picketlink.idm.api.Group> plGroups = new HashSet<org.picketlink.idm.api.Group>();

        try {
            orgService.flush();

            plGroups.addAll(getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, null, true, false));
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        // Get members of all types mapped below the parent group id path.
        if (orgService.getConfiguration().isForceMembershipOfMappedTypes()) {

            String id = parent != null ? parent.getId() : "/";

            for (String type : orgService.getConfiguration().getTypes(id)) {
                try {
                    plGroups.addAll(getIdentitySession().getPersistenceManager().findGroup(type));
                } catch (Exception e) {
                    // TODO:
                    handleException("Identity operation error: ", e);
                }
            }
        }

        Set<Group> exoGroups = new HashSet<Group>();

        org.picketlink.idm.api.Group root = getRootGroup();

        for (org.picketlink.idm.api.Group group : plGroups) {
            if (!group.equals(root)) {
                Group g = convertGroup(group);

                // If membership of mapped types is forced then we need to exclude those that are not direct child
                if (orgService.getConfiguration().isForceMembershipOfMappedTypes()) {
                    String id = g.getParentId();
                    if ((parent == null && id == null) || (parent != null && id != null && id.equals(parent.getId()))
                            || (parent == null && id != null && id.equals("/"))) {
                        exoGroups.add(g);
                        continue;
                    }
                } else {
                    exoGroups.add(g);
                }
            }
        }

        // UI has hardcoded casts to List
        List results = new LinkedList<Group>(exoGroups);

        if (orgService.getConfiguration().isSortGroups()) {
            Collections.sort(results);
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findGroups", results);
        }

        return results;

    }

    public Collection<Group> findGroupsOfUser(String user) throws Exception {

        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findGroupsOfUser", new Object[] { "user", user });
        }

        if (user == null) {
            // julien : integration bug
            // need to look at that later
            //
            // Caused by: java.lang.IllegalArgumentException: User name cannot be null
            // at org.picketlink.idm.impl.api.session.managers.AbstractManager.checkNotNullArgument(AbstractManager.java:267)
            // at
            // org.picketlink.idm.impl.api.session.managers.RelationshipManagerImpl.findRelatedGroups(RelationshipManagerImpl.java:753)
            // at org.exoplatform.services.organization.idm.GroupDAOImpl.findGroupsOfUser(GroupDAOImpl.java:225)
            // at org.exoplatform.organization.webui.component.GroupManagement.isMemberOfGroup(GroupManagement.java:72)
            // at org.exoplatform.organization.webui.component.GroupManagement.isAdministrator(GroupManagement.java:125)
            // at org.exoplatform.organization.webui.component.UIGroupExplorer.<init>(UIGroupExplorer.java:57)

            if (log.isTraceEnabled()) {
                Tools.logMethodOut(log, LogLevel.TRACE, "findGroupsOfUser", Collections.emptyList());
            }

            return Collections.emptyList();
        }

        Collection<org.picketlink.idm.api.Group> allGroups = new HashSet<org.picketlink.idm.api.Group>();

        try {
            orgService.flush();

            allGroups = getIdentitySession().getRelationshipManager().findRelatedGroups(user, null, null);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        List<Group> exoGroups = new LinkedList<Group>();

        for (org.picketlink.idm.api.Group group : allGroups) {
            exoGroups.add(convertGroup(group));

        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findGroupsOfUser", exoGroups);
        }

        return exoGroups;
    }

    public Collection<Group> getAllGroups() throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "getAllGroups", null);
        }

        Set<org.picketlink.idm.api.Group> plGroups = new HashSet<org.picketlink.idm.api.Group>();

        try {

            orgService.flush();

            plGroups.addAll(getIdentitySession().getRelationshipManager()
                    .findAssociatedGroups(getRootGroup(), null, true, true));
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        // Check for all type groups mapped as part of the group tree but not connected with the root group by association
        if (orgService.getConfiguration().isForceMembershipOfMappedTypes()) {
            for (String type : orgService.getConfiguration().getAllTypes()) {
                try {
                    plGroups.addAll(getIdentitySession().getPersistenceManager().findGroup(type));
                } catch (Exception e) {
                    // TODO:
                    handleException("Identity operation error: ", e);
                }
            }
        }

        Set<Group> exoGroups = new HashSet<Group>();

        org.picketlink.idm.api.Group root = getRootGroup();

        for (org.picketlink.idm.api.Group group : plGroups) {
            if (!group.equals(root)) {
                exoGroups.add(convertGroup(group));
            }
        }

        // UI has hardcoded casts to List
        Collection<Group> result = new LinkedList<Group>(exoGroups);

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "getAllGroups", result);
        }

        return result;

    }

    private void preSave(Group group, boolean isNew) throws Exception {
        for (GroupEventListener listener : listeners_) {
            listener.preSave(group, isNew);
        }
    }

    private void postSave(Group group, boolean isNew) throws Exception {
        for (GroupEventListener listener : listeners_) {
            listener.postSave(group, isNew);
        }
    }

    private void preDelete(Group group) throws Exception {
        for (GroupEventListener listener : listeners_) {
            listener.preDelete(group);
        }
    }

    private void postDelete(Group group) throws Exception {
        for (GroupEventListener listener : listeners_) {
            listener.postDelete(group);
        }
    }

    protected Group convertGroup(org.picketlink.idm.api.Group jbidGroup) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "convertGroup", new Object[] { "jbidGroup", jbidGroup });
        }

        Map<String, Attribute> attrs = new HashMap<String, Attribute>();

        try {
            orgService.flush();

            attrs = getIdentitySession().getAttributesManager().getAttributes(jbidGroup);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);
        }

        String gtnGroupName = getGtnGroupName(jbidGroup.getName());

        ExtGroup exoGroup = new ExtGroup(gtnGroupName);

        if (attrs.containsKey(GROUP_DESCRIPTION) && attrs.get(GROUP_DESCRIPTION).getValue() != null) {
            exoGroup.setDescription(attrs.get(GROUP_DESCRIPTION).getValue().toString());
        }
        if (attrs.containsKey(GROUP_LABEL) && attrs.get(GROUP_LABEL).getValue() != null) {
            exoGroup.setLabel(attrs.get(GROUP_LABEL).getValue().toString());

            // UI requires that group has label
        } else {
            exoGroup.setLabel(gtnGroupName);
        }

        // Resolve full ID
        String id = getGroupId(jbidGroup, null);

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "getGroupId", id);
        }

        exoGroup.setId(id);

        // child of root
        if (id.length() == gtnGroupName.length() + 1) {
            exoGroup.setParentId(null);
        } else if (!id.equals("") && !id.equals("/")) {

            exoGroup.setParentId(id.substring(0, id.lastIndexOf("/")));
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "convertGroup", exoGroup);
        }

        return exoGroup;
    }

    /**
     * Calculates group id by checking all parents up to the root group or group type mapping from the configuration.
     *
     * @param jbidGroup
     * @param processed
     * @return
     * @throws Exception
     */
    private String getGroupId(org.picketlink.idm.api.Group jbidGroup, List<org.picketlink.idm.api.Group> processed)
            throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "getGroupId",
                    new Object[] { "jbidGroup", jbidGroup, "processed", processed });
        }

        // Check in cache
        if (getIntegrationCache() != null) {
            String cachedId = getIntegrationCache().getGtnGroupId(getCacheNS(), jbidGroup.getKey());
            if (cachedId != null) {
                return cachedId;
            }
        }

        if (jbidGroup.equals(getRootGroup())) {
            String calculatedId = "";

            if (getIntegrationCache() != null) {
                getIntegrationCache().putGtnGroupId(getCacheNS(), jbidGroup.getKey(), calculatedId);
            }

            return calculatedId;
        }

        if (processed == null) {
            processed = new LinkedList<org.picketlink.idm.api.Group>();
        }

        Collection<org.picketlink.idm.api.Group> parents = new HashSet<org.picketlink.idm.api.Group>();

        String gtnGroupName = getGtnGroupName(jbidGroup.getName());

        try {
            orgService.flush();

            parents = getIdentitySession().getRelationshipManager().findAssociatedGroups(jbidGroup, null, false, false);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);
        }

        // Check if there is cross reference so we ended in a loop and break the process.
        if (parents.size() > 0 && processed.contains(parents.iterator().next())) {
            if (log.isTraceEnabled()) {
                log.trace("Detected looped relationship between groups!!!");
            }
            processed.remove(processed.size() - 1);
            return CYCLIC_ID;
        }
        // If there are no parents or more then one parent
        if (parents.size() == 0 || parents.size() > 1) {

            if (parents.size() > 1) {
                log.info("PLIDM Group has more than one parent: " + jbidGroup.getName() + "; Will try to use parent path "
                        + "defined by type mappings or just place it under root /");
            }

            String calculatedId = obtainMappedId(jbidGroup, gtnGroupName);

            if (getIntegrationCache() != null) {
                getIntegrationCache().putGtnGroupId(getCacheNS(), jbidGroup.getKey(), calculatedId);
            }

            return calculatedId;

        }

        processed.add(jbidGroup);
        String parentGroupId = getGroupId(((org.picketlink.idm.api.Group) parents.iterator().next()), processed);

        // Check if loop occured
        if (parentGroupId.equals(CYCLIC_ID)) {
            // if there are still processed groups in the list we are in nested call so remove last one and go back
            if (processed.size() > 0) {
                processed.remove(processed.size() - 1);
                return parentGroupId;

                // if we finally reached the first group from the looped ones then just return id calculated from
                // mappings or connect it to the root
            } else {
                String calculatedId = obtainMappedId(jbidGroup, gtnGroupName);

                if (getIntegrationCache() != null) {
                    getIntegrationCache().putGtnGroupId(getCacheNS(), jbidGroup.getKey(), calculatedId);
                }

                return calculatedId;
            }
        }

        String calculatedId = parentGroupId + "/" + gtnGroupName;

        if (getIntegrationCache() != null) {
            getIntegrationCache().putGtnGroupId(getCacheNS(), jbidGroup.getKey(), calculatedId);
        }

        return calculatedId;

    }

    /**
     * Obtain group id based on groupType mapping from configuration or if this fails just place it under root /
     *
     * @param jbidGroup
     * @param gtnGroupName
     * @return
     */
    private String obtainMappedId(org.picketlink.idm.api.Group jbidGroup, String gtnGroupName) {
        String id = orgService.getConfiguration().getParentId(jbidGroup.getGroupType());

        if (id != null && orgService.getConfiguration().isForceMembershipOfMappedTypes()) {
            if (id.endsWith("/*")) {
                id = id.substring(0, id.length() - 2);
            }

            return id + "/" + gtnGroupName;
        }

        // All groups not connected to the root should be just below the root
        return "/" + gtnGroupName;

        // TODO: make it configurable
        // throw new IllegalStateException("Group present that is not connected to the root: " + jbidGroup.getName());
    }

    private org.picketlink.idm.api.Group persistGroup(Group exoGroup) {

        org.picketlink.idm.api.Group jbidGroup = null;

        String plGroupName = getPLIDMGroupName(exoGroup.getGroupName());

        try {
            jbidGroup = getIdentitySession().getPersistenceManager().findGroup(plGroupName,
                    orgService.getConfiguration().getGroupType(exoGroup.getParentId()));
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);
        }

        if (jbidGroup == null) {
            try {
                jbidGroup = getIdentitySession().getPersistenceManager().createGroup(plGroupName,
                        orgService.getConfiguration().getGroupType(exoGroup.getParentId()));
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);
            }
        }

        String description = exoGroup.getDescription();
        String label = exoGroup.getLabel();

        List<Attribute> attrsList = new ArrayList<Attribute>();
        if (description != null) {
            attrsList.add(new SimpleAttribute(GROUP_DESCRIPTION, description));
        }

        if (label != null) {
            attrsList.add(new SimpleAttribute(GROUP_LABEL, label));
        }

        if (attrsList.size() > 0) {
            Attribute[] attrs = new Attribute[attrsList.size()];

            attrs = attrsList.toArray(attrs);

            try {
                getIdentitySession().getAttributesManager().updateAttributes(jbidGroup, attrs);
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);
            }

        }

        return jbidGroup;
    }

    private IntegrationCache getIntegrationCache() {
        // TODO: refactor to remove cast. For now to avoid adding new config option and share existing cache instannce
        // TODO: it should be there.
        return ((PicketLinkIDMServiceImpl) service_).getIntegrationCache();
    }

    /**
     * Returns namespace to be used with integration cache
     *
     * @return
     */
    private String getCacheNS() {
        // TODO: refactor to remove cast. For now to avoid adding new config option and share existing cache instannce
        // TODO: it should be there.
        return ((PicketLinkIDMServiceImpl) service_).getRealmName();
    }

    /**
     * Returns mock of PLIDM group representing "/" group. This method uses cache and delegates to obtainRootGroup().
     *
     * @return
     * @throws Exception
     */
    protected org.picketlink.idm.api.Group getRootGroup() throws Exception {
        org.picketlink.idm.api.Group rootGroup = null;

        if (getIntegrationCache() != null) {
            rootGroup = getIntegrationCache().getRootGroup(getCacheNS());
        }

        if (rootGroup == null) {
            rootGroup = obtainRootGroup();

            if (getIntegrationCache() != null) {
                getIntegrationCache().putRootGroup(getCacheNS(), rootGroup);
            }
        }

        return rootGroup;

    }

    /**
     * Obtains PLIDM group representing "/" group. If such group doens't exist it creates one.
     *
     * @return
     * @throws Exception
     */
    protected org.picketlink.idm.api.Group obtainRootGroup() {
        org.picketlink.idm.api.Group rootGroup = null;
        try {
            rootGroup = getIdentitySession().getPersistenceManager().findGroup(
                    orgService.getConfiguration().getRootGroupName(), orgService.getConfiguration().getGroupType("/"));
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);
        }

        if (rootGroup == null) {
            try {
                rootGroup = getIdentitySession().getPersistenceManager().createGroup(
                        orgService.getConfiguration().getRootGroupName(), orgService.getConfiguration().getGroupType("/"));
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);
            }
        }

        return rootGroup;
    }

    public String getPLIDMGroupName(String gtnGroupName) {
        return orgService.getConfiguration().getPLIDMGroupName(gtnGroupName);
    }

    public String getGtnGroupName(String plidmGroupName) {
        return orgService.getConfiguration().getGtnGroupName(plidmGroupName);
    }
}
