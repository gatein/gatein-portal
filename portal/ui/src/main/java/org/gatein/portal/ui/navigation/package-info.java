/**
 * The portal web application.
 */
@Application
@Bindings({
    @Binding(NavigationService.class),
    @Binding(DescriptionService.class),
    @Binding(SSOHelper.class)})
@Portlet
@Assets(@Asset("navigationportlet.css"))
@WithAssets
package org.gatein.portal.ui.navigation;

import juzu.Application;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.navigation.NavigationService;
