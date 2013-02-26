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

package org.gatein.common.exception;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GateInExceptionConstants {

    // Exception codes

    /**
     * Unspecified GateIn+OAuth error
     */
    public static final int EXCEPTION_CODE_UNSPECIFIED = 0;

    /**
     * This error could happen during saving of user into GateIn identity database.
     * It happens when there is an attempt to save user with facebookUsername (or googleUsername), but there is already an existing
     * user with same facebookUsername.
     *
     * For example: We want to save user 'john' with facebookUsername 'john.doyle' but we already have user 'johny2' with same facebookUsername 'john.doyle'
     */
    public static final int EXCEPTION_CODE_DUPLICATE_OAUTH_PROVIDER_USERNAME = 10;


    // Key of exception attributes

    /**
     * Name of attribute with OAuth provider username
     */
    public static final String EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME = "OAuthProviderUsernameAttributeName";

    /**
     * OAuth provider username
     */
    public static final String EXCEPTION_OAUTH_PROVIDER_USERNAME = "OAuthProviderUsername";

}
