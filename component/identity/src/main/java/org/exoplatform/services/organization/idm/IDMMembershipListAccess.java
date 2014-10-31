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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.gatein.common.logging.LogLevel;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySearchCriteria;
import org.picketlink.idm.api.Role;
import org.picketlink.idm.api.SortOrder;
import org.picketlink.idm.impl.api.IdentitySearchCriteriaImpl;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class IDMMembershipListAccess implements ListAccess<Membership>, Serializable {
    private static Logger log = LoggerFactory.getLogger(IDMMembershipListAccess.class);

    private final Group group;

    private final org.picketlink.idm.api.User user;

    private int size = -1;

    private Membership lastExisting;

    private final boolean usePaginatedQuery;

    // List of all requested roles for given user or role. This field is used only if we skip pagination
    private List<Role> fullResults;

    public IDMMembershipListAccess(Group group, boolean usePaginatedQuery) {
        this.group = group;
        this.user = null;
        this.usePaginatedQuery = usePaginatedQuery;
    }

    public IDMMembershipListAccess(org.picketlink.idm.api.User user, boolean usePaginatedQuery) {
        this.group = null;
        this.user = user;
        this.usePaginatedQuery = usePaginatedQuery;
    }

    public Membership[] load(int index, int length) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "load", new Object[] { "index", index, "length", length });
        }

        List<Role> roles = null;

        if (fullResults != null) {
            // If we already have fullResults (all pages) we can simply sublist them
            roles = fullResults.subList(index, index + length);
        } else {
            // Decide if use paginated query or skip pagination and obtain full results
            IdentitySearchCriteria crit = usePaginatedQuery ? new IdentitySearchCriteriaImpl().page(index, length) : new IdentitySearchCriteriaImpl().page(0, size);

            crit.sort(SortOrder.ASCENDING);

            if (group != null) {
                roles = new LinkedList<Role>(getIDMService().getIdentitySession().getRoleManager().findRoles(group, null, crit));
            } else if (user != null) {
                roles = new LinkedList<Role>(getIDMService().getIdentitySession().getRoleManager().findRoles(user, null, crit));
            }


            // If pagination wasn't used, we have all roles and we can save them for future
            if (!usePaginatedQuery) {
                fullResults = roles;
                roles = fullResults.subList(index, index + length);
            }
        }

        Membership[] memberships = new Membership[length];

        //
        int i = 0;

        for (; i < length; i++) {

            Role role = roles.get(i);

            org.exoplatform.services.organization.Group exoGroup = ((GroupDAOImpl) getOrganizationService().getGroupHandler())
                    .convertGroup(role.getGroup());

            MembershipImpl memb = new MembershipImpl();
            memb.setGroupId(exoGroup.getId());
            memb.setUserName(role.getUser().getId());

            // LDAP store may return raw membership type as role type
            if (role.getRoleType().getName().equals("JBOSS_IDENTITY_MEMBERSHIP")) {
                memb.setMembershipType(getOrganizationService().getConfiguration().getAssociationMembershipType());
            } else {
                memb.setMembershipType(role.getRoleType().getName());
            }

            lastExisting = memb;

            memberships[i] = memb;
        }

        // Size can be greater then number of existing memberships
        if (length > roles.size()) {
            for (; i < length; i++) {
                memberships[i] = lastExisting;
            }
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "load", memberships);
        }

        return memberships;
    }

    public int getSize() throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "getSize", null);
        }

        int result = 0;

        if (size < 0) {
            if (group != null && user == null) {
                result = getIDMService().getIdentitySession().getRoleManager().getRolesCount(group, null, null);
            } else if (group == null && user != null) {
                result = getIDMService().getIdentitySession().getRoleManager().getRolesCount(user, null, null);
            }

            size = result;
        } else {
            result = size;
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "getSize", result);
        }

        return result;

    }

    PicketLinkIDMService getIDMService() {
        return (PicketLinkIDMService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PicketLinkIDMService.class);
    }

    PicketLinkIDMOrganizationServiceImpl getOrganizationService() {
        return (PicketLinkIDMOrganizationServiceImpl) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
                OrganizationService.class);
    }
}
