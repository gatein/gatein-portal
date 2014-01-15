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

package org.exoplatform.web.login;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.authentication.AuthenticationEvent;
import org.gatein.wci.authentication.AuthenticationEventType;
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.authentication.AuthenticationListener;
import org.gatein.wci.security.Credentials;

/**
 * The login servlet which proceeds as.
 *
 * 0. If user is already authenticated : nothing happens 1. When username and password are provided, a login is attempted 1.1 if
 * login is successful the user is authenticated 1.2 if login fails the user is not authenticated and the login form is
 * displayed with the user name filled 2. When username is provided, the login form is displayed with the user name filled 3.
 * Finally if nothing is provided the login form is displayed
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LoginServlet extends AbstractHttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -1330051083735349589L;

    /** . */
    private static final int UNAUTHENTICATED = 0;

    /** . */
    private static final int AUTHENTICATED = 1;

    /** . */
    private static final int FAILED = 2;

    /** . */
    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);

    /** . */
    public static final String COOKIE_NAME = "rememberme";

    /**
     * Register WCI authentication listener, which is used to bind credentials to temporary authentication registry after each
     * successful login
     *
     * @param config
     * @throws ServletException
     */
    @Override
    protected void afterInit(ServletConfig config) throws ServletException {
        ServletContainerFactory.getServletContainer().addAuthenticationListener(new AuthenticationListener() {
            @Override
            public void onEvent(AuthenticationEvent event) {
                if (event.getType() == AuthenticationEventType.LOGIN) {
                    bindCredentialsToAuthenticationRegistry(getContainer(), event.getRequest(), event.getCredentials());
                }
            }
        });
    }

    /**
     * Extract the remember me token from the request or returns null.
     *
     * @param req the incoming request
     * @return the token
     */
    public static String getRememberMeTokenCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        //
        int status;
        if (req.getRemoteUser() == null) {
            if (username != null && password != null) {
                Credentials credentials = new Credentials(username, password);
                ServletContainer container = ServletContainerFactory.getServletContainer();

                // This will login or send an AuthenticationException
                try {
                    container.login(req, resp, credentials);
                } catch (AuthenticationException e) {
                    log.debug("User authentication failed");
                    if (log.isTraceEnabled()) {
                        log.trace(e.getMessage(), e);
                    }
                }

                //
                status = req.getRemoteUser() != null ? AUTHENTICATED : FAILED;

                // If we are authenticated
                if (status == AUTHENTICATED) {
                    if (log.isTraceEnabled()) {
                        log.trace("User authenticated successfuly through WCI. Will redirect to initialURI");
                    }

                    // Handle remember me
                    String rememberme = req.getParameter("rememberme");
                    if ("true".equals(rememberme)) {
                        // Create token for credentials
                        CookieTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
                        String cookieToken = tokenService.createToken(credentials);
                        if (log.isDebugEnabled()) {
                            log.debug("Found a remember me request parameter, created a persistent token " + cookieToken
                                    + " for it and set it up " + "in the next response");
                        }
                        Cookie cookie = new Cookie(COOKIE_NAME, cookieToken);
                        cookie.setPath(req.getContextPath());
                        cookie.setMaxAge((int) tokenService.getValidityTime());
                        resp.addCookie(cookie);
                    }
                }
            } else {
                log.debug("username or password not provided. Changing status to UNAUTHENTICATED");
                status = UNAUTHENTICATED;
            }
        } else {
            log.debug("User already authenticated. Will redirect to initialURI");
            status = AUTHENTICATED;
        }

        // Obtain initial URI
        String uri = req.getParameter("initialURI");

        // Otherwise compute one
        if (uri == null || uri.length() == 0) {
            uri = req.getContextPath();
            log.debug("No initial URI found, will use default " + uri + " instead ");
        } else {
            log.debug("Found initial URI " + uri);
        }

        // Redirect to initialURI
        if (status == AUTHENTICATED) {
            resp.sendRedirect(resp.encodeRedirectURL(uri));
        } else {
            if (status == FAILED) {
                req.setAttribute("org.gatein.portal.login.error", "whatever");
            }

            // Show login form or redirect to SSO url (/portal/sso) if SSO is enabled
            req.setAttribute("org.gatein.portal.login.initial_uri", uri);
            SSOHelper ssoHelper = (SSOHelper) getContainer().getComponentInstanceOfType(SSOHelper.class);
            if (ssoHelper.skipJSPRedirection()) {
                String ssoRedirectUrl = req.getContextPath() + ssoHelper.getSSORedirectURLSuffix();
                ssoRedirectUrl = resp.encodeRedirectURL(ssoRedirectUrl);
                if (log.isTraceEnabled()) {
                    log.trace("Redirected to SSO login URL: " + ssoRedirectUrl);
                }
                resp.sendRedirect(ssoRedirectUrl);
            } else {
                resp.setContentType("text/html; charset=UTF-8");
                getServletContext().getRequestDispatcher("/login/jsp/login.jsp").include(req, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * Add credentials to {@link ConversationState}.
     *
     * @param credentials the credentials
     */
    private static void bindCredentialsToAuthenticationRegistry(ExoContainer exoContainer, HttpServletRequest req,
            Credentials credentials) {
        AuthenticationRegistry authRegistry = (AuthenticationRegistry) exoContainer
                .getComponentInstanceOfType(AuthenticationRegistry.class);
        if (log.isTraceEnabled()) {
            log.trace("Binding credentials to temporary authentication registry for user " + credentials.getUsername());
        }
        authRegistry.setCredentials(req, credentials);
    }
}
