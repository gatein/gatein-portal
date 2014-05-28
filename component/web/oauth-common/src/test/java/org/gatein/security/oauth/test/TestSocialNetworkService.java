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

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;
import org.gatein.security.oauth.spi.SocialNetworkService;
import org.gatein.security.oauth.facebook.FacebookAccessTokenContext;
import org.gatein.security.oauth.google.GoogleAccessTokenContext;
import org.gatein.security.oauth.twitter.TwitterAccessTokenContext;

/**
 * Test persistence of data with usage of {@link SocialNetworkService}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.web.oauth-configuration.xml") })
public class TestSocialNetworkService extends AbstractKernelTest {

    private OrganizationService orgService;
    private SocialNetworkService socialNetworkService;
    private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;
    private AbstractCodec codec;

    @Override
    protected void setUp() throws Exception {
        PortalContainer portalContainer = PortalContainer.getInstance();
        orgService = (OrganizationService) portalContainer.getComponentInstanceOfType(OrganizationService.class);
        socialNetworkService = (SocialNetworkService) portalContainer.getComponentInstanceOfType(SocialNetworkService.class);
        oAuthProviderTypeRegistry = (OAuthProviderTypeRegistry) portalContainer.getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
        CodecInitializer codecInitializer = (CodecInitializer) portalContainer.getComponentInstanceOfType(CodecInitializer.class);
        this.codec = codecInitializer.getCodec();
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
        User foundUser = socialNetworkService.findUserByOAuthProviderUsername(getFacebookProvider(), "joseph.doyle");
        assertNotNull(foundUser);
        assertEquals(foundUser.getUserName(), user1.getUserName());

        User foundUser2 = socialNetworkService.findUserByOAuthProviderUsername(getFacebookProvider(), "john.doyle");
        assertNotNull(foundUser2);
        assertEquals(foundUser2.getUserName(), user2.getUserName());

        User foundUser3 = socialNetworkService.findUserByOAuthProviderUsername(getGoogleProvider(), "john.something");
        assertNotNull(foundUser3);
        assertEquals(foundUser3.getUserName(), user2.getUserName());

        // Try to change facebook username for user1 with socialNetworkService
        socialNetworkService.updateOAuthInfo(getFacebookProvider(), user1.getUserName(), "joseph.doyle.changed", createFacebookAccessToken("someToken"));

        User foundUser4 = socialNetworkService.findUserByOAuthProviderUsername(getFacebookProvider(), "joseph.doyle.changed");
        assertNotNull(foundUser4);
        assertEquals(foundUser4.getUserName(), user1.getUserName());

        try {
            // This should fail because of duplicated facebook username
            socialNetworkService.updateOAuthInfo(getFacebookProvider(), user2.getUserName(), "joseph.doyle.changed", createFacebookAccessToken("someToken"));

            fail("Exception should occur because of duplicated facebook username");
        } catch (OAuthException gtnOauthOAuthException) {
            assertEquals(OAuthExceptionCode.DUPLICATE_OAUTH_PROVIDER_USERNAME, gtnOauthOAuthException.getExceptionCode());
            assertEquals(OAuthConstants.PROFILE_FACEBOOK_USERNAME, gtnOauthOAuthException.getExceptionAttribute(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME));
            assertEquals("joseph.doyle.changed", gtnOauthOAuthException.getExceptionAttribute(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME));
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }

        orgService.getUserHandler().removeUser(user1.getUserName(), false);
        orgService.getUserHandler().removeUser(user2.getUserName(), false);
    }

    public void testPersistOAuthAccessTokens() throws Exception {
        User user1 = new UserImpl("testUser1");
        User user2 = new UserImpl("testUser2");
        orgService.getUserHandler().createUser(user1, false);
        orgService.getUserHandler().createUser(user2, false);

        // Update some facebook accessTokens
        socialNetworkService.updateOAuthAccessToken(getFacebookProvider(), user1.getUserName(), createFacebookAccessToken("aaa123"));
        socialNetworkService.updateOAuthAccessToken(getFacebookProvider(), user2.getUserName(), createFacebookAccessToken("bbb456"));

        // Update some google accessToken
        GoogleAccessTokenContext googleToken = createGoogleAccessToken("ccc789", "rfrc487", "http://someScope");
        socialNetworkService.updateOAuthAccessToken(getGoogleProvider(), user1.getUserName(), googleToken);

        // Update some twitter accessToken
        TwitterAccessTokenContext twitterToken = new TwitterAccessTokenContext("tok1", "secret1");
        socialNetworkService.updateOAuthAccessToken(getTwitterProvider(), user1.getUserName(), twitterToken);

        // Verify that FB accessTokens could be obtained
        assertEquals("aaa123", socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()).getAccessToken());
        assertEquals("bbb456", socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user2.getUserName()).getAccessToken());

        // Verify that Google accessToken could be obtained
        googleToken = createGoogleAccessToken("ccc789", "rfrc487", "http://someScope");
        assertEquals(googleToken, socialNetworkService.getOAuthAccessToken(getGoogleProvider(), user1.getUserName()));
        assertNull(socialNetworkService.getOAuthAccessToken(getGoogleProvider(), user2.getUserName()));

        // Verify that twitter accessToken could be obtained
        assertEquals(new TwitterAccessTokenContext("tok1", "secret1"), socialNetworkService.getOAuthAccessToken(getTwitterProvider(), user1.getUserName()));
        assertNull(socialNetworkService.getOAuthAccessToken(getTwitterProvider(), user2.getUserName()));

        // Directly obtain accessTokens from userProfile and verify that they are encoded
        UserProfile userProfile1 = orgService.getUserProfileHandler().findUserProfileByName("testUser1");
        UserProfile userProfile2 = orgService.getUserProfileHandler().findUserProfileByName("testUser2");
        String encodedAccessToken1 = userProfile1.getAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN + ".1");
        String encodedAccessToken2 = userProfile2.getAttribute(OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN + ".1");
        assertFalse("aaa123".equals(encodedAccessToken1));
        assertFalse("bbb456".equals(encodedAccessToken2));
        assertTrue(codec.encode("aaa123").equals(encodedAccessToken1));
        assertTrue(codec.encode("bbb456").equals(encodedAccessToken2));

        // Verify that tokens are null after invalidation
        socialNetworkService.removeOAuthAccessToken(getFacebookProvider(), user1.getUserName());
        socialNetworkService.removeOAuthAccessToken(getGoogleProvider(), user1.getUserName());
        socialNetworkService.removeOAuthAccessToken(getTwitterProvider(), user1.getUserName());
        assertNull(socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()));
        assertNull(socialNetworkService.getOAuthAccessToken(getGoogleProvider(), user1.getUserName()));
        assertNull(socialNetworkService.getOAuthAccessToken(getTwitterProvider(), user1.getUserName()));
        assertNotNull(socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user2.getUserName()));

        orgService.getUserHandler().removeUser(user1.getUserName(), false);
        orgService.getUserHandler().removeUser(user2.getUserName(), false);
    }

    public void testInvalidationOfAccessTokens() throws Exception {
        User user1 = new UserImpl("testUser1");
        orgService.getUserHandler().createUser(user1, false);

        // Update some accessToken and verify that it's available
        socialNetworkService.updateOAuthInfo(getFacebookProvider(), user1.getUserName(), "fbUsername1", createFacebookAccessToken("fbAccessToken1"));
        assertEquals("fbAccessToken1", socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()).getAccessToken());

        // Update some accessToken again
        socialNetworkService.updateOAuthInfo(getFacebookProvider(), user1.getUserName(), "fbUsername1", createFacebookAccessToken("fbAccessToken2"));
        assertEquals("fbAccessToken2", socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()).getAccessToken());

        // Update userProfile and change FB username. AccessToken should be invalidated
        UserProfile userProfile1 = orgService.getUserProfileHandler().findUserProfileByName(user1.getUserName());
        userProfile1.setAttribute(getFacebookProvider().getUserNameAttrName(), "fbUsername2");
        orgService.getUserProfileHandler().saveUserProfile(userProfile1, true);
        assertNull(socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()));

        // Update some accessToken and verify it's here now
        socialNetworkService.updateOAuthAccessToken(getFacebookProvider(), user1.getUserName(), createFacebookAccessToken("fbAccessToken3"));
        assertEquals("fbAccessToken3", socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()).getAccessToken());

        // Null FB username and verify that accessToken is invalidated
        userProfile1 = orgService.getUserProfileHandler().findUserProfileByName(user1.getUserName());
        userProfile1.setAttribute(getFacebookProvider().getUserNameAttrName(), null);
        orgService.getUserProfileHandler().saveUserProfile(userProfile1, true);
        assertNull(socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()));

        // Test this with Twitter
        TwitterAccessTokenContext twitterToken = new TwitterAccessTokenContext("token1", "secret1");
        socialNetworkService.updateOAuthInfo(getTwitterProvider(), user1.getUserName(), "twitterUsername1", twitterToken);

        userProfile1 = orgService.getUserProfileHandler().findUserProfileByName(user1.getUserName());
        userProfile1.setAttribute(getTwitterProvider().getUserNameAttrName(), "twitterUsername2");
        orgService.getUserProfileHandler().saveUserProfile(userProfile1, true);
        assertNull(socialNetworkService.getOAuthAccessToken(getTwitterProvider(), user1.getUserName()));

        // Test this with Google
        GoogleAccessTokenContext grc = createGoogleAccessToken("token1", "rf1", "http://someScope");
        socialNetworkService.updateOAuthInfo(getGoogleProvider(), user1.getUserName(), "googleUsername1", grc);

        userProfile1 = orgService.getUserProfileHandler().findUserProfileByName(user1.getUserName());
        userProfile1.setAttribute(getGoogleProvider().getUserNameAttrName(), "googleUsername2");
        orgService.getUserProfileHandler().saveUserProfile(userProfile1, true);
        assertNull(socialNetworkService.getOAuthAccessToken(getGoogleProvider(), user1.getUserName()));

        orgService.getUserHandler().removeUser(user1.getUserName(), false);
    }

    public void testLongAccessToken() throws Exception {
        // Create some example token of length 800
        User user1 = new UserImpl("testUser1");
        orgService.getUserHandler().createUser(user1, false);

        String longAccessToken = createLongString();

        // FACEBOOK
        // Update long accessToken and verify that it's available
        socialNetworkService.updateOAuthInfo(getFacebookProvider(), user1.getUserName(), "fbUsername1", createFacebookAccessToken(longAccessToken));
        assertEquals(longAccessToken, socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()).getAccessToken());

        // Update with some short token now
        String shortAccessToken = "someToken1";
        socialNetworkService.updateOAuthInfo(getFacebookProvider(), user1.getUserName(), "fbUsername1", createFacebookAccessToken(shortAccessToken));
        assertEquals(shortAccessToken, socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()).getAccessToken());

        // GOOGLE
        GoogleAccessTokenContext grc = createGoogleAccessToken(longAccessToken, longAccessToken, "http://someScope");
        socialNetworkService.updateOAuthInfo(getGoogleProvider(), user1.getUserName(), "googleUsername1", grc);
        assertEquals(grc, socialNetworkService.getOAuthAccessToken(getGoogleProvider(), user1.getUserName()));

        socialNetworkService.removeOAuthAccessToken(getGoogleProvider(), user1.getUserName());
        assertNull(socialNetworkService.getOAuthAccessToken(getGoogleProvider(), user1.getUserName()));

        // TWITTER
        TwitterAccessTokenContext twitterToken = new TwitterAccessTokenContext(longAccessToken, longAccessToken);
        socialNetworkService.updateOAuthAccessToken(getTwitterProvider(), user1.getUserName(), twitterToken);
        assertEquals(twitterToken, socialNetworkService.getOAuthAccessToken(getTwitterProvider(), user1.getUserName()));

        twitterToken = new TwitterAccessTokenContext(shortAccessToken, shortAccessToken);
        socialNetworkService.updateOAuthAccessToken(getTwitterProvider(), user1.getUserName(), twitterToken);
        assertEquals(twitterToken, socialNetworkService.getOAuthAccessToken(getTwitterProvider(), user1.getUserName()));

        orgService.getUserHandler().removeUser(user1.getUserName(), false);
    }

    public void testAccessTokensWithMoreScopes() throws Exception {
        User user1 = new UserImpl("testUser1");
        orgService.getUserHandler().createUser(user1, false);

        // Update some google accessToken with two scopes
        GoogleAccessTokenContext googleToken = createGoogleAccessToken("ccc789", "rfrc487", "http://someScope", "http://someScope2");
        socialNetworkService.updateOAuthAccessToken(getGoogleProvider(), user1.getUserName(), googleToken);

        // Verify that Google accessTokens could be obtained and are equals to saved access token
        assertEquals(googleToken, socialNetworkService.getOAuthAccessToken(getGoogleProvider(), user1.getUserName()));

        // Update some google accessToken with two scopes
        FacebookAccessTokenContext facebookToken = createFacebookAccessToken("ddd789", "rfrc4876", "email", "publish_stream");
        socialNetworkService.updateOAuthAccessToken(getFacebookProvider(), user1.getUserName(), facebookToken);

        // Verify that Facebook accessTokens could be obtained and are equals to saved access token
        assertEquals(facebookToken, socialNetworkService.getOAuthAccessToken(getFacebookProvider(), user1.getUserName()));

        orgService.getUserHandler().removeUser(user1.getUserName(), false);
    }

    private OAuthProviderType<FacebookAccessTokenContext> getFacebookProvider() {
        return oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_FACEBOOK, FacebookAccessTokenContext.class);
    }

    private OAuthProviderType<GoogleAccessTokenContext> getGoogleProvider() {
        return oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_GOOGLE, GoogleAccessTokenContext.class);
    }

    private OAuthProviderType<TwitterAccessTokenContext> getTwitterProvider() {
        return oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_TWITTER, TwitterAccessTokenContext.class);
    }

    private GoogleAccessTokenContext createGoogleAccessToken(String accessToken, String refreshToken, String... scope) {
        GoogleTokenResponse grc = new GoogleTokenResponse();
        grc.setAccessToken(accessToken);
        grc.setRefreshToken(refreshToken);
        grc.setExpiresInSeconds(1000L);
        grc.setTokenType("Bearer");
        grc.setIdToken("someTokenId");
        return new GoogleAccessTokenContext(grc, scope);
    }

    private FacebookAccessTokenContext createFacebookAccessToken(String accessToken, String... scope) {
        if (scope == null) {
            scope = new String[] { "email" };
        }
        return new FacebookAccessTokenContext(accessToken, scope);
    }

    /**
     * Get string of length 800
     * @return
     */
    private String createLongString() {
        StringBuilder builder = new StringBuilder("1324567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        for (int i=0 ; i<3 ; i++) {
            builder.append(builder);
        }
        return builder.toString();
    }

}
