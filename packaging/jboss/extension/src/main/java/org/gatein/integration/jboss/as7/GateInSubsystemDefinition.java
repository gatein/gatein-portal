/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import java.util.EnumSet;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.DefaultResourceAddDescriptionProvider;
import org.jboss.as.controller.descriptions.DefaultResourceRemoveDescriptionProvider;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;

/**
 * @author Tomaz Cerar
 */
public class GateInSubsystemDefinition extends SimpleResourceDefinition {
    private GateInConfiguration config;

    GateInSubsystemDefinition(GateInConfiguration config) {
        super(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, GateInExtension.SUBSYSTEM_NAME), GateInExtension
                .getResourceDescriptionResolver(GateInExtension.SUBSYSTEM_NAME));
        this.config = config;
    }

    @Override
    public void registerOperations(final ManagementResourceRegistration registration) {
        final GateInSubsystemAdd subsystemAdd = new GateInSubsystemAdd(config);
        final ResourceDescriptionResolver rootResolver = getResourceDescriptionResolver();
        final DescriptionProvider subsystemAddDescription = new DefaultResourceAddDescriptionProvider(registration,
                rootResolver);
        registration.registerOperationHandler(ADD, subsystemAdd, subsystemAddDescription,
                EnumSet.of(OperationEntry.Flag.RESTART_ALL_SERVICES));
        registration.registerOperationHandler(REMOVE, ReloadRequiredRemoveStepHandler.INSTANCE,
                new DefaultResourceRemoveDescriptionProvider(rootResolver),
                EnumSet.of(OperationEntry.Flag.RESTART_ALL_SERVICES));
    }
}
