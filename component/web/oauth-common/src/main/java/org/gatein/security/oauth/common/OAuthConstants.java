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
 * Various constants related to OAuth
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthConstants {

    // OAuth parameters used in http requests

    public static final String CODE_PARAMETER = "code";

    public static final String CLIENT_ID_PARAMETER = "client_id";

    public static final String CLIENT_SECRET_PARAMETER = "client_secret";

    public static final String SCOPE_PARAMETER = "scope";

    public static final String REDIRECT_URI_PARAMETER = "redirect_uri";

    public static final String ACCESS_TOKEN_PARAMETER = "access_token";

    public static final String ERROR_PARAMETER = "error";

    // Properties from configuration.properties

    public static final String PROPERTY_FACEBOOK_ENABLED = "gatein.oauth.facebook.enabled";

    public static final String PROPERTY_FACEBOOK_APPID = "gatein.oauth.facebook.appid";

    public static final String PROPERTY_FACEBOOK_APPSECRET = "gatein.oauth.facebook.appsecret";

    public static final String PROPERTY_FACEBOOK_REDIRECT_URL = "gatein.oauth.facebook.redirecturl";

    public static final String PROPERTY_FACEBOOK_SCOPE = "gatein.oauth.facebook.scope";

    public static final String PROPERTY_FACEBOOK_DISPLAY = "gatein.oauth.facebook.display";

    public static final String PROPERTY_GOOGLE_ENABLED = "gatein.oauth.google.enabled";

    public static final String PROPERTY_GOOGLE_CLIENTID = "gatein.oauth.google.clientid";

    public static final String PROPERTY_GOOGLE_CLIENTSECRET = "gatein.oauth.google.clientsecret";


    // User profile attributes

    public static final String PROFILE_FACEBOOK_USERNAME = "user.social-info.facebook.userName";

    public static final String PROFILE_GOOGLE_USERNAME = "user.social-info.google.userName";

    public static final String PROFILE_FACEBOOK_ACCESS_TOKEN = "user.social-info.facebook.accessToken";

    public static final String PROFILE_GOOGLE_ACCESS_TOKEN = "user.social-info.google.accessToken";


    // Session (or AuthenticationRegistry) attributes

    public static final String ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL = "_authenticatedOAuthPrincipal";

    public static final String ATTRIBUTE_AUTHENTICATED_PORTAL_USER = "_authenticatedPortalUser";

    public static final String ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT = "_urlToRedirectAfterLinkSocialAccount";

    public static final String ATTRIBUTE_SOCIAL_NETWORK_PROVIDER_TO_UNLINK = "_socialNetworkProviderToUnlink";

    public static final String ATTRIBUTE_LINKED_OAUTH_PROVIDER_USERNAME_ATTR_NAME = "_linkedOAuthProviderUsernameAttrName";

    public static final String ATTRIBUTE_EXCEPTION_AFTER_FAILED_LINK = "_oauthExceptionAfterFailedLink";

    // URL

    public static final String FACEBOOK_AUTHENTICATION_URL_PATH = "/facebookAuth";

    public static final String GOOGLE_AUTHENTICATION_URL_PATH = "/googleAuth";


    // Request parameters

    public static final String PARAM_OAUTH_INTERACTION = "_oauthInteraction";

    public static final String PARAM_OAUTH_INTERACTION_VALUE_START = "start";

}
