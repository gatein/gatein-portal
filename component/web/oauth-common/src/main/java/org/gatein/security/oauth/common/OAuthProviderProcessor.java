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

package org.gatein.security.oauth.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.services.organization.UserProfile;
import org.gatein.security.oauth.exception.OAuthException;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface OAuthProviderProcessor<T extends AccessTokenContext> {

    void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, OAuthCodec codec, T accessToken);

    /**
     * @param userProfile
     * @param codec
     * @return null if accessToken is not found in persistent storage
     */
    T getAccessTokenFromUserProfile(UserProfile userProfile, OAuthCodec codec);

    void removeAccessTokenFromUserProfile(UserProfile userProfile);



    InteractionState<T> processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws
            IOException, OAuthException;


    /**
     * Possibility to create new OAuth interaction with custom scope (not just the scope which is provided in configuration)
     *
     * @param httpRequest
     * @param httpResponse
     * @param scope custom scope
     * @return
     * @throws IOException
     */
    InteractionState<T> processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String scope) throws
            IOException, OAuthException;



    void revokeToken(T accessToken) throws OAuthException;

    /**
     * Send request to OAuth Provider to validate if given access token is valid and ask for scopes, which are available for given accessToken.
     *
     * @param accessToken accessToken which will be used to ask OAuthProvider about validation and for available scopes
     * @return accessTokenContext, which will be quite same as the one from accessToken parameter. It will have some info updated (like scopes)
     */
    T validateTokenAndUpdateScopes(T accessToken) throws OAuthException;
}
