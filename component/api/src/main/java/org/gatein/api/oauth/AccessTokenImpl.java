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

package org.gatein.api.oauth;

import org.exoplatform.commons.utils.Safe;
import org.gatein.api.internal.Parameters;
import org.gatein.security.oauth.spi.AccessTokenContext;

/**
 * Implementation of {@link AccessToken}. It's simply wrapper around {@link AccessTokenContext} object
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AccessTokenImpl implements AccessToken {

    private static final long serialVersionUID = -7034897192745766943L;

    private final AccessTokenContext internalAccessTokenContext;

    public AccessTokenImpl(AccessTokenContext internalAccessTokenContext) {
        Parameters.requireNonNull(internalAccessTokenContext, "internalAccessTokenContext");
        this.internalAccessTokenContext = internalAccessTokenContext;
    }

    public AccessTokenContext getAccessTokenContext() {
        return internalAccessTokenContext;
    }

    @Override
    public String getAvailableScopes() {
        return internalAccessTokenContext.getScopesAsString();
    }

    @Override
    public boolean isScopeAvailable(String scope) {
        return internalAccessTokenContext.isScopeAvailable(scope);
    }

    @Override
    public String getAccessToken() {
        return internalAccessTokenContext.getAccessToken();
    }

    @Override
    public int hashCode() {
        return internalAccessTokenContext.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(obj.getClass().equals(AccessTokenImpl.class))) {
            return false;
        }

        AccessTokenImpl that = (AccessTokenImpl)obj;
        return Safe.equals(this.internalAccessTokenContext, that.getAccessTokenContext());
    }

    @Override
    public String toString() {
        return new StringBuilder("AccessTokenImpl [ internalAccessTokenContext=")
                .append(internalAccessTokenContext)
                .append(" ]").toString();
    }
}
