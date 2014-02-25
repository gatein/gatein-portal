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

    // OAuth parameters used in http requests (most of these parameters are mentioned in OAuth2 specification)

    public static final String CODE_PARAMETER = "code";

    public static final String CLIENT_ID_PARAMETER = "client_id";

    public static final String CLIENT_SECRET_PARAMETER = "client_secret";

    public static final String SCOPE_PARAMETER = "scope";

    public static final String STATE_PARAMETER = "state";

    public static final String REDIRECT_URI_PARAMETER = "redirect_uri";

    public static final String ACCESS_TOKEN_PARAMETER = "access_token";

    public static final String ERROR_PARAMETER = "error";

    public static final String ERROR_ACCESS_DENIED = "access_denied";

    // Used in twitter
    public static final String OAUTH_VERIFIER = "oauth_verifier";

    public static final String OAUTH_DENIED = "denied";

    // Properties from configuration.properties

    public static final String PROPERTY_FACEBOOK_ENABLED = "gatein.oauth.facebook.enabled";

    public static final String PROPERTY_FACEBOOK_CLIENT_ID = "gatein.oauth.facebook.clientId";

    public static final String PROPERTY_FACEBOOK_CLIENT_SECRET = "gatein.oauth.facebook.clientSecret";

    public static final String PROPERTY_FACEBOOK_REDIRECT_URL = "gatein.oauth.facebook.redirectURL";

    public static final String PROPERTY_FACEBOOK_SCOPE = "gatein.oauth.facebook.scope";

    public static final String PROPERTY_GOOGLE_ENABLED = "gatein.oauth.google.enabled";

    public static final String PROPERTY_GOOGLE_CLIENT_ID = "gatein.oauth.google.clientId";

    public static final String PROPERTY_GOOGLE_CLIENT_SECRET = "gatein.oauth.google.clientSecret";

    public static final String PROPERTY_TWITTER_ENABLED = "gatein.oauth.twitter.enabled";

    public static final String PROPERTY_TWITTER_CLIENT_ID = "gatein.oauth.twitter.clientId";

    public static final String PROPERTY_TWITTER_CLIENTSECRET = "gatein.oauth.twitter.clientSecret";

    public static final String PROPERTY_TWITTER_REDIRECT_URL = "gatein.oauth.twitter.redirectURL";

    // Key of OAuthProviders

    public static final String OAUTH_PROVIDER_KEY_FACEBOOK = "FACEBOOK";

    public static final String OAUTH_PROVIDER_KEY_TWITTER = "TWITTER";

    public static final String OAUTH_PROVIDER_KEY_GOOGLE = "GOOGLE";

    public static final String OAUTH_PROVIDER_KEY_LINKEDIN = "LINKEDIN";

    // User profile attributes

    public static final String PROFILE_FACEBOOK_USERNAME = "user.social-info.facebook.userName";

    public static final String PROFILE_GOOGLE_USERNAME = "user.social-info.google.userName";

    public static final String PROFILE_TWITTER_USERNAME = "user.social-info.twitter.userName";

    // Facebook accessToken could be very long, so we need to split to two attributes
    public static final String PROFILE_FACEBOOK_ACCESS_TOKEN = "user.social-info.facebook.accessToken";

    public static final String PROFILE_FACEBOOK_SCOPE = "user.social-info.facebook.scope";

    public static final String PROFILE_GOOGLE_ACCESS_TOKEN = "user.social-info.google.accessToken";

    public static final String PROFILE_GOOGLE_REFRESH_TOKEN = "user.social-info.google.refreshToken";

    public static final String PROFILE_GOOGLE_SCOPE = "user.social-info.google.scope";

    public static final String PROFILE_TWITTER_ACCESS_TOKEN = "user.social-info.twitter.accessToken";

    public static final String PROFILE_TWITTER_ACCESS_TOKEN_SECRET = "user.social-info.twitter.accessTokenSecret";

    public static final String PROFILE_LINKEDIN_ACCESS_TOKEN = "user.social-info.linkedin.accessToken";

    public static final String PROFILE_LINKEDIN_ACCESS_TOKEN_SECRET = "user.social-info.linkedin.accessTokenSecret";

    // Session (or AuthenticationRegistry) attributes

    public static final String ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL = "_authenticatedOAuthPrincipal";

    public static final String ATTRIBUTE_AUTHENTICATED_PORTAL_USER = "_authenticatedPortalUser";

    public static final String ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS = "_authenticatedPortalUserForJaas";

    public static final String ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT = "_urlToRedirectAfterLinkSocialAccount";

    public static final String ATTRIBUTE_EXCEPTION_OAUTH = "_oauthException";

    public static final String ATTRIBUTE_LINKED_OAUTH_PROVIDER = "_linkedOAuthProviderUsernameAttrName";

    public static final String ATTRIBUTE_EXCEPTION_AFTER_FAILED_LINK = "_oauthExceptionAfterFailedLink";

    public static final String ATTRIBUTE_TWITTER_REQUEST_TOKEN = "_twitterRequestToken";

    public static final String ATTRIBUTE_LINKEDIN_REQUEST_TOKEN = "_linkedinRequestToken";

    public static final String ATTRIBUTE_AUTH_STATE = "_authState";

    public static final String ATTRIBUTE_VERIFICATION_STATE = "_verificationState";

    public static final String ATTRIBUTE_REMEMBER_ME = "_rememberme";

    // URL

    public static final String FACEBOOK_AUTHENTICATION_URL_PATH = "/facebookAuth";

    public static final String GOOGLE_AUTHENTICATION_URL_PATH = "/googleAuth";

    public static final String TWITTER_AUTHENTICATION_URL_PATH = "/twitterAuth";


    // Request parameters

    public static final String PARAM_OAUTH_INTERACTION = "_oauthInteraction";

    public static final String PARAM_CUSTOM_SCOPE = "_oauthCustomScope";

    public static final String PARAM_INITIAL_URI = "_initialURI";

    public static final String PARAM_REMEMBER_ME = "_rememberme";

    public static final String PARAM_OAUTH_INTERACTION_VALUE_START = "start";


    // Exception constants

    public static final String EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME = "OAuthProviderUsernameAttributeName";

    public static final String EXCEPTION_OAUTH_PROVIDER_USERNAME = "OAuthProviderUsername";

    public static final String EXCEPTION_OAUTH_PROVIDER_NAME = "OAuthProviderName";

}
