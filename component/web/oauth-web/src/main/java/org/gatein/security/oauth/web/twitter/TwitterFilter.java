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

package org.gatein.security.oauth.web.twitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gatein.security.oauth.spi.InteractionState;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.spi.OAuthPrincipal;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.twitter.TwitterAccessTokenContext;
import org.gatein.security.oauth.twitter.TwitterProcessor;
import org.gatein.security.oauth.utils.OAuthUtils;
import org.gatein.security.oauth.web.OAuthProviderFilter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Filter for  integration with authentication handhsake via Twitter with usage of OAuth1
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TwitterFilter extends OAuthProviderFilter<TwitterAccessTokenContext> {

    @Override
    protected OAuthProviderType<TwitterAccessTokenContext> getOAuthProvider() {
        return getOauthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_TWITTER, TwitterAccessTokenContext.class);
    }

    @Override
    protected void initInteraction(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN);
    }

    @Override
    protected OAuthPrincipal<TwitterAccessTokenContext> getOAuthPrincipal(HttpServletRequest request, HttpServletResponse response,
                                                                          InteractionState<TwitterAccessTokenContext> interactionState) {
        TwitterAccessTokenContext accessTokenContext = interactionState.getAccessTokenContext();
        Twitter twitter = ((TwitterProcessor)getOauthProviderProcessor()).getAuthorizedTwitterInstance(accessTokenContext);

        User twitterUser;
        try {
            twitterUser = twitter.verifyCredentials();
        } catch (TwitterException te) {
            throw new OAuthException(OAuthExceptionCode.TWITTER_ERROR, "Error when obtaining user", te);
        }

        OAuthPrincipal<TwitterAccessTokenContext> oauthPrincipal = OAuthUtils.convertTwitterUserToOAuthPrincipal(twitterUser,
                accessTokenContext, getOAuthProvider());
        return oauthPrincipal;
    }
}
