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

package org.gatein.portal.login;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.request.RequestContext;
import juzu.request.RequestParameter;
import juzu.template.Template;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.portal.servlet.Context;

public class LoginController {

    @Inject
    private LoginFailureMessage loginFailureMessage;

    @Inject
    private LoginHelper loginHelper;

    @Inject
    private SSOHelper ssoHelper;

    @Inject
    @Path("login.gtmpl")
    private Template login;

    @View
    @Route(value = "/dologout", priority = 1)
    public Response doLogout(RequestContext context) throws Exception {
        loginHelper.logout();

        Map<String, RequestParameter> parameters = context.getParameters();
        String initURL = parameters.containsKey("initURL") ? parameters.get("initURL").getValue() : context.getHttpContext().getContextPath();
        return Response.redirect(initURL);
    }

    @Action
    @Route(value = "/actionLogin", priority = 1)
    public Response actionLogin(String username, String password, String initURL) {
        try {
            this.loginHelper.login(username, password);
        } catch (Exception e) {
            loginFailureMessage.setError("Username or password incorrect!");
        }

        return LoginController_.login(initURL);
    }


    @View
    @Route(value = "/dologin", priority = 1)
    public Response doLogin(String initURL, RequestContext context) {
        return this.doView(initURL, context);
    }
    @View
    @Route(value = "/login", priority = 1)
    public Response login(String initURL, RequestContext context) throws Exception {
        return this.doView(initURL, context);
    }

    private Response doView(String initURL, RequestContext context) {
        String contextPath = context.getHttpContext().getContextPath();
        if(initURL == null || initURL.isEmpty()) initURL = contextPath;

        //TODO: WebRequestBridge alway return null when call getSecurityContext()
        /*if(context.getSecurityContext() != null) {
            status = context.getSecurityContext().getRemoteUser() != null ? AUTHENTICATED : FAILED;
        }*/
        HttpServletRequest request = Context.getCurrentRequest();
        if(request != null && request.getRemoteUser() != null) {
            //Redirect to init page when user logged in
            return Response.redirect(initURL);
        }

        //If SSO enable: redirect to sso
        if(this.ssoHelper.isSSOEnabled()) {
            return Response.redirect(contextPath + ssoHelper.getSSORedirectURLSuffix());
        }

        List<OauthProviderDescriptor> oauthProviders = loginHelper.getOauthProviderDescriptors(contextPath);
        if(oauthProviders == null) {
            oauthProviders = Collections.emptyList();
        }

        return login.with()
                .set("oauthProviders", oauthProviders)
                .set("initURL", initURL)
                .ok()
                .with(juzu.PropertyType.STYLESHEET, "login-stylesheet")
                .with(juzu.PropertyType.STYLESHEET, "social-buttons");
    }
}
