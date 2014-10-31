/*
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
package org.exoplatform.portal.webui.register;

import org.exoplatform.portal.webui.CaptchaValidator;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PasswordPolicyValidator;
import org.exoplatform.webui.form.validator.PersonalNameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;

import javax.portlet.PortletPreferences;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class UIRegisterInputSet extends UIFormInputWithActions {
    protected static String USER_NAME = "username";

    protected static String PASSWORD = "password";

    protected static String CONFIRM_PASSWORD = "confirmPassword";

    protected static String FIRST_NAME = "firstName";

    protected static String LAST_NAME = "lastName";

    protected static String DISPLAY_NAME = "displayName";

    protected static String EMAIL_ADDRESS = "emailAddress";

    protected static String CAPTCHA = "captcha";

    private boolean captchaInputAvailability;

    public UIRegisterInputSet(String name, boolean skipCaptcha) throws Exception {
        super(name);

        /*
         * addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).addValidator(MandatoryValidator.class)
         * .addValidator(UsernameValidator.class, 3, 30));
         */
        addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).addValidator(MandatoryValidator.class).addValidator(
                UserConfigurableValidator.class, UserConfigurableValidator.USERNAME));

        addUIFormInput(new UIFormStringInput(PASSWORD, PASSWORD, null).setType(UIFormStringInput.PASSWORD_TYPE)
                .addValidator(MandatoryValidator.class).addValidator(PasswordPolicyValidator.class));

        addUIFormInput(new UIFormStringInput(CONFIRM_PASSWORD, CONFIRM_PASSWORD, null).setType(UIFormStringInput.PASSWORD_TYPE)
                .addValidator(MandatoryValidator.class).addValidator(PasswordPolicyValidator.class));

        addUIFormInput(new UIFormStringInput(FIRST_NAME, FIRST_NAME, null).addValidator(StringLengthValidator.class, 1, 45)
                .addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class));

        addUIFormInput(new UIFormStringInput(LAST_NAME, LAST_NAME, null).addValidator(StringLengthValidator.class, 1, 45)
                .addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class));

        addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(StringLengthValidator.class, 0, 90)
                .addValidator(UserConfigurableValidator.class, "displayname",
                        UserConfigurableValidator.KEY_PREFIX + "displayname", false));

        addUIFormInput(new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, null).addValidator(MandatoryValidator.class)
                .addValidator(UserConfigurableValidator.class, UserConfigurableValidator.EMAIL));

        // Never use captcha if skipCaptcha is true. Otherwise read value from portlet property
        boolean useCaptcha;
        if (skipCaptcha) {
            useCaptcha = false;
        } else {
            PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
            PortletPreferences pref = pcontext.getRequest().getPreferences();
            useCaptcha = Boolean.parseBoolean(pref.getValue(UIRegisterEditMode.USE_CAPTCHA, "true"));
        }

        if (useCaptcha) {
            addUIFormInput(new UICaptcha(CAPTCHA, CAPTCHA, null).addValidator(MandatoryValidator.class).addValidator(
                    CaptchaValidator.class));
            this.captchaInputAvailability = true;
        }
    }

    public void setCaptchaInputAvailability(boolean availability) {
        this.captchaInputAvailability = availability;
    }

    public boolean getCaptchaInputAvailability() {
        return this.captchaInputAvailability;
    }

    private String getUserName() {
        return getUIStringInput(USER_NAME).getValue();
    }

    private String getEmail() {
        return getUIStringInput(EMAIL_ADDRESS).getValue();
    }

    private String getPassword() {
        return getUIStringInput(PASSWORD).getValue();
    }

    private String getFirstName() {
        return getUIStringInput(FIRST_NAME).getValue();
    }

    private String getLastName() {
        return getUIStringInput(LAST_NAME).getValue();
    }

    private String getDisplayName() {
        return getUIStringInput(DISPLAY_NAME).getValue();
    }

    /**
     * Use this method instead of invokeSetBinding, to avoid abusing reflection
     *
     * @param user
     */
    private void bindingFields(User user) {
        user.setPassword(getPassword());
        user.setFirstName(getFirstName());
        user.setLastName(getLastName());
        // TODO: GTNPORTAL-2358 switch to setDisplayName once it will be available in Organization API
        user.setFullName(getDisplayName());
        user.setEmail(getEmail());
    }

    public boolean save(UserHandler userHandler, WebuiRequestContext context) throws Exception {
        UIApplication uiApp = context.getUIApplication();
        String pass = getPassword();
        String confirm_pass = getUIStringInput(CONFIRM_PASSWORD).getValue();

        if (!pass.equals(confirm_pass)) {
            uiApp.addMessage(new ApplicationMessage("UIAccountForm.msg.password-is-not-match", null));
            return false;
        }

        String username = getUserName();

        // Check if user name already existed
        if (userHandler.findUserByName(username) != null) {
            Object[] args = { username };
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.user-exist", args));
            return false;
        }

        // Check if mail address is already used
        Query query = new Query();
        query.setEmail(getEmail());
        if (userHandler.findUsersByQuery(query).getSize() > 0) {
            Object[] args = { username };
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.email-exist", args));
            return false;
        }

        User user = userHandler.createUserInstance(username);
        bindingFields(user);

        try {
            userHandler.createUser(user, true);// Broadcast user creaton event
        } catch (Exception e) {
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.fail.create.user", null, ApplicationMessage.ERROR));
            return false;
        }
        reset();// Reset the input form

        // save user as attribute to WebuiRequestContext for later use
        context.setAttribute(UIRegisterForm.ATTR_USER, user);
        return true;
    }
}
