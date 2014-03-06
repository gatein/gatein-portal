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

import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.utils.OAuthUtils;

/**
 * Encapsulate data about single OAuth provider (social network), which are needed by portal (not data, which are needed to perform specific OAuth
 * operations like consumerKey or consumerSecret as these are provided by concrete {@link org.gatein.security.oauth.spi.OAuthProviderProcessor})
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthProviderType<T extends AccessTokenContext> {

    // Key of particular provider (for example 'FACEBOOK')
    private final String key;

    // Whether this OAuth provider should be enabled or not
    private final boolean enabled;

    // Name of UserProfile attribute, which will be used to save OAuth userName (userName of user in given social network)
    private final String userNameAttrName;

    // Reference to OAuthProviderProcessor for this OAuth Provider. Processor can be used to call OAuth operations on given OAuth provider
    private final OAuthProviderProcessor<T> oauthProviderProcessor;

    private final OAuthPrincipalProcessor oauthPrincipalProcessor;

    // URL suffix used to start OAuth authentication workflow with given OAuth provider
    private final String initOAuthURL;

    // Friendly name of given OAuth provider (For example 'Facebook')
    private final String friendlyName;

    public OAuthProviderType(String key, boolean enabled, String userNameAttrName, OAuthProviderProcessor<T> oauthProviderProcessor,
                             OAuthPrincipalProcessor principalProcessor, String initOAuthURL, String friendlyName) {
        this.key = key;
        this.enabled = enabled;
        this.userNameAttrName = userNameAttrName;
        this.oauthProviderProcessor = oauthProviderProcessor;
        this.oauthPrincipalProcessor = principalProcessor;
        this.initOAuthURL = initOAuthURL;
        this.friendlyName = friendlyName;
    }

    public String getKey() {
        return key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUserNameAttrName() {
        return userNameAttrName;
    }

    public OAuthProviderProcessor<T> getOauthProviderProcessor() {
        return oauthProviderProcessor;
    }

    public OAuthPrincipalProcessor getOauthPrincipalProcessor() {
        return this.oauthPrincipalProcessor;
    }

    public String getInitOAuthURL(String contextPath, String requestURI) {
        requestURI = OAuthUtils.encodeParam(requestURI);

        return contextPath + initOAuthURL
                + "?" + OAuthConstants.PARAM_OAUTH_INTERACTION + "=" + OAuthConstants.PARAM_OAUTH_INTERACTION_VALUE_START
                + "&" + OAuthConstants.PARAM_INITIAL_URI + "=" + requestURI;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String toString() {
        return new StringBuilder("OAuthProviderType [ ")
                .append("key=" + key)
                .append(", enabled=" + enabled)
                .append(", userNameAttrName=" + userNameAttrName)
                .append(", oauthProviderProcessor=" + oauthProviderProcessor)
                .append(", oauthPrincipalProcessor=" + oauthPrincipalProcessor)
                .append(", initOAuthURL=" + initOAuthURL)
                .append(", friendlyName=" + friendlyName)
                .append(" ]").toString();
    }
}
