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

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.template.Template;

/**
 * @author Julien Viet
 */
public class Controller {

    @Inject
    @Path("index.gtmpl")
    Template index;

    @Inject
    Flash flash;

    @Inject
    UserManager manager;

    @View
    public Response index() {
        return index.with().ok();
    }

    @Action
    public Response register(UserBean userBean, String confirmPassword) {
        if (!userBean.password.equals(confirmPassword)) {
            flash.setError("Password and Confirm Password must be the same.");
        } else {
            try {
                UserBean isExisted = manager.getUser(userBean.userName);
                if (isExisted != null) {
                    flash.setError("This user is already existed. Please enter different userName.");
                } else {
                    manager.saveUser(userBean);
                    flash.setSuccess("You have successfully registered a new account!");
                    flash.setUserName(userBean.userName);
                }
            } catch (Exception e) {
                flash.setError("Could not register user due to internal error.");
                e.printStackTrace();
            }
        }
        return Controller_.index();
    }
}