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

import java.io.Serializable;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.gatein.security.oauth.common.AccessTokenContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GoogleAccessTokenContext extends AccessTokenContext implements Serializable {

    private static final long serialVersionUID = -7038197192745766989L;

    private final GoogleTokenResponse tokenData;

    public GoogleAccessTokenContext(GoogleTokenResponse tokenData, String... scopes) {
        // Obtain scopes from token response
        super(scopes);
        this.tokenData = tokenData;
    }

    public GoogleTokenResponse getTokenData() {
        return tokenData;
    }

    @Override
    public String toString() {
        return new StringBuilder("GoogleAccessTokenContext [")
                .append("accessToken=" + tokenData)
                .append(super.toString())
                .toString();
    }

    @Override
    public boolean equals(Object that) {
        if (!super.equals(that)) {
            return false;
        }
        GoogleAccessTokenContext thatt = (GoogleAccessTokenContext)that;
        return this.tokenData.equals(thatt.getTokenData());
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 13 + tokenData.hashCode();
    }
}
