/**
 * The portal web application.
 */
@Application
@Bindings({
   @Binding(Flash.class)
})
@Portlet
package org.gatein.portal.ui.register;

import juzu.Application;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;

