/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.exoplatform.portal.account;

import org.exoplatform.portal.application.UserProfileLifecycle;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.organization.UIUserProfileInputSet;
import org.gatein.common.exception.GateInException;
import org.gatein.common.exception.GateInExceptionConstants;
import org.gatein.security.oauth.utils.OAuthConstants;

/**
 * Social networks tab of user profile
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl",
        events = { @EventConfig(listeners = UIAccountSocial.SaveActionListener.class, csrfCheck = true),
                   @EventConfig(listeners = UIAccountSocial.ResetActionListener.class, phase = Event.Phase.DECODE) })
public class UIAccountSocial extends UIForm {

    public UIAccountSocial() throws Exception {
        OAuthUsernames oauthUsernames = getOauthUsernamesFromBackend();

        addUIFormInput(new UIFormStringInput(OAuthConstants.PROFILE_FACEBOOK_USERNAME, OAuthConstants.PROFILE_FACEBOOK_USERNAME, oauthUsernames.facebookUsername));
        addUIFormInput(new UIFormStringInput(OAuthConstants.PROFILE_GOOGLE_USERNAME, OAuthConstants.PROFILE_GOOGLE_USERNAME, oauthUsernames.googleUsername));
    }

    private OAuthUsernames getOauthUsernamesFromBackend() {
        UserProfile userProfile = (UserProfile)Util.getPortalRequestContext().getAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME);

        String facebookUsername = null;
        String googleUsername = null;
        if (userProfile != null) {
            facebookUsername = userProfile.getAttribute(OAuthConstants.PROFILE_FACEBOOK_USERNAME);
            googleUsername = userProfile.getAttribute(OAuthConstants.PROFILE_GOOGLE_USERNAME);
        }

        return new OAuthUsernames(facebookUsername, googleUsername);
    }

    public static class ResetActionListener extends EventListener<UIAccountSocial> {
        public void execute(Event<UIAccountSocial> event) throws Exception {
            UIAccountSocial uiForm = event.getSource();

            OAuthUsernames oauthUsernames = uiForm.getOauthUsernamesFromBackend();
            uiForm.getUIStringInput(OAuthConstants.PROFILE_FACEBOOK_USERNAME).setValue(oauthUsernames.facebookUsername);
            uiForm.getUIStringInput(OAuthConstants.PROFILE_GOOGLE_USERNAME).setValue(oauthUsernames.googleUsername);

            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        }
    }

    public static class SaveActionListener extends EventListener<UIAccountSocial> {
        public void execute(Event<UIAccountSocial> event) throws Exception {
            UIAccountSocial uiForm = event.getSource();

            OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
            UIApplication uiApp = context.getUIApplication();

            ConversationState state = ConversationState.getCurrent();
            String userName = ((User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE)).getUserName();
            User user = service.getUserHandler().findUserByName(userName);

            if (user != null) {
                UserProfile userProfile = (UserProfile)Util.getPortalRequestContext().getAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME);
                userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_USERNAME, uiForm.getUIStringInput(OAuthConstants.PROFILE_FACEBOOK_USERNAME).getValue());
                userProfile.setAttribute(OAuthConstants.PROFILE_GOOGLE_USERNAME, uiForm.getUIStringInput(OAuthConstants.PROFILE_GOOGLE_USERNAME).getValue());

                try {
                    service.getUserProfileHandler().saveUserProfile(userProfile, true);
                    uiApp.addMessage(new ApplicationMessage("UIAccountSocial.msg.successful-update", null));
                } catch (GateInException gtnOauthException) {
                    // Show warning message if user with this facebookUsername (or googleUsername) already exists
                    if (gtnOauthException.getExceptionCode() == GateInExceptionConstants.EXCEPTION_CODE_DUPLICATE_OAUTH_PROVIDER_USERNAME) {
                        Object[] args = UIUserProfileInputSet.convertOAuthExceptionAttributes(Util.getPortalRequestContext(), "UIAccountSocial.label.", gtnOauthException.getExceptionAttributes());
                        ApplicationMessage appMessage = new ApplicationMessage("UIUserProfileInputSet.msg.oauth-username-exists", args, ApplicationMessage.WARNING);
                        appMessage.setArgsLocalized(false);
                        uiApp.addMessage(appMessage);
                        return;
                    } else {
                        throw gtnOauthException;
                    }
                }

                Util.getPortalRequestContext().setAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME, userProfile);
                UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);
                uiWorkingWS.updatePortletsByName("UserInfoPortlet");
                uiWorkingWS.updatePortletsByName("OrganizationPortlet");
            } else {
                JavascriptManager jsManager = Util.getPortalRequestContext().getJavascriptManager();
                jsManager.require("SHARED/base").addScripts(
                        "if(confirm('"
                                + Util.getPortalRequestContext().getApplicationResourceBundle()
                                .getString("UIAccountProfiles.msg.NotExistingAccount") + "')) {eXo.portal.logout();}");
            }
        }
    }

    private static class OAuthUsernames {
        private final String facebookUsername;
        private final String googleUsername;

        public OAuthUsernames(String fb, String google) {
            this.facebookUsername = fb;
            this.googleUsername = google;
        }
    }
}
