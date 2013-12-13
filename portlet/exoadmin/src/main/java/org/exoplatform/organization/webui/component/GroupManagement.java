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

package org.exoplatform.organization.webui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Created by The eXo Platform SAS Author : Huu-Dung Kieu kieuhdung@gmail.com 22 dec. 08
 */
public class GroupManagement {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(GroupManagement.class);

    public static OrganizationService getOrganizationService() {
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
        return orgService;
    }

    public static UserACL getUserACL() {
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        UserACL acl = (UserACL) container.getComponentInstanceOfType(UserACL.class);
        return acl;
    }

    public static boolean isMembershipOfGroup(String username, String membership, String groupId) throws Exception {
        boolean ret = false;
        if (username == null)
            username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
        OrganizationService orgService = getOrganizationService();
        Collection groups = orgService.getGroupHandler().resolveGroupByMembership(username, membership);
        for (Object group : groups) {
            if (((Group) group).getId().equals(groupId)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public static boolean isManagerOfGroup(String username, String groupId) throws Exception {
        return isMembershipOfGroup(username, getUserACL().getAdminMSType(), groupId);
    }

    public static boolean isMemberOfGroup(String username, String groupId) throws Exception {
        boolean ret = false;
        if (username == null)
            username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
        OrganizationService orgService = getOrganizationService();
        Collection groups = orgService.getGroupHandler().findGroupsOfUser(username);
        for (Object group : groups) {
            if (((Group) group).getId().equals(groupId)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public static boolean isRelatedOfGroup(String username, String groupId) throws Exception {
        boolean ret = false;
        if (username == null)
            username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
        OrganizationService orgService = getOrganizationService();
        Collection groups = orgService.getGroupHandler().findGroupsOfUser(username);
        for (Object group : groups) {
            if (((Group) group).getId().startsWith(groupId)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public static Collection getRelatedGroups(String username, Collection groups) throws Exception {
        if (username == null)
            username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
        List relatedGroups = new ArrayList();
        OrganizationService orgService = getOrganizationService();
        Collection userGroups = orgService.getGroupHandler().findGroupsOfUser(username);
        for (Object group : groups) {
            if (isRelatedGroup((Group) group, userGroups))
                relatedGroups.add(group);
        }
        return relatedGroups;
    }

    private static boolean isRelatedGroup(Group group, Collection groups) {
        boolean ret = false;
        String groupId = group.getId();
        for (Object g : groups) {
            if (((Group) g).getId().startsWith(groupId)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public static boolean isAdministrator(String username) throws Exception {
        if (username == null)
            username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();

        // if getRemoteUser() returns null, then there isn't a logged in user, which means they are not an admin
        if (username == null) {
            return false;
        } else if (username.equals(getUserACL().getSuperUser())) {
            return true;
        } else {
            return isMemberOfGroup(username, getUserACL().getAdminGroups());
        }
    }

    // public static boolean isSuperUser(String username) throws Exception {
    // if (username == null)
    // username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
    // return isMemberOfGroup(username, getUserACL().getAdminGroups());
    // }

    // public static boolean isPlatformAdminGroup(String groupId) {
    // return groupId.equals(PLATFORM_ADMIN_GROUP);
    // }
    //
    // public static boolean isPlatformUsersGroup(String groupId) {
    // return groupId.equals(PLATFORM_USERS_GROUP);
    // }

    public static boolean isSuperUserOfGroup(String username, String groupId) {
        try {
            // return false;
            // 2nd the selected group must be a normal group
            // if (isPlatformAdminGroup(groupId) || isPlatformUsersGroup(groupId))
            // return false;
            //
            boolean ret = (GroupManagement.isManagerOfGroup(username, groupId) || (GroupManagement.isAdministrator(username)));
            // finally, user must be manager of that group
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
