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

import java.security.Principal;


/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthPrincipal<T extends AccessTokenContext> implements Principal {

    private final String userName;
    private final String firstName;
    private final String lastName;
    private final String displayName;
    private final String email;
    private final T accessToken;
    private final OAuthProviderType<T> oauthProviderType;


    public OAuthPrincipal(String userName, String firstName, String lastName, String displayName, String email, T accessToken, OAuthProviderType<T> oauthProviderType) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.email = email;
        this.oauthProviderType = oauthProviderType;
        this.accessToken = accessToken;
    }

    @Override
    public String getName() {
        // Using userName as name of OAuthPrincipal
        return userName;
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public T getAccessToken() {
        return accessToken;
    }

    public OAuthProviderType getOauthProviderType() {
        return oauthProviderType;
    }
}
