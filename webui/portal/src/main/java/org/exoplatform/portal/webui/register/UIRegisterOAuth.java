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

package org.exoplatform.portal.webui.register;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.spi.OAuthPrincipal;

/**
 * Registration form for user, which has been successfully authenticated via OAuth2
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ComponentConfig(template = "system:/groovy/portal/webui/portal/UIRegisterOAuthForm.gtmpl")
public class UIRegisterOAuth extends UIContainer {

    private static Logger log = LoggerFactory.getLogger(UIRegisterOAuth.class);

    private static final String[] ACTIONS = { "SubscribeOAuth", "Reset", "Cancel" };

    static final String REGISTER_FORM_CONFIG_ID = "UIRegisterFormOAuth";

    private final User portalUser;

    public UIRegisterOAuth() throws Exception {
        addChild(UIRegisterForm.class, REGISTER_FORM_CONFIG_ID, REGISTER_FORM_CONFIG_ID);

        UIRegisterForm uiRegisterForm = getChild(UIRegisterForm.class);
        uiRegisterForm.setActions(ACTIONS);

        AuthenticationRegistry authRegistry = getApplicationComponent(AuthenticationRegistry.class);
        User portalUser = (User)authRegistry.getAttributeOfClient(Util.getPortalRequestContext().getRequest(), OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
        if (portalUser == null) {
            log.warn("portalUser from OAuth login is not available!");
            this.portalUser = new UserImpl();
        } else {
            this.portalUser = portalUser;
        }

        setupUserToRegisterForm();
    }

    private void setupUserToRegisterForm() {
        UIRegisterForm uiRegisterForm = getChild(UIRegisterForm.class);
        UIRegisterInputSet uiRegisterInputSet = uiRegisterForm.getChild(UIRegisterInputSet.class);

        uiRegisterInputSet.getUIStringInput(UIRegisterInputSet.USER_NAME).setValue(portalUser.getUserName());
        uiRegisterInputSet.getUIStringInput(UIRegisterInputSet.FIRST_NAME).setValue(portalUser.getFirstName());
        uiRegisterInputSet.getUIStringInput(UIRegisterInputSet.LAST_NAME).setValue(portalUser.getLastName());
        uiRegisterInputSet.getUIStringInput(UIRegisterInputSet.EMAIL_ADDRESS).setValue(portalUser.getEmail());
        uiRegisterInputSet.getUIStringInput(UIRegisterInputSet.PASSWORD).setValue(null);
        uiRegisterInputSet.getUIStringInput(UIRegisterInputSet.CONFIRM_PASSWORD).setValue(null);
        uiRegisterInputSet.getUIStringInput(UIRegisterInputSet.DISPLAY_NAME).setValue(portalUser.getDisplayName());
    }

    public static class ResetActionListener extends EventListener<UIRegisterForm> {

        @Override
        public void execute(Event<UIRegisterForm> event) throws Exception {
            UIRegisterForm registerForm = event.getSource();
            UIRegisterOAuth uiRegisterOAuth = registerForm.getAncestorOfType(UIRegisterOAuth.class);
            uiRegisterOAuth.setupUserToRegisterForm();
            event.getRequestContext().addUIComponentToUpdateByAjax(registerForm);
        }
    }

    public static class CancelActionListener extends UIMaskWorkspace.CloseActionListener {

        @Override
        public void execute(Event<UIComponent> event) throws Exception {
            super.execute(event);

            AuthenticationRegistry authRegistry = event.getSource().getApplicationComponent(AuthenticationRegistry.class);
            HttpServletRequest httpRequest = Util.getPortalRequestContext().getRequest();

            // Clear whole context of OAuth login. See OAuthAuthenticationFilter.cleanAuthenticationContext
            authRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
            authRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);

            if (log.isTraceEnabled()) {
                log.trace("Registration with OAuth properties terminated. Clearing authentication context");
            }
        }
    }

    public static class SubscribeOAuthActionListener extends UIRegisterForm.SubscribeActionListener {

        @Override
        public void execute(Event<UIRegisterForm> event) throws Exception {
            super.execute(event);

            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
            User newUser = (User)context.getAttribute(UIRegisterForm.ATTR_USER);

            // This means that registration has been successful
            if (newUser != null) {
                UIApplication uiApp = context.getUIApplication();
                UIRegisterForm uiRegisterForm = event.getSource();
                PortalRequestContext portalRequestContext = Util.getPortalRequestContext();

                // Save OAuth username as part of user profile of new user
                OrganizationService orgService = uiRegisterForm.getApplicationComponent(OrganizationService.class);
                UserProfileHandler profileHandler = orgService.getUserProfileHandler();
                UserProfile newUserProfile = profileHandler.findUserProfileByName(newUser.getUserName());

                AuthenticationRegistry authRegistry = uiRegisterForm.getApplicationComponent(AuthenticationRegistry.class);
                HttpServletRequest httpRequest = portalRequestContext.getRequest();
                OAuthPrincipal oauthPrincipal = (OAuthPrincipal)authRegistry.getAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);

                if (newUserProfile == null) {
                    newUserProfile = orgService.getUserProfileHandler().createUserProfileInstance(newUser.getUserName());
                }
                newUserProfile.setAttribute(oauthPrincipal.getOauthProviderType().getUserNameAttrName(), oauthPrincipal.getUserName());
                try {
                    profileHandler.saveUserProfile(newUserProfile, true);
                } catch (OAuthException gtnOAuthException) {
                    // Show warning message if user with this facebookUsername (or googleUsername) already exists
                    // NOTE: It could happen only in case of parallel registration of same oauth user from more browser windows
                    if (gtnOAuthException.getExceptionCode() == OAuthExceptionCode.DUPLICATE_OAUTH_PROVIDER_USERNAME) {

                        // Drop new user
                        orgService.getUserHandler().removeUser(newUser.getUserName(), true);

                        // Clear previous message about successful creation of user because we dropped him. Add message about duplicate oauth username
                        Object[] args = new Object[] {gtnOAuthException.getExceptionAttribute(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME),
                                gtnOAuthException.getExceptionAttribute(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_NAME)};
                        ApplicationMessage appMessage = new ApplicationMessage("UIAccountSocial.msg.failed-registration", args, ApplicationMessage.WARNING);
                        uiApp.addMessage(appMessage);
                        return;
                    } else {
                        throw gtnOAuthException;
                    }
                }

                // Clean portalUser from context as we don't need it anymore
                authRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);

                // Clear messages (message about successful registration of user)
                uiApp.clearMessages();

                // Close the registration popup
                UIMaskWorkspace.CloseActionListener closePopupListener = new UIMaskWorkspace.CloseActionListener();
                closePopupListener.execute((Event)event);

                // Redirect to finish login with new user
                SiteKey siteKey = portalRequestContext.getSiteKey();
                NodeURL urlToRedirect = portalRequestContext.createURL(NodeURL.TYPE);
                urlToRedirect.setResource(new NavigationResource(siteKey, portalRequestContext.getNodePath()));

                portalRequestContext.getJavascriptManager().addJavascript("window.location = '" + urlToRedirect.toString() + "';");
            }

        }
    }
}
