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
public enum OAuthProviderType {

    FACEBOOK(OAuthConstants.PROPERTY_FACEBOOK_ENABLED,
            OAuthConstants.PROFILE_FACEBOOK_USERNAME,
            OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN,
            OAuthConstants.FACEBOOK_AUTHENTICATION_URL_PATH + "?" + OAuthConstants.PARAM_OAUTH_INTERACTION + "=" + OAuthConstants.PARAM_OAUTH_INTERACTION_VALUE_START,
            "Facebook"),
    GOOGLE(OAuthConstants.PROPERTY_GOOGLE_ENABLED,
            OAuthConstants.PROFILE_GOOGLE_USERNAME,
            OAuthConstants.PROFILE_GOOGLE_ACCESS_TOKEN,
            OAuthConstants.GOOGLE_AUTHENTICATION_URL_PATH + "?" + OAuthConstants.PARAM_OAUTH_INTERACTION + "=" + OAuthConstants.PARAM_OAUTH_INTERACTION_VALUE_START,
            "Google+");

    private final boolean enabled;
    private final String userNameAttrName;
    private final String accessTokenAttrName;
    private final String initOAuthURL;
    private final String friendlyName;

    OAuthProviderType(String enabledPropertyName, String userNameAttrName, String accessTokenAttrName, String initOAuthURL, String friendlyName) {
        this.enabled = Boolean.getBoolean(enabledPropertyName);
        this.userNameAttrName = userNameAttrName;
        this.accessTokenAttrName = accessTokenAttrName;
        this.initOAuthURL = initOAuthURL;
        this.friendlyName = friendlyName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUserNameAttrName() {
        return userNameAttrName;
    }

    public String getAccessTokenAttrName() {
        return accessTokenAttrName;
    }

    public String getInitOAuthURL(String contextPath) {
        return contextPath + initOAuthURL;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @return true if at least one OAuth provider is enabled
     */
    public static boolean isOAuthEnabled() {
        OAuthProviderType[] allProviders = OAuthProviderType.values();
        for (OAuthProviderType current : allProviders) {
            if (current.isEnabled())
                return true;
        }

        return false;
    }
}
