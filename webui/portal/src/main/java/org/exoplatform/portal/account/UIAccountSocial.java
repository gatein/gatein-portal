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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.exoplatform.portal.application.PortalRequestContext;
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
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.utils.OAuthConstants;

/**
 * Social networks tab of user profile
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/portal/webui/portal/UIAccountSocial.gtmpl",
                 events    = { @EventConfig(listeners = UIAccountSocial.UnlinkSocialAccountActionListener.class) })
public class UIAccountSocial extends UIForm {

    private static final Logger log = LoggerFactory.getLogger(UIAccountSocial.class);

    public UIAccountSocial() throws Exception {
        addReadOnlyInput(OAuthConstants.PROFILE_FACEBOOK_USERNAME, OAuthConstants.PROFILE_FACEBOOK_USERNAME);
        addReadOnlyInput(OAuthConstants.PROFILE_GOOGLE_USERNAME, OAuthConstants.PROFILE_GOOGLE_USERNAME);

        updateUIFields();
    }

    @Override
    public void processRender(WebuiRequestContext context) throws Exception {
        updateUIFields();
        super.processRender(context);
    }

    private void updateUIFields() {
        OAuthUsernames oauthUsernames = getOauthUsernamesFromBackend();
        getUIStringInput(OAuthConstants.PROFILE_FACEBOOK_USERNAME).setValue(oauthUsernames.facebookUsername);
        getUIStringInput(OAuthConstants.PROFILE_GOOGLE_USERNAME).setValue(oauthUsernames.googleUsername);
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

    private void addReadOnlyInput(String name, String bindingExpression) {
        UIFormStringInput uiInput = new UIFormStringInput(name, null);
        uiInput.setReadOnly(true);
        addUIFormInput(uiInput);
    }

    public void saveURLAfterLinkSocialAccount() {
        PortalRequestContext prContext = Util.getPortalRequestContext();
        HttpSession session = prContext.getRequest().getSession();
        session.setAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT, prContext.getRequestURI());
    }

    public String getLinkSocialAccountURL(String fieldName) {
        String reqContextPath = Util.getPortalRequestContext().getRequestContextPath();
        if (OAuthConstants.PROFILE_FACEBOOK_USERNAME.equals(fieldName)) {
             return reqContextPath + OAuthConstants.FACEBOOK_AUTHENTICATION_URL_PATH;
        } else if (OAuthConstants.PROFILE_GOOGLE_USERNAME.equals(fieldName)) {
            return reqContextPath + OAuthConstants.GOOGLE_AUTHENTICATION_URL_PATH;
        } else {
            throw new IllegalArgumentException("Unknown argument " + fieldName);
        }
    }

    public void saveFieldNameForSocialAccountUnlink(String fieldName) {
        ConversationState.getCurrent().setAttribute(OAuthConstants.ATTRIBUTE_SOCIAL_NETWORK_PROVIDER_TO_UNLINK, fieldName);
    }

    public static class UnlinkSocialAccountActionListener extends EventListener<UIAccountSocial> {

        public void execute(Event<UIAccountSocial> event) throws Exception {
            UIAccountSocial uiForm = event.getSource();

            OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIApplication uiApp = context.getUIApplication();

            ConversationState state = ConversationState.getCurrent();
            String userName = ((User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE)).getUserName();
            User user = service.getUserHandler().findUserByName(userName);

            if (user != null) {
                UserProfile userProfile = (UserProfile)prContext.getAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME);
                String socialAccountFieldToUnlink = (String)ConversationState.getCurrent().getAttribute(OAuthConstants.ATTRIBUTE_SOCIAL_NETWORK_PROVIDER_TO_UNLINK);

                if (socialAccountFieldToUnlink != null) {
                    userProfile.setAttribute(socialAccountFieldToUnlink, null);
                } else {
                    log.warn("Social account field to unlink not found");
                }

                service.getUserProfileHandler().saveUserProfile(userProfile, true);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME, socialAccountFieldToUnlink);
                map.put(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME, userName);
                Object[] args = UIUserProfileInputSet.convertOAuthExceptionAttributes(context, "UIAccountSocial.label.", map);
                uiApp.addMessage(new ApplicationMessage("UIAccountSocial.msg.successful-unlink", args));

                prContext.setAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME, userProfile);
                uiForm.updateUIFields();
                prContext.addUIComponentToUpdateByAjax(uiForm);

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
