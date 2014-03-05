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

package org.exoplatform.portal.webui.register;

import java.util.ArrayList;
import java.util.List;

import nl.captcha.Captcha;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.registration.PostRegistrationService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.Validator;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 *
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */

@ComponentConfigs({
    @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
        @EventConfig(listeners = UIRegisterForm.SubscribeActionListener.class),
        @EventConfig(listeners = UIRegisterForm.ResetActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIRegisterForm.CancelActionListener.class, phase = Phase.DECODE),
        @EventConfig(name = UIRegisterForm.CheckUsernameAvailability.LISTENER_NAME, listeners = UIRegisterForm.CheckUsernameAvailability.class, phase = Phase.DECODE) },
        initParams = { @ParamConfig(name=UIRegisterForm.SKIP_CAPTCHA_PARAM_NAME, value="false") }),
    @ComponentConfig(id = UIRegisterOAuth.REGISTER_FORM_CONFIG_ID, lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
        @EventConfig(listeners = UIRegisterOAuth.SubscribeOAuthActionListener.class),
        @EventConfig(listeners = UIRegisterOAuth.ResetActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIRegisterOAuth.CancelActionListener.class, phase = Phase.DECODE),
        @EventConfig(name = UIRegisterForm.CheckUsernameAvailability.LISTENER_NAME, listeners = UIRegisterForm.CheckUsernameAvailability.class, phase = Phase.DECODE) },
        initParams = { @ParamConfig(name=UIRegisterForm.SKIP_CAPTCHA_PARAM_NAME, value="true") })
})
public class UIRegisterForm extends UIForm {

    private static final String[] ACTIONS = { "Subscribe", "Reset", "Cancel" };

    static final String ATTR_USER = "UIRegisterForm$User";

    static final String SKIP_CAPTCHA_PARAM_NAME = "skipCaptcha";

    public UIRegisterForm(InitParams params) throws Exception {
        String skipCaptchaParam = params.getParam(SKIP_CAPTCHA_PARAM_NAME).getValue();
        boolean skipCaptcha = Boolean.parseBoolean(skipCaptchaParam);

        UIFormInputWithActions registerInput = new UIRegisterInputSet("RegisterInputSet", skipCaptcha);

        // Set actions on registerInput 's User Name field
        List<ActionData> fieldActions = new ArrayList<ActionData>();
        ActionData checkAvailable = new ActionData();
        checkAvailable.setActionListener(CheckUsernameAvailability.LISTENER_NAME);
        checkAvailable.setActionName(CheckUsernameAvailability.LISTENER_NAME);
        checkAvailable.setActionType(ActionData.TYPE_ICON);
        checkAvailable.setCssIconClass("SearchIcon");
        fieldActions.add(checkAvailable);
        registerInput.setActionField(UIRegisterInputSet.USER_NAME, fieldActions);

        addUIFormInput(registerInput);
        setActions(ACTIONS);
    }

    private void resetInput() {
        getChild(UIRegisterInputSet.class).reset();
    }

    @Override
    public void processAction(WebuiRequestContext context) throws Exception {
        super.processAction(context);

        if (context.getProcessRender()) {
            // Invalidate the capcha
            if (context instanceof PortletRequestContext) {
                PortletRequestContext prc = (PortletRequestContext) context;
                prc.getRequest().getPortletSession().removeAttribute(Captcha.NAME);
            }
            context.addUIComponentToUpdateByAjax(getChild(UIRegisterInputSet.class));
        }
    }

    public static class SubscribeActionListener extends EventListener<UIRegisterForm> {
        @Override
        public void execute(Event<UIRegisterForm> event) throws Exception {
            UIRegisterForm registerForm = event.getSource();
            OrganizationService orgService = registerForm.getApplicationComponent(OrganizationService.class);
            UserHandler userHandler = orgService.getUserHandler();
            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
            UIRegisterInputSet registerInput = registerForm.getChild(UIRegisterInputSet.class);

            if (registerInput.save(userHandler, context)) {
                // TODO: Add Account Activating feature
                UIApplication uiApp = context.getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIRegisterForm.registerWithSuccess.message", null));

                // Send mail to administrator after successful registration of user
                PostRegistrationService postRegistrationService = uiApp.getApplicationComponent(PostRegistrationService.class);
                User user = (User) context.getAttribute(ATTR_USER);
                postRegistrationService.sendMailAfterSuccessfulRegistration(user);
            }

            // Invalidate the capcha
            if (context instanceof PortletRequestContext) {
                PortletRequestContext prc = (PortletRequestContext) context;
                prc.getRequest().getPortletSession().removeAttribute(Captcha.NAME);
            }
        }
    }

    public static class CheckUsernameAvailability extends EventListener<UIRegisterForm> {

        /** . */
        private final Logger log = LoggerFactory.getLogger(CheckUsernameAvailability.class);

        static final String LISTENER_NAME = "CheckUsernameAvailability";

        @Override
        public void execute(Event<UIRegisterForm> event) throws Exception {
            UIRegisterForm registerForm = event.getSource();
            OrganizationService orgService = registerForm.getApplicationComponent(OrganizationService.class);
            UIRegisterInputSet registerInput = registerForm.getChild(UIRegisterInputSet.class);
            UIFormStringInput userNameInput = registerInput.getUIStringInput(UIRegisterInputSet.USER_NAME);
            List<Validator> validators = userNameInput.getValidators();
            for (Validator validator : validators) {
                try {
                    validator.validate(userNameInput);
                } catch (MessageException e) {
                    event.getRequestContext().getUIApplication().addMessage(e.getDetailMessage());
                    return;
                }
            }

            String typedUsername = userNameInput.getValue();
            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
            UIApplication uiApp = context.getUIApplication();
            if (usernameIsUsed(typedUsername, orgService)) {
                uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.user-exist", new String[] { typedUsername },
                        ApplicationMessage.WARNING));
            } else {
                uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.user-not-exist", new String[] { typedUsername },
                        ApplicationMessage.INFO));
            }
        }

        private boolean usernameIsUsed(String username, OrganizationService orgService) {
            UserHandler userHandler = orgService.getUserHandler();
            try {
                if (userHandler.findUserByName(username, UserStatus.ANY) != null) {
                    return true;
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return false;
        }
    }

    public static class ResetActionListener extends EventListener<UIRegisterForm> {
        @Override
        public void execute(Event<UIRegisterForm> event) throws Exception {
            UIRegisterForm registerForm = event.getSource();
            registerForm.resetInput();
        }
    }

    public static class CancelActionListener extends EventListener<UIRegisterForm> {
        @Override
        public void execute(Event<UIRegisterForm> event) throws Exception {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            JavascriptManager jsManager = prContext.getJavascriptManager();
            jsManager.addJavascript("history.go(-1);");
        }
    }
}
