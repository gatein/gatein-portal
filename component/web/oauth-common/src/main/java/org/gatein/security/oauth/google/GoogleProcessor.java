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

package org.gatein.security.oauth.google;

import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.services.plus.Plus;
import org.gatein.security.oauth.spi.OAuthProviderProcessor;

/**
 * OAuth processor for calling Google+ operations
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface GoogleProcessor extends OAuthProviderProcessor<GoogleAccessTokenContext> {

    /**
     * Obtain informations about user from Google+ .
     *
     * @param accessTokenContext google access token
     * @return userinfo object with filled info about this user
     */
    Userinfo obtainUserInfo(GoogleAccessTokenContext accessTokenContext);

    /**
     * Obtain instance of Google {@link Oauth2} object, which can be used to call various operations in Google API (obtain user informations,
     * obtain informations about your access token etc)
     *
     * @param accessTokenContext
     * @return oauth2 object
     */
    Oauth2 getOAuth2Instance(GoogleAccessTokenContext accessTokenContext);

    /**
     * Obtain instance of Google (@link Plus} object, which can be used to call various operations in Google+ API (Obtain list of your friends,
     * obtain your statuses, comments, activities etc...)
     *
     * @param accessTokenContext
     * @return plus object
     */
    Plus getPlusService(GoogleAccessTokenContext accessTokenContext);

    /**
     * Refresh Google+ token. Note that argument needs to have "refreshToken" available. The "accessToken" will be refreshed and updated
     * directly on this instance of accessTokenContext
     *
     * @param accessTokenContext
     */
    void refreshToken(GoogleAccessTokenContext accessTokenContext);

}
