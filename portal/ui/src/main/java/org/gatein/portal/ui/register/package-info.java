/**
 * The portal web application.
 */
@Application
@Bindings({
        @Binding(Flash.class)
})
@Portlet
@Assets(stylesheets = @Stylesheet(src = "registerportlet.css")) package org.gatein.portal.ui.register;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;

