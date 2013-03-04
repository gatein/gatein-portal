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

package org.exoplatform.portal.application;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.exoplatform.portal.webui.register.UIRegisterOAuth;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.organization.UIUserProfileInputSet;
import org.gatein.common.exception.GateInException;
import org.gatein.common.exception.GateInExceptionConstants;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.utils.OAuthConstants;

/**
 * This lifecycle is used to update WebUI state based on OAuth events from Http filters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthLifecycle implements ApplicationLifecycle<PortalRequestContext> {

    /** . */
    private final Logger log = LoggerFactory.getLogger(OAuthLifecycle.class);

    private AuthenticationRegistry authRegistry;

    @Override
    public void onInit(Application app) throws Exception {
        this.authRegistry = (AuthenticationRegistry)app.getApplicationServiceContainer().getComponentInstanceOfType(AuthenticationRegistry.class);
    }

    @Override
    public void onStartRequest(Application app, PortalRequestContext context) throws Exception {
        HttpServletRequest httpRequest = context.getRequest();
        HttpSession httpSession = httpRequest.getSession();

        User oauthAuthenticatedUser = (User)authRegistry.getAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);

        // Display Registration form after successful OAuth authentication.
        if (oauthAuthenticatedUser != null) {
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

            if (log.isTraceEnabled()) {
                log.trace("Found user, which has been authenticated through OAuth. Username is " + oauthAuthenticatedUser.getUserName());
            }

            if (!uiMaskWS.isShow() || !uiMaskWS.getUIComponent().getClass().equals(UIRegisterOAuth.class)) {
                if (log.isTraceEnabled()) {
                    log.trace("Showing registration form for OAuth registration");
                }
                UIComponent uiLogin = uiMaskWS.createUIComponent(UIRegisterOAuth.class, null, null);
                uiMaskWS.setUIComponent(uiLogin);
                Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
            }
        }

        // Show message about successful social account linking
        String oauthProviderUsernameAttrName = (String)httpSession.getAttribute(OAuthConstants.ATTRIBUTE_LINKED_OAUTH_PROVIDER_USERNAME_ATTR_NAME);
        if (oauthProviderUsernameAttrName != null) {
            httpSession.removeAttribute(OAuthConstants.ATTRIBUTE_LINKED_OAUTH_PROVIDER_USERNAME_ATTR_NAME);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME, oauthProviderUsernameAttrName);
            map.put(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME, context.getRemoteUser());
            Object[] args = UIUserProfileInputSet.convertOAuthExceptionAttributes(context, "UIAccountSocial.label.", map);
            context.getUIApplication().addMessage(new ApplicationMessage("UIAccountSocial.msg.successful-link", args));
        }

        // Show message about failed social account linking
        GateInException gtnException = (GateInException)httpSession.getAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_AFTER_FAILED_LINK);
        if (gtnException != null) {
            httpSession.removeAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_AFTER_FAILED_LINK);

            UIUserProfileInputSet.addOAuthExceptionMessage(context, gtnException, context.getUIApplication());
        }
    }

    @Override
    public void onFailRequest(Application app, PortalRequestContext context, RequestFailure failureType) {
    }

    @Override
    public void onEndRequest(Application app, PortalRequestContext context) throws Exception {
    }

    @Override
    public void onDestroy(Application app) throws Exception {
    }
}
