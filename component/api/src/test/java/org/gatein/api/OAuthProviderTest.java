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

package org.gatein.api;

import static org.gatein.api.Assert.assertEquals;
import static org.gatein.api.Assert.assertFalse;
import static org.gatein.api.Assert.assertNotNull;
import static org.gatein.api.Assert.assertNull;
import static org.gatein.api.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.plus.Plus;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;
import org.gatein.api.oauth.AccessToken;
import org.gatein.api.oauth.AccessTokenImpl;
import org.gatein.api.oauth.OAuthProvider;
import org.gatein.api.oauth.exception.OAuthApiException;
import org.gatein.api.oauth.exception.OAuthApiExceptionCode;
import org.gatein.security.oauth.facebook.FacebookAccessTokenContext;
import org.gatein.security.oauth.google.GoogleAccessTokenContext;
import org.junit.Test;

/**
 * Test of OAuth stuff in context of GateIn API
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthProviderTest extends AbstractApiTest {

    @Test
    public void testPersistence() throws Exception {
        OrganizationService orgService = getOrganizationService();
        User user1 = new UserImpl("testUser1");
        User user2 = new UserImpl("testUser2");
        orgService.getUserHandler().createUser(user1, false);
        orgService.getUserHandler().createUser(user2, false);

        OAuthProvider facebook = portal.getOAuthProvider(OAuthProvider.FACEBOOK);
        OAuthProvider google = portal.getOAuthProvider(OAuthProvider.GOOGLE);

        // Update some facebook accessTokens
        AccessToken accessToken1 = createFacebookAccessToken("aaa123");
        AccessToken accessToken2 = createFacebookAccessToken("bbb456");
        facebook.saveAccessToken(user1.getUserName(), accessToken1);
        facebook.saveAccessToken(user2.getUserName(), accessToken2);

        // Update some google accessToken
        AccessToken accessToken3 = createGoogleAccessToken("ccc789", "ddd123", "someScope");
        google.saveAccessToken(user1.getUserName(), accessToken3);

        // Test that facebook and google accessTokens are available
        assertEquals(accessToken1, facebook.loadAccessToken(user1.getUserName()));
        assertEquals(accessToken2, facebook.loadAccessToken(user2.getUserName()));
        assertEquals(accessToken3, google.loadAccessToken(user1.getUserName()));

        // Remove access tokens
        facebook.removeAccessToken(user1.getUserName());
        facebook.removeAccessToken(user2.getUserName());
        google.removeAccessToken(user1.getUserName());

        // Test that they are removed
        assertNull(facebook.loadAccessToken(user1.getUserName()));
        assertNull(facebook.loadAccessToken(user2.getUserName()));
        assertNull(google.loadAccessToken(user1.getUserName()));
        assertNull(google.loadAccessToken(user2.getUserName()));
    }

    @Test
    public void testGetSocialApiObjects() {
        AccessToken accessToken = createGoogleAccessToken("ccc789", "ddd123", "someScope");
        OAuthProvider google = portal.getOAuthProvider(OAuthProvider.GOOGLE);
        assertNotNull(google.getAuthorizedSocialApiObject(accessToken, Plus.class));
        assertNotNull(google.getAuthorizedSocialApiObject(accessToken, Oauth2.class));

        try {
            String something = google.getAuthorizedSocialApiObject(accessToken, String.class);
            fail("getAuthorizedSocialApiObject call should fail, but returned: " + something);
        } catch (OAuthApiException oae) {
            assertEquals(OAuthApiExceptionCode.SOCIAL_API_OBJECT_NOT_FOUND, oae.getExceptionCode());
        }
    }


    private OrganizationService getOrganizationService() {
        return (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    }

    private AccessToken createGoogleAccessToken(String accessToken, String refreshToken, String... scope) {
        GoogleTokenResponse grc = new GoogleTokenResponse();
        grc.setAccessToken(accessToken);
        grc.setRefreshToken(refreshToken);
        grc.setExpiresInSeconds(1000L);
        grc.setTokenType("Bearer");
        grc.setIdToken("someTokenId");
        GoogleAccessTokenContext ctx = new GoogleAccessTokenContext(grc, scope);
        return new AccessTokenImpl(ctx);
    }

    private AccessToken createFacebookAccessToken(String accessToken, String... scope) {
        if (scope == null || scope.length == 0) {
            scope = new String[] { "email" };
        }
        FacebookAccessTokenContext ctx = new FacebookAccessTokenContext(accessToken, scope);
        return new AccessTokenImpl(ctx);
    }
}
