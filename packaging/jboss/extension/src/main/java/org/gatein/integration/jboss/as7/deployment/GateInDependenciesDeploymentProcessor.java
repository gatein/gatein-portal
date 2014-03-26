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

import org.gatein.integration.jboss.as7.GateInConfiguration;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInDependenciesDeploymentProcessor implements DeploymentUnitProcessor {

    private static final ModuleDependency GATEIN_LIB;
    private static final ModuleDependency GATEIN_WCI;

    static {
        ModuleLoader loader = Module.getBootModuleLoader();

        GATEIN_LIB = new ModuleDependency(loader, ModuleIdentifier.fromString("org.gatein.lib"), false, false, true, false);
        GATEIN_WCI = new ModuleDependency(loader, ModuleIdentifier.fromString("org.gatein.wci"), false, false, true, false);
    }

    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit du = phaseContext.getDeploymentUnit();

        if (GateInConfiguration.isGateInArchiveOrSubDeployment(du) || GateInConfiguration.isPortletArchive(du)) {
            // add dependency on org.gatein.lib
            ModuleSpecification moduleSpec = du.getAttachment(Attachments.MODULE_SPECIFICATION);
            moduleSpec.addSystemDependency(GATEIN_LIB);
            moduleSpec.addSystemDependency(GATEIN_WCI);

            // add gatein deployment modules cross-dependencies
            ModuleIdentifier moduleId = du.getAttachment(Attachments.MODULE_IDENTIFIER);

            if (GateInConfiguration.isGateInArchive(du)) {
                final GateInConfiguration config = du.getAttachment(GateInConfigurationKey.KEY);
                final ServiceModuleLoader deploymentModuleLoader = du.getAttachment(Attachments.SERVICE_MODULE_LOADER);

                if (!moduleId.equals(config.getGateInEarModule())) {
                    moduleSpec.addSystemDependency(new ModuleDependency(deploymentModuleLoader,
                        config.getGateInEarModule(), false, false, false, false));
                }

                for (ModuleIdentifier id : config.getGateInExtModules()) {
                    if (!moduleId.equals(id)) {
                        moduleSpec.addSystemDependency(new ModuleDependency(deploymentModuleLoader,
                            id, false, false, false, false));
                    }
                }
            }
        }
    }

    public void undeploy(DeploymentUnit context) {
    }
}
