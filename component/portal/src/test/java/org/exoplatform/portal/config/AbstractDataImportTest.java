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
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.Workspace;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public abstract class AbstractDataImportTest extends AbstractGateInTest
{
   private Set<String> clearProperties = new HashSet<String>();
   
   protected abstract ImportMode getMode();

   protected abstract String getConfig2();

   protected abstract String getConfig1();

   protected abstract void afterOneBootWithExtention(PortalContainer container) throws Exception;
   
   protected abstract void afterFirstBoot(PortalContainer container) throws Exception;
   
   protected abstract void afterSecondBoot(PortalContainer container) throws Exception;
   
   protected abstract void afterSecondBootWithOverride(PortalContainer container) throws Exception;
   
   protected abstract void afterSecondBootWithWantReimport(PortalContainer container) throws Exception;

   protected abstract void afterSecondBootWithNoMixin(PortalContainer container) throws Exception;

   protected void setSystemProperty(String key, String value)
   {
      clearProperties.add(key);
      System.setProperty(key, value);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      for (String key : clearProperties)
      {
         System.clearProperty(key);
      }
      clearProperties.clear();
   }

   public void testOneBootWithExtension() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport2-configuration.xml");

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", getMode().toString());
      setSystemProperty("import.portal.1", getConfig1());
      setSystemProperty("override_2", "false");
      setSystemProperty("import.mode_2", getMode().toString());
      setSystemProperty("import.portal_2", getConfig2());

      //
      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      afterOneBootWithExtention(container);
      bootstrap.dispose();
   }
   
   public void testOneBoot() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", getMode().toString());
      setSystemProperty("import.portal.1", getConfig1());

      //
      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      afterFirstBoot(container);
      bootstrap.dispose();
   }

   public void testTwoBoots() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", getMode().toString());
      setSystemProperty("import.portal.1", getConfig1());

      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      afterFirstBoot(container);
      bootstrap.dispose();

      //
      setSystemProperty("import.portal.1", getConfig2());
      
      bootstrap.boot();
      container = bootstrap.getContainer();
      afterSecondBoot(container);
      bootstrap.dispose();
   }
   
   public void testTwoBootsWithOverride() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      setSystemProperty("override.1", "true");
      setSystemProperty("import.mode.1", getMode().toString());
      setSystemProperty("import.portal.1", getConfig1());

      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      afterFirstBoot(container);
      bootstrap.dispose();

      //
      setSystemProperty("import.portal.1", getConfig2());
      
      bootstrap.boot();
      container = bootstrap.getContainer();
      afterSecondBootWithOverride(container);
      bootstrap.dispose();
   }

   public void testTwoBootsWithWantReimport() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", getMode().toString());
      setSystemProperty("import.portal.1", getConfig1());

      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      afterFirstBoot(container);
      
      RequestLifeCycle.begin(container);
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      workspace.adapt(Imported.class).setStatus(Imported.WANT_REIMPORT);
      long creationTime1 = workspace.adapt(Imported.class).getCreationDate().getTime();
      long lastModificationTime1 = workspace.adapt(Imported.class).getLastModificationDate().getTime();
      mgr.getSession().save();
      RequestLifeCycle.end();
      
      bootstrap.dispose();

      //
      setSystemProperty("import.portal.1", getConfig2());
      
      bootstrap.boot();
      container = bootstrap.getContainer();
      afterSecondBootWithWantReimport(container);
      
      RequestLifeCycle.begin(container);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      long creationTime2 = workspace.adapt(Imported.class).getCreationDate().getTime();
      assertEquals(creationTime1, creationTime2);
      long lastModificationTime2 = workspace.adapt(Imported.class).getLastModificationDate().getTime();
      assertTrue(lastModificationTime2 > lastModificationTime1);
      mgr.getSession().save();
      RequestLifeCycle.end();
      bootstrap.dispose();
   }
   
   public void testTwoBootsWithNoMixin() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport1-configuration.xml");

      //
      setSystemProperty("override.1", "false");
      setSystemProperty("import.mode.1", getMode().toString());
      setSystemProperty("import.portal.1", getConfig1());

      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      afterFirstBoot(container);
      
      RequestLifeCycle.begin(container);
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      Imported imported = workspace.adapt(Imported.class);
      long creationTime1 = imported.getCreationDate().getTime();
      workspace.removeAdapter(Imported.class);
      mgr.getSession().save();
      RequestLifeCycle.end();
      
      bootstrap.dispose();

      //
      setSystemProperty("import.portal.1", getConfig2());
      
      bootstrap.boot();
      container = bootstrap.getContainer();
      afterSecondBootWithNoMixin(container);
      
      RequestLifeCycle.begin(container);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      imported = workspace.adapt(Imported.class);
      long creationTime2 = imported.getCreationDate().getTime();
      assertTrue(creationTime2 > creationTime1);
      mgr.getSession().save();
      RequestLifeCycle.end();
      bootstrap.dispose();
   }
}
