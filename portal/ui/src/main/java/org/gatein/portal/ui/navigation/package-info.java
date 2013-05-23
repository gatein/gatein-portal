/**
 * The portal web application.
 */
@Application
@Bindings({@Binding(NavigationService.class),@Binding(DescriptionService.class)})
@Portlet
package org.gatein.portal.ui.navigation;

import juzu.Application;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.navigation.NavigationService;
