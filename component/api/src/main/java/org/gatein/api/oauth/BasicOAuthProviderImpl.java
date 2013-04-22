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

package org.gatein.api.oauth;

import java.io.IOException;

import org.gatein.api.internal.Parameters;
import org.gatein.api.oauth.exception.OAuthApiException;
import org.gatein.api.oauth.exception.OAuthApiExceptionCode;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.spi.AccessTokenContext;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.SocialNetworkService;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;

/**
 * Basic implementation of OAuth Provider. It doesn't have full access to WebUI, so some operations are not supported
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class BasicOAuthProviderImpl implements OAuthProvider {

    private final OAuthProviderType internalOAuthProvider;
    private final SocialNetworkService socialNetworkService;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public BasicOAuthProviderImpl(OAuthProviderType internalOAuthProvider, SocialNetworkService socialNetworkService) {
        Parameters.requireNonNull(internalOAuthProvider, "internalOAuthProvider");
        Parameters.requireNonNull(socialNetworkService, "socialNetworkService");
        this.internalOAuthProvider = internalOAuthProvider;
        this.socialNetworkService = socialNetworkService;
    }

    @Override
    public String getKey() {
        return internalOAuthProvider.getKey();
    }

    @Override
    public String getFriendlyName() {
        return internalOAuthProvider.getFriendlyName();
    }

    @Override
    public AccessToken loadAccessToken(String username) throws OAuthApiException {
        try {
            AccessTokenContext accessTokenContext = this.socialNetworkService.getOAuthAccessToken(this.internalOAuthProvider, username);

            if (accessTokenContext == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Not found access token for user " + username + " with provider " + internalOAuthProvider);
                }
                return null;
            } else {
                return new AccessTokenImpl(accessTokenContext);
            }
        } catch (OAuthException oauthEx) {
            throw new OAuthApiException(translateExceptionCode(oauthEx.getExceptionCode()), oauthEx.getMessage(), oauthEx);
        }
    }


    @Override
    public void saveAccessToken(String username, AccessToken accessToken) throws OAuthApiException {
        AccessTokenContext accessTokenContext = getInternalAccessTokenContext(accessToken);

        try {
            this.socialNetworkService.updateOAuthAccessToken(this.internalOAuthProvider, username, accessTokenContext);
        } catch (OAuthException oauthEx) {
            throw new OAuthApiException(translateExceptionCode(oauthEx.getExceptionCode()), oauthEx.getMessage(), oauthEx);
        }
    }

    @Override
    public void removeAccessToken(String username) throws OAuthApiException {
        try {
            this.socialNetworkService.removeOAuthAccessToken(this.internalOAuthProvider, username);
        } catch (OAuthException oauthEx) {
            throw new OAuthApiException(translateExceptionCode(oauthEx.getExceptionCode()), oauthEx.getMessage(), oauthEx);
        }
    }


    @Override
    public void startOAuthWorkflow(String neededCustomScope) throws OAuthApiException, IOException {
        throw new OAuthApiException(OAuthApiExceptionCode.OTHER_ERROR, "Not supported for this implementation");
    }


    @Override
    public AccessToken validateTokenAndUpdateScopes(AccessToken accessToken) throws OAuthApiException {
        AccessTokenContext accessTokenContext = getInternalAccessTokenContext(accessToken);

        try {
            accessTokenContext = this.internalOAuthProvider.getOauthProviderProcessor().validateTokenAndUpdateScopes(accessTokenContext);
        } catch (OAuthException oauthEx) {
            throw new OAuthApiException(translateExceptionCode(oauthEx.getExceptionCode()), oauthEx.getMessage(), oauthEx);
        }

        return new AccessTokenImpl(accessTokenContext);
    }


    @Override
    public void revokeToken(AccessToken accessToken) throws OAuthApiException {
        AccessTokenContext accessTokenContext = getInternalAccessTokenContext(accessToken);

        try {
            this.internalOAuthProvider.getOauthProviderProcessor().revokeToken(accessTokenContext);
        } catch (OAuthException oauthEx) {
            throw new OAuthApiException(translateExceptionCode(oauthEx.getExceptionCode()), oauthEx.getMessage(), oauthEx);
        }
    }

    @Override
    public <T> T getAuthorizedSocialApiObject(AccessToken accessToken, Class<T> socialApiObjectType) throws OAuthApiException {
        AccessTokenContext accessTokenContext = getInternalAccessTokenContext(accessToken);
        T socialApiObject = (T)this.internalOAuthProvider.getOauthProviderProcessor().getAuthorizedSocialApiObject(accessTokenContext, socialApiObjectType);

        if (socialApiObject == null) {
            throw new OAuthApiException(OAuthApiExceptionCode.SOCIAL_API_OBJECT_NOT_FOUND,
                    "Class '" + socialApiObjectType + "' not supported by processor " + this.internalOAuthProvider.getOauthProviderProcessor());
        }

        return socialApiObject;
    }

    protected OAuthProviderType getInternalOAuthProvider() {
        return internalOAuthProvider;
    }

    // protected method, so it could be overriden if needed
    protected AccessTokenContext getInternalAccessTokenContext(AccessToken accessToken) throws OAuthApiException {
        AccessTokenImpl accessTokenImpl = (AccessTokenImpl)accessToken;
        return accessTokenImpl.getAccessTokenContext();
    }


    // Translation between internal exception codes to API exception codes
    private OAuthApiExceptionCode translateExceptionCode(OAuthExceptionCode internalCode) {
        switch (internalCode) {
            case ACCESS_TOKEN_ERROR: return OAuthApiExceptionCode.ACCESS_TOKEN_ERROR;
            case IO_ERROR: return OAuthApiExceptionCode.IO_ERROR;
            case TOKEN_REVOCATION_FAILED: return OAuthApiExceptionCode.TOKEN_REVOCATION_FAILED;
            case PERSISTENCE_ERROR: return OAuthApiExceptionCode.PERSISTENCE_ERROR;
            case DUPLICATE_OAUTH_PROVIDER_USERNAME: return OAuthApiExceptionCode.DUPLICATE_OAUTH_PROVIDER_USERNAME;
            default: return OAuthApiExceptionCode.OTHER_ERROR;
        }
    }
}
