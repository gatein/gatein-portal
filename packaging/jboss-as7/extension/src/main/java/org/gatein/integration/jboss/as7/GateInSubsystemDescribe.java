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

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

import java.util.Locale;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInSubsystemDescribe implements OperationStepHandler, DescriptionProvider
{

   static final GateInSubsystemDescribe INSTANCE = new GateInSubsystemDescribe();

   /**
    * {@inheritDoc}
    */
   @Override
   public void execute(OperationContext context, ModelNode operation) throws OperationFailedException
   {
      final PathAddress rootAddress = PathAddress.pathAddress(PathAddress.pathAddress(operation.require(OP_ADDR)).getLastElement());
      final ModelNode subModel = context.readResource(PathAddress.EMPTY_ADDRESS).getModel();

      final ModelNode subsystemAdd = new ModelNode();
      subsystemAdd.get(OP).set(ADD);
      subsystemAdd.get(OP_ADDR).set(rootAddress.toModelNode());
      if (subModel.hasDefined(Constants.DEPLOYMENT_ARCHIVES))
      {
         subsystemAdd.get(Constants.DEPLOYMENT_ARCHIVES).set(subModel.get(Constants.DEPLOYMENT_ARCHIVES));
      }
      if (subModel.hasDefined(Constants.PORTLET_WAR_DEPENDENCIES))
      {
         subsystemAdd.get(Constants.PORTLET_WAR_DEPENDENCIES).set(subModel.get(Constants.PORTLET_WAR_DEPENDENCIES));
      }
      context.getResult().add(subsystemAdd);

      context.completeStep();
   }

   @Override
   public ModelNode getModelDescription(Locale locale)
   {
      return new ModelNode();
   }
}
