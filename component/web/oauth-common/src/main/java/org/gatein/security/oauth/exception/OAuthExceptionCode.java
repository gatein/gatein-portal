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

package org.gatein.security.oauth.exception;

/**
 * Enum with various exception codes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum OAuthExceptionCode {

    /**
     * Unspecified GateIn+OAuth error
     */
    UNKNOWN_ERROR,

    /**
     * This error could happen during saving of user into GateIn identity database.
     * It happens when there is an attempt to save user with facebookUsername (or googleUsername), but there is already an existing
     * user with same facebookUsername.
     *
     * For example: We want to save user 'john' with facebookUsername 'john.doyle' but we already have user 'johny2' with same facebookUsername 'john.doyle'
     */
    DUPLICATE_OAUTH_PROVIDER_USERNAME,

    /**
     * Some error during Twitter processing
     */
    TWITTER_ERROR,


    /**
     * Some error during Facebook processing
     */
    FACEBOOK_ERROR,

    /**
     * Some error during Google processing
     */
    GOOGLE_ERROR,

    /**
     * Some error during LinkedIn processing
     */
    LINKEDIN_ERROR,

    /**
     * Error when we have invalid or revoked access token
     */
    ACCESS_TOKEN_ERROR,

    /**
     * Generic IO error (for example network error)
     */
    IO_ERROR,

    /**
     * Error when state parameter from request parameter, which is sent from OAuth provider, is not equals to previously sent state
     */
    INVALID_STATE,

    /**
     * Error when revoking of accessToken of any provider failed
     */
    TOKEN_REVOCATION_FAILED,

    /**
     * Error when OAuth2 flow failed because user denied to permit privileges (scope) for OAuth provider
     */
    USER_DENIED_SCOPE,

    /**
     * Error during DB operation (For example get/set/remove access token from DB)
     */
    PERSISTENCE_ERROR


}
