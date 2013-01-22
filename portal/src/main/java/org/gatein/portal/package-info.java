/**
 * The portal web application.
 */
@Application
@Bindings({@Binding(PortalContainer.class), @Binding(RamPersistence.class)})
package org.gatein.portal;

import juzu.Application;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import org.exoplatform.container.PortalContainer;
import org.gatein.portal.impl.mop.ram.RamPersistence;
