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

/**
 * Component for accessing registered {@link OAuthProvider} instances
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface OAuthProviderAccessor {

    /**
     * Return {@link org.gatein.api.oauth.OAuthProvider} for given key. Key could be {@link OAuthProvider#FACEBOOK},
     * {@link OAuthProvider#GOOGLE}, {@link OAuthProvider#TWITTER} or other OAuth provider registered in Portal via OAuth SPI
     *
     * @param oauthProviderKey Key of OAuth provider
     * @return OAuth provider or null if OAuth provider with given key was not found
     */
    OAuthProvider getOAuthProvider(String oauthProviderKey);

}
