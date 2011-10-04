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
package org.gatein.integration.jboss.as7.support;

import org.gatein.integration.jboss.as7.support.AbstractParsingTest.StringConfigurationPersister;
import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.ModelController.OperationTransactionControl;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceContainer;

import java.util.concurrent.TimeUnit;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Allows access to the service container and the model controller
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class KernelServices
{

   private final ServiceContainer container;
   private final ModelController controller;
   private final StringConfigurationPersister persister;

   KernelServices(ServiceContainer container, ModelController controller, StringConfigurationPersister persister)
   {
      this.container = container;
      this.controller = controller;
      this.persister = persister;
   }

   /**
    * Gets the service container
    *
    * @return the service container
    */
   public ServiceContainer getContainer()
   {
      return container;
   }

   /**
    * Execute an operation in the model controller
    *
    * @param operation the operation to execute
    * @return the result of the operation
    */
   public ModelNode executeOperation(ModelNode operation)
   {
      return controller.execute(operation, null, OperationTransactionControl.COMMIT, null);
   }

   /**
    * Reads the persisted subsystem xml
    *
    * @return the xml
    */
   public String getPersistedSubsystemXml()
   {
      return persister.marshalled;
   }

   /**
    * Reads the whole model from the model controller
    *
    * @return the whole model
    */
   public ModelNode readWholeModel()
   {
      ModelNode op = new ModelNode();
      op.get(OP).set(READ_RESOURCE_OPERATION);
      op.get(OP_ADDR).set(PathAddress.EMPTY_ADDRESS.toModelNode());
      op.get(RECURSIVE).set(true);
      ModelNode result = executeOperation(op);
      return AbstractParsingTest.checkResultAndGetContents(result);
   }

   void shutdown()
   {
      if (container != null)
      {
         container.shutdown();
         try
         {
            container.awaitTermination(5, TimeUnit.SECONDS);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }
   }
}
