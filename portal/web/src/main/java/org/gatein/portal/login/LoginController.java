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
    private static final int AUTHENTICATED = 1;
    private static final int FAILED = 2;

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

    @View
    @Route(value = "/dologin", priority = 1)
    public Response doLogin() {
        return LoginController_.login();
    }

    @Action
    @Route(value = "/actionLogin", priority = 1)
    public Response actionLogin(String username, String password) {
        try {
            this.loginHelper.login(username, password);
        } catch (Exception e) {
            loginFailureMessage.setError("Username or password incorrect!");
        }

        return LoginController_.login();
    }

    @View
    @Route(value = "/login", priority = 1)
    public Response login(RequestContext context) throws Exception {
        String contextPath = context.getHttpContext().getContextPath();
        Map<String, RequestParameter> parameters = context.getParameters();
        int status = FAILED;

        //TODO: WebRequestBridge alway return null when call getSecurityContext()
        /*if(context.getSecurityContext() != null) {
            status = context.getSecurityContext().getRemoteUser() != null ? AUTHENTICATED : FAILED;
        }*/
        HttpServletRequest request = Context.getCurrentRequest();
        if(request != null) {
            status = request.getRemoteUser() != null ? AUTHENTICATED : FAILED;
        }

        if (status == AUTHENTICATED) {
            String initURL  = parameters.containsKey("initURL")  ? parameters.get("initURL").getValue()  : contextPath;
            return Response.redirect(initURL);
        }

        String username = parameters.containsKey("username") ? parameters.get("username").getValue() : null;
        String password = parameters.containsKey("password") ? parameters.get("password").getValue() : null;
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            try {
                loginHelper.login(username, password);
                return LoginController_.login();
            } catch (Exception e){}
        }

        if(this.ssoHelper.isSSOEnabled()) {
            return Response.redirect(contextPath + ssoHelper.getSSORedirectURLSuffix());
        }

        List<OauthProviderDescriptor> oauthProviders = loginHelper.getOauthProviderDescriptors(contextPath);

        return login.with().set("oauthProviders", oauthProviders).ok().with(juzu.PropertyType.STYLESHEET, "login-stylesheet").with(juzu.PropertyType.STYLESHEET, "social-buttons");
    }
}
