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

import org.gatein.integration.jboss.as7.deployment.GateInDependenciesDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInInitDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInStarterDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.GateInStructureDeploymentProcessor;
import org.gatein.integration.jboss.as7.deployment.PortletWarClassloadingDependencyProcessor;
import org.gatein.integration.jboss.as7.deployment.PortletWarDeploymentInitializingProcessor;
import org.gatein.integration.jboss.as7.deployment.WarDependenciesDeploymentProcessor;
import org.gatein.wci.jboss.GateInWCIService;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.web.WebServer;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;

import java.util.List;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInSubsystemAdd extends AbstractBoottimeAddStepHandler
{

   static final int STRUCTURE_PORTLET_WAR_DEPLOYMENT_INIT = 0x0B80;
   static final int DEPENDENCIES_PORTLET_MODULE = 0x1100;
   static final int STRUCTURE_GATEIN = 0x2000;
   static final int POST_MODULE_GATEIN_INIT = 0x2000;
   static final int INSTALL_GATEIN_CHILD_WARS = 0x4000;
   static final int INSTALL_GATEIN_START = 0x4000;
   static final int MANIFEST_DEPENDENCIES_GATEIN = 0x4000;
   static final int CLEANUP_ATTACHMENTS = 0x4000;

   private GateInConfiguration config;

   protected GateInSubsystemAdd(GateInConfiguration config)
   {
      this.config = config;
   }

   protected void populateModel(ModelNode operation, ModelNode model)
   {
      // DO NOTHING
   }

   protected void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model,
                                  final ServiceVerificationHandler verificationHandler,
                                  final List<ServiceController<?>> newControllers) throws OperationFailedException
   {
      context.addStep(new AbstractDeploymentChainStep()
      {
         protected void execute(DeploymentProcessorTarget processorTarget)
         {
            final SharedPortletTldsMetaDataBuilder tldsBuilder = new SharedPortletTldsMetaDataBuilder();

            // if 'gatein' deployment scanner is set up, use it
            DeploymentDirHandler.handleDeploymentDir(context, config);

            processorTarget.addDeploymentProcessor(Phase.STRUCTURE, STRUCTURE_GATEIN, new GateInStructureDeploymentProcessor(config));
            processorTarget.addDeploymentProcessor(Phase.PARSE, STRUCTURE_PORTLET_WAR_DEPLOYMENT_INIT, new PortletWarDeploymentInitializingProcessor(config));
            processorTarget.addDeploymentProcessor(Phase.PARSE, MANIFEST_DEPENDENCIES_GATEIN, new GateInDependenciesDeploymentProcessor());
            processorTarget.addDeploymentProcessor(Phase.PARSE, INSTALL_GATEIN_CHILD_WARS, new WarDependenciesDeploymentProcessor());

            processorTarget.addDeploymentProcessor(Phase.DEPENDENCIES, DEPENDENCIES_PORTLET_MODULE, new PortletWarClassloadingDependencyProcessor(tldsBuilder.create()));
            processorTarget.addDeploymentProcessor(Phase.POST_MODULE, POST_MODULE_GATEIN_INIT, new GateInInitDeploymentProcessor());
            processorTarget.addDeploymentProcessor(Phase.INSTALL, INSTALL_GATEIN_START, new GateInStarterDeploymentProcessor());
            processorTarget.addDeploymentProcessor(Phase.CLEANUP, CLEANUP_ATTACHMENTS, new GateInCleanupDeploymentProcessor());
         }
      }, OperationContext.Stage.RUNTIME);
   }

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                                 ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException
   {
      super.performRuntime(context, operation, model, verificationHandler, newControllers);

      final GateInWCIService service = new GateInWCIService();
      final ServiceBuilder<GateInWCIService> serviceBuilder = context.getServiceTarget().addService(GateInWCIService.NAME, service)
         .addDependency(WebSubsystemServices.JBOSS_WEB, WebServer.class, service.getWebServer())
         .addListener(verificationHandler)
         .setInitialMode(ServiceController.Mode.ACTIVE);
      newControllers.add(serviceBuilder.install());
   }

   protected boolean requiresRuntimeVerification()
   {
      return false;
   }
}
