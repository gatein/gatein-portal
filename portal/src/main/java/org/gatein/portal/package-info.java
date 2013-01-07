/**
 * The portal web application.
 */
@Application
@Bindings(@Binding(PortalContainer.class))
package org.gatein.portal;

import juzu.Application;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import org.exoplatform.container.PortalContainer;