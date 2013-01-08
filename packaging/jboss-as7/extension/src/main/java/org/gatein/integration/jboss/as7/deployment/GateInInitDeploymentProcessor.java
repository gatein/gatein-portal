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
import org.gatein.integration.jboss.as7.GateInExtension;
import org.gatein.integration.jboss.as7.web.InitService;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.logging.Logger;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInInitDeploymentProcessor implements DeploymentUnitProcessor {
    private final Logger log = Logger.getLogger(GateInInitDeploymentProcessor.class);

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit du = phaseContext.getDeploymentUnit();

        if (GateInConfiguration.isGateInArchive(du)) {
            log.info("Module is on GateIn Extension modules list");
            final GateInConfiguration config = du.getAttachment(GateInConfigurationKey.KEY);

            final ServiceName initSvcName = GateInExtension.deploymentUnitName(config.getGateInEarModule(), "gatein", "init");
            final ServiceTarget target = phaseContext.getServiceTarget();

            if (du.getAttachment(GateInEarKey.KEY) != null) {
                // Install InitService with dependency on all the deployment modules reaching POST_MODULE
                // TODO: we are starting up InitService before child modules (jboss.deployment.subunit.*) have gone through
                // POST_MODULE
                final ServiceBuilder<InitService> builder = target.addService(initSvcName, new InitService(config))
                        .addDependency(GateInExtension.deploymentUnitName(config.getGateInEarModule(), Phase.POST_MODULE));

                for (ModuleIdentifier module : config.getGateInExtModules()) {
                    builder.addDependency(GateInExtension.deploymentUnitName(module, Phase.POST_MODULE));
                }
                builder.install();
                log.info("Installed " + initSvcName);
            }
            // all gatein deployment modules use InitService as barrier on POST_MODULE to ensure
            // they are all available on the classpath when init time resource loading takes place
            phaseContext.addToAttachmentList(Attachments.NEXT_PHASE_DEPS, initSvcName);
            log.info("Added NEXT_PHASE_DEP on " + initSvcName);
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
