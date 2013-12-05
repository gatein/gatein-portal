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

package org.gatein.integration.jboss.as7.portal;

import static org.gatein.integration.jboss.as7.portal.PortalResourceConstants.*;
import static org.gatein.integration.jboss.as7.portal.PortalResourceDescriptions.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;

import java.util.EnumSet;
import java.util.Locale;

import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.CommonProviders;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalResourceRegistrar {
    private PortalResourceRegistrar() {
    }

    // Register portal resources with the gatein subsystem.
    public static void registerPortalResources(ManagementResourceRegistration subsystem, boolean isRuntimeOnlyRegistrationValid) {
        EnumSet<OperationEntry.Flag> runtimeOnly = EnumSet.of(OperationEntry.Flag.RUNTIME_ONLY);

        // Register all read operations that should have the child-type portal be populated with the currently installed portal
        // containers and child resources.
        OperationStepHandler handler = subsystem.getOperationHandler(PathAddress.EMPTY_ADDRESS, READ_RESOURCE_OPERATION);
        subsystem.registerOperationHandler(READ_RESOURCE_OPERATION, new PortalReadOperationHandler(handler),
                new DescriptionProvider() {
                    @Override
                    public ModelNode getModelDescription(Locale locale) {
                        return getPortalReadResourceDescription(locale);
                    }
                }, runtimeOnly);

        handler = subsystem.getOperationHandler(PathAddress.EMPTY_ADDRESS, READ_CHILDREN_NAMES_OPERATION);
        subsystem.registerOperationHandler(READ_CHILDREN_NAMES_OPERATION, new PortalReadOperationHandler(handler),
                CommonProviders.READ_CHILDREN_NAMES_PROVIDER, runtimeOnly);

        handler = subsystem.getOperationHandler(PathAddress.EMPTY_ADDRESS, READ_CHILDREN_RESOURCES_OPERATION);
        subsystem.registerOperationHandler(READ_CHILDREN_RESOURCES_OPERATION, new PortalReadOperationHandler(handler),
                CommonProviders.READ_CHILDREN_RESOURCES_PROVIDER, runtimeOnly);

        // /subsystem=gatein/portal=*
        ManagementResourceRegistration portal = subsystem.registerSubModel(PORTAL_PATH, new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(Locale locale) {
                return getPortalResourceDescription(locale);
            }
        });

        // /subsystem=gatein/portal=*/site=*
        ManagementResourceRegistration site = portal.registerSubModel(SITE_PATH, new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(Locale locale) {
                return getSiteResourceDescription(locale);
            }
        });

        if (isRuntimeOnlyRegistrationValid) {
            StatisticsMetricHandler.registerMetrics(SITE, site);
        }

        // /subsystem=gatein/portal=*/application=*
        ManagementResourceRegistration application = portal.registerSubModel(APPLICATION_PATH, new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(Locale locale) {
                return getApplicationResourceDescription(locale);
            }
        });

        if (isRuntimeOnlyRegistrationValid) {
            StatisticsMetricHandler.registerMetrics(APPLICATION, application);
        }
    }
}
