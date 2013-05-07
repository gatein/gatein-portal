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

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.spi.AccessTokenContext;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.SocialNetworkService;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;

/**
 * Basic implementation of OAuth provider accessor
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class BasicOAuthProviderAccessorImpl implements OAuthProviderAccessor {

    private final OAuthProviderTypeRegistry oauthProviderTypeRegistry;
    private final SocialNetworkService socialNetworkService;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public BasicOAuthProviderAccessorImpl(OAuthProviderTypeRegistry oauthProviderTypeRegistry, SocialNetworkService socialNetworkService) {
        this.oauthProviderTypeRegistry = oauthProviderTypeRegistry;
        this.socialNetworkService = socialNetworkService;
    }

    @Override
    public OAuthProvider getOAuthProvider(String oauthProviderKey) {
        OAuthProviderType<?> internalOAuthProvider = getInternalOAuthProvider(oauthProviderKey);
        if (internalOAuthProvider == null) {
            return null;
        } else {
            return new BasicOAuthProviderImpl(internalOAuthProvider, socialNetworkService);
        }
    }

    protected OAuthProviderType<?> getInternalOAuthProvider(String oauthProviderKey) {
        OAuthProviderType<?> oauthProviderType = oauthProviderTypeRegistry.getOAuthProvider(oauthProviderKey, AccessTokenContext.class);
        if (oauthProviderType == null && log.isTraceEnabled()) {
            log.trace("OAuthProvider '" + oauthProviderKey + "' not found");
        }
        return oauthProviderType;
    }

    protected SocialNetworkService getSocialNetworkService() {
        return socialNetworkService;
    }
}
