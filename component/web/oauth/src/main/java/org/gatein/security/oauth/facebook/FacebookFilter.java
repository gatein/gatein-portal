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

package org.gatein.security.oauth.facebook;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.User;
import org.gatein.common.exception.GateInExceptionConstants;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.data.OAuthDataStorage;
import org.gatein.common.exception.GateInException;
import org.gatein.security.oauth.utils.OAuthConstants;
import org.gatein.sso.agent.GenericAgent;
import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;
import org.gatein.wci.security.Credentials;
import org.picketlink.social.standalone.fb.FacebookPrincipal;
import org.picketlink.social.standalone.fb.FacebookProcessor;

/**
 * Filter for integration with authentication handhsake via Facebook with usage of OAuth2
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FacebookFilter extends AbstractSSOInterceptor {

    private static Logger log = LoggerFactory.getLogger(FacebookFilter.class);

    // URL to redirect after finish whole Facebook authentication process
    private String loginUrl;
    private boolean attachUsernamePasswordToLoginURL;

    // URL to redirect from Facebook during authentication process
    private String redirectURL;

    private String appid;
    private String appsecret;
    private String scope;
    private FacebookProcessor facebookProcessor;

    private OAuthDataStorage oauthDataStorage;

    @Override
    protected void initImpl() {
        this.loginUrl = getInitParameter("loginUrl");

        this.appid = getInitParameter("appid");
        this.appsecret = getInitParameter("appsecret");
        this.scope = getInitParameter("scope");
        this.redirectURL = getInitParameter("redirectUrl");

        if (appid == null || appid.length() == 0 || appid.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'appid' of FacebookFilter needs to be provided. The value should be " +
                    "appId (clientId) of your Facebook application");
        }

        if (appsecret == null || appsecret.length() == 0 || appsecret.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'appsecret' of FacebookFilter needs to be provided. The value should be " +
                    "appSecret (clientSecret) of your Facebook application");
        }

        if (scope == null || scope.length() == 0) {
            scope = "email";
        }

        if (redirectURL == null || redirectURL.length() == 0) {
            redirectURL = "http://localhost:8080/" + PortalContainer.getInstance().getName() + "/facebookAuth";
        }

        String attachUsernamePasswordToLoginURLConfig = getInitParameter("attachUsernamePasswordToLoginURL");
        this.attachUsernamePasswordToLoginURL = attachUsernamePasswordToLoginURLConfig == null ? true : Boolean.parseBoolean(attachUsernamePasswordToLoginURLConfig);

        log.info("FacebookFilter configuration: loginURL=" + loginUrl +
                ", attachUsernamePasswordToLoginURL=" + this.attachUsernamePasswordToLoginURL +
                ", appid=" + this.appid +
                ", appsecret=" + this.appsecret +
                ", scope=" + this.scope +
                ", redirectURL=" + this.redirectURL);

        // Use empty rolesList because we don't need rolesList for GateIn integration
        facebookProcessor = new FacebookProcessor(appid, appsecret, scope, redirectURL, Arrays.asList(new String[]{}));

        oauthDataStorage = (OAuthDataStorage)getExoContainer().getComponentInstanceOfType(OAuthDataStorage.class);
    }

    @Override
    public void destroy() {
    }

    protected String getRedirectURL() {
        return redirectURL;
    }

    protected String getAppid() {
        return appid;
    }

    protected String getAppsecret() {
        return appsecret;
    }

    protected String getScope() {
        return scope;
    }

    protected FacebookProcessor getFacebookProcessor() {
        return facebookProcessor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        HttpSession session = httpRequest.getSession();
        String state = (String) session.getAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);

        if (log.isTraceEnabled()) {
            log.trace("state=" + state);
        }

        if (FacebookProcessor.STATES.FINISH.name().equals(state)) {
            Principal principal = (Principal)httpRequest.getSession().getAttribute(OAuthConstants.SESSION_ATTRIBUTE_AUTHENTICATED_PRINCIPAL);
            if (principal == null) {
                log.error("Principal was null. Maybe login modules need to be configured properly.");
            }
            processPrincipal(httpRequest, httpResponse, (FacebookPrincipal)principal);
            return;
        }

        // Very initial request to portal
        if (state == null || state.isEmpty()) {
            facebookProcessor.initialInteraction(httpRequest, httpResponse);
            return;
        }

        // We have sent an auth request
        if (state.equals(FacebookProcessor.STATES.AUTH.name())) {
            facebookProcessor.handleAuthStage(httpRequest, httpResponse);
            return;
        }


        // Finish OAuth handshake
        if (state.equals(FacebookProcessor.STATES.AUTHZ.name())) {
            Principal principal = facebookProcessor.getPrincipal(httpRequest, httpResponse);

            if (principal == null) {
                log.error("Principal was null. Maybe login modules need to be configured properly.");
            } else {
                httpRequest.getSession().setAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE, FacebookProcessor.STATES.FINISH.name());
                httpRequest.getSession().setAttribute(OAuthConstants.SESSION_ATTRIBUTE_AUTHENTICATED_PRINCIPAL, principal);
                processPrincipal(httpRequest, httpResponse, (FacebookPrincipal)principal);
            }
        }
    }

    protected void processPrincipal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FacebookPrincipal principal) throws IOException {
        if (log.isTraceEnabled()) {
            log.trace("Obtained principal from Facebook authentication: " + principal);
            log.trace("Facebook accessToken: " + principal.getAccessToken());
        }

        User portalUser = oauthDataStorage.findUserByFacebookUsername(principal.getUsername());
        if (portalUser == null) {
            // TODO: Here we need to start registration flow
            throw new GateInException(GateInExceptionConstants.EXCEPTION_CODE_UNSPECIFIED, null, "TODO: There is not portalUser corresponding to facebookPrincipal: " + principal);
        }

        if (log.isTraceEnabled()) {
            log.trace("Found portalUser " + portalUser + " corresponding to facebookPrincipal with username " + principal.getUsername());
        }

        // TODO: Refactor this by made the method saveSSOCredentials public instead of protected
        new GenericAgent() {

            @Override
            public void saveSSOCredentials(String username, HttpServletRequest httpRequest) {
                super.saveSSOCredentials(username, httpRequest);
            }

        }.saveSSOCredentials(portalUser.getUserName(), httpRequest);

        oauthDataStorage.saveFacebookAccessToken(portalUser.getUserName(), principal.getAccessToken());

        // Now Facebook authentication handshake is finished and credentials are in session. We can redirect to JAAS authentication
        String loginRedirectURL = httpResponse.encodeRedirectURL(getLoginRedirectUrl(httpRequest));
        httpResponse.sendRedirect(loginRedirectURL);
    }

    // Forked from InitiateLoginFilter
    protected String getLoginRedirectUrl(HttpServletRequest req) {
        StringBuilder url = new StringBuilder(this.loginUrl);

        if (attachUsernamePasswordToLoginURL) {
            String fakePassword = req.getSession().getId() + "_" + String.valueOf(System.currentTimeMillis());

            // Try to use username from authenticated credentials
            String username;
            Credentials creds = (Credentials)req.getSession().getAttribute(GenericAgent.AUTHENTICATED_CREDENTIALS);
            if (creds != null) {
                username = creds.getUsername();
            } else {
                // Fallback to fakePassword, but this should't happen (credentials should always be available when this method is called)
                username = fakePassword;
            }

            // Use sessionId and system millis as password (similar like spnego is doing)
            url.append("?username=").append(username).append("&password=").append(fakePassword);
        }

        return url.toString();
    }
}
