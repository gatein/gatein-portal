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

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.OAuthProviderType;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AccessTokenInvalidationListener extends UserProfileEventListener {

    private static Logger log = LoggerFactory.getLogger(AccessTokenInvalidationListener.class);

    private final UserProfileHandler userProfileHandler;

    public AccessTokenInvalidationListener(OrganizationService orgService) {
        this.userProfileHandler = orgService.getUserProfileHandler();
    }

    @Override
    public void preSave(UserProfile userProfile, boolean isNew) throws Exception {
        UserProfile foundUserProfile = userProfileHandler.findUserProfileByName(userProfile.getUserName());

        for (OAuthProviderType opt : OAuthProviderType.values()) {
            String oauthProviderUsername = userProfile.getAttribute(opt.getUserNameAttrName());
            String foundOauthProviderUsername = foundUserProfile.getAttribute(opt.getUserNameAttrName());

            // This means that oauthUsername has been changed. We may need to invalidate current accessToken as well
            if (!Safe.equals(oauthProviderUsername, foundOauthProviderUsername)) {
                String currentAccessToken = userProfile.getAttribute(opt.getAccessTokenAttrName());
                String foundAccessToken = foundUserProfile.getAttribute(opt.getAccessTokenAttrName());

                // In this case, we need to remove existing accessToken
                if (currentAccessToken != null && currentAccessToken.equals(foundAccessToken)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Removing accessToken for oauthProvider=" + opt + ", username=" + userProfile.getUserName());
                    }
                    userProfile.setAttribute(opt.getAccessTokenAttrName(), null);
                }
            }
        }
    }
}
