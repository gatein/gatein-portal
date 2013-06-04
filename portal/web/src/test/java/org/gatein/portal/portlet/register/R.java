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
package org.gatein.portal.portlet.register;

import org.gatein.portal.ui.register.Controller_;
import org.gatein.portal.ui.register.Flash;
import org.gatein.portal.ui.register.User;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.template.Template;

import javax.inject.Inject;

/**
 * @author Julien Viet
 */
public class R {

   @Inject
   @Path("index.gtmpl")
   Template index;

   @Inject
   Flash flash;

   @View
   public Response index() {
      return index.with().ok();
   }

   @Action
   public Response register(User user, String confirmPassword) {
      if (!user.password.equals(confirmPassword)) {
         flash.setError("Password and Confirm Password must be the same.");
         return R_.index();
      }
      User isExisted = User.getUser(user.userName);
      if (isExisted != null) {
         flash.setError("This user is already existed. Please enter different userName.");
         return R_.index();
      }
      User.saveUser(user);
      flash.setSuccess("You have successfully registered a new account!");
      flash.setUserName(user.userName);
      return R_.index();
   }
}