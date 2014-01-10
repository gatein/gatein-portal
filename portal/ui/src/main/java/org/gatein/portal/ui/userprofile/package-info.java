/**
 * The portal web application.
 */
@Application
@Portlet(name = "UserProfilePortlet")
@Bindings({
        @Binding(SSOHelper.class),
        @Binding(SocialNetworkService.class),
        @Binding(OrganizationService.class),
        @Binding(AuthenticationRegistry.class),
        @Binding(OAuthProviderTypeRegistry.class),
        @Binding(OauthProviderHelper.class),
        @Binding(Message.class)
})
package org.gatein.portal.ui.userprofile;

import juzu.Application;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.portal.ui.register.OauthProviderHelper;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;
import org.gatein.security.oauth.spi.SocialNetworkService;