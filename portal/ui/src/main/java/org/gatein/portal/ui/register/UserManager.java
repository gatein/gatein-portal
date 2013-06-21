/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.ui.register;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

/**
 * Encapsulate operations with org service.
 *
 * @author Julien Viet
 */
@Singleton
public class UserManager {

    @Inject
    OrganizationService orgService;

    public UserBean getUser(String userName) throws Exception {
        UserHandler handler = orgService.getUserHandler();
        User user = handler.findUserByName(userName);
        if (user != null) {
            return new UserBean(user);
        } else {
            return null;
        }
    }

    public void saveUser(UserBean userBean) throws Exception {
        UserHandler handler = orgService.getUserHandler();
        User user = handler.createUserInstance(userBean.userName);
        userBean.update(user);
        handler.saveUser(user, true);
    }
}
