/**
 * The portal web application.
 */
@Application
@Bindings({
        @Binding(Flash.class),
        @Binding(UserManager.class),
        @Binding(OrganizationService.class),
        @Binding(AuthenticationRegistry.class),
        @Binding(OAuthProviderTypeRegistry.class),
        @Binding(OauthProviderHelper.class)
})
@Portlet
@Assets({@Asset("registerportlet.css"), @Asset("social-buttons.css")})
@WithAssets
package org.gatein.portal.ui.register;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;

