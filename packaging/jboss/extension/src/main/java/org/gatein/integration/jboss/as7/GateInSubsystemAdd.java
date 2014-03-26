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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import org.gatein.integration.jboss.as7.deployment.CdiContextDependencyProcessor;
import org.gatein.integration.jboss.as7.deployment.CdiWebIntegrationProcessor;
import org.gatein.integration.jboss.as7.deployment.CdiContextExtensionProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInWarStructureDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.DeploymentScannerService;
import org.gatein.integration.jboss.as7.deployment.GateInCleanupDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInDependenciesDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInInitDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInStarterDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInStructureDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInTransactionsFixProcessor;
import org.gatein.integration.jboss.as7.deployment.PortletBridgeDependencyProcessor;
import org.gatein.integration.jboss.as7.deployment.PortletWarClassloadingDependencyProcessor;
import org.gatein.integration.jboss.as7.deployment.PortletWarDeploymentInitializingProcessor;
import org.gatein.integration.jboss.as7.deployment.WarDependenciesDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.wsrp.WSRPPostModuleDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.wsrp.WSRPStructureDeploymentProcessor;
import org.gatein.wci.jboss.GateInWCIService;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.web.WebServer;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInSubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final int STRUCTURE_PORTLET_WAR_DEPLOYMENT_INIT = 0x0801;
    static final int DEPENDENCIES_PORTLET_MODULE = 0x1100;
    static final int DEPENDENCIES_PORTLET_BRIDGE_MODULE = 0x2300;
    static final int PARSE_CDI_WEB_INTEGRATION = 0x2B11;
    static final int STRUCTURE_CDI_CONTEXT_DEPENDENCY = 0x0802;
    static final int STRUCTURE_WSRP = 0x2001;
    static final int STRUCTURE_GATEIN = 0x2000;
    static final int POST_MODULE_GATEIN_INIT = 0x2000;
    static final int POST_MODULE_CDI_CONTEXT_EXTENSION = 0x1000;
    static final int INSTALL_GATEIN_CHILD_WARS = 0x4000;
    static final int INSTALL_GATEIN_START = 0x4000;
    static final int MANIFEST_DEPENDENCIES_GATEIN = 0x4000;
    static final int CLEANUP_ATTACHMENTS = 0x4000;
    static final int GATEIN_TRANSACTIONS_FIX = 0x4000;

    private GateInConfiguration config;

    protected GateInSubsystemAdd(GateInConfiguration config) {
        this.config = config;
    }

    protected void populateModel(final OperationContext context, final ModelNode operation, final Resource resource)
            throws OperationFailedException {
        if (requiresRuntime(context)) {
            final DeploymentScannerService scannerService = new DeploymentScannerService(config);
            ModelNode op = scannerService.prepareDeploymentModel();

            final ModelNode result = new ModelNode();
            final PathAddress opPath = PathAddress.pathAddress(op.get(OP_ADDR));
            final OperationStepHandler handler = context.getRootResourceRegistration().getOperationHandler(opPath,
                    op.get(OP).asString());
            context.addStep(result, op, handler, OperationContext.Stage.MODEL);
        }
    }

    protected void populateModel(ModelNode operation, ModelNode model) {
        // DO NOTHING
    }

    protected void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model,
            final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget processorTarget) {
                final SharedPortletTldsMetaDataBuilder tldsBuilder = new SharedPortletTldsMetaDataBuilder();

                processorTarget.addDeploymentProcessor(Phase.STRUCTURE, STRUCTURE_GATEIN,
                        new GateInWarStructureDeploymentProcessor());
                processorTarget.addDeploymentProcessor(Phase.STRUCTURE, STRUCTURE_GATEIN,
                        new GateInStructureDeploymentProcessor(config));
                processorTarget.addDeploymentProcessor(Phase.STRUCTURE, STRUCTURE_WSRP, new WSRPStructureDeploymentProcessor());
                processorTarget.addDeploymentProcessor(Phase.STRUCTURE, STRUCTURE_PORTLET_WAR_DEPLOYMENT_INIT,
                        new PortletWarDeploymentInitializingProcessor(config));
                processorTarget.addDeploymentProcessor(Phase.STRUCTURE, STRUCTURE_CDI_CONTEXT_DEPENDENCY,
                        new CdiContextDependencyProcessor());

                processorTarget.addDeploymentProcessor(Phase.PARSE, PARSE_CDI_WEB_INTEGRATION,
                        new CdiWebIntegrationProcessor());
                processorTarget.addDeploymentProcessor(Phase.PARSE, MANIFEST_DEPENDENCIES_GATEIN,
                        new GateInDependenciesDeploymentProcessor());
                processorTarget.addDeploymentProcessor(Phase.PARSE, INSTALL_GATEIN_CHILD_WARS,
                        new WarDependenciesDeploymentProcessor());
                processorTarget.addDeploymentProcessor(Phase.PARSE, GATEIN_TRANSACTIONS_FIX,
                        new GateInTransactionsFixProcessor());

                processorTarget.addDeploymentProcessor(Phase.DEPENDENCIES, DEPENDENCIES_PORTLET_MODULE,
                        new PortletWarClassloadingDependencyProcessor(tldsBuilder.create()));
                processorTarget.addDeploymentProcessor(Phase.DEPENDENCIES, DEPENDENCIES_PORTLET_BRIDGE_MODULE,
                        new PortletBridgeDependencyProcessor());

                processorTarget.addDeploymentProcessor(Phase.POST_MODULE, POST_MODULE_GATEIN_INIT,
                        new WSRPPostModuleDeploymentProcessor());
                processorTarget.addDeploymentProcessor(Phase.POST_MODULE, POST_MODULE_GATEIN_INIT,
                        new GateInInitDeploymentProcessor());
                processorTarget.addDeploymentProcessor(Phase.POST_MODULE, POST_MODULE_CDI_CONTEXT_EXTENSION,
                        new CdiContextExtensionProcessor());
                processorTarget.addDeploymentProcessor(Phase.INSTALL, INSTALL_GATEIN_START,
                        new GateInStarterDeploymentProcessor());
                processorTarget.addDeploymentProcessor(Phase.CLEANUP, CLEANUP_ATTACHMENTS,
                        new GateInCleanupDeploymentProcessor());
            }
        }, OperationContext.Stage.RUNTIME);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        super.performRuntime(context, operation, model, verificationHandler, newControllers);

        final GateInWCIService wciService = new GateInWCIService();
        final ServiceBuilder<GateInWCIService> serviceBuilder = context.getServiceTarget()
                .addService(GateInWCIService.NAME, wciService)
                .addDependency(WebSubsystemServices.JBOSS_WEB, WebServer.class, wciService.getWebServer())
                .addListener(verificationHandler).setInitialMode(ServiceController.Mode.ACTIVE);
        newControllers.add(serviceBuilder.install());
    }

    protected boolean requiresRuntimeVerification() {
        return false;
    }
}
