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

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class SetGateInArchivesHandler extends AbstractAddStepHandler implements DescriptionProvider
{
   public static final SetGateInArchivesHandler INSTANCE = new SetGateInArchivesHandler();

   private SetGateInArchivesHandler()
   {
   }

   @Override
   protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException
   {

   }

   public ModelNode getModelDescription(Locale locale)
   {
      final ResourceBundle bundle = GateInSubsystemDescriptions.getResourceBundle(locale);
      ModelNode node = new ModelNode();
      node.get(DESCRIPTION).set(bundle.getString("gatein.set-deployment-archives.description"));
      node.get(REQUEST_PROPERTIES, "services", DESCRIPTION).set(bundle.getString("gatein.deployment-archives.archive.services.description"));
      node.get(REQUEST_PROPERTIES, "services", TYPE).set(ModelType.UNDEFINED);
      node.get(REQUEST_PROPERTIES, "services", REQUIRED).set(false);
      return node;
   }
}
