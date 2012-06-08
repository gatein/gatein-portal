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

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.Services;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.logging.Logger;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.ServiceName;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInExtension implements Extension
{
   private static final Logger log = Logger.getLogger("org.gatein");

   /**
    * The name space used for the {@code substystem} element
    */
   public static final String NAMESPACE = "urn:jboss:domain:gatein:1.0";

   public static final String SUBSYSTEM_NAME = "gatein";

   protected static final String DEFAULT_PORTAL_NAME = "default";

   private static final String RESOURCE_NAME = GateInExtension.class.getPackage().getName() + ".LocalDescriptions";

   static ResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix)
   {
      return new StandardResourceDescriptionResolver(keyPrefix, RESOURCE_NAME, GateInExtension.class.getClassLoader(), true, false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize(ExtensionContext context)
   {
      log.debug("Activating GateIn Extension");
      final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
      final GateInConfiguration config = new GateInConfiguration();
      final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(new GateInSubsystemDefinition(config));
      registration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE, GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);

      registration.registerSubModel(new DeploymentArchiveDefinition(config));
      registration.registerSubModel(new PortletWarDependencyDefinition(config));
      registration.registerSubModel(new GateInPortalDefinition(config, DEFAULT_PORTAL_NAME));

      subsystem.registerXMLElementWriter(GateInSubsystemParser.getInstance());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void initializeParsers(ExtensionParsingContext context)
   {
      context.setSubsystemXmlMapping(NAMESPACE, GateInSubsystemParser.getInstance());
   }

   public static String skipModuleLoaderPrefix(String name)
   {
      if (name.startsWith(ServiceModuleLoader.MODULE_PREFIX))
      {
         return name.substring(ServiceModuleLoader.MODULE_PREFIX.length());
      }

      return name;
   }

   public static ServiceName deploymentUnitName(ModuleIdentifier moduleId, Phase phase)
   {
      return ServiceName.of(Services.deploymentUnitName(
         GateInExtension.skipModuleLoaderPrefix(moduleId.getName())), phase.name());
   }

   public static ServiceName deploymentUnitName(ModuleIdentifier moduleId, String... postfix)
   {
      return ServiceName.of(Services.deploymentUnitName(
         GateInExtension.skipModuleLoaderPrefix(moduleId.getName())), postfix);
   }
}
