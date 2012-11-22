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

package org.exoplatform.webui.form.validator;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL Author : Nhu Dinh Thuan nhudinhthuan@exoplatform.com Apr 12, 2007
 *
 * Validates whether the current user is allowed to perform the current operation
 */
public class PermissionValidaror {

    public void validate(UIComponent uicomponent, String permission) throws Exception {
        OrganizationService service = (OrganizationService) ExoContainerContext.getCurrentContainer()
                .getComponentInstanceOfType(OrganizationService.class);
        if (permission == null || permission.length() < 1 || permission.equals("*"))
            return;
        Object[] args = { uicomponent.getName() };
        String[] tmp = permission.split(":", 2);
        if (tmp.length != 2) {
            throw new MessageException(new ApplicationMessage("PermissionValidator.msg.invalid-permission-input", args));
        }
        String membership = tmp[0];
        String groupId = tmp[1];
        Group group = null;
        MembershipType membershipType = null;
        try {
            membershipType = service.getMembershipTypeHandler().findMembershipType(membership);
            group = service.getGroupHandler().findGroupById(groupId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (membership.equals("*")) {
            if (membershipType != null && group != null)
                return;
            throw new MessageException(new ApplicationMessage("PermissionValidator.msg.membership-group-not-found", args));
        }
        if (group != null)
            return;
        throw new MessageException(new ApplicationMessage("PermissionValidator.msg.membership-group-not-found", args));
    }
}
