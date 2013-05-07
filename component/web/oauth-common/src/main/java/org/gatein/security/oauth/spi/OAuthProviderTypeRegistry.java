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

package org.gatein.security.oauth.spi;

import java.util.Collection;

/**
 * Registry of all registered instances of {@link OAuthProviderType}, which is used by portal to know about all registered
 * OAuth Providers (social networks)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface OAuthProviderTypeRegistry {

    /**
     * Obtain registered OAuth provider
     *
     * @param key of Oauth provider (for example 'FACEBOOK')
     * @param accessTokenContextClass just for adding more type safety, so caller knows the type of returned {@link OAuthProviderType}
     * @return oauth provider for given key
     */
    <T extends AccessTokenContext> OAuthProviderType<T> getOAuthProvider(String key, Class<T> accessTokenContextClass);

    /**
     * @return collection of all registered OAuth providers
     */
    Collection<OAuthProviderType> getEnabledOAuthProviders();

    /**
     * @return true if at least one OAuth provider is enabled
     */
    boolean isOAuthEnabled();
}
