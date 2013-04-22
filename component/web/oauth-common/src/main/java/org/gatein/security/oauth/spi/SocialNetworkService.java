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

import org.exoplatform.services.organization.User;

/**
 * Service for handling persistence of OAuth data (usernames, access tokens)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface SocialNetworkService {

    /**
     * Find user from Identity DB by oauth provider username
     *
     * @param oauthProviderType
     * @param oauthProviderUsername
     * @return portal user
     */
    User findUserByOAuthProviderUsername(OAuthProviderType oauthProviderType, String oauthProviderUsername);


    /**
     * Save access token of given user into DB
     */
    <T extends AccessTokenContext> void updateOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username, T accessToken);

    /**
     * Obtain access token of given user from DB
     */
    <T extends AccessTokenContext> T getOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username);

    /**
     * Save OAuth informations (both username and access token) into DB
     */
    <T extends AccessTokenContext> void updateOAuthInfo(OAuthProviderType<T> oauthProviderType, String username, String oauthUsername, T accessToken);

    /**
     * Remove access token of given user from DB
     */
    <T extends AccessTokenContext> void removeOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username);

}
