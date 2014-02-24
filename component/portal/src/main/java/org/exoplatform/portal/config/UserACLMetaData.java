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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

/**
 * A metadata class to describe security configuration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserACLMetaData {

    /** . */
    private String superUser;

    /** . */
    private String guestsGroups;

    /** . */
    private String navigationCreatorMembershipType;

    /** . */
    private String portalCreateGroups;

    private String userImpersonateGroups;

    public UserACLMetaData() {
    }

    /**
     * Initialize the metadata for UserACL service base on the {@link InitParams} params passed to the constructor
     *
     * @param params
     */
    public UserACLMetaData(InitParams params) {
        ValueParam superUserParam = params.getValueParam("super.user");
        if (superUserParam != null) {
            setSuperUser(superUserParam.getValue());
        }
        ValueParam guestGroupParam = params.getValueParam("guests.group");
        if (guestGroupParam != null) {
            setGuestsGroups(guestGroupParam.getValue());
        }
        ValueParam navCretorParam = params.getValueParam("navigation.creator.membership.type");
        if (navCretorParam != null) {
            setNavigationCreatorMembershipType(navCretorParam.getValue());
        }
        ValueParam portalCretorGroupsParam = params.getValueParam("portal.creator.groups");
        if (portalCretorGroupsParam != null) {
            setPortalCreateGroups(portalCretorGroupsParam.getValue());
        }
        ValueParam userImpersonateGroupsParam = params.getValueParam("user.impersonate.groups");
        if (userImpersonateGroupsParam != null) {
            setUserImpersonateGroups(userImpersonateGroupsParam.getValue());
        }
    }

    public String getSuperUser() {
        return superUser;
    }

    public void setSuperUser(String superUser) {
        this.superUser = superUser;
    }

    public String getGuestsGroups() {
        return guestsGroups;
    }

    public void setGuestsGroups(String guestsGroups) {
        this.guestsGroups = guestsGroups;
    }

    public String getNavigationCreatorMembershipType() {
        return navigationCreatorMembershipType;
    }

    public void setNavigationCreatorMembershipType(String navigationCreatorMembershipType) {
        this.navigationCreatorMembershipType = navigationCreatorMembershipType;
    }

    public String getPortalCreateGroups() {
        return portalCreateGroups;
    }

    public void setPortalCreateGroups(String portalCreateGroups) {
        this.portalCreateGroups = portalCreateGroups;
    }

    public String getUserImpersonateGroups() {
        return userImpersonateGroups;
    }

    public void setUserImpersonateGroups(String userImpersonateGroups) {
        this.userImpersonateGroups = userImpersonateGroups;
    }
}
