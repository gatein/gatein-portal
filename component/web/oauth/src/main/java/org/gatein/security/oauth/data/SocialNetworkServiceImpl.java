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
import org.gatein.common.exception.GateInException;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.generic.OAuthProviderType;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SocialNetworkServiceImpl implements SocialNetworkService {

    private static Logger log = LoggerFactory.getLogger(SocialNetworkServiceImpl.class);

    private OrganizationService orgService;

    public SocialNetworkServiceImpl(OrganizationService orgService) {
        this.orgService = orgService;
    }

    @Override
    public User findUserByOAuthProviderUsername(OAuthProviderType oauthProviderType, String oauthProviderUsername) {
        UserHandler userHandler = orgService.getUserHandler();

        // TODO: Ugly, but it's used due to OrganizationService API limitations because it doesn't allow to find user by unique userProfile attribute
        try {
            Method m = userHandler.getClass().getDeclaredMethod("findUserByUniqueAttribute", String.class, String.class);
            return (User)m.invoke(userHandler, oauthProviderType.getUserNameAttrName(), oauthProviderUsername);
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
    public void updateOAuthAccessToken(OAuthProviderType oauthProviderType, String username, String accessToken) {
        try {
            UserProfileHandler userProfileHandler = orgService.getUserProfileHandler();
            UserProfile userProfile = userProfileHandler.findUserProfileByName(username);

            String encryptedAccessTokenValue = encryptAccessToken(accessToken);

            userProfile.setAttribute(oauthProviderType.getAccessTokenAttrName(), encryptedAccessTokenValue);

            userProfileHandler.saveUserProfile(userProfile, true);
        } catch (GateInException gtnEx) {
            throw gtnEx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getOAuthAccessToken(OAuthProviderType oauthProviderType, String username) {
        try {
            UserProfileHandler userProfileHandler = orgService.getUserProfileHandler();
            UserProfile userProfile = userProfileHandler.findUserProfileByName(username);

            String encryptedAccessToken = userProfile.getAttribute(oauthProviderType.getAccessTokenAttrName());

            return decryptAccessToken(encryptedAccessToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateOAuthInfo(OAuthProviderType oauthProviderType, String username, String oauthUsername, String accessToken) {
        try {
            UserProfileHandler userProfileHandler = orgService.getUserProfileHandler();
            UserProfile userProfile = userProfileHandler.findUserProfileByName(username);

            String encryptedAccessToken = encryptAccessToken(accessToken);

            userProfile.setAttribute(oauthProviderType.getAccessTokenAttrName(), encryptedAccessToken);
            userProfile.setAttribute(oauthProviderType.getUserNameAttrName(), oauthUsername);

            userProfileHandler.saveUserProfile(userProfile, true);
        } catch (GateInException gtnEx) {
            throw gtnEx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String encryptAccessToken(String accessToken) {
        // TODO: implement
        return accessToken;
    }

    protected String decryptAccessToken(String accessToken) {
        // TODO: implement
        return accessToken;
    }

}
