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

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthProviderType<T extends AccessTokenContext> {

    private final String key;
    private final boolean enabled;
    private final String userNameAttrName;
    private final OAuthProviderProcessor<T> oauthProviderProcessor;
    private final String initOAuthURL;
    private final String friendlyName;

    public OAuthProviderType(String key, boolean enabled, String userNameAttrName, OAuthProviderProcessor<T> oauthProviderProcessor, String initOAuthURL, String friendlyName) {
        this.key = key;
        this.enabled = enabled;
        this.userNameAttrName = userNameAttrName;
        this.oauthProviderProcessor = oauthProviderProcessor;
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

    public String getInitOAuthURL(String contextPath) {
        return contextPath + initOAuthURL + "?" + OAuthConstants.PARAM_OAUTH_INTERACTION + "=" + OAuthConstants.PARAM_OAUTH_INTERACTION_VALUE_START;
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
                .append(", initOAuthURL=" + initOAuthURL)
                .append(", friendlyName=" + friendlyName)
                .append(" ]").toString();
    }
}
