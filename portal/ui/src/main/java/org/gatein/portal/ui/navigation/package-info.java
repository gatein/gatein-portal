/**
 * The portal web application.
 */
@Application
@Bindings({@Binding(NavigationService.class),@Binding(DescriptionService.class)})
@Portlet
@Assets(stylesheets = @Stylesheet(src = "navigationportlet.css"))
package org.gatein.portal.ui.navigation;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.navigation.NavigationService;
