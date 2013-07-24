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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gatein.portal.servlet.Context;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.security.Credentials;

public class LoginHelper {
    @Inject
    private OAuthProviderTypeRegistry providerTypeRegistry;

    public void login(String username, String password) throws ServletException, IOException {
        if(username == null || username.isEmpty()) throw new ServletException("Login failure with empty username");
        Credentials credentials = new Credentials(username, password);
        ServletContainer container = ServletContainerFactory.getServletContainer();
        container.login(this.getCurrentRequest(), this.getCurrentResponse(), credentials);
    }

    public void logout() {
        try {
            ServletContainer container = ServletContainerFactory.getServletContainer();
            container.logout(Context.getCurrentRequest(), Context.getCurrentResponse());
        } catch (Exception e){}
    }

    public List<OauthProviderDescriptor> getOauthProviderDescriptors(String contextPath) {
        List<OauthProviderDescriptor> oauthProviders = new LinkedList<OauthProviderDescriptor>();
        if (providerTypeRegistry.isOAuthEnabled()) {
            for (OAuthProviderType provider : providerTypeRegistry.getEnabledOAuthProviders()) {
                String type = "twitter";
                if (provider.getKey().equals("GOOGLE")) {
                    type = "google-plus";
                } else if (provider.getKey().equals("FACEBOOK")) {
                    type = "facebook";
                }
                oauthProviders.add(new OauthProviderDescriptor(provider.getFriendlyName(), provider.getInitOAuthURL(contextPath), type));
            }
        }
        return oauthProviders;
    }

    private HttpServletRequest getCurrentRequest() {
        HttpServletRequest request = Context.getCurrentRequest();
        if(request != null) {
            return request;
        }
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass("org.gatein.sso.agent.tomcat.ServletAccess");
            Method getRequestMethod = clazz.getDeclaredMethod("getRequest");
            request = (HttpServletRequest)getRequestMethod.invoke(null);
        } catch (Exception e){}

        return request;
    }

    private HttpServletResponse getCurrentResponse() {
        HttpServletResponse response = Context.getCurrentResponse();
        if(response != null) {
            return response;
        }
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass("org.gatein.sso.agent.tomcat.ServletAccess");
            Method getRequestMethod = clazz.getDeclaredMethod("getResponse");
            response = (HttpServletResponse)getRequestMethod.invoke(null);
        } catch (Exception e){}


        return response;
    }
}
