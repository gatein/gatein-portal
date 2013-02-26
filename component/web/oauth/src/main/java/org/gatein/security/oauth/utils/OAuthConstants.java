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

package org.gatein.security.oauth.utils;

/**
 * Various constants related to OAuth
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthConstants {


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


    // Session attributes

    public static final String SESSION_ATTRIBUTE_AUTHENTICATED_PRINCIPAL = "authenticatedPrincipal";

}
