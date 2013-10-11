/**
 * The portal web application.
 */
@Application
@Bindings({
        @Binding(NavigationService.class),
        @Binding(DescriptionService.class)
})
@Portlet(name = "SiteMapPortlet")
@Assets({@Asset("sitemap.css"), @Asset("jquery-1.7.1.min.js"), @Asset("sitemap.js")})
@WithAssets
package org.gatein.portal.ui.sitemap;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.navigation.NavigationService;
