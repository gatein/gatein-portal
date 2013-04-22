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

package org.gatein.security.oauth.spi;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.services.organization.UserProfile;
import org.gatein.security.oauth.exception.OAuthException;

/**
 * Processor to call operations on given OAuth provider (Social network)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface OAuthProviderProcessor<T extends AccessTokenContext> {


    /**
     * Process OAuth workflow for this OAuth provider (social network). Workflow is finished if returned {@link org.gatein.security.oauth.spi.InteractionState}
     * is in state {@link org.gatein.security.oauth.spi.InteractionState.State#FINISH} and in this case, InteractionState should also have accessToken filled.
     *
     * If {@link org.gatein.security.oauth.spi.InteractionState} is in state {@link org.gatein.security.oauth.spi.InteractionState.State#AUTH}, then more redirections are needed. In this case,
     * given {@link HttpServletResponse} should be already committed and prepared for redirection.
     *
     * @param httpRequest
     * @param httpResponse
     * @return InteractionState with state of OAuth interaction
     * @throws IOException if IO error occured (for example if httpResponse.sendRedirect failed)
     * @throws OAuthException in case of some other error, which may be specific for this OAuth processor (Details are available in error code)
     * Caller should be able to handle at least {@link org.gatein.security.oauth.exception.OAuthExceptionCode#USER_DENIED_SCOPE}
     * which happens when user denied scope (authorization screen in web of given social network)
     */
    InteractionState<T> processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws
            IOException, OAuthException;


    /**
     * Possibility to create new OAuth interaction with custom scope (not just the scope which is provided in configuration of this OAuth processor)
     * @see {@link #processOAuthInteraction(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     *
     * @param httpRequest
     * @param httpResponse
     * @param scope custom scope, which contains all scopes in single String divided by {@link AccessTokenContext#DELIMITER}
     * @return
     * @throws IOException
     * @throws OAuthException
     */
    InteractionState<T> processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String scope) throws
            IOException, OAuthException;


    /**
     * Revoke given access token on OAuth provider side, so application is removed from list of supported applications for given user
     *
     * @param accessToken access token to revoke
     * @throws OAuthException with code {@link org.gatein.security.oauth.exception.OAuthExceptionCode#TOKEN_REVOCATION_FAILED}
     * if remote revocation of access token failed for some reason
     */
    void revokeToken(T accessToken) throws OAuthException;


    /**
     * Send request to OAuth Provider to validate if given access token is valid and ask for scopes, which are available for given accessToken.
     * Returned access token should be always valid and prepared for invoke other operations
     *
     * @param accessToken accessToken which will be used to ask OAuthProvider about validation and for available scopes
     * @return accessTokenContext, which will be quite same as the one from accessToken parameter. It could have some info updated (like scopes)
     * @throws OAuthException usually with codes:
     *  - {@link org.gatein.security.oauth.exception.OAuthExceptionCode#ACCESS_TOKEN_ERROR} if invalid access is used as argument
     *  - {@link org.gatein.security.oauth.exception.OAuthExceptionCode#IO_ERROR} if IO error occurs
     */
    T validateTokenAndUpdateScopes(T accessToken) throws OAuthException;

    /**
     * Return object, which can be used to call some operations on this Social network. For example "Plus" object for Google+ network
     *
     * @param socialApiObjectType Type of object, which we wanted to return. Method will return null if this type is not supported by this processor
     * @param accessToken access token used to initialize object
     * @return initialized object of required type or null if type wasn't found (supported) by this processor
     */
    <C> C getAuthorizedSocialApiObject(T accessToken, Class<C> socialApiObjectType);

    // OPERATIONS FOR ACCESS TOKEN PERSISTENCE


    /**
     * Save accessToken data to given userProfile. Note that we are not calling any DB save operations, just filling data into
     * given userProfile
     *
     * @param userProfile where data about access token will be filled
     * @param codec to encode some attributes (sensitive data) before save them to user profile
     * @param accessToken specific access token for this OAuth processor
     */
    void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, OAuthCodec codec, T accessToken);


    /**
     * Obtain needed data from given userProfile and create accessToken from them
     *
     * @param userProfile where data from access token will be obtained
     * @param codec to decode data from userProfile
     * @return accesstoken or null if accessToken is not found in persistent storage
     */
    T getAccessTokenFromUserProfile(UserProfile userProfile, OAuthCodec codec);


    /**
     * Remove data about access token from this user profile
     * @param userProfile from which data will be removed
     */
    void removeAccessTokenFromUserProfile(UserProfile userProfile);
}
