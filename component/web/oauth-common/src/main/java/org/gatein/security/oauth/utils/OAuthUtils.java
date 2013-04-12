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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.oauth2.model.Userinfo;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;
import org.gatein.security.oauth.common.OAuthProviderType;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.common.OAuthPrincipal;
import org.gatein.security.oauth.facebook.FacebookAccessTokenContext;
import org.gatein.security.oauth.google.GoogleAccessTokenContext;
import org.gatein.security.oauth.registry.OAuthProviderTypeRegistry;
import org.gatein.security.oauth.social.FacebookPrincipal;
import org.gatein.security.oauth.twitter.TwitterAccessTokenContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthUtils {

    // Converting objects

    public static OAuthPrincipal<FacebookAccessTokenContext> convertFacebookPrincipalToOAuthPrincipal(FacebookPrincipal facebookPrincipal,
                                            OAuthProviderType<FacebookAccessTokenContext> facebookProviderType, FacebookAccessTokenContext fbAccessTokenContext) {
        return new OAuthPrincipal<FacebookAccessTokenContext>(facebookPrincipal.getUsername(), facebookPrincipal.getFirstName(), facebookPrincipal.getLastName(),
                facebookPrincipal.getAttribute("name"), facebookPrincipal.getEmail(), fbAccessTokenContext, facebookProviderType);
    }

    public static OAuthPrincipal<TwitterAccessTokenContext> convertTwitterUserToOAuthPrincipal(twitter4j.User twitterUser, TwitterAccessTokenContext accessToken,
                                                             OAuthProviderType<TwitterAccessTokenContext> twitterProviderType) {
        String fullName = twitterUser.getName();
        String firstName;
        String lastName;

        int spaceIndex = fullName.lastIndexOf(' ');

        if (spaceIndex != -1) {
            firstName = fullName.substring(0, spaceIndex);
            lastName = fullName.substring(spaceIndex + 1);
        } else {
            firstName = fullName;
            lastName = null;
        }

        return new OAuthPrincipal<TwitterAccessTokenContext>(twitterUser.getScreenName(), firstName, lastName, fullName, null, accessToken,
                twitterProviderType);
    }

    public static OAuthPrincipal<GoogleAccessTokenContext> convertGoogleInfoToOAuthPrincipal(Userinfo userInfo, GoogleAccessTokenContext accessToken,
                                                               OAuthProviderType<GoogleAccessTokenContext> googleProviderType) {
        // Assume that username is first part of email
        String email = userInfo.getEmail();
        String username = email != null ? email.substring(0, email.indexOf('@')) : userInfo.getGivenName();
        return new OAuthPrincipal<GoogleAccessTokenContext>(username, userInfo.getGivenName(), userInfo.getFamilyName(), userInfo.getName(), userInfo.getEmail(),
                accessToken, googleProviderType);
    }

    public static User convertOAuthPrincipalToGateInUser(OAuthPrincipal principal) {
        User gateinUser = new UserImpl(principal.getUserName());
        gateinUser.setFirstName(principal.getFirstName());
        gateinUser.setLastName(principal.getLastName());
        gateinUser.setEmail(principal.getEmail());
        gateinUser.setDisplayName(principal.getDisplayName());
        return gateinUser;
    }

    // HTTP related utils

    /**
     * Given a {@link java.util.Map} of params, construct a query string
     *
     * @param params parameters for query
     * @return query string
     */
    public static String createQueryString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            if (first) {
                first = false;
            } else {
                queryString.append("&");
            }
            queryString.append(paramName).append("=");
            String encodedParamValue;
            try {
                if (paramValue == null)
                    throw new RuntimeException("paramValue is null for paramName=" + paramName);
                encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            queryString.append(encodedParamValue);
        }
        return queryString.toString();
    }

    public static String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new OAuthException(OAuthExceptionCode.EXCEPTION_UNSPECIFIED, uee);
        }
    }

    /**
     * Whole HTTP response as String from given URLConnection
     *
     * @param connection
     * @return whole HTTP response as String
     */
    public static HttpResponseContext readUrlContent(URLConnection connection) throws IOException {
        StringBuilder result = new StringBuilder();

        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
        int statusCode = httpURLConnection.getResponseCode();

        Reader reader;
        try {
            reader = new InputStreamReader(connection.getInputStream());
        } catch (IOException ioe) {
            reader = new InputStreamReader(httpURLConnection.getErrorStream());
        }

        char[] buffer = new char[50];
        int nrOfChars;
        while ((nrOfChars = reader.read(buffer)) != -1) {
            result.append(buffer, 0, nrOfChars);
        }

        String response = result.toString();
        return new HttpResponseContext(statusCode, response, null);
    }

    /**
     * Decode given String to map. For example for input: accessToken=123456&expires=20071458 it returns map with two keys
     * "accessToken" and "expires" and their corresponding values
     *
     * @param encodedData
     * @return map with output data
     */
    public static Map<String, String> formUrlDecode(String encodedData) {
        Map<String, String> params = new HashMap<String, String>();
        String[] elements = encodedData.split("&");
        for (String element : elements) {
            String[] pair = element.split("=");
            if (pair.length == 2) {
                String paramName = pair[0];
                String paramValue;
                try {
                    paramValue = URLDecoder.decode(pair[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                params.put(paramName, paramValue);
            } else {
                throw new RuntimeException("Unexpected name-value pair in response: " + element);
            }
        }
        return params;
    }
}
