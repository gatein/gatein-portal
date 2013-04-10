/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gatein.security.oauth.social;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.facebook.GateInFacebookProcessorImpl;
import org.gatein.security.oauth.utils.OAuthUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Processor to perform Facebook interaction
 *
 * @author Anil Saldhana
 * @since Sep 22, 2011
 */
public class FacebookProcessor {
    public static final String FB_AUTH_STATE_SESSION_ATTRIBUTE = "FB_AUTH_STATE_SESSION_ATTRIBUTE";

    private static Logger log = LoggerFactory.getLogger(GateInFacebookProcessorImpl.class);

    protected boolean trace = log.isTraceEnabled();

    protected List<String> roles = new ArrayList<String>();

    public enum STATES {
        AUTH, AUTHZ, FINISH
    };

    protected String clientID;
    protected String clientSecret;
    protected String scope;
    protected String returnURL;

    public FacebookProcessor(String clientID, String clientSecret, String scope, String returnURL, List<String> requiredRoles) {
        super();
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.returnURL = returnURL;
        this.roles.addAll(requiredRoles);
    }

    public void setRoleString(String roleStr) {
        if (roleStr == null)
            throw new RuntimeException("Role String is null in configuration");
        StringTokenizer st = new StringTokenizer(roleStr, ",");
        while (st.hasMoreElements()) {
            roles.add(st.nextToken());
        }
    }

    public boolean initialInteraction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnURL);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);

        if (scope != null) {
            params.put(OAuthConstants.SCOPE_PARAMETER, scope);
        }

        String location = new StringBuilder(FacebookConstants.SERVICE_URL).append("?").append(OAuthUtils.createQueryString(params))
                .toString();
        try {
            session.setAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE, STATES.AUTH.name());
            if (trace)
                log.trace("Redirect:" + location);
            response.sendRedirect(location);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean handleAuthStage(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().setAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE, STATES.AUTHZ.name());
        sendAuthorizeRequest(this.returnURL, response);
        return false;
    }

    protected void sendAuthorizeRequest(String returnUrl, HttpServletResponse response) {
        String returnUri = returnUrl;

        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnUri);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);
        if (scope != null) {
            params.put(OAuthConstants.SCOPE_PARAMETER, scope);
        }
        String location = new StringBuilder(FacebookConstants.AUTHENTICATION_ENDPOINT_URL).append("?")
                .append(OAuthUtils.createQueryString(params)).toString();
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Principal getPrincipal(HttpServletRequest request, HttpServletResponse response) {
        Principal facebookPrincipal = handleAuthenticationResponse(request, response);
        if (facebookPrincipal == null)
            return null;

        return facebookPrincipal;
    }

    protected Principal handleAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) throws OAuthException {
        String error = request.getParameter(OAuthConstants.ERROR_PARAMETER);
        if (error != null) {
            if (OAuthConstants.ERROR_ACCESS_DENIED.equals(error)) {
                throw new OAuthException(OAuthExceptionCode.EXCEPTION_CODE_USER_DENIED_SCOPE, error);
            } else {
                throw new OAuthException(OAuthExceptionCode.EXCEPTION_UNSPECIFIED, error);
            }
        } else {
            String authorizationCode = request.getParameter(OAuthConstants.CODE_PARAMETER);
            if (authorizationCode == null) {
                log.error("Authorization code parameter not found");
                return null;
            }

            URLConnection connection = sendAccessTokenRequest(authorizationCode);

            Map<String, String> params = OAuthUtils.formUrlDecode(OAuthUtils.readUrlContent(connection));
            String accessToken = params.get(OAuthConstants.ACCESS_TOKEN_PARAMETER);
            String expires = params.get(FacebookConstants.EXPIRES);

            if (trace)
                log.trace("Access Token=" + accessToken + " :: Expires=" + expires);

            if (accessToken == null) {
                throw new RuntimeException("No access token found");
            }

            return readInIdentity(accessToken);
        }
    }

    protected URLConnection sendAccessTokenRequest(String authorizationCode) {
        String returnUri = returnURL;

        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnUri);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);
        params.put(OAuthConstants.CLIENT_SECRET_PARAMETER, clientSecret);
        params.put(OAuthConstants.CODE_PARAMETER, authorizationCode);

        String location = new StringBuilder(FacebookConstants.ACCESS_TOKEN_ENDPOINT_URL).append("?")
                .append(OAuthUtils.createQueryString(params)).toString();

        try {
            if (trace)
                log.trace("AccessToken Request=" + location);
            URL url = new URL(location);
            URLConnection connection = url.openConnection();
            return connection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Principal readInIdentity(String accessToken) {
        FacebookPrincipal facebookPrincipal;
        try {
            String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("?access_token=")
                    .append(URLEncoder.encode(accessToken, "UTF-8")).toString();
            if (trace)
                log.trace("Profile read:" + urlString);

            URL profileUrl = new URL(urlString);
            String profileContent = OAuthUtils.readUrlContent(profileUrl.openConnection());
            JSONObject jsonObject = new JSONObject(profileContent);

            facebookPrincipal = new FacebookPrincipal();
            facebookPrincipal.setAccessToken(accessToken);
            facebookPrincipal.setId(jsonObject.optString("id"));
            facebookPrincipal.setName(jsonObject.optString("name"));
            facebookPrincipal.setUsername(jsonObject.optString("username"));
            facebookPrincipal.setFirstName(jsonObject.optString("first_name"));
            facebookPrincipal.setLastName(jsonObject.optString("last_name"));
            facebookPrincipal.setGender(jsonObject.optString("gender"));
            facebookPrincipal.setTimezone(jsonObject.optString("timezone"));
            facebookPrincipal.setLocale(jsonObject.optString("locale"));
            facebookPrincipal.setEmail(jsonObject.optString("email"));
            facebookPrincipal.setJsonObject(jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return facebookPrincipal;
    }

}

