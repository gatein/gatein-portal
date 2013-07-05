/**
 * The portal web application.
 */
@Application
@Bindings({
    @Binding(PortletAppManager.class),
    @Binding(PortalContainer.class),
    @Binding(LayoutService.class),
    @Binding(CustomizationService.class),
    @Binding(PageService.class),
    @Binding(NavigationService.class),
    @Binding(SiteService.class),
    @Binding(SimpleLayoutFactory.class),
    @Binding(PortletContentProvider.class),
    @Binding(KernelFilter.class),
    @Binding(Flash.class)})
@Assets(stylesheets = @Stylesheet(src = "bootstrap-2.3.1.min.css"),
        declaredStylesheets = @Stylesheet(id = "login-stylesheet", src = "login.css")
)
package org.gatein.portal;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import org.exoplatform.container.PortalContainer;
import org.gatein.portal.kernel.KernelFilter;
import org.gatein.portal.layout.SimpleLayoutFactory;
import org.gatein.portal.login.Flash;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.page.spi.portlet.PortletContentProvider;
import org.gatein.portal.portlet.PortletAppManager;
