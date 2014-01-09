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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.portal.mop.permission.SecurityService;
import org.gatein.portal.mop.permission.SecurityState;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;

/** Jun 27, 2006 */
public class UserACL {
    public static final String EVERYONE = "Everyone";

    protected static Log log = ExoLogger.getLogger("organization:UserACL");

    private final Collection<MembershipEntry> NO_MEMBERSHIP = Collections.emptyList();

    private final Collection<String> NO_ROLES = Collections.emptyList();

    private final Identity guest = new Identity(null, NO_MEMBERSHIP, NO_ROLES);

    private String superUser_;

    private String guestGroup_;

    private List<String> portalCreatorGroups_;

    private String navigationCreatorMembershipType_;

    private List<String> mandatoryGroups_;

    private List<String> mandatoryMSTypes_;

    private PortalACLPlugin portalACLPlugin;

    private String adminGroups;

    private String adminMSType;

    private SecurityService securityService;

    @SuppressWarnings("unchecked")
    public UserACL(InitParams params, SecurityService securityService) {
        UserACLMetaData md = new UserACLMetaData(params);

        ValuesParam mandatoryGroupsParam = params.getValuesParam("mandatory.groups");
        if (mandatoryGroupsParam != null) {
            mandatoryGroups_ = mandatoryGroupsParam.getValues();
        } else {
            mandatoryGroups_ = new ArrayList<String>();
        }

        ValuesParam mandatoryMSTypesParam = params.getValuesParam("mandatory.mstypes");
        if (mandatoryMSTypesParam != null)
            mandatoryMSTypes_ = mandatoryMSTypesParam.getValues();
        else
            mandatoryMSTypes_ = new ArrayList<String>();

        // tam.nguyen get admin group value
        ValueParam adminGroupsParam = params.getValueParam("portal.administrator.groups");
        if (adminGroupsParam != null) {
            setAdminGroups(adminGroupsParam.getValue());
        }

        // tam.nguyen get administrator member type
        ValueParam adminMSTypeParam = params.getValueParam("portal.administrator.mstype");
        if (adminMSTypeParam != null) {
            setAdminMSType(adminMSTypeParam.getValue());
        }

        this.securityService = securityService;

        init(md);
    }

    public UserACL(UserACLMetaData md) {
        if (md == null) {
            throw new NullPointerException("No meta data provided");
        }
        init(md);
    }

    private void init(UserACLMetaData md) {
        if (md.getSuperUser() != null) {
            superUser_ = md.getSuperUser();
        }
        if (superUser_ == null || superUser_.trim().length() == 0) {
            superUser_ = "root";
        }

        if (md.getGuestsGroups() != null) {
            guestGroup_ = md.getGuestsGroups();
        }
        if (guestGroup_ == null || guestGroup_.trim().length() < 1) {
            guestGroup_ = "/platform/guests";
        }

        if (md.getNavigationCreatorMembershipType() != null) {
            navigationCreatorMembershipType_ = md.getNavigationCreatorMembershipType();
        }
        if (navigationCreatorMembershipType_ == null || navigationCreatorMembershipType_.trim().length() == 0) {
            navigationCreatorMembershipType_ = "owner";
        }

        String allGroups = "";
        if (md.getPortalCreateGroups() != null) {
            allGroups = md.getPortalCreateGroups();
        }
        portalCreatorGroups_ = defragmentPermission(allGroups);
    }

    // TODO: unnecessary to keep potalACLPlugin
    public void addPortalACLPlugin(PortalACLPlugin plugin) {
        this.portalACLPlugin = plugin;
        String superUser = portalACLPlugin.getSuperUser();
        if (superUser != null) {
            log.info("Overidden SuperUser by PortalACLPlugin");
            superUser_ = superUser;
        }
        List<String> portalCreationRoles = portalACLPlugin.getPortalCreationRoles();
        if (portalCreationRoles != null) {
            log.info("Overidden PortalCreatorGroup by PortalACLPlugin");
            portalCreatorGroups_ = portalCreationRoles;
        }
    }

    public String getMakableMT() {
        return navigationCreatorMembershipType_;
    }

    public List<String> getPortalCreatorGroups() {
        return portalCreatorGroups_;
    }

    public String getSuperUser() {
        return superUser_;
    }

    public String getGuestsGroup() {
        return guestGroup_;
    }

    public List<String> getMandatoryGroups() {
        return mandatoryGroups_;
    }

    public List<String> getMandatoryMSTypes() {
        return mandatoryMSTypes_;
    }

    public void setAdminGroups(String adminGroups) {
        this.adminGroups = adminGroups;
    }

    public String getAdminGroups() {
        return adminGroups;
    }

