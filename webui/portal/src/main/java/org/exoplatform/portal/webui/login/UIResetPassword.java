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

package org.exoplatform.portal.webui.login;

import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.security.Token;
import org.exoplatform.web.security.security.RemindPasswordTokenService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Created by The eXo Platform SARL Author : dang.tung tungcnw@gmail.com Jul 09, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
        @EventConfig(listeners = UIResetPassword.SaveActionListener.class),
        @EventConfig(phase = Phase.DECODE, listeners = UIMaskWorkspace.CloseActionListener.class) })
public class UIResetPassword extends UIForm {
    static final String USER_NAME = "username";

    static final String NEW_PASSWORD = "newpassword";

    static final String CONFIRM_NEW_PASSWORD = "confirmnewpassword";

    private String userName;

    private String tokenId;

    public UIResetPassword() throws Exception {
        addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).setReadOnly(true));
        addUIFormInput(((UIFormStringInput) new UIFormStringInput(NEW_PASSWORD, NEW_PASSWORD, null))
                .setType(UIFormStringInput.PASSWORD_TYPE).addValidator(MandatoryValidator.class)
                .addValidator(StringLengthValidator.class, 6, 30));
        addUIFormInput(((UIFormStringInput) new UIFormStringInput(CONFIRM_NEW_PASSWORD, CONFIRM_NEW_PASSWORD, null))
                .setType(UIFormStringInput.PASSWORD_TYPE).addValidator(MandatoryValidator.class)
                .addValidator(StringLengthValidator.class, 6, 30));
    }

    public void setUser(User user) {
        userName = user.getUserName();
        getUIStringInput(USER_NAME).setValue(user.getUserName());
    }

    public String getUserName() {
        return userName;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return this.tokenId;
    }

    @Override
    public void reset() {
        UIFormStringInput newPasswordForm = getUIStringInput(NEW_PASSWORD);
        newPasswordForm.reset();
        UIFormStringInput confirmPasswordForm = getUIStringInput(CONFIRM_NEW_PASSWORD);
        confirmPasswordForm.reset();
    }

    public static class SaveActionListener extends EventListener<UIResetPassword> {
        public void execute(Event<UIResetPassword> event) throws Exception {
            UIResetPassword uiForm = event.getSource();
            String newpassword = uiForm.getUIStringInput(NEW_PASSWORD).getValue();
            String confirmnewpassword = uiForm.getUIStringInput(CONFIRM_NEW_PASSWORD).getValue();
            WebuiRequestContext request = event.getRequestContext();
            UIApplication uiApp = request.getUIApplication();
            UIMaskWorkspace uiMaskWorkspace = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            OrganizationService orgService = uiForm.getApplicationComponent(OrganizationService.class);
            RemindPasswordTokenService tokenService = uiForm.getApplicationComponent(RemindPasswordTokenService.class);

            uiForm.reset();
            boolean setPassword = true;

            if (!newpassword.equals(confirmnewpassword)) {
                uiApp.addMessage(new ApplicationMessage("UIResetPassword.msg.password-is-not-match", null));
                setPassword = false;
            }

            Token token = tokenService.deleteToken(uiForm.getTokenId());
            if (token == null || token.isExpired()) {
                uiApp.addMessage(new ApplicationMessage("UIForgetPassword.msg.expration", null));
                setPassword = false;
            }

            if (setPassword) {
                User user = orgService.getUserHandler().findUserByName(uiForm.getUserName());
                if (user == null) {
                    uiApp.addMessage(new ApplicationMessage("UIForgetPassword.msg.user-not-exist", null));
                    return;
                } else {
                    try {
                        user.setPassword(newpassword);
                        orgService.getUserHandler().saveUser(user, true);
                        uiMaskWorkspace.createEvent("Close", Phase.DECODE, request).broadcast();
                        uiApp.addMessage(new ApplicationMessage("UIResetPassword.msg.change-password-successfully", null));
                    } catch (Exception e) {
                        uiApp.addMessage(new ApplicationMessage("UIResetPassword.msg.change-password-fail", null, ApplicationMessage.ERROR));
                    }
                }
            }
            request.addUIComponentToUpdateByAjax(uiMaskWorkspace);
        }
    }

}
