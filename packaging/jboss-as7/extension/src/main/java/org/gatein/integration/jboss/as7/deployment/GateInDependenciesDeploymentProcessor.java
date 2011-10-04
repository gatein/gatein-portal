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
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

import java.util.List;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInDependenciesDeploymentProcessor implements DeploymentUnitProcessor
{

   final ModuleIdentifier gateInLibId = ModuleIdentifier.fromString("org.gatein.lib");

   private GateInExtension extension;
   private GateInExtensionConfiguration config;

   public GateInDependenciesDeploymentProcessor(GateInExtension extension)
   {
      this.extension = extension;
      this.config = extension.getConfiguration();
   }

   public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException
   {
      final DeploymentUnit du = phaseContext.getDeploymentUnit();

      if (du.getAttachment(GateInEarKey.KEY) != null
            || du.getAttachment(GateInExtKey.KEY) != null
            || du.getAttachment(PortletWarKey.INSTANCE) != null)
      {
         // add dependency on org.gatein.lib
         List<ModuleDependency> dependencies = du.getAttachmentList(Attachments.MANIFEST_DEPENDENCIES);

         if (!containsDependency(dependencies, gateInLibId))
         {
            du.addToAttachmentList(Attachments.MANIFEST_DEPENDENCIES,
                  new ModuleDependency(Module.getBootModuleLoader(), gateInLibId, false, false, true));
         }

         // add gatein deployment modules cross-dependencies
         ModuleIdentifier moduleId = du.getAttachment(Attachments.MODULE_IDENTIFIER);

         if (du.getAttachment(GateInEarKey.KEY) != null
               || du.getAttachment(GateInExtKey.KEY) != null)
         {
            final ServiceModuleLoader deploymentModuleLoader = du.getAttachment(Attachments.SERVICE_MODULE_LOADER);

            if (!moduleId.equals(config.getGateInEarModule()))
            {
               if (!containsDependency(dependencies, config.getGateInEarModule()))
               {
                  du.addToAttachmentList(Attachments.MANIFEST_DEPENDENCIES,
                        new ModuleDependency(deploymentModuleLoader, config.getGateInEarModule(), false, false, false));
               }
            }

            for (ModuleIdentifier id : config.getGateInExtModules())
            {
               if (!moduleId.equals(id))
               {
                  if (!containsDependency(dependencies, id))
                  {
                     du.addToAttachmentList(Attachments.MANIFEST_DEPENDENCIES,
                           new ModuleDependency(deploymentModuleLoader, id, false, false, false));
                  }
               }
            }
         }
      }
   }

   private boolean containsDependency(List<ModuleDependency> dependencies, ModuleIdentifier moduleId)
   {
      boolean exists = false;
      for (ModuleDependency dep : dependencies)
      {
         if (dep.getIdentifier().equals(moduleId))
         {
            exists = true;
            break;
         }
      }
      return exists;
   }

   public void undeploy(DeploymentUnit context)
   {
   }
}