    public void setAdminMSType(String adminMSType) {
        this.adminMSType = adminMSType;
    }

    public String getAdminMSType() {
        return adminMSType;
    }

    public boolean hasPermission(PortalConfig pconfig) {
        Identity identity = getIdentity();
        if(hasEditPermission(pconfig)) {
            pconfig.setModifiable(true);
            return true;
        }
        pconfig.setModifiable(false);
        String[] accessPerms = (pconfig.getAccessPermissions());
        for (String per : accessPerms) {
            if (hasPermission(identity, per)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEditPermission(PortalConfig pconfig) {
        Identity identity = getIdentity();

        if (superUser_.equals(identity.getUserId())) {
            return true;
        }

        String[] editPerms = pconfig.getEditPermissions();
        if(editPerms != null && editPerms.length > 0) {
            for(String perms : editPerms) {
                if(hasPermission(identity, perms)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method is equivalent to <code>hasEditPermission(PortalConfig)</code>. That allows us to check edit permission on a
     * UIPortal, without converting UIPortal into PortalConfig via PortalDataMapper.
     *
     * @param ownerType the owner type
     * @param ownerId the owner id
     * @param editPermExpression the permission expression
     * @return true or false
     */
    public boolean hasEditPermissionOnPortal(String ownerType, String ownerId, String editPermExpression) {
        Identity identity = this.getIdentity();
        if (superUser_.equals(identity.getUserId())) {
            return true;
        }

        if (PortalConfig.USER_TYPE.equals(ownerType)) {
            return identity.getUserId().equals(ownerId);
        }

        return hasPermission(identity, editPermExpression);
    }

    public boolean hasCreatePortalPermission() {
        Identity identity = getIdentity();
        if (superUser_.equals(identity.getUserId())) {
            return true;
        }
        if (portalCreatorGroups_ == null || portalCreatorGroups_.size() < 1) {
            return false;
        }
        for (String ele : portalCreatorGroups_) {
            if (hasPermission(identity, ele)) {
                return true;
            }
        }
        return false;
    }

    // copied from @link{#hasEditPermission}
    public boolean hasEditPermissionOnNavigation(SiteKey siteKey) {
        Identity identity = getIdentity();
        if (superUser_.equals(identity.getUserId())) {
            return true;
        }

        //
        switch (siteKey.getType()) {
            case PORTAL:
                // TODO: We should also take care of Portal's navigation
                return false;
            case GROUP:
                String temp = siteKey.getName().trim();
                String expAdminGroup = getAdminGroups();
                String expPerm = null;

                // Check to see whether current user is member of admin group or not,
                // if so grant
                // edit permission for group navigation for that user.
                if (expAdminGroup != null) {
                    expAdminGroup = expAdminGroup.startsWith("/") ? expAdminGroup : "/" + expAdminGroup;
                    expPerm = temp.startsWith("/") ? temp : "/" + temp;
                    if (isUserInGroup(expPerm) && isUserInGroup(expAdminGroup)) {
                        return true;
                    }
                }

                expPerm = navigationCreatorMembershipType_ + (temp.startsWith("/") ? ":" + temp : ":/" + temp);
                return hasPermission(identity, expPerm);
            case USER:
                return siteKey.getName().equals(identity.getUserId());
            default:
                return false;
        }
    }

    public boolean hasPermission(Page page) {
        Identity identity = getIdentity();
        if (PortalConfig.USER_TYPE.equals(page.getOwnerType())) {
            if (page.getOwnerId().equals(identity.getUserId())) {
                page.setModifiable(true);
                return true;
            }
        }
        if (superUser_.equals(identity.getUserId())) {
            page.setModifiable(true);
            return true;
        }
        if (hasEditPermission(page)) {
            page.setModifiable(true);
            return true;
        }
        page.setModifiable(false);
        String[] accessPerms = page.getAccessPermissions();
        if (accessPerms != null) {
            for (String per : accessPerms) {
                if (hasPermission(identity, per)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasPermission(PageContext page) {
        PageKey key = page.getKey();
        Identity identity = getIdentity();
        if (SiteType.USER == key.getSite().getType()) {
            if (key.getSite().getName().equals(identity.getUserId())) {
                return true;
            }
        }
        if (superUser_.equals(identity.getUserId())) {
            return true;
        }
        if (hasEditPermission(page)) {
            return true;
        }
        String[] accessPerms = loadPermission(page).getAccessPermission();
        if (accessPerms != null) {
            for (String per : accessPerms) {
                if (hasPermission(identity, per)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasEditPermission(Page page) {
        Identity identity = getIdentity();
        if (PortalConfig.USER_TYPE.equals(page.getOwnerType())) {
            if (page.getOwnerId().equals(identity.getUserId())) {
                page.setModifiable(true);
                return true;
            }
            return false;
        }

        String currentUser = identity.getUserId();
        if (superUser_.equals(currentUser)) {
            page.setModifiable(true);
            return true;
        }

        String[] editPerms = page.getEditPermissions();
        if(editPerms != null && editPerms.length > 0) {
            for(String perm : editPerms) {
                if(hasPermission(identity, perm)) {
                    page.setModifiable(true);
                    return true;
                }
            }
        }
        page.setModifiable(false);
        return false;
    }

    public boolean hasEditPermission(PageContext page) {
        PageKey key = page.getKey();
        Identity identity = getIdentity();
        if (SiteType.USER == key.getSite().getType()) {
            return key.getSite().getName().equals(identity.getUserId());
        } else {
            if(superUser_.equals(identity.getUserId())) {
                return true;
            }
            String[] editPerms = loadPermission(page).getEditPermissions();
            if(editPerms != null) {
                for(String perm : editPerms) {
                    if(hasPermission(identity, perm)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private SecurityState loadPermission(PageContext page) {
        return securityService != null ? securityService.loadPermission(page.getData().id) : new SecurityState(new String[0], new String[0]);
    }

    /**
     *
     * Minh Hoang TO - This method is equivalent to <code>hasEditPermission(Page)</code>. It allows us to check edit permission
     * with a UIPage, without converting UIPage into Page via PortalDataMapper
     *
     */
    public boolean hasEditPermissionOnPage(String ownerType, String ownerId, String editPermExpression) {
        Identity identity = this.getIdentity();

        if (PortalConfig.USER_TYPE.equals(ownerType)) {
            if (ownerId.equals(identity.getUserId())) {
                return true;
            }
            return false;
        }

        return hasPermission(identity, editPermExpression);
    }

    public boolean hasPermission(String expPerm) {
        return hasPermission(getIdentity(), expPerm);
    }

    /**
     * @param group
     * @return
     */
    public boolean isUserInGroup(String group) {
        ConversationState conv = ConversationState.getCurrent();
        Identity id = null;
        if (conv != null) {
            id = conv.getIdentity();
        }

        if (id == null) {
            return false;
        }

        Iterator<String> iter = id.getGroups().iterator();

        while (iter.hasNext()) {
            if (iter.next().equals(group)) {
                return true;
            }
        }

        return false;
    }

    private Identity getIdentity() {
        ConversationState conv = ConversationState.getCurrent();
        if (conv == null) {
            return guest;
        }

        Identity id = conv.getIdentity();
        if (id == null) {
            return guest;
        }

        return id;
    }

    public boolean hasPermission(Identity identity, String expPerm) {
        String currentUser = identity.getUserId();
        if (superUser_.equals(currentUser)) {
            return true;
        }
        if (expPerm == null) {
            return false;
        }
        if (EVERYONE.equals(expPerm)) {
            return true;
        }
        Permission permission = new Permission();
        permission.setPermissionExpression(expPerm);
        String groupId = permission.getGroupId();
        if ((currentUser == null || currentUser.equals(IdentityConstants.ANONIM)) && groupId.equals(guestGroup_)) {
            return true;
        }
        String membership = permission.getMembership();
        return identity.isMemberOf(groupId, membership);
    }

    private List<String> defragmentPermission(String permission) {
        List<String> result = new ArrayList<String>();
        if (permission != null) {
            if (permission.contains(",")) {
                String[] groups = permission.split(",");
                for (String group : groups) {
                    result.add(group.trim());
                }
            } else {
                result.add(permission);
            }
        }
        return result;
    }

    public static class Permission implements Serializable {

        private String name_;

        private String groupId_ = "";

        private String membership_ = "";

        private String expression;

        private boolean selected_ = false;

        public void setPermissionExpression(String exp) {
            if (exp == null || exp.length() == 0) {
                return;
            }
            String[] temp = exp.split(":");
            if (temp.length < 2) {
                return;
            }
            expression = exp;
            membership_ = temp[0].trim();
            groupId_ = temp[1].trim();
        }

        public String getGroupId() {
            return groupId_;
        }

        public void setGroupId(String groupId) {
            groupId_ = groupId;
        }

        public String getName() {
            return name_;
        }

        public void setName(String name) {
            name_ = name;
        }

        public String getValue() {
            if (membership_.length() == 0 || groupId_.length() == 0) {
                return null;
            }
            return membership_ + ":" + groupId_;
        }

        public String getMembership() {
            return membership_;
        }

        public void setMembership(String membership) {
            membership_ = membership;
        }

        public boolean isSelected() {
            return selected_;
        }

        public void setSelected(boolean selected) {
            selected_ = selected;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }
    }
}
