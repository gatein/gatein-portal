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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FacebookAccessTokenContext implements Serializable {

    private final String accessToken;
    private final Set<String> scopes = new HashSet<String>();

    public FacebookAccessTokenContext(String accessToken, String... scopes) {
        this.accessToken = accessToken;
        for (String scope : scopes) {
            this.scopes.add(scope);
        }
    }

    public FacebookAccessTokenContext(String accessToken, String scopesAsString) {
        this.accessToken = accessToken;
        String[] scopes = scopesAsString.split(",");
        for (String scope : scopes) {
            this.scopes.add(scope);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isScopeAvailable(String scope) {
        return scopes.contains(scope);
    }

    public String getScopesAsString() {
        Iterator<String> iterator = scopes.iterator();
        StringBuilder result;

        if (iterator.hasNext()) {
            result = new StringBuilder(iterator.next());
        } else {
            return "";
        }

        while (iterator.hasNext()) {
            result.append("," + iterator.next());
        }
        return result.toString();
    }

    public boolean addScope(String scope) {
        return scopes.add(scope);
    }

    @Override
    public String toString() {
        return new StringBuilder("FacebookAccessTokenContext [")
                .append("accessToken=" + accessToken)
                .append(", scope=" + getScopesAsString())
                .append("]").toString();
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }

        if (!(that instanceof FacebookAccessTokenContext)) {
            return false;
        }

        FacebookAccessTokenContext thatt = (FacebookAccessTokenContext)that;
        return this.accessToken.equals(thatt.getAccessToken()) && this.scopes.equals(thatt.scopes);
    }

    @Override
    public int hashCode() {
        return accessToken.hashCode() * 13 + scopes.hashCode();
    }
}
