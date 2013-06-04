/**
 * The portal web application.
 */
@Application
@Bindings({@Binding(Flash.class)})
@Portlet(name = "RegisterPortlet")
package org.gatein.portal.portlet.register;

import org.gatein.portal.ui.register.Flash;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.binding.Bindings;
import juzu.plugin.binding.Binding;
import juzu.plugin.portlet.Portlet;

