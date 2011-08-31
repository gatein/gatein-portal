/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.config;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.Node;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.Workspace;

import java.io.File;
import java.util.Collection;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestImport extends AbstractGateInTest
{

   public void testMixin() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      System.setProperty("override.1", "false");
      System.setProperty("import.mode.1", "conserve");
      System.setProperty("import.portal.1", "navigation1");

      //
      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);

      //
      RequestLifeCycle.begin(container);
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      RequestLifeCycle.end();
      bootstrap.dispose();
   }

   public void testDefaultMode() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport0-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");
      System.setProperty("import.portal.0", "navigation2");
      System.setProperty("override.1", "false");
      System.setProperty("import.mode.1", "merge");
      System.setProperty("import.portal.1", "navigation1");

      //
      bootstrap.boot();

      //
      PortalContainer container = bootstrap.getContainer();
      NavigationService service = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      RequestLifeCycle.begin(container);
      NavigationContext nav = service.loadNavigation(SiteKey.portal("classic"));
      NodeContext<Node> root = service.loadNode(Node.MODEL, nav, Scope.ALL, null);
      Collection<Node> c = root.getNodes();
      assertEquals(3, c.size());
      assertNotNull(root.get("foo"));
      assertNotNull(root.get("daa"));
      assertNotNull(root.get("bar"));
      RequestLifeCycle.end();
      bootstrap.dispose();
   }
}
