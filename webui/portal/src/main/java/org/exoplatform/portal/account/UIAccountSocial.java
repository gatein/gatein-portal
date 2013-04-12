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

import java.util.Collection;

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
import org.exoplatform.web.application.Parameter;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.AccessTokenContext;
import org.gatein.security.oauth.common.OAuthProviderType;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.data.SocialNetworkService;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.registry.OAuthProviderTypeRegistry;

/**
 * Social networks tab of user profile
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ComponentConfig(lifecycle = Lifecycle.class, template = "system:/groovy/portal/webui/portal/UIAccountSocial.gtmpl",
                 events    = { @EventConfig(listeners = UIAccountSocial.UnlinkSocialAccountActionListener.class) })
public class UIAccountSocial extends UIForm {

    private static final Logger log = LoggerFactory.getLogger(UIAccountSocial.class);

    private static final String PARAM_PROVIDER_FOR_UNLINK = "_paramProviderForUnlink";

    public UIAccountSocial() throws Exception {
        for (OAuthProviderType oauthPrType : getOAuthProviderTypes()) {
            UIFormStringInput uiInput = new UIFormStringInput(oauthPrType.getUserNameAttrName(), null);
            uiInput.setReadOnly(true);
            addUIFormInput(uiInput);
        }

        updateUIFields();
    }

    public Collection<OAuthProviderType> getOAuthProviderTypes() {
        // For now return all available oauthProviderTypes
        return getApplicationComponent(OAuthProviderTypeRegistry.class).getEnabledOAuthProviders();
    }

    @Override
    public void processRender(WebuiRequestContext context) throws Exception {
        updateUIFields();
        super.processRender(context);
    }

    private void updateUIFields() {
        UserProfile userProfile = (UserProfile)Util.getPortalRequestContext().getAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME);

        if (userProfile == null) {
            return;
        }

        for (OAuthProviderType oauthPrType : getOAuthProviderTypes()) {
            String usernameAttrName = oauthPrType.getUserNameAttrName();
            String oauthUsername = userProfile.getAttribute(usernameAttrName);
            getUIStringInput(usernameAttrName).setValue(oauthUsername);
        }
    }

    public void saveURLAfterLinkSocialAccount() {
        PortalRequestContext prContext = Util.getPortalRequestContext();
        HttpSession session = prContext.getRequest().getSession();
        session.setAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT, prContext.getRequestURI());
    }

    public String getLinkSocialAccountURL(OAuthProviderType oauthPrType) {
        String reqContextPath = Util.getPortalRequestContext().getRequestContextPath();
        return oauthPrType.getInitOAuthURL(reqContextPath);
    }

    public String getUnlinkSocialAccountURL(OAuthProviderType oauthPrType) throws Exception {
        Parameter[] params = new Parameter[] { new Parameter(PARAM_PROVIDER_FOR_UNLINK, oauthPrType.getKey()) };
        return super.event("UnlinkSocialAccount", null, params);
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

                String unlinkProviderKey = prContext.getRequestParameter(PARAM_PROVIDER_FOR_UNLINK);
                OAuthProviderType oauthProviderTypeToUnlink = uiForm.getApplicationComponent(OAuthProviderTypeRegistry.class).getOAuthProvider(unlinkProviderKey);

                // Obtain current accessToken
                AccessTokenContext accessToken = uiForm.getApplicationComponent(SocialNetworkService.class).getOAuthAccessToken(oauthProviderTypeToUnlink, userName);

                // Unlink social account in userProfile (AccessTokenInvalidationListener will automatically remove accessToken)
                if (oauthProviderTypeToUnlink != null) {
                    userProfile.setAttribute(oauthProviderTypeToUnlink.getUserNameAttrName(), null);
                } else {
                    log.warn("Social account field to unlink not found");
                }
                service.getUserProfileHandler().saveUserProfile(userProfile, true);

                // Revoke accessToken remotely
                if (accessToken != null) {
                    try {
                        oauthProviderTypeToUnlink.getOauthProviderProcessor().revokeToken(accessToken);
                    } catch (OAuthException oe) {
                        if (OAuthExceptionCode.EXCEPTION_CODE_TOKEN_REVOKE_FAILED.equals(oe.getExceptionCode())) {
                            Throwable t = oe.getCause() != null ? oe.getCause() : oe;
                            ApplicationMessage appMessage = new ApplicationMessage("UIAccountSocial.msg.failed-revoke", null, ApplicationMessage.WARNING);
                            appMessage.setArgsLocalized(false);
                            uiApp.addMessage(appMessage);
                            log.warn("Revocation of accessToken failed for user " + userName + ". Details: " + t.getClass() + ": " + t.getMessage());
                        } else {
                            throw oe;
                        }
                    }
                }

                Object[] args = { oauthProviderTypeToUnlink.getFriendlyName(), userName};
                ApplicationMessage appMessage = new ApplicationMessage("UIAccountSocial.msg.successful-unlink", args);
                appMessage.setArgsLocalized(false);
                uiApp.addMessage(appMessage);

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

}
