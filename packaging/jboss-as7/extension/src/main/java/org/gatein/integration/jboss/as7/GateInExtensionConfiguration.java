/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.gatein.integration.jboss.as7;

import org.gatein.integration.jboss.as7.deployment.GateInEarKey;
import org.gatein.integration.jboss.as7.deployment.GateInExtKey;
import org.gatein.integration.jboss.as7.deployment.PortletWarKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceName;

import java.util.*;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInExtensionConfiguration
{
   public static final GateInExtensionConfiguration INSTANCE = new GateInExtensionConfiguration();

   private Set<ModuleIdentifier> extModules;

   private Set<ModuleDependency> portletWarDependencies;

   private ModuleIdentifier earModule = ModuleIdentifier.create("deployment.gatein.ear");

   private final List<ServiceName> childWars = new LinkedList<ServiceName>();

   private final List<ServiceName> childSubUnits = new LinkedList<ServiceName>();

   final ModuleLoader moduleLoader = Module.getBootModuleLoader();

   private GateInExtensionConfiguration()
   {
      Set<ModuleIdentifier> set = new LinkedHashSet<ModuleIdentifier>();
      set.add(ModuleIdentifier.create("deployment.gatein-sample-extension.ear"));
      set.add(ModuleIdentifier.create("deployment.gatein-sample-portal.ear"));
      set.add(ModuleIdentifier.create("deployment.gatein-sample-skin.war"));
      //set.add(ModuleIdentifier.create("deployment.gatein-wsrp-integration.ear"));
      extModules = Collections.unmodifiableSet(set);

      Set<ModuleDependency> dset = new LinkedHashSet<ModuleDependency>();
      dset.add(new ModuleDependency(moduleLoader, ModuleIdentifier.create("org.gatein.wci"), false, false, false, false));
      dset.add(new ModuleDependency(moduleLoader, ModuleIdentifier.create("org.gatein.pc"), false, false, false, false));
      dset.add(new ModuleDependency(moduleLoader, ModuleIdentifier.create("javax.portlet.api"), false, false, false, false));
      portletWarDependencies = Collections.unmodifiableSet(dset);
   }

   public Set<ModuleIdentifier> getGateInExtModules()
   {
      return extModules;
   }

   public Set<ModuleDependency> getPortletWarDependencies()
   {
      return portletWarDependencies;
   }

   public ModuleIdentifier getGateInEarModule()
   {
      return earModule;
   }

   public void setGateInEarModule(ModuleIdentifier modules)
   {
      this.earModule = modules;
   }

   public synchronized List<ServiceName> getChildWars()
   {
      return Collections.unmodifiableList(childWars);
   }

   public synchronized void addChildWar(ServiceName deploymentServiceName) {
      childWars.add(deploymentServiceName);
   }
    
   public synchronized List<ServiceName> getChildSubUnits()
   {
      return Collections.unmodifiableList(childSubUnits);
   }

   public synchronized List<String> getChildSubUnitComponentPrefixes()
   {
      LinkedList<String> ret = new LinkedList<String>();
      for (ServiceName name : childSubUnits)
      {
         ret.add(name.getCanonicalName() + ".component.");
      }
      return ret;
   }

   public synchronized void addChildSubUnit(ServiceName serviceName) {
      childSubUnits.add(serviceName);
   }

   public void setConfigurationFromModel(ModelNode model)
   {
      ModelNode archives = model.get(Constants.DEPLOYMENT_ARCHIVES);
      if (archives.isDefined())
      {
         Set<ModuleIdentifier> set = new LinkedHashSet<ModuleIdentifier>();

         for (Property p: archives.asPropertyList())
         {
            boolean isMain = false;
            if (p.getValue().isDefined())
            {
               for (Property attr: p.getValue().asPropertyList())
               {
                  if (Constants.MAIN.equals(attr.getName()))
                     isMain = true;
               }
            }
            if (isMain)
               earModule = ModuleIdentifier.create("deployment." + p.getName());
            else
               set.add(ModuleIdentifier.create("deployment." + p.getName()));
         }
         extModules = Collections.unmodifiableSet(set);
      }

      ModelNode deps = model.get(Constants.PORTLET_WAR_DEPENDENCIES);
      if (deps.isDefined())
      {
         Set<ModuleDependency> dset = new LinkedHashSet<ModuleDependency>();

         for (Property p: deps.asPropertyList())
         {
            boolean importSvcs = false;
            if (p.getValue().isDefined())
            {
               for (Property attr: p.getValue().asPropertyList())
               {
                  if (Constants.IMPORT_SERVICES.equals(attr.getName()))
                     importSvcs = true;
               }
            }
            String [] parts = parseNameSlotPair(p.getName());
            dset.add(new ModuleDependency(moduleLoader, ModuleIdentifier.create(parts[0], parts[1]), false, false, importSvcs, true));
            // TODO: add optional, and exports
         }
         portletWarDependencies = Collections.unmodifiableSet(dset);
      }
   }

   private String[] parseNameSlotPair(String name)
   {
      String [] parts = name.split(":");
      if (parts.length == 2)
         return parts;
      return new String[] {parts[0], null};
   }

   public boolean isGateInArchive(DeploymentUnit du)
   {
      return du.getAttachment(GateInEarKey.KEY) != null
            || du.getAttachment(GateInExtKey.KEY) != null;
   }

   public boolean isPortletArchive(DeploymentUnit du)
   {
      return du.getAttachment(PortletWarKey.INSTANCE) != null;
   }

   public boolean isNonGateInPortletArchive(DeploymentUnit du)
   {
      return !isGateInArchive(du) && isPortletArchive(du);
   }

   public boolean isGateInOrPortletArchive(DeploymentUnit du)
   {
      return isGateInArchive(du) || isPortletArchive(du);
   }
}
