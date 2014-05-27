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

package org.exoplatform.webui.organization;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PasswordPolicyValidator;
import org.exoplatform.webui.form.validator.PersonalNameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com Jun 28, 2006
 */
@Serialized
public class UIAccountInputSet extends UIFormInputWithActions {

    static final String USERNAME = "username";

    static final String PASSWORD1X = "password";

    static final String PASSWORD2X = "Confirmpassword";

    public UIAccountInputSet() {
    }

    public UIAccountInputSet(String name) throws Exception {
        super(name);
        addUIFormInput(new UIFormStringInput(USERNAME, "userName", null).addValidator(MandatoryValidator.class).addValidator(
                UserConfigurableValidator.class, UserConfigurableValidator.USERNAME));

        addUIFormInput(new UIFormStringInput(PASSWORD1X, "password", null).setType(UIFormStringInput.PASSWORD_TYPE)
                .addValidator(MandatoryValidator.class).addValidator(PasswordPolicyValidator.class));

        addUIFormInput(new UIFormStringInput(PASSWORD2X, "password", null).setType(UIFormStringInput.PASSWORD_TYPE)
                .addValidator(MandatoryValidator.class).addValidator(PasswordPolicyValidator.class));

        addUIFormInput(new UIFormStringInput("firstName", "firstName", null).addValidator(StringLengthValidator.class, 1, 45)
                .addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class));

        addUIFormInput(new UIFormStringInput("lastName", "lastName", null).addValidator(StringLengthValidator.class, 1, 45)
                .addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class));

        // TODO: GTNPORTAL-2358 switch bindingField fullName to displayName once displayName will be available in Organization
        // API
        addUIFormInput(new UIFormStringInput("displayName", "fullName", null).addValidator(StringLengthValidator.class, 0, 90)
                .addValidator(UserConfigurableValidator.class, "displayname",
                        UserConfigurableValidator.KEY_PREFIX + "displayname", false));

        addUIFormInput(new UIFormStringInput("email", "email", null).addValidator(MandatoryValidator.class).addValidator(
                UserConfigurableValidator.class, UserConfigurableValidator.EMAIL));
    }

    public String getUserName() {
        return getUIStringInput(USERNAME).getValue();
    }

    public String getPropertyPrefix() {
        return "UIAccountForm";
    }

    public void setValue(User user) throws Exception {
        if (user == null)
            return;
        invokeGetBindingField(user);
        getUIStringInput(USERNAME).setReadOnly(true);
    }

    public boolean save(OrganizationService service, boolean newUser) throws Exception {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        UIApplication uiApp = context.getUIApplication();
        String pass1x = getUIStringInput(PASSWORD1X).getValue();
        String pass2x = getUIStringInput(PASSWORD2X).getValue();
        if (!pass1x.equals(pass2x)) {
            uiApp.addMessage(new ApplicationMessage("UIAccountForm.msg.password-is-not-match", null, ApplicationMessage.ERROR));
            return false;
        }
        String username = getUIStringInput(USERNAME).getValue();
        if (newUser) {
            User user = service.getUserHandler().createUserInstance(username);
            invokeSetBindingField(user);
            // user.setPassword(Util.encodeMD5(pass1x)) ;
            if (service.getUserHandler().findUserByName(user.getUserName(), UserStatus.ANY) != null) {
                Object[] args = { user.getUserName() };
                uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.user-exist", args, ApplicationMessage.ERROR));
                return false;
            }

            Query query = new Query();
            query.setEmail(getUIStringInput("email").getValue());
            if (service.getUserHandler().findUsersByQuery(query, UserStatus.ANY).getSize() > 0) {
                Object[] args = { user.getUserName() };
                uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.email-exist", args, ApplicationMessage.ERROR));
                return false;
            }

            try {
                service.getUserHandler().createUser(user, true);
            } catch (Exception e) {
                uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.fail.create.user", null, ApplicationMessage.ERROR));
                return false;
            }
            reset();
            return true;
        }
        User user = service.getUserHandler().findUserByName(username);
        invokeSetBindingField(user);
        // user.setPassword(Util.encodeMD5(pass1x)) ;
        try {
            service.getUserHandler().saveUser(user, true);
        } catch (Exception e) {
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.fail.update.user", null, ApplicationMessage.ERROR));
            return false;
        }
        return true;
    }

}
