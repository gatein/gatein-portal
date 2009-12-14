/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.application;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/organization/organization-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/application-registry-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/portal-configuration.xml")
})
public abstract class AbstractApplicationRegistryTest extends AbstractGateInTest
{

/*
   public void testFoo() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      ApplicationRegistryService registry = (ApplicationRegistryService)container.getComponentInstanceOfType(ApplicationRegistryService.class);
      registry.getAllApplications();
   }
*/

}
