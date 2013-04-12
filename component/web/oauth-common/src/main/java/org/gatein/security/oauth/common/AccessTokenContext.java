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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.exoplatform.commons.utils.Safe;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AccessTokenContext implements Serializable {

    private static final long serialVersionUID = -7034897192745766989L;
    public static final String DELIMITER = " ";

    private final Set<String> scopes = new HashSet<String>();

    public AccessTokenContext(String... scopes) {
        if (scopes != null) {
            for (String scope : scopes) {
                this.scopes.add(scope);
            }
        }
    }

    public AccessTokenContext(String scopesAsString) {
        String[] scopes = scopesAsString.split(DELIMITER);
        for (String scope : scopes) {
            this.scopes.add(scope);
        }
    }

    public AccessTokenContext(Collection<String> scopes) {
        if (scopes != null) {
            for (String scope : scopes) {
                this.scopes.add(scope);
            }
        }
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
            result.append(DELIMITER + iterator.next());
        }
        return result.toString();
    }

    public boolean addScope(String scope) {
        return scopes.add(scope);
    }

    @Override
    public String toString() {
        return new StringBuilder("scope=" + getScopesAsString()).append("]").toString();
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (that == null) {
            return false;
        }

        if (!(that.getClass().equals(this.getClass()))) {
            return false;
        }

        AccessTokenContext thatt = (AccessTokenContext)that;
        return Safe.equals(this.scopes, thatt.scopes);
    }

    @Override
    public int hashCode() {
        return scopes.hashCode();
    }
}
