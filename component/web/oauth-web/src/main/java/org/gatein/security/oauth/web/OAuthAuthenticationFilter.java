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

package org.gatein.security.oauth.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.services.organization.User;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.spi.OAuthPrincipal;
import org.gatein.security.oauth.spi.SocialNetworkService;
import org.gatein.security.oauth.utils.OAuthUtils;
import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;

/**
 * This filter has already access to authenticated OAuth principal, so it's work starts after successful OAuth authentication.
 *
 * Filter is useful only for anonymous user
 *
 * Responsibility of this filter is to handle integration with GateIn (Redirect to GateIn registration if needed, establish context
 * and redirect to JAAS to finish GateIn authentication etc)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthAuthenticationFilter extends AbstractSSOInterceptor {

    private static Logger log = LoggerFactory.getLogger(OAuthAuthenticationFilter.class);

    private String loginUrl;
    private String registrationUrl;
    private boolean attachUsernamePasswordToLoginURL;

    private SocialNetworkService socialNetworkService;
    private AuthenticationRegistry authenticationRegistry;

    @Override
    protected void initImpl() {
        this.loginUrl = getInitParameter("loginUrl");
        this.registrationUrl = getInitParameter("registrationUrl");
        if (registrationUrl == null) {
            registrationUrl = "/" + getExoContainer().getContext().getName() + "/";
        }

        String attachUsernamePasswordToLoginURLConfig = getInitParameter("attachUsernamePasswordToLoginURL");
        this.attachUsernamePasswordToLoginURL = attachUsernamePasswordToLoginURLConfig == null ? true : Boolean.parseBoolean(attachUsernamePasswordToLoginURLConfig);

        log.debug("OAuthAuthenticationFilter configuration: loginURL=" + loginUrl +
                ", registrationUrl=" + this.registrationUrl +
                ", attachUsernamePasswordToLoginURL=" + this.attachUsernamePasswordToLoginURL);

        socialNetworkService = (SocialNetworkService)getExoContainer().getComponentInstanceOfType(SocialNetworkService.class);
        authenticationRegistry = (AuthenticationRegistry)getExoContainer().getComponentInstanceOfType(AuthenticationRegistry.class);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        // Simply continue with request if we are already authenticated
        if (httpRequest.getRemoteUser() != null) {
            chain.doFilter(request, response);
            return;
        }

        // Simply continue with request if we are in the middle of registration process
        User oauthAuthenticatedUser = (User)authenticationRegistry.getAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
        if (oauthAuthenticatedUser != null) {
            chain.doFilter(request, response);
            return;
        }

        OAuthPrincipal principal = (OAuthPrincipal)authenticationRegistry.getAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);

        if (principal != null) {
            processPrincipal(httpRequest, httpResponse, principal);
        }  else {
            chain.doFilter(request, response);
        }
    }


    protected void processPrincipal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OAuthPrincipal principal) throws IOException {
        User portalUser = socialNetworkService.findUserByOAuthProviderUsername(principal.getOauthProviderType(), principal.getUserName());

        if (portalUser == null) {
            // This means that user has been successfully authenticated via OAuth, but doesn't exist in GateIn. So we need to establish context
            // with AuthenticationRegistry and redirect to GateIn registration form
            handleRedirectToRegistrationForm(httpRequest, httpResponse, principal);
        } else {
            // This means that user has been successfully authenticated via OAuth and exists in GateIn. So we need to establish SSO context
            // and clean our own local context from AuthenticationRegistry. Then redirect to loginUrl to perform GateIn WCI login
            handleRedirectToPortalLogin(httpRequest, httpResponse, portalUser, principal);
            cleanAuthenticationContext(httpRequest);
        }
    }


    protected void handleRedirectToRegistrationForm(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OAuthPrincipal principal)
            throws IOException {
        if (log.isTraceEnabled()) {
            log.trace("Not found portalUser with username " + principal.getUserName() + ". Redirecting to registration form");
        }

        User gateInUser = OAuthUtils.convertOAuthPrincipalToGateInUser(principal);
        authenticationRegistry.setAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER, gateInUser);

        String registrationRedirectUrl = getRegistrationRedirectURL(httpRequest);
        registrationRedirectUrl = httpResponse.encodeRedirectURL(registrationRedirectUrl);
        httpResponse.sendRedirect(registrationRedirectUrl);
    }


    protected String getRegistrationRedirectURL(HttpServletRequest req) {
        String registrationURL = (String)req.getSession().getAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT);
        if (registrationURL == null) {
            registrationURL = this.registrationUrl;
        }

        return registrationURL;
    }


    protected void handleRedirectToPortalLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse, User portalUser, OAuthPrincipal principal)
            throws IOException {
        if (log.isTraceEnabled()) {
            log.trace("Found portalUser " + portalUser + " corresponding to oauthPrincipal");
        }

        authenticationRegistry.setAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS, portalUser);
        if(portalUser.isEnabled()) {
            socialNetworkService.updateOAuthAccessToken(principal.getOauthProviderType(), portalUser.getUserName(), principal.getAccessToken());
        }

        // Now Facebook/Google authentication handshake is finished and credentials are in session. We can redirect to JAAS authentication
        String loginRedirectURL = httpResponse.encodeRedirectURL(getLoginRedirectUrl(httpRequest, portalUser.getUserName()));
        httpResponse.sendRedirect(loginRedirectURL);
    }


    protected String getLoginRedirectUrl(HttpServletRequest req, String username) {
        StringBuilder url = new StringBuilder(this.loginUrl);

        if (attachUsernamePasswordToLoginURL) {
            String fakePassword = req.getSession().getId() + "_" + String.valueOf(System.currentTimeMillis());

            // Use sessionId and system millis as password (similar like spnego is doing)
            url.append("?username=").append(username).append("&password=").append(fakePassword);

            String initialURI = OAuthUtils.getURLToRedirectAfterLinkAccount(req, req.getSession());
            initialURI = OAuthUtils.encodeParam(initialURI);
            url.append("&").append("initialURI").append("=").append(initialURI);
        }

        return url.toString();
    }


    protected void cleanAuthenticationContext(HttpServletRequest httpRequest) {
        authenticationRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
        authenticationRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    }

}
