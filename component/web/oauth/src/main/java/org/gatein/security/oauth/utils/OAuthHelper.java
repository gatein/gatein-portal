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

import org.exoplatform.container.xml.InitParams;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Helper for OAuth related things
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthHelper {
    private final boolean facebookAuthenticationEnabled;
    private final boolean googleAuthenticationEnabled;

    private static final Logger log = LoggerFactory.getLogger(OAuthHelper.class);

    public OAuthHelper(InitParams params) {
        String facebookAuthenticationEnabledParam = params.getValueParam("isFacebookAuthenticationEnabled").getValue();
        this.facebookAuthenticationEnabled = Boolean.parseBoolean(facebookAuthenticationEnabledParam);

        String googleAuthenticationEnabledParam = params.getValueParam("isGoogleAuthenticationEnabled").getValue();
        this.googleAuthenticationEnabled = Boolean.parseBoolean(googleAuthenticationEnabledParam);

        log.debug("OAuthHelper initialized. facebookAuthenticationEnabled: " + facebookAuthenticationEnabled + ", googleAuthenticationEnabled: " + googleAuthenticationEnabled);
    }

    public boolean isOauthEnabled() {
        return facebookAuthenticationEnabled || googleAuthenticationEnabled;
    }

    public boolean isFacebookAuthenticationEnabled() {
        return facebookAuthenticationEnabled;
    }

    public boolean isGoogleAuthenticationEnabled() {
        return googleAuthenticationEnabled;
    }

    public String getFacebookAuthenticationUrl(String contextPath) {
        return contextPath + OAuthConstants.FACEBOOK_AUTHENTICATION_URL_PATH;
    }

    public String getGoogleAuthenticationUrl(String contextPath) {
        return contextPath + OAuthConstants.GOOGLE_AUTHENTICATION_URL_PATH;
    }
}
