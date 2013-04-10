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
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.UserProfile;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.OAuthCodec;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.social.FacebookConstants;
import org.gatein.security.oauth.social.FacebookPrincipal;
import org.gatein.security.oauth.social.FacebookProcessor;
import org.gatein.security.oauth.utils.OAuthUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GateInFacebookProcessorImpl implements GateInFacebookProcessor {

    private static Logger log = LoggerFactory.getLogger(GateInFacebookProcessorImpl.class);

    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String redirectURL;
    private final FacebookProcessor facebookProcessor;

    public GateInFacebookProcessorImpl(ExoContainerContext context, InitParams params) {
        this.clientId = params.getValueParam("clientId").getValue();
        this.clientSecret = params.getValueParam("clientSecret").getValue();
        String scope = params.getValueParam("scope").getValue();
        String redirectURL = params.getValueParam("redirectURL").getValue();

        if (clientId == null || clientId.length() == 0 || clientId.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'clientId' needs to be provided. The value should be " +
                    "clientId of your Facebook application");
        }

        if (clientSecret == null || clientSecret.length() == 0 || clientSecret.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'clientSecret' needs to be provided. The value should be " +
                    "clientSecret of your Facebook application");
        }

        this.scope = scope == null ? "email" : scope;

        if (redirectURL == null || redirectURL.length() == 0) {
            this.redirectURL = "http://localhost:8080/" + context.getName() + OAuthConstants.FACEBOOK_AUTHENTICATION_URL_PATH;
        }  else {
            this.redirectURL = redirectURL.replaceAll("@@portal.container.name@@", context.getName());
        }

        log.debug("configuration: clientId=" + this.clientId +
                ", clientSecret=" + clientSecret +
                ", scope=" + this.scope +
                ", redirectURL=" + this.redirectURL);

        // Use empty rolesList because we don't need rolesList for GateIn integration
        this.facebookProcessor = new FacebookProcessor(this.clientId , this.clientSecret, this.scope, this.redirectURL, Arrays.asList(new String[]{}));
    }

    @Override
    public FacebookInteractionState processFacebookAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String scope) throws IOException {
        // Save custom scope as attribute into session
        httpRequest.getSession().setAttribute(OAuthConstants.ATTRIBUTE_OAUTH_SCOPE, scope);

        return processFacebookAuthInteractionImpl(httpRequest, httpResponse, new FacebookProcessor(this.clientId , this.clientSecret, scope, this.redirectURL, Arrays.asList(new String[]{})));
    }


    @Override
    public FacebookInteractionState processFacebookAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        return processFacebookAuthInteractionImpl(httpRequest, httpResponse, this.facebookProcessor);
    }


    protected FacebookInteractionState processFacebookAuthInteractionImpl(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FacebookProcessor facebookProcessor) throws IOException {
        HttpSession session = httpRequest.getSession();
        String state = (String) session.getAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);

        if (log.isTraceEnabled()) {
            log.trace("state=" + state);
        }

        // Very initial request to portal
        if (state == null || state.isEmpty()) {
            facebookProcessor.initialInteraction(httpRequest, httpResponse);
            state = (String) session.getAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);
            return new FacebookInteractionState(state, null, null);
        }

        // We are authenticated in Facebook and our app is authorized. Finish OAuth handshake by obtaining accessToken and initial info
        if (state.equals(FacebookProcessor.STATES.AUTH.name())) {
            FacebookPrincipal principal = (FacebookPrincipal)facebookProcessor.getPrincipal(httpRequest, httpResponse);

            if (principal == null) {
                throw new OAuthException(OAuthExceptionCode.EXCEPTION_UNSPECIFIED, null, "Principal was null. Maybe login modules need to be configured properly.");
            } else {
                state = FacebookProcessor.STATES.FINISH.name();
                session.setAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE, state);

                // Try to obtain scope to use from session. Use default one if it's not available in session
                String scope = (String)session.getAttribute(OAuthConstants.ATTRIBUTE_OAUTH_SCOPE);
                if (scope == null) {
                    scope = this.scope;
                }

                return new FacebookInteractionState(state, principal, scope);
            }
        }

        // Likely shouldn't happen...
        return new FacebookInteractionState(state, null, null);
    }

    @Override
    public FacebookPrincipal getPrincipal(String accessToken) {
        return (FacebookPrincipal)facebookProcessor.readInIdentity(accessToken);
    }

    @Override
    public void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, OAuthCodec codec, FacebookAccessTokenContext accessTokenContext) {
        String realAccessToken = accessTokenContext.getAccessToken();
        String encodedAccessToken = codec.encodeString(realAccessToken);

        // Encoded accessToken could be longer than 255 characters. So we need to split it
        if (encodedAccessToken.length() > 255) {
            String encodedAccessToken1 = encodedAccessToken.substring(0, 250);
            String encodedAccessToken2 = encodedAccessToken.substring(250);
            userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_1, encodedAccessToken1);
            userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_2, encodedAccessToken2);
        } else {
            userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_1, encodedAccessToken);
            userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_2, null);
        }
        userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_SCOPE, accessTokenContext.getScopesAsString());
    }

    @Override
    public FacebookAccessTokenContext getAccessTokenFromUserProfile(UserProfile userProfile, OAuthCodec codec) {
        String encodedAccessToken1 = userProfile.getAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_1);
        String encodedAccessToken2 = userProfile.getAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_2);

        // We don't have token in userProfile
        if (encodedAccessToken1 == null) {
            return null;
        }

        String encodedAccessToken = encodedAccessToken1;
        if (encodedAccessToken2 != null) {
            encodedAccessToken = encodedAccessToken + encodedAccessToken2;
        }

        String accessToken = codec.decodeString(encodedAccessToken);
        String scopesAsString = userProfile.getAttribute(OAuthConstants.PROFILE_FACEBOOK_SCOPE);
        return new FacebookAccessTokenContext(accessToken, scopesAsString);
    }

    @Override
    public void removeAccessTokenFromUserProfile(UserProfile userProfile) {
        userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_1, null);
        userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN_2, null);
        userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_SCOPE, null);
    }

    @Override
    public void revokeToken(FacebookAccessTokenContext accessToken) {
        try {
            String realAccessToken = accessToken.getAccessToken();
            String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("/permissions?access_token=")
                    .append(URLEncoder.encode(realAccessToken, "UTF-8")).append("&method=delete").toString();
            URL revokeUrl = new URL(urlString);
            String revokeContent = OAuthUtils.readUrlContent(revokeUrl.openConnection());
            if (log.isTraceEnabled()) {
                log.trace("Successfully revoked facebook accessToken " + realAccessToken + ", revokeContent=" + revokeContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
