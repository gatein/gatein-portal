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

package org.gatein.security.oauth.test;

import java.lang.reflect.UndeclaredThrowableException;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.impl.UserImpl;
import org.gatein.common.exception.GateInExceptionConstants;
import org.gatein.security.oauth.data.SocialNetworkService;
import org.gatein.common.exception.GateInException;
import org.gatein.security.oauth.utils.OAuthConstants;
import org.gatein.security.oauth.generic.OAuthProviderType;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.web.oauth-configuration.xml") })
public class TestSocialNetworkService extends AbstractKernelTest {

    private OrganizationService orgService;
    private SocialNetworkService socialNetworkService;

    @Override
    protected void setUp() throws Exception {
        PortalContainer portalContainer = PortalContainer.getInstance();
        orgService = (OrganizationService) portalContainer.getComponentInstanceOfType(OrganizationService.class);
        socialNetworkService = (SocialNetworkService) portalContainer.getComponentInstanceOfType(SocialNetworkService.class);
        begin();
    }

    @Override
    protected void tearDown() throws Exception {
        end();
    }

    public void testPersistOAuthProviderUsernames() throws Exception {
        User user1 = new UserImpl("testUser1");
        User user2 = new UserImpl("testUser2");
        orgService.getUserHandler().createUser(user1, false);
        orgService.getUserHandler().createUser(user2, false);

        // Save facebook username and google username for user1
        UserProfile userProfile1 = orgService.getUserProfileHandler().createUserProfileInstance(user1.getUserName());
        userProfile1.setAttribute(OAuthConstants.PROFILE_FACEBOOK_USERNAME, "joseph.doyle");
        userProfile1.setAttribute(OAuthConstants.PROFILE_GOOGLE_USERNAME, "joseph.something");
        orgService.getUserProfileHandler().saveUserProfile(userProfile1, true);

        // Save facebook username and google username for user2
        UserProfile userProfile2 = orgService.getUserProfileHandler().createUserProfileInstance(user2.getUserName());
        userProfile2.setAttribute(OAuthConstants.PROFILE_FACEBOOK_USERNAME, "john.doyle");
        userProfile2.setAttribute(OAuthConstants.PROFILE_GOOGLE_USERNAME, "john.something");
        orgService.getUserProfileHandler().saveUserProfile(userProfile2, true);

        // Find user by facebook and google username
        User foundUser = socialNetworkService.findUserByOAuthProviderUsername(OAuthProviderType.FACEBOOK, "joseph.doyle");
        assertNotNull(foundUser);
        assertEquals(foundUser.getUserName(), user1.getUserName());

        User foundUser2 = socialNetworkService.findUserByOAuthProviderUsername(OAuthProviderType.FACEBOOK, "john.doyle");
        assertNotNull(foundUser2);
        assertEquals(foundUser2.getUserName(), user2.getUserName());

        User foundUser3 = socialNetworkService.findUserByOAuthProviderUsername(OAuthProviderType.GOOGLE, "john.something");
        assertNotNull(foundUser3);
        assertEquals(foundUser3.getUserName(), user2.getUserName());

        // Try to change facebook username for user1 with socialNetworkService
        socialNetworkService.updateOAuthInfo(OAuthProviderType.FACEBOOK, user1.getUserName(), "joseph.doyle.changed", "someToken");

        User foundUser4 = socialNetworkService.findUserByOAuthProviderUsername(OAuthProviderType.FACEBOOK, "joseph.doyle.changed");
        assertNotNull(foundUser4);
        assertEquals(foundUser4.getUserName(), user1.getUserName());

        try {
            // This should fail because of duplicated facebook username
            socialNetworkService.updateOAuthInfo(OAuthProviderType.FACEBOOK, user2.getUserName(), "joseph.doyle.changed", "someToken");

            fail("Exception should occur because of duplicated facebook username");
        } catch (GateInException gtnOauthException) {
            assertEquals(GateInExceptionConstants.EXCEPTION_CODE_DUPLICATE_OAUTH_PROVIDER_USERNAME, gtnOauthException.getExceptionCode());
            assertEquals(OAuthConstants.PROFILE_FACEBOOK_USERNAME, gtnOauthException.getExceptionAttribute(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME));
            assertEquals("joseph.doyle.changed", gtnOauthException.getExceptionAttribute(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME));
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public void testPersistOAuthAccessTokens() throws Exception {
        User user1 = new UserImpl("testUser1");
        User user2 = new UserImpl("testUser2");
        orgService.getUserHandler().createUser(user1, false);
        orgService.getUserHandler().createUser(user2, false);

        socialNetworkService.updateOAuthAccessToken(OAuthProviderType.FACEBOOK, user1.getUserName(), "aaa123");
        socialNetworkService.updateOAuthAccessToken(OAuthProviderType.FACEBOOK, user2.getUserName(), "bbb456");
        socialNetworkService.updateOAuthAccessToken(OAuthProviderType.GOOGLE, user1.getUserName(), "ccc789");

        assertEquals("aaa123", socialNetworkService.getOAuthAccessToken(OAuthProviderType.FACEBOOK, user1.getUserName()));
        assertEquals("bbb456", socialNetworkService.getOAuthAccessToken(OAuthProviderType.FACEBOOK, user2.getUserName()));
        assertEquals("ccc789", socialNetworkService.getOAuthAccessToken(OAuthProviderType.GOOGLE, user1.getUserName()));
        assertNull(socialNetworkService.getOAuthAccessToken(OAuthProviderType.GOOGLE, user2.getUserName()));

        // TODO: Verify that accessTokens are encoded by directly access them through userProfiles
    }

}
