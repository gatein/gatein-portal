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
    @Binding(SSOHelper.class),
    @Binding(OrganizationService.class),
    @Binding(SecurityService.class)})
@WebJars(@WebJar("bootstrap"))

@Modules({
    @Module(
      value = @Asset(id = "jquery", value = "javascripts/jquery-1.7.1.min.js"),
      adapter = "(function() { @{include} return jQuery.noConflict(true);})();"
    ),
    @Module(
      value = @Asset(id = "jquery-ui", value = "javascripts/jquery-ui-1.10.3.custom.js", depends = {"jquery"}),
      adapter = "(function(){ @{include} })(jQuery);",
      aliases = {"jQuery"}
    ),
    @Module(
      value = @Asset(id = "bootstrapjs", value = "javascripts/bootstrap.min.js", depends = {"jquery"}),
      adapter = "(function(){ window.jQuery = $; @{include} window.jQuery = null;})($);",
      aliases = {"$"}
    ),
    @Module(
      value = @Asset(id = "underscore",  value = "javascripts/underscore.js"),
      adapter = "(function() { @{include} return _; })();"
    ),
    @Module(
      value = @Asset(id = "backbone",  value = "javascripts/backbone.js", depends = {"underscore", "jquery"}),
      adapter = "(function() { window.jQuery = $; @{include} window.jQuery = null; return Backbone; })(_, $);",
      aliases = {"_", "$"}
    ),
    @Module(
      value = @Asset(id = "layout-model", value = "javascripts/layout-model.js", depends = {"backbone", "jquery", "underscore"}),
      aliases = {"Backbone", "$", "_"}
    ),
    @Module(
      value = @Asset(id="composer-view", value = "javascripts/composer-view.js", depends = {
        "backbone", "layout-model", "jquery", "editor-view"
      }), aliases = {"Backbone", "layoutDef", "$", "editorView"}
    ),
    @Module(
      value = @Asset(id = "layout-view", value = "javascripts/layout-view.js", depends = {
        "backbone", "layout-model", "jquery", "jquery-ui", "editor-view"
      }), aliases = {"Backbone", "layoutDef", "$", "jqueryUI", "editorView"}
    ),
    @Module(
      value = @Asset(id = "page-properties-view", value = "javascripts/page-properties-view.js", depends = {
        "backbone", "jquery", "underscore", "editor-view"
      }), aliases = {"Backbone", "$", "underscore", "editorView"}
   ),
    @Module(
      value = @Asset(id="editor-view", value = "javascripts/editor-view.js", depends = {
        "backbone", "jquery"
      }), aliases = {"Backbone", "$"}
    )
})

@Assets({
    @Asset(id = "bootstrap", value = "bootstrap/2.3.2/css/bootstrap.min.css"),
    @Asset(id = "editor", value = "editor.css"),
    @Asset(id = "login-stylesheet", value = "login.css"),
    @Asset(id = "social-buttons", value = "social-buttons.css")
})
@WithAssets
@Tags(@Tag(name = "window", path = "window.gtmpl"))
package org.gatein.portal.web;

import juzu.Application;
import juzu.plugin.amd.Module;
import juzu.plugin.amd.Modules;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
import juzu.template.Tag;
import juzu.template.Tags;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.portal.content.ProviderRegistry;
import org.gatein.portal.mop.permission.SecurityService;
import org.gatein.portal.web.kernel.KernelFilter;
import org.gatein.portal.web.layout.SimpleLayoutFactory;
import org.gatein.portal.web.login.LoginFailureMessage;
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
