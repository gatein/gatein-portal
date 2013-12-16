/**
 * The portal web application.
 */
@Application(defaultController = Controller.class)
@Bindings({
    @Binding(ProviderRegistry.class),
    @Binding(PortletAppManager.class),
    @Binding(PortalContainer.class),
    @Binding(LayoutService.class),
    @Binding(CustomizationService.class),
    @Binding(PageService.class),
    @Binding(NavigationService.class),
    @Binding(DescriptionService.class),
    @Binding(SiteService.class),
    @Binding(SimpleLayoutFactory.class),
    @Binding(PortletContentProvider.class),
    @Binding(KernelFilter.class),
    @Binding(LoginFailureMessage.class),
    @Binding(OAuthProviderTypeRegistry.class),
    @Binding(LoginHelper.class),
    @Binding(SSOHelper.class)})
@Assets({
    @Asset(id = "bootstrap", value = "bootstrap-2.3.1.min.css"),
    @Asset(id = "editor", value = "editor.css"),
    @Asset(id = "login-stylesheet", value = "login.css"),
    @Asset(id = "social-buttons", value = "social-buttons.css"),
    @Asset(id = "jquery", value = "javascripts/jquery-1.7.1.min.js"),
    @Asset(id = "jquery-ui", value = "javascripts/jquery-ui-1.10.3.custom.js"),
	@Asset(id = "bootstrapjs", value = "javascripts/bootstrap.min.js"),
    @Asset(id = "bootstrap-dropdown", value = "javascripts/bootstrap-dropdown.js"),
    @Asset(id = "underscore", value = "javascripts/underscore.js"),
    @Asset(id = "backbone", value = "javascripts/backbone.js"),
    @Asset(id = "layout-model", value = "javascripts/layout-model.js"),
    @Asset(id = "layout-view", value = "javascripts/layout-view.js")
})
@WithAssets({
    "bootstrap",
    "editor",
    "jquery",
    "jquery-ui",
	"bootstrapjs",
	"underscore",
	"backbone",
	"layout-model",
	"layout-view"
})
@Tags(@Tag(name = "window", path = "window.gtmpl"))
package org.gatein.portal.web;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.template.Tag;
import juzu.template.Tags;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.portal.content.ProviderRegistry;
import org.gatein.portal.web.kernel.KernelFilter;
import org.gatein.portal.web.layout.SimpleLayoutFactory;
import org.gatein.portal.web.login.LoginFailureMessage;
import org.gatein.portal.web.login.LoginHelper;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.web.page.Controller;
import org.gatein.portal.web.content.portlet.PortletContentProvider;
import org.gatein.portal.web.portlet.PortletAppManager;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;
