/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.web.login.LoginError;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.web.security.impersonation.ImpersonatedIdentity;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class PortalLogoutLifecycle implements ApplicationLifecycle<WebuiRequestContext> {
    public void onInit(Application app) throws Exception {
    }

    public void onStartRequest(Application app, WebuiRequestContext context) throws Exception {
        LogoutControl.cancelLogout();

        String uid = context.getRemoteUser();
        User user = null;
        if (uid != null) {
            ExoContainer exoContainer = app.getApplicationServiceContainer();
            if (exoContainer != null) {
                OrganizationService organizationService = (OrganizationService) exoContainer
                        .getComponentInstanceOfType(OrganizationService.class);
                user = organizationService.getUserHandler().findUserByName(uid, UserStatus.ANY);
            }

            // If user is not existed OR disabled
            if (user == null || !user.isEnabled()) {
                // If we are in the middle of impersonation, we won't logout
                if (!(ConversationState.getCurrent().getIdentity() instanceof ImpersonatedIdentity)) {
                    logout(user, context);
                }
            }
        }
    }

    private void logout(User user, WebuiRequestContext context) throws Exception {
        LogoutControl.wantLogout();

        Map<String, String> param = null;
        if(user != null) {
            LoginError error = new LoginError(LoginError.DISABLED_USER_ERROR, user.getUserName());
            param = new HashMap<String, String>();
            param.put(LoginError.ERROR_PARAM, error.toString());
        }

        PortalRequestContext prContext = (PortalRequestContext)context;
        prContext.requestAuthenticationLogin(param);
    }

    public void onFailRequest(Application app, WebuiRequestContext context, RequestFailure failureType) {
    }

    public void onEndRequest(Application app, WebuiRequestContext context) throws Exception {
        if (LogoutControl.isLogoutRequired()) {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            HttpServletRequest request = prContext.getRequest();
            HttpServletResponse response = prContext.getResponse();

            if (request.getRemoteUser() != null) {
                ServletContainerFactory.getServletContainer().logout(request, response);
            }
        }
    }

    public void onDestroy(Application app) throws Exception {
    }
}
