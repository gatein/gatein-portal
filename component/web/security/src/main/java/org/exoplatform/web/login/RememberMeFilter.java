/*
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.web.login;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.exoplatform.web.security.security.CookieTokenService;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.security.Credentials;

/**
 * The remember me filter performs an authentication using the {@link ServletContainer} when the current request is a GET
 * request, the user is not authenticated and there is a remember me token cookie in the request.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RememberMeFilter extends AbstractFilter {
    //value of this field need equals with: org.gatein.security.oauth.common.OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS
    public static final String ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS = "_authenticatedPortalUserForJaas";

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) req, (HttpServletResponse) resp, chain);
    }

    private void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        if (req.getRemoteUser() == null) {
            String token = LoginServlet.getRememberMeTokenCookie(req);
            if (token != null) {
                ExoContainer container = getContainer();
                CookieTokenService tokenservice = container.getComponentInstanceOfType(CookieTokenService.class);
                Credentials credentials = tokenservice.validateToken(token, false);
                if (credentials != null) {
                    ServletContainer servletContainer = ServletContainerFactory.getServletContainer();
                    try {
                        servletContainer.login(req, resp, credentials);
                    } catch (Exception e) {
                        // Could not authenticate
                    }
                }
            }

            // Clear token cookie if we did not authenticate
            if (req.getRemoteUser() == null) {
                Cookie cookie = new Cookie(LoginServlet.COOKIE_NAME, "");
                cookie.setPath(req.getContextPath());
                cookie.setMaxAge(0);
                resp.addCookie(cookie);
            }
        }

        //Process oauth rememberMe
        if(req.getRemoteUser() == null) {
            String token = LoginServlet.getOauthRememberMeTokenCookie(req);
            if(token != null) {
                ExoContainer container = getContainer();
                CookieTokenService tokenService = container.getComponentInstanceOfType(CookieTokenService.class);
                Credentials credentials = tokenService.validateToken(token, false);
                AuthenticationRegistry authRegistry = container.getComponentInstanceOfType(AuthenticationRegistry.class);
                OrganizationService orgService = container.getComponentInstanceOfType(OrganizationService.class);

                if (credentials != null) {
                    ServletContainer servletContainer = ServletContainerFactory.getServletContainer();
                    try {
                        String username = credentials.getUsername();

                        User portalUser = orgService.getUserHandler().findUserByName(username, UserStatus.ENABLED);
                        if(portalUser != null) {
                            authRegistry.setAttributeOfClient(req, ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS, portalUser);

                            servletContainer.login(req, resp, credentials);
                        }
                    } catch (Exception e) {
                        // Could not authenticate
                    }
                }

                // Clear token cookie if we did not authenticate
                if (req.getRemoteUser() == null) {
                    Cookie cookie = new Cookie(LoginServlet.OAUTH_COOKIE_NAME, "");
                    cookie.setPath(req.getContextPath());
                    cookie.setMaxAge(0);
                    resp.addCookie(cookie);
                }
            }
        }

        // Continue
        chain.doFilter(req, resp);
    }

    public void destroy() {
    }
}
