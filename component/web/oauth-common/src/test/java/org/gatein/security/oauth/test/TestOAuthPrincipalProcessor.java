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

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.User;
import org.gatein.security.oauth.facebook.FacebookAccessTokenContext;
import org.gatein.security.oauth.linkedin.LinkedInPrincipalProcessor;
import org.gatein.security.oauth.linkedin.LinkedinAccessTokenContext;
import org.gatein.security.oauth.principal.DefaultPrincipalProcessor;
import org.gatein.security.oauth.spi.OAuthPrincipal;
import org.gatein.security.oauth.spi.OAuthPrincipalProcessor;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.web.oauth-configuration.xml") })
public class TestOAuthPrincipalProcessor extends AbstractKernelTest {

    private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

    @Override
    protected void setUp() throws Exception {
        PortalContainer portalContainer = PortalContainer.getInstance();
        oAuthProviderTypeRegistry = (OAuthProviderTypeRegistry) portalContainer.getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
        begin();
    }

    @Override
    protected void tearDown() throws Exception {
        end();
    }

    public void testOAuthPrincipalRegistration() {
        OAuthProviderType oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider("FACEBOOK",
                FacebookAccessTokenContext.class);
        OAuthPrincipalProcessor oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
        assertNotNull(oauthPrincipalProcessor);
        assertEquals(DefaultPrincipalProcessor.class, oauthPrincipalProcessor.getClass());

        oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider("LINKEDIN", LinkedinAccessTokenContext.class);
        oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
        assertNotNull(oauthPrincipalProcessor);
        assertEquals(LinkedInPrincipalProcessor.class, oauthPrincipalProcessor.getClass());
    }

    public void testDefaultGenerateGateInUser() {
        OAuthProviderType providerType = new OAuthProviderType("OAUTH", true, "", null, null, "", "");
        OAuthPrincipal principal = new OAuthPrincipal("username", "firstName", "lastName", "displayName",
                "email@localhost.com", null, providerType);

        DefaultPrincipalProcessor principalProcessor = new DefaultPrincipalProcessor();
        User user = principalProcessor.convertToGateInUser(principal);

        assertNotNull(user);
        assertEquals("username", user.getUserName());
        assertEquals("email@localhost.com", user.getEmail());
        assertEquals("firstName", user.getFirstName());
        assertEquals("lastName", user.getLastName());
        assertEquals("displayName", user.getDisplayName());
    }

    public void testLinkedInGenerateGateInUser() {
        OAuthProviderType providerType = new OAuthProviderType("LINKEDIN", true, "", null, null, "", "");
        OAuthPrincipal principal = new OAuthPrincipal("randomString", "firstName", "lastName", "displayName",
                "linkedin_user@localhost.com", null, providerType);

        LinkedInPrincipalProcessor principalProcessor = new LinkedInPrincipalProcessor();
        User user = principalProcessor.convertToGateInUser(principal);

        assertNotNull(user);
        assertEquals("linkedin_user", user.getUserName());
        assertEquals("linkedin_user@localhost.com", user.getEmail());
        assertEquals("firstName", user.getFirstName());
        assertEquals("lastName", user.getLastName());
        assertEquals("displayName", user.getDisplayName());
    }
}
