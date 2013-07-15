/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.ui.register;

import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.request.RequestContext;
import juzu.template.Template;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.gatein.portal.common.kernel.ThreadContext;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.spi.OAuthPrincipal;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;

/**
 * @author Julien Viet
 */
public class Controller {

    @Inject
    @Path("index.gtmpl")
    Template index;

    @Inject
    Flash flash;

    @Inject
    UserManager manager;

    @Inject
    AuthenticationRegistry registry;

    @Inject
    OauthProviderHelper oauthProviderHelper;

    @View
    public Response index(RequestContext context) {
        User portalUser = null;
        HttpServletRequest request = ThreadContext.getCurentHttpServletRequest();
        if(request != null) {
            portalUser = (User)registry.getAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
        }

        if (portalUser == null) {
            portalUser = new UserImpl();
        }
        UserBean userBean = new UserBean(portalUser);

        List<OauthProviderDescriptor> oauthProviders = oauthProviderHelper.getOauthProviderDescriptors(context.getHttpContext().getContextPath());

        return index.with().set("userBean", userBean).set("oauthProviders", oauthProviders).ok();
    }

    @Action
    public Response register(UserBean userBean, String confirmPassword) {
        if (!userBean.password.equals(confirmPassword)) {
            flash.setError("Password and Confirm Password must be the same.");
        } else {
            try {
                UserBean isExisted = manager.getUser(userBean.userName);
                if (isExisted != null) {
                    flash.setError("This user is already existed. Please enter different userName.");
                } else {
                    OAuthPrincipal oauthPrincipal = null;
                    HttpServletRequest request = ThreadContext.getCurentHttpServletRequest();
                    if(request != null) {
                         oauthPrincipal = (OAuthPrincipal)registry.getAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
                    }
                    manager.saveUser(userBean, oauthPrincipal);
                    flash.setSuccess("You have successfully registered a new account!");
                    flash.setUserName(userBean.userName);
                }
            } catch (Exception e) {
                flash.setError("Could not register user due to internal error.");
                e.printStackTrace();
            }
        }
        return Controller_.index();
    }
}