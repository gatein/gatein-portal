/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.web.redirect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.RedirectMappings;
import org.exoplatform.portal.mop.importer.ImportMode;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public abstract class TestConfig extends AbstractGateInTest
{

   private Set<String> sysProperties = new HashSet<String>();
   
   protected KernelBootstrap bootstrap;
   
   public PortalContainer getContainer(String configurationFile, String origin)
   {
      if (bootstrap == null)
      {
         bootstrap = new KernelBootstrap();
         bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
         bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
         bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
         bootstrap.addConfiguration(ContainerScope.PORTAL, configurationFile);

         setSystemProperty("override.origin", "false");
         setSystemProperty("import.mode.origin", ImportMode.OVERWRITE.toString());
         setSystemProperty("import.portal.origin", origin);

         //
         bootstrap.boot();
      }
      return bootstrap.getContainer();
   }
   
   protected void setSystemProperty(String key, String value)
   {
      sysProperties.add(key);
      System.setProperty(key, value);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      for (String key : sysProperties)
      {
         System.clearProperty(key);
      }
      sysProperties.clear();
      bootstrap.dispose();
   }
}

