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
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Services;
import org.jboss.logging.Logger;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;

/** @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a> */
public class GateInStructureDeploymentProcessor implements DeploymentUnitProcessor {
    private static Logger log = Logger.getLogger(GateInStructureDeploymentProcessor.class);

    private static final ServiceListener listener = new AbstractServiceListener() {

        @Override
        public void transition(ServiceController serviceController, ServiceController.Transition transition) {
            switch (transition) {
                case STARTING_to_UP:
                case STOP_REQUESTED_to_UP:
                    log.trace("Service started: " + serviceController.getName() + " [" + transition + "]");
                    break;
                case REMOVING_to_DOWN:
                case START_FAILED_to_DOWN:
                case START_REQUESTED_to_DOWN:
                case STOPPING_to_DOWN:
                case WAITING_to_DOWN:
                case WONT_START_to_DOWN:
                    log.trace("Service stopped: " + serviceController.getName() + " [" + transition + "]");
                    break;
            }
        }
    };

    private GateInConfiguration config;

    public GateInStructureDeploymentProcessor(GateInConfiguration config) {
        this.config = config;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit du = phaseContext.getDeploymentUnit();
        log.debug("Deploy: " + du.getName() + " [" + phaseContext.getPhase() + "]");
        if (du.getParent() != null) {
            processChildDeployment(phaseContext);
        } else {
            final ModuleIdentifier moduleId = du.getAttachment(Attachments.MODULE_IDENTIFIER);
            if (config.getGateInEarModule().equals(moduleId)) {
                log.debugf("Recognized %s as main GateIn deployment archive", moduleId);
                du.putAttachment(GateInEarKey.KEY, GateInEarKey.INSTANCE);
                du.putAttachment(GateInConfigurationKey.KEY, config);
                if (log.isTraceEnabled()) {
                    installListener(phaseContext, moduleId);
                }
            } else if (config.getGateInExtModules().contains(moduleId)) {
                log.debugf("Recognized %s as part of GateIn deployment", moduleId);
                du.putAttachment(GateInExtKey.KEY, GateInExtKey.INSTANCE);
                du.putAttachment(GateInConfigurationKey.KEY, config);

                if (log.isTraceEnabled()) {
                    installListener(phaseContext, moduleId);
                }
            }
        }
    }

    private void processChildDeployment(DeploymentPhaseContext phaseContext) {
        DeploymentUnit du = phaseContext.getDeploymentUnit();
        DeploymentUnit parent = du.getParent();
        if (GateInConfiguration.isGateInArchive(parent)) {
            du.putAttachment(GateInConfigurationKey.KEY, config);
            config.addChildSubUnit(Services.deploymentUnitName(parent.getName(), du.getName()));
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

    void installListener(DeploymentPhaseContext phaseContext, ModuleIdentifier moduleId) {
        ServiceName serviceName = Services.deploymentUnitName(GateInExtension.skipModuleLoaderPrefix(moduleId.getName()));

        ServiceController<?> svcc = phaseContext.getServiceRegistry().getService(serviceName);
        svcc.addListener(ServiceListener.Inheritance.ALL, listener);
    }
}
