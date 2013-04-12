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
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum OAuthExceptionCode {

    /**
     * Unspecified GateIn+OAuth error
     */
    EXCEPTION_UNSPECIFIED,

    /**
     * This error could happen during saving of user into GateIn identity database.
     * It happens when there is an attempt to save user with facebookUsername (or googleUsername), but there is already an existing
     * user with same facebookUsername.
     *
     * For example: We want to save user 'john' with facebookUsername 'john.doyle' but we already have user 'johny2' with same facebookUsername 'john.doyle'
     */
    EXCEPTION_CODE_DUPLICATE_OAUTH_PROVIDER_USERNAME,

    /**
     * Some error during Twitter processing
     */
    EXCEPTION_CODE_TWITTER_ERROR,


    /**
     * Some error during Facebook processing
     */
    EXCEPTION_CODE_FACEBOOK_ERROR,

    /**
     * Some error during Google processing
     */
    EXCEPTION_CODE_GOOGLE_ERROR,

    /**
     * Error when we have invalid or revoked access token
     */
    EXCEPTION_CODE_ACCESS_TOKEN_ERROR,

    /**
     * Generic IO error (for example network error)
     */
    EXCEPTION_CODE_UNSPECIFIED_IO_ERROR,

    /**
     * Error when state parameter from request parameter, which is sent from OAuth provider, is not equals to previously sent state
     */
    EXCEPTION_CODE_INVALID_STATE,

    /**
     * Error when revoking of accessToken of any provider failed
     */
    EXCEPTION_CODE_TOKEN_REVOKE_FAILED,

    /**
     * Error when OAuth2 flow failed because user denied to permit privileges (scope) for OAuth provider
     */
    EXCEPTION_CODE_USER_DENIED_SCOPE;


}
