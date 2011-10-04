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
import org.gatein.integration.jboss.as7.deployment.PortletWarDeploymentInitializingProcessor;
import org.gatein.integration.jboss.as7.deployment.WarDependenciesDeploymentProcessor;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInSubsystemAdd extends AbstractBoottimeAddStepHandler
{
   static final DescriptionProvider DESCRIPTION = new DescriptionProvider()
   {
      public ModelNode getModelDescription(Locale locale)
      {
         return GateInSubsystemDescriptions.getSubsystemAddDescription(locale);
      }
   };


   static final int STRUCTURE_PORTLET_WAR_DEPLOYMENT_INIT = 0x0B80;
   static final int DEPENDENCIES_PORTLET_MODULE = 0x1100;
   static final int STRUCTURE_GATEIN = 0x2000;
   static final int POST_MODULE_GATEIN_INIT = 0x2000;
   static final int INSTALL_GATEIN_CHILD_WARS = 0x4000;
   static final int INSTALL_GATEIN_START = 0x4000;
   static final int MANIFEST_DEPENDENCIES_GATEIN = 0x4000;

   private GateInExtension extension;

   public GateInSubsystemAdd(GateInExtension extension)
   {
      this.extension = extension;
   }

   protected void populateModel(ModelNode operation, ModelNode model)
   {
      //Initialize the 'type' child node
      if (operation.hasDefined(Constants.DEPLOYMENT_ARCHIVES))
      {
         model.get(Constants.DEPLOYMENT_ARCHIVES).set(operation.get(Constants.DEPLOYMENT_ARCHIVES));
      }
      if (operation.hasDefined(Constants.PORTLET_WAR_DEPENDENCIES))
      {
         model.get(Constants.PORTLET_WAR_DEPENDENCIES).set(operation.get(Constants.PORTLET_WAR_DEPENDENCIES));
      }
      // Can we read extension configuration here?
   }

   protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
                                  ServiceVerificationHandler verificationHandler,
                                  List<ServiceController<?>> newControllers) throws OperationFailedException
   {
      context.addStep(new AbstractDeploymentChainStep()
      {
         protected void execute(DeploymentProcessorTarget processorTarget)
         {
            final SharedPortletTldsMetaDataBuilder tldsBuilder = new SharedPortletTldsMetaDataBuilder();

            processorTarget.addDeploymentProcessor(Phase.STRUCTURE, STRUCTURE_GATEIN, new GateInStructureDeploymentProcessor(extension));
            processorTarget.addDeploymentProcessor(Phase.PARSE, STRUCTURE_PORTLET_WAR_DEPLOYMENT_INIT, new PortletWarDeploymentInitializingProcessor(extension));
            processorTarget.addDeploymentProcessor(Phase.PARSE, MANIFEST_DEPENDENCIES_GATEIN, new GateInDependenciesDeploymentProcessor(extension));
            processorTarget.addDeploymentProcessor(Phase.PARSE, INSTALL_GATEIN_CHILD_WARS, new WarDependenciesDeploymentProcessor(extension));

            //processorTarget.addDeploymentProcessor(Phase.DEPENDENCIES, DEPENDENCIES_PORTLET_MODULE,
            //      new PortletWarClassloadingDependencyProcessor(tldsBuilder.create()));
            processorTarget.addDeploymentProcessor(Phase.POST_MODULE, POST_MODULE_GATEIN_INIT, new GateInInitDeploymentProcessor(extension));
            processorTarget.addDeploymentProcessor(Phase.INSTALL, INSTALL_GATEIN_START, new GateInStarterDeploymentProcessor(extension));
         }
      }, OperationContext.Stage.RUNTIME);
   }

   protected boolean requiresRuntimeVerification()
   {
      return false;
   }
}
