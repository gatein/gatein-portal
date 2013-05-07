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


import org.gatein.security.oauth.spi.OAuthProviderProcessor;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.social.FacebookPrincipal;

/**
 * OAuth processor for calling Facebook operations
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface GateInFacebookProcessor extends OAuthProviderProcessor<FacebookAccessTokenContext> {

    /**
     * Obtain informations about user from Facebook and wrap them into FacebookPrincipal object
     *
     * @param accessTokenContext Facebook access token
     * @return FacebookPrincipal
     */
    FacebookPrincipal getPrincipal(FacebookAccessTokenContext accessTokenContext) throws OAuthException;

}
