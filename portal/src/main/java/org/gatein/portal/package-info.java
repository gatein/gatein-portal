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
    @Binding(SimpleLayoutFactory.class)})
package org.gatein.portal;

import juzu.Application;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import org.exoplatform.container.PortalContainer;
import org.gatein.portal.layout.SimpleLayoutFactory;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.portlet.PortletAppManager;
