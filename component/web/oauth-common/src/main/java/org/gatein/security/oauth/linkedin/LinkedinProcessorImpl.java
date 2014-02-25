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
package org.gatein.security.oauth.linkedin;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.UserProfile;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.spi.InteractionState;
import org.gatein.security.oauth.spi.OAuthCodec;
import org.gatein.security.oauth.utils.OAuthPersistenceUtils;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class LinkedinProcessorImpl implements LinkedinProcessor {

    private static Logger log = LoggerFactory.getLogger(LinkedinProcessorImpl.class);

    private final String redirectURL;
    private final String apiKey;
    private final String apiSecret;

    private final int chunkLength;

    private OAuthService oAuthService;

    public LinkedinProcessorImpl(ExoContainerContext context, InitParams params) {
        this.apiKey = params.getValueParam("apiKey").getValue();
        this.apiSecret = params.getValueParam("apiSecret").getValue();
        String redirectURLParam = params.getValueParam("redirectURL").getValue();

        if (apiKey == null || apiKey.length() == 0 || apiKey.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'clientId' needs to be provided. The value should be " +
                    "clientId of your Twitter application");
        }

        if (apiSecret == null || apiSecret.length() == 0 || apiSecret.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'clientSecret' needs to be provided. The value should be " +
                    "clientSecret of your Twitter application");
        }

        if (redirectURLParam == null || redirectURLParam.length() == 0) {
            this.redirectURL = "http://localhost:8080/" + context.getName() + OAuthConstants.TWITTER_AUTHENTICATION_URL_PATH;
        }  else {
            this.redirectURL = redirectURLParam.replaceAll("@@portal.container.name@@", context.getName());
        }

        if (log.isDebugEnabled()) {
            log.debug("configuration: apiKey=" + apiKey +
                    ", apiSecret=" + apiSecret +
                    ", redirectURL=" + redirectURL);
        }

        this.chunkLength = OAuthPersistenceUtils.getChunkLength(params);

        this.oAuthService = new ServiceBuilder()
                                        .provider(LinkedInApi.class)
                                        .apiKey(apiKey)
                                        .apiSecret(apiSecret)
                                        .callback(redirectURL)
                                        .build();
    }

    @Override
    public InteractionState<LinkedinAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, OAuthException {
        HttpSession session = httpRequest.getSession();

        //See if we are a callback
        Token requestToken = (Token) session.getAttribute(OAuthConstants.ATTRIBUTE_LINKEDIN_REQUEST_TOKEN);
        if (requestToken == null) {
            requestToken = oAuthService.getRequestToken();
            String redirect = oAuthService.getAuthorizationUrl(requestToken);
            oAuthService.getRequestToken();
            httpResponse.sendRedirect(redirect);

            session.setAttribute(OAuthConstants.ATTRIBUTE_LINKEDIN_REQUEST_TOKEN, requestToken);

            return new InteractionState<LinkedinAccessTokenContext>(InteractionState.State.AUTH, null);
        } else {
            session.removeAttribute(OAuthConstants.ATTRIBUTE_LINKEDIN_REQUEST_TOKEN);

            String verifierCode = httpRequest.getParameter("oauth_verifier");
            Verifier verifier = new Verifier(verifierCode);
            Token accessToken = oAuthService.getAccessToken(requestToken, verifier);
            LinkedinAccessTokenContext accessTokenContext = new LinkedinAccessTokenContext(accessToken, this.oAuthService);

            return new InteractionState<LinkedinAccessTokenContext>(InteractionState.State.FINISH, accessTokenContext);
        }
    }

    @Override
    public InteractionState<LinkedinAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String scope) throws IOException, OAuthException {
        if(scope != null) {
            this.oAuthService = new ServiceBuilder()
                    .provider(LinkedInApi.class)
                    .apiKey(apiKey)
                    .apiSecret(apiSecret)
                    .scope(scope)
                    .callback(redirectURL)
                    .build();
        } else {
            this.oAuthService = new ServiceBuilder()
                    .provider(LinkedInApi.class)
                    .apiKey(apiKey)
                    .apiSecret(apiSecret)
                    .callback(redirectURL)
                    .build();
        }
        return this.processOAuthInteraction(httpRequest, httpResponse);
    }

    @Override
    public void revokeToken(LinkedinAccessTokenContext accessToken) throws OAuthException {}

    @Override
    public LinkedinAccessTokenContext validateTokenAndUpdateScopes(LinkedinAccessTokenContext accessToken) throws OAuthException {
        return accessToken;
    }

    @Override
    public <C> C getAuthorizedSocialApiObject(LinkedinAccessTokenContext accessToken, Class<C> socialApiObjectType) {
        return null;
    }

    @Override
    public void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, OAuthCodec codec, LinkedinAccessTokenContext accessToken) {
        String encodedAccessToken = codec.encodeString(accessToken.accessToken.getToken());
        String encodedAccessTokenSecret = codec.encodeString(accessToken.accessToken.getSecret());
        OAuthPersistenceUtils.saveLongAttribute(encodedAccessToken, userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN,
                false, chunkLength);
        OAuthPersistenceUtils.saveLongAttribute(encodedAccessTokenSecret, userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN_SECRET,
                false, chunkLength);
    }

    @Override
    public LinkedinAccessTokenContext getAccessTokenFromUserProfile(UserProfile userProfile, OAuthCodec codec) {
        String encodedAccessToken = OAuthPersistenceUtils.getLongAttribute(userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN, false);
        String encodedAccessTokenSecret = OAuthPersistenceUtils.getLongAttribute(userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN_SECRET, false);
        String decodedAccessToken = codec.decodeString(encodedAccessToken);
        String decodedAccessTokenSecret = codec.decodeString(encodedAccessTokenSecret);

        if(decodedAccessToken == null || decodedAccessTokenSecret == null) {
            return null;
        } else {
            Token token = new Token(decodedAccessToken, decodedAccessTokenSecret);
            return new LinkedinAccessTokenContext(token, oAuthService);
        }
    }

    @Override
    public void removeAccessTokenFromUserProfile(UserProfile userProfile) {
        OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN, false);
        OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN_SECRET, false);
    }
}
