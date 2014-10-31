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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.InvalidNameException;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.commons.utils.ListenerStack;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;
import org.gatein.common.logging.LogLevel;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.Role;
import org.picketlink.idm.api.RoleType;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class MembershipDAOImpl extends AbstractDAOImpl implements MembershipHandler {

    private List listeners_;

    public MembershipDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService service) {
        super(orgService, service);
        listeners_ = new ListenerStack(5);
    }

    public void addMembershipEventListener(MembershipEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners_.add(listener);
    }

    public void removeMembershipEventListener(MembershipEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners_.remove(listener);
    }

    public final Membership createMembershipInstance() {
        return new MembershipImpl();
    }

    public void createMembership(Membership m, boolean broadcast) throws Exception {

        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "createMembership",
                    new Object[] { "membership", m, "broadcast", broadcast, });
        }

        if (broadcast) {
            preSave(m, true);
        }

        saveMembership(m, false);

        if (broadcast) {
            postSave(m, true);
        }

    }

    public void linkMembership(User user, Group g, MembershipType mt, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "linkMembership", new Object[] { "user", user, "group", g, "membershipType",
                    mt, "broadcast", broadcast });
        }

        orgService.flush();

        if (user == null) {
            throw new InvalidNameException("Can not create membership record because user is null");
        }
        if (orgService.getUserHandler().findUserByName(user.getUserName()) == null) {
            throw new InvalidNameException("Can not create membership record because user " + user.getUserName() + " does not exist.");
        }


        if (g == null) {
            throw new InvalidNameException("Can not create membership record for " + user.getUserName()
                    + " because group is null");
        }
        //Check group exist
        Group g1 = this.orgService.getGroupHandler().findGroupById(g.getId());
        if(g1 == null) {
            throw new InvalidNameException("Can not create membership record for " + user.getUserName()
                    + " because group " + g.getGroupName() + " is not exist");
        }

        if (mt == null) {
            throw new InvalidNameException("Can not create membership record for " + user.getUserName()
                    + " because membership type is null");
        }

        if (orgService.getMembershipTypeHandler().findMembershipType(mt.getName()) == null) {
            throw new InvalidNameException("MembershipType doesn't exist: " + mt.getName());
        }

        String plGroupName = getPLIDMGroupName(g.getGroupName());

        String groupId = getIdentitySession().getPersistenceManager().createGroupKey(plGroupName,
                orgService.getConfiguration().getGroupType(g.getParentId()));

        if (isCreateMembership(mt.getName(), g.getId())) {
            if (getIdentitySession().getRoleManager().getRoleType(mt.getName()) == null) {
                getIdentitySession().getRoleManager().createRoleType(mt.getName());
            }

            if (getIdentitySession().getRoleManager().hasRole(user.getUserName(), groupId, mt.getName())) {
                return;
            }
        }

        MembershipImpl membership = new MembershipImpl();
        membership.setMembershipType(mt.getName());
        membership.setUserName(user.getUserName());
        membership.setGroupId(g.getId());

        if (broadcast) {
            preSave(membership, true);
        }

        if (isAssociationMapped() && getAssociationMapping().equals(mt.getName())) {
            if(!getIdentitySession().getRelationshipManager().isAssociatedByKeys(groupId, user.getUserName())) {
                getIdentitySession().getRelationshipManager().associateUserByKeys(groupId, user.getUserName());
            }
        }

        if (isCreateMembership(mt.getName(), g.getId())) {
            getIdentitySession().getRoleManager().createRole(mt.getName(), user.getUserName(), groupId);
        }

        if (broadcast) {
            postSave(membership, true);
        }

    }

    public void saveMembership(Membership m, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "saveMembership", new Object[] { "membership", m, "broadcast", broadcast });
        }

        orgService.flush();

        String plGroupName = getPLIDMGroupName(getGroupNameFromId(m.getGroupId()));

        String groupId = getIdentitySession().getPersistenceManager().createGroupKey(plGroupName,
                getGroupTypeFromId(m.getGroupId()));

        boolean hasRole = false;

        try {
            hasRole = getIdentitySession().getRoleManager().hasRole(m.getUserName(), groupId, m.getMembershipType());
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        if (hasRole) {
            return;
        }

        if (broadcast) {
            preSave(m, false);
        }

        if (isCreateMembership(m.getMembershipType(), m.getGroupId())) {

            try {
                getIdentitySession().getRoleManager().createRole(m.getMembershipType(), m.getUserName(), groupId);
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }
        }
        if (isAssociationMapped() && getAssociationMapping().equals(m.getMembershipType())) {
            try {
                getIdentitySession().getRelationshipManager().associateUserByKeys(groupId, m.getUserName());
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }
        }

        if (broadcast) {
            postSave(m, false);
        }
    }

    public Membership removeMembership(String id, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "removeMembership", new Object[] { "id", id, "broadcast", broadcast });
        }

        orgService.flush();

        Membership m = null;
        try {
            m = new MembershipImpl(id);
        } catch (ArrayIndexOutOfBoundsException e) {
            //If MembershipID is not valid with format 'membershipType:username:groupId'
            //It is seemed as membership not exist
            return null;
        }

        String plGroupName = getPLIDMGroupName(getGroupNameFromId(m.getGroupId()));

        String groupId = getIdentitySession().getPersistenceManager().createGroupKey(plGroupName,
                getGroupTypeFromId(m.getGroupId()));

        boolean hasRole = false;

        try {
            hasRole = getIdentitySession().getRoleManager().hasRole(m.getUserName(), groupId, m.getMembershipType());
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        boolean associated = false;

        try {
            associated = getIdentitySession().getRelationshipManager().isAssociatedByKeys(groupId, m.getUserName());
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);
        }

        if (!hasRole && !(isAssociationMapped() && getAssociationMapping().equals(m.getMembershipType()) && associated)) {
            //As test case expect, if meembership does not exist
            //This method should return null
            return null;
            //return m;
        }

        if (broadcast) {
            preDelete(m);
        }

        if (isCreateMembership(m.getMembershipType(), m.getGroupId())) {

            try {
                getIdentitySession().getRoleManager().removeRole(m.getMembershipType(), m.getUserName(), groupId);
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }
        }

        if (isAssociationMapped() && getAssociationMapping().equals(m.getMembershipType()) && associated) {
            Set<String> keys = new HashSet<String>();
            keys.add(m.getUserName());
            try {
                getIdentitySession().getRelationshipManager().disassociateUsersByKeys(groupId, keys);
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }
        }

        if (broadcast) {
            postDelete(m);
        }
        return m;
    }

    public Collection removeMembershipByUser(String userName, boolean broadcast) throws Exception {

        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "removeMembershipByUser", new Object[] { "userName", userName, "broadcast",
                    broadcast });
        }

        orgService.flush();

        Collection<Role> roles = new HashSet();

        try {
            roles = getIdentitySession().getRoleManager().findRoles(userName, null);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        HashSet<MembershipImpl> memberships = new HashSet<MembershipImpl>();

        for (Role role : roles) {
            MembershipImpl m = new MembershipImpl();
            Group g = ((GroupDAOImpl) orgService.getGroupHandler()).convertGroup(role.getGroup());
            m.setGroupId(g.getId());
            m.setUserName(role.getUser().getId());
            m.setMembershipType(role.getRoleType().getName());
            memberships.add(m);

            if (broadcast) {
                preDelete(m);
            }

            getIdentitySession().getRoleManager().removeRole(role);

            if (broadcast) {
                postDelete(m);
            }

        }

        if (isAssociationMapped()) {

            Collection<org.picketlink.idm.api.Group> groups = new HashSet();

            try {
                groups = getIdentitySession().getRelationshipManager().findAssociatedGroups(userName, null);
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }

            Set<String> keys = new HashSet<String>();
            keys.add(userName);

            for (org.picketlink.idm.api.Group group : groups) {
                try {
                    getIdentitySession().getRelationshipManager().disassociateUsersByKeys(group.getKey(), keys);
                } catch (Exception e) {
                    // TODO:
                    handleException("Identity operation error: ", e);

                }
            }

        }

        // TODO: Exo UI has hardcoded casts to List
        return new LinkedList(memberships);

    }

    public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception {

        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findMembershipByUserAndType", new Object[] { "userName", userName,
                    "groupId", groupId, "type", type, });
        }

        orgService.flush();

        String plGroupName = getPLIDMGroupName(getGroupNameFromId(groupId));

        String gid = getIdentitySession().getPersistenceManager().createGroupKey(plGroupName, getGroupTypeFromId(groupId));

        boolean hasMembership = false;

        boolean associated = false;

        try {
            associated = getIdentitySession().getRelationshipManager().isAssociatedByKeys(gid, userName);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        if (isAssociationMapped() && getAssociationMapping().equals(type) && associated) {
            hasMembership = true;
        }

        Role role = null;

        try {
            role = getIdentitySession().getRoleManager().getRole(type, userName, gid);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        if (role != null
                && (!isAssociationMapped() || !getAssociationMapping().equals(role.getRoleType()) || !ignoreMappedMembershipType(groupId))) {
            hasMembership = true;
        }

        Membership result = null;

        if (hasMembership) {

            MembershipImpl m = new MembershipImpl();
            m.setGroupId(groupId);
            m.setUserName(userName);
            m.setMembershipType(type);

            result = m;
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findMembershipByUserGroupAndType", result);
        }

        return result;
    }

    public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findMembershipByUserAndGroup", new Object[] { "userName", userName,
                    "groupId", groupId, });
        }

        orgService.flush();

        if (userName == null) {
            // julien fix : if user name is null, need to check if we do need to return a special group

            if (log.isTraceEnabled()) {
                Tools.logMethodOut(log, LogLevel.TRACE, "findMembershipByUserAndGroup", Collections.emptyList());
            }

            return Collections.emptyList();
        }

        String plGroupName = getPLIDMGroupName(getGroupNameFromId(groupId));

        String gid = getIdentitySession().getPersistenceManager().createGroupKey(plGroupName, getGroupTypeFromId(groupId));

        Collection<RoleType> roleTypes = new HashSet();

        try {
            roleTypes = getIdentitySession().getRoleManager().findRoleTypes(userName, gid, null);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        HashSet<MembershipImpl> memberships = new HashSet<MembershipImpl>();

        for (RoleType roleType : roleTypes) {
            if (isCreateMembership(roleType.getName(), groupId)) {
                MembershipImpl m = new MembershipImpl();
                m.setGroupId(groupId);
                m.setUserName(userName);
                m.setMembershipType(roleType.getName());
                memberships.add(m);
            }
        }

        boolean associated = false;

        try {
            associated = getIdentitySession().getRelationshipManager().isAssociatedByKeys(gid, userName);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        if (isAssociationMapped() && associated) {
            MembershipImpl m = new MembershipImpl();
            m.setGroupId(groupId);
            m.setUserName(userName);
            m.setMembershipType(getAssociationMapping());
            memberships.add(m);
        }

        // TODO: Exo UI has hardcoded casts to List
        Collection result = new LinkedList(memberships);

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findMembershipByUserAndGroup", result);
        }

        return result;
    }

    public Collection findMembershipsByUser(String userName) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findMembershipsByUser", new Object[] { "userName", userName });
        }

        orgService.flush();

        Collection<Role> roles = new HashSet();

        try {
            roles = getIdentitySession().getRoleManager().findRoles(userName, null);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        HashSet<MembershipImpl> memberships = new HashSet<MembershipImpl>();

        for (Role role : roles) {
            Group g = ((GroupDAOImpl) orgService.getGroupHandler()).convertGroup(role.getGroup());
            if (isCreateMembership(role.getRoleType().getName(), g.getId())) {
                MembershipImpl m = new MembershipImpl();
                m.setGroupId(g.getId());
                m.setUserName(role.getUser().getId());

                // LDAP store may return raw membership type as role type
                if (role.getRoleType().getName().equals("JBOSS_IDENTITY_MEMBERSHIP")) {
                    m.setMembershipType(orgService.getConfiguration().getAssociationMembershipType());
                } else {
                    m.setMembershipType(role.getRoleType().getName());
                }

                memberships.add(m);
            }
        }

        if (isAssociationMapped()) {

            Collection<org.picketlink.idm.api.Group> groups = new HashSet();

            try {
                groups = getIdentitySession().getRelationshipManager().findAssociatedGroups(userName, null);
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }

            for (org.picketlink.idm.api.Group group : groups) {
                MembershipImpl m = new MembershipImpl();
                Group g = ((GroupDAOImpl) orgService.getGroupHandler()).convertGroup(group);
                m.setGroupId(g.getId());
                m.setUserName(userName);
                m.setMembershipType(getAssociationMapping());
                memberships.add(m);
            }

        }

        Collection result = new LinkedList(memberships);

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findMembershipsByUser", result);
        }

        return result;
    }

    public ListAccess<Membership> findAllMembershipsByUser(User user) throws Exception {
        org.picketlink.idm.api.User gtnUser = service_.getIdentitySession().getPersistenceManager()
                .findUser(user.getUserName());

        if (gtnUser == null) {
            log.log(LogLevel.ERROR, "Internal ERROR. Cannot obtain user: " + user.getUserName());
            return new ListAccessImpl(Membership.class, Collections.emptyList());
        }
        return new IDMMembershipListAccess(gtnUser, !orgService.getConfiguration().isSkipPaginationInMembershipQuery());
    }

    public Collection findMembershipsByGroup(Group group) throws Exception {
        return findMembershipsByGroupId(group.getId());
    }

    public ListAccess<Membership> findAllMembershipsByGroup(Group group) throws Exception {
        String plGroupName = getPLIDMGroupName(getGroupNameFromId(group.getId()));

        String gid = getIdentitySession().getPersistenceManager()
                .createGroupKey(plGroupName, getGroupTypeFromId(group.getId()));

        org.picketlink.idm.api.Group gtnGroup = service_.getIdentitySession().getPersistenceManager().findGroupByKey(gid);

        if (gtnGroup == null) {
            log.log(LogLevel.ERROR, "Internal ERROR. Cannot obtain group: " + group.getId());
            return new ListAccessImpl(Membership.class, Collections.emptyList());
        }
        return new IDMMembershipListAccess(gtnGroup, !orgService.getConfiguration().isSkipPaginationInMembershipQuery());
    }

    public Collection findMembershipsByGroupId(String groupId) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findMembershipsByGroup", new Object[] { "groupId", groupId });
        }

        orgService.flush();

        String plGroupName = getPLIDMGroupName(getGroupNameFromId(groupId));

        String gid = getIdentitySession().getPersistenceManager().createGroupKey(plGroupName, getGroupTypeFromId(groupId));

        Collection<Role> roles = new HashSet();

        try {
            roles = getIdentitySession().getRoleManager().findRoles(gid, null);
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        HashSet<MembershipImpl> memberships = new HashSet<MembershipImpl>();

        Group g = orgService.getGroupHandler().findGroupById(groupId);

        for (Role role : roles) {
            if (isCreateMembership(role.getRoleType().getName(), g.getId())) {
                MembershipImpl m = new MembershipImpl();
                m.setGroupId(g.getId());
                m.setUserName(role.getUser().getId());
                m.setMembershipType(role.getRoleType().getName());
                memberships.add(m);
            }
        }

        if (isAssociationMapped()) {

            Collection<org.picketlink.idm.api.User> users = new HashSet();

            try {
                users = getIdentitySession().getRelationshipManager().findAssociatedUsers(gid, false, null);
            } catch (Exception e) {
                // TODO:
                handleException("Identity operation error: ", e);

            }

            for (org.picketlink.idm.api.User user : users) {
                MembershipImpl m = new MembershipImpl();
                m.setGroupId(groupId);
                m.setUserName(user.getId());
                m.setMembershipType(getAssociationMapping());
                memberships.add(m);
            }

        }

        // TODO: Exo UI has harcoded casts to List
        List<MembershipImpl> results = new LinkedList<MembershipImpl>(memberships);

        if (orgService.getConfiguration().isSortMemberships()) {
            Collections.sort(results);
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findMembershipsByGroupId", results);
        }

        return results;
    }

    public Membership findMembership(String id) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findMembership", new Object[] { "id", id });
        }

        orgService.flush();

        Membership m = new MembershipImpl(id);

        String plGroupName = getPLIDMGroupName(getGroupNameFromId(m.getGroupId()));

        String groupId = getIdentitySession().getPersistenceManager().createGroupKey(plGroupName,
                getGroupTypeFromId(m.getGroupId()));

        try {
            if (isCreateMembership(m.getMembershipType(), m.getGroupId())
                    && getIdentitySession().getRoleManager().hasRole(m.getUserName(), groupId, m.getMembershipType())) {
                if (log.isTraceEnabled()) {
                    Tools.logMethodOut(log, LogLevel.TRACE, "findMembership", m);
                }
                return m;
            }
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        try {
            if (isAssociationMapped() && getAssociationMapping().equals(m.getMembershipType())
                    && getIdentitySession().getRelationshipManager().isAssociatedByKeys(groupId, m.getUserName())) {
                if (log.isTraceEnabled()) {
                    Tools.logMethodOut(log, LogLevel.TRACE, "findMembership", m);
                }

                return m;
            }
        } catch (Exception e) {
            // TODO:
            handleException("Identity operation error: ", e);

        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findMembership", null);
        }

        return null;
    }

    private void preSave(Membership membership, boolean isNew) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipEventListener listener = (MembershipEventListener) listeners_.get(i);
            listener.preSave(membership, isNew);
        }
    }

    private void postSave(Membership membership, boolean isNew) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipEventListener listener = (MembershipEventListener) listeners_.get(i);
            listener.postSave(membership, isNew);
        }
    }

    private void preDelete(Membership membership) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipEventListener listener = (MembershipEventListener) listeners_.get(i);
            listener.preDelete(membership);
        }
    }

    private void postDelete(Membership membership) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipEventListener listener = (MembershipEventListener) listeners_.get(i);
            listener.postDelete(membership);
        }
    }

    private String getGroupNameFromId(String groupId) {
        String[] ids = groupId.split("/");

        return ids[ids.length - 1];
    }

    private String getGroupTypeFromId(String groupId) {
        //Need to fixbug: exception when groupId is not valid format
        String parentId = "";
        if(groupId != null && groupId.lastIndexOf('/') > -1) {
            parentId = groupId.substring(0, groupId.lastIndexOf("/"));
        }

        return orgService.getConfiguration().getGroupType(parentId);
    }

    protected boolean isAssociationMapped() {
        String mapping = orgService.getConfiguration().getAssociationMembershipType();

        if (mapping != null && mapping.length() > 0) {
            return true;
        }
        return false;
    }

    protected String getAssociationMapping() {
        return orgService.getConfiguration().getAssociationMembershipType();
    }

    protected boolean ignoreMappedMembershipType(String groupId) {
        return orgService.getConfiguration().isIgnoreMappedMembershipTypeForGroup(groupId);
    }

    protected boolean isCreateMembership(String typeName, String groupId) {
        if (isAssociationMapped() && getAssociationMapping().equals(typeName) && ignoreMappedMembershipType(groupId)) {
            return false;
        }
        return true;
    }

    public String getPLIDMGroupName(String gtnGroupName) {
        return orgService.getConfiguration().getPLIDMGroupName(gtnGroupName);
    }

    public String getGtnGroupName(String plidmGroupName) {
        return orgService.getConfiguration().getGtnGroupName(plidmGroupName);
    }
}
