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

package org.gatein.security.oauth.facebook;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.gatein.security.oauth.common.AccessTokenContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FacebookAccessTokenContext extends AccessTokenContext implements Serializable {

    private static final long serialVersionUID = -7264897192745766989L;

    private final String accessToken;

    public FacebookAccessTokenContext(String accessToken, String... scopes) {
        super(scopes);
        this.accessToken = accessToken;
    }

    public FacebookAccessTokenContext(String accessToken, String scopesAsString) {
        super(scopesAsString);
        this.accessToken = accessToken;
    }

    public FacebookAccessTokenContext(String accessToken, Collection<String> scopes) {
        super(scopes);
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return new StringBuilder("FacebookAccessTokenContext [")
                .append("accessToken=" + accessToken)
                .append(super.toString())
                .toString();
    }

    @Override
    public boolean equals(Object that) {
        if (!super.equals(that)) {
            return false;
        }
        FacebookAccessTokenContext thatt = (FacebookAccessTokenContext)that;
        return this.accessToken.equals(thatt.getAccessToken());
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 13 + accessToken.hashCode();
    }
}
