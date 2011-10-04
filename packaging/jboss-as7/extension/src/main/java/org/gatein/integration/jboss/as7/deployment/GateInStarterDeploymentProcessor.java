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
package org.gatein.integration.jboss.as7.deployment;

import org.gatein.integration.jboss.as7.GateInExtension;
import org.gatein.integration.jboss.as7.GateInExtensionConfiguration;
import org.gatein.integration.jboss.as7.web.StartupService;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInStarterDeploymentProcessor implements DeploymentUnitProcessor
{
   private ConcurrentHashMap<ModuleIdentifier, ModuleIdentifier> deploymentModules;

   private GateInExtension extension;
   private GateInExtensionConfiguration config;

   public GateInStarterDeploymentProcessor(GateInExtension extension)
   {
      this.extension = extension;
      this.config = extension.getConfiguration();
   }

   private synchronized ConcurrentHashMap<ModuleIdentifier, ModuleIdentifier> getDeploymentModules()
   {
      if (deploymentModules == null)
      {
         deploymentModules = new ConcurrentHashMap<ModuleIdentifier, ModuleIdentifier>();
         deploymentModules.put(config.getGateInEarModule(), config.getGateInEarModule());
         for (ModuleIdentifier id : config.getGateInExtModules())
         {
            deploymentModules.put(id, id);
         }
      }
      return deploymentModules;
   }

   @Override
   public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException
   {
      DeploymentUnit du = phaseContext.getDeploymentUnit();

      // Wait to the last GateIn archive deployment unit install
      // Then enumerate all the child components which should all be scheduled for startup at that point
      // to get the dependencies for StartupService

      if (du.getAttachment(GateInEarKey.KEY) != null || du.getAttachment(GateInExtKey.KEY) != null)
      {
         ModuleIdentifier moduleId = du.getAttachment(Attachments.MODULE_IDENTIFIER);
         getDeploymentModules().remove(moduleId);

         if (deploymentModules.size() == 0)
         {
            StartupService startup = new StartupService();
            startup.setGateInModule(du.getAttachment(Attachments.MODULE));

            ServiceBuilder<StartupService> builder = phaseContext.getServiceTarget()
                  .addService(StartupService.SERVICE_NAME, startup);

            builder.addDependency(GateInExtension.deploymentUnitName(config.getGateInEarModule(), Phase.CLEANUP));
            for (ModuleIdentifier id : config.getGateInExtModules())
            {
               builder.addDependency(GateInExtension.deploymentUnitName(id, Phase.CLEANUP));
            }

            // Looks like web archives are completely covered by jboss.web services
            for (ServiceName svcName : config.getChildWars())
            {
               builder.addDependency(svcName);
            }

            // Subunit not necessary for web archives - but might be necessary for non-web
            for (ServiceName svcName : config.getChildSubUnits())
            {
               builder.addDependency(svcName.append(Phase.CLEANUP.name()));
            }

            // Subcomponents not necessary for web archives - but might necessary for non-web
            List<String> prefixes = config.getChildSubUnitComponentPrefixes();
            for (ServiceName name : du.getServiceRegistry().getServiceNames())
            {
               for (String prefix : prefixes)
               {
                  if (name.getCanonicalName().startsWith(prefix))
                  {
                     builder.addDependency(name);
                  }
               }
            }

            builder.install();
         }
      }
   }

   @Override
   public void undeploy(DeploymentUnit context)
   {
   }
}
