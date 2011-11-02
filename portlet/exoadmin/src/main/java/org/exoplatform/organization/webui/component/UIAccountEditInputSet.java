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

import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PasswordStringLengthValidator;
import org.exoplatform.webui.form.validator.ResourceValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.organization.UIUserProfileInputSet;

/** Created by The eXo Platform SARL Author : dang.tung tungcnw@gmail.com Jun 25, 2008 */
@Serialized
public class UIAccountEditInputSet extends UIFormInputSet
{

   final static String USERNAME = "userName";

   final static String PASSWORD1X = "newPassword";

   final static String PASSWORD2X = "confirmPassword";

   final static String CHANGEPASS = "changePassword";

   public UIAccountEditInputSet()
   {
   }

   public UIAccountEditInputSet(String name) throws Exception
   {
      super(name);
      addUIFormInput(new UIFormStringInput(USERNAME, "userName", null).setReadOnly(true).addValidator(
         MandatoryValidator.class).addValidator(StringLengthValidator.class, 3, 30).addValidator(
         ResourceValidator.class).addValidator(ExpressionValidator.class, Utils.USER_NAME_VALIDATOR_REGEX,
         "ResourceValidator.msg.Invalid-char"));
      addUIFormInput(new UIFormStringInput("firstName", "firstName", null).addValidator(
         StringLengthValidator.class, 1, 45).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput("lastName", "lastName", null).addValidator(
         StringLengthValidator.class, 1, 45).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput("email", "email", null).addValidator(MandatoryValidator.class).addValidator(
         EmailAddressValidator.class));
      UIFormCheckBoxInput<Boolean> uiCheckbox = new UIFormCheckBoxInput<Boolean>(CHANGEPASS, null, false);
      uiCheckbox.setOnChange("ToggleChangePassword", "UIUserInfo");
      addUIFormInput(uiCheckbox);
      UIFormInputBase<String> uiInput =
         new UIFormStringInput(PASSWORD1X, null, null).setType(UIFormStringInput.PASSWORD_TYPE).addValidator(
            PasswordStringLengthValidator.class, 6, 30).addValidator(MandatoryValidator.class);
      uiInput.setRendered(false);
      addUIFormInput(uiInput);
      uiInput =
         new UIFormStringInput(PASSWORD2X, null, null).setType(UIFormStringInput.PASSWORD_TYPE).addValidator(
            MandatoryValidator.class).addValidator(PasswordStringLengthValidator.class, 6, 30);
      uiInput.setRendered(false);
      addUIFormInput(uiInput);
   }

   public String getUserName()
   {
      return getUIStringInput(USERNAME).getValue();
   }

   public String getPropertyPrefix()
   {
      return "UIAccountForm";
   }

   public void setValue(User user) throws Exception
   {
      if (user == null)
      {
         return;
      }
      invokeGetBindingField(user);
   }

   public boolean save(OrganizationService service) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();
      String username = getUIStringInput(USERNAME).getValue();
      User user = service.getUserHandler().findUserByName(username);      
      if (user == null)
      {
         uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.user-is-deleted", null, ApplicationMessage.WARNING));
         UIUserInfo userInfo = getParent();
         if (userInfo != null)
         {
            UIAccountEditInputSet accountInput = userInfo.getChild(UIAccountEditInputSet.class);
            UIUserProfileInputSet userProfile = userInfo.getChild(UIUserProfileInputSet.class);
            userInfo.setRenderSibling(UIListUsers.class);
            accountInput.reset();
            userProfile.reset();
            context.setProcessRender(true);
         }
         return false;
      }
      String oldEmail = user.getEmail();
      invokeSetBindingField(user);
      if (isChangePassword())
      {
         String pass1x = getUIStringInput(PASSWORD1X).getValue();
         String pass2x = getUIStringInput(PASSWORD2X).getValue();
         if (!pass1x.equals(pass2x))
         {
            uiApp.addMessage(new ApplicationMessage("UIAccountForm.msg.password-is-not-match", null, ApplicationMessage.WARNING));
            return false;
         }
         user.setPassword(pass1x);
      }
      
      //Check if mail address is already used
      Query query = new Query();
      String email = getUIStringInput("email").getValue();
      query.setEmail(email);
      if (service.getUserHandler().findUsers(query).getAll().size() > 0 && !oldEmail.equals(email))
      {
         //Be sure it keep old value
         user.setEmail(oldEmail);
         query.setEmail(oldEmail);
         Object[] args = {username};
         uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.email-exist", args, ApplicationMessage.WARNING));
         return false;
      }
      service.getUserHandler().saveUser(user, true);
      enableChangePassword(false);
      
      ConversationState state = ConversationState.getCurrent();
      if (username.equals(((User)state.getAttribute(CacheUserProfileFilter.USER_PROFILE)).getUserName()))
      {
         state.setAttribute(CacheUserProfileFilter.USER_PROFILE, user);
      }
      return true;
   }

   public boolean isChangePassword()
   {
      return getUIFormCheckBoxInput(UIAccountEditInputSet.CHANGEPASS).isChecked();
   }

   public void enableChangePassword(boolean enable)
   {
      getUIFormCheckBoxInput(UIAccountEditInputSet.CHANGEPASS).setChecked(enable);
      checkChangePassword();
   }

   public void checkChangePassword()
   {
      UIFormStringInput password1 = getUIStringInput(UIAccountEditInputSet.PASSWORD1X);
      UIFormStringInput password2 = getUIStringInput(UIAccountEditInputSet.PASSWORD2X);
      boolean isChange = isChangePassword();
      ((UIFormStringInput)password1.setValue(null)).setRendered(isChange);
      ((UIFormStringInput)password2.setValue(null)).setRendered(isChange);
   }

   @Override
   public void reset()
   {
      super.reset();
      enableChangePassword(false);
   }      
   
}
