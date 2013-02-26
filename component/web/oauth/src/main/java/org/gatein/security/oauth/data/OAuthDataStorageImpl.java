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

package org.gatein.security.oauth.data;

import java.lang.reflect.Method;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.utils.OAuthConstants;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthDataStorageImpl implements OAuthDataStorage {

    private static Logger log = LoggerFactory.getLogger(OAuthDataStorageImpl.class);

    private OrganizationService orgService;

    public OAuthDataStorageImpl(OrganizationService orgService) {
        this.orgService = orgService;
    }

    @Override
    public User findUserByFacebookUsername(String facebookUsername) {
        return findUserByOAuthProviderUsername(OAuthConstants.PROFILE_FACEBOOK_USERNAME, facebookUsername);
    }

    @Override
    public User findUserByGoogleUsername(String googleUsername) {
        return findUserByOAuthProviderUsername(OAuthConstants.PROFILE_GOOGLE_USERNAME, googleUsername);
    }

    @Override
    public User findUserByOAuthProviderUsername(String oauthProviderUsernameAttrName, String oauthProviderUsername) {
        UserHandler userHandler = orgService.getUserHandler();

        // TODO: Ugly, but it's used due to OrganizationService API limitations because it doesn't allow to find user by unique userProfile attribute
        try {
            Method m = userHandler.getClass().getDeclaredMethod("findUserByUniqueAttribute", String.class, String.class);
            return (User)m.invoke(userHandler, oauthProviderUsernameAttrName, oauthProviderUsername);
        } catch (NoSuchMethodException e) {
            String error = "Method findUserByUniqueAttribute(String, String) is not available on userHandler object " + userHandler +
                    "of class " + userHandler.getClass();
            log.error(error);
            throw new RuntimeException(error, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveFacebookAccessToken(String portalUsername, String facebookAccessToken) {
        saveAccessToken(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN, facebookAccessToken, portalUsername);
        if (log.isTraceEnabled()) {
            log.trace("Facebook accessToken saved to userProfile of user " + portalUsername);
        }
    }

    @Override
    public void saveGoogleAccessToken(String portalUsername, String googleAccessToken) {
        saveAccessToken(OAuthConstants.PROFILE_GOOGLE_ACCESS_TOKEN, googleAccessToken, portalUsername);
        if (log.isTraceEnabled()) {
            log.trace("Google accessToken saved to userProfile of user " + portalUsername);
        }
    }

    @Override
    public String getFacebookAccessToken(String username) {
        return getAccessToken(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN, username);
    }

    @Override
    public String getGoogleAccessToken(String username) {
        return getAccessToken(OAuthConstants.PROFILE_GOOGLE_ACCESS_TOKEN, username);
    }

    protected void saveAccessToken(String accessTokenAttributeName, String accessTokenValue, String portalUsername) {
        try {
            UserProfileHandler userProfileHandler = orgService.getUserProfileHandler();
            UserProfile userProfile = userProfileHandler.findUserProfileByName(portalUsername);

            // TODO: Encryption before saving
            userProfile.setAttribute(accessTokenAttributeName, accessTokenValue);

            userProfileHandler.saveUserProfile(userProfile, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getAccessToken(String accessTokenAttributeName, String portalUsername) {
        try {
            UserProfileHandler userProfileHandler = orgService.getUserProfileHandler();
            UserProfile userProfile = userProfileHandler.findUserProfileByName(portalUsername);

            // TODO: Decryption before loading
            return userProfile.getAttribute(accessTokenAttributeName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
