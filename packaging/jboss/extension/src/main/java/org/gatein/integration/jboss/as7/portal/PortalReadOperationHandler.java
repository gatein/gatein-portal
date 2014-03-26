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

import static org.gatein.integration.jboss.as7.portal.PortalResourceConstants.PORTAL;

import org.gatein.integration.jboss.as7.portal.resources.PortalRuntimeResource;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 * Responsible for ensuring custom resource has been added prior to reading the gatein subsystem.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalReadOperationHandler implements OperationStepHandler {
    private final OperationStepHandler delegate;

    public PortalReadOperationHandler(OperationStepHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final Resource resource = context.readResource(PathAddress.EMPTY_ADDRESS);

        // This step will ensure that we have added our custom PortalRuntimeResource before any read operation has happened.
        context.addStep(new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
                if (!resource.hasChildren(PORTAL)) {
                    synchronized (resource) {
                        if (!resource.hasChildren(PORTAL)) {
                            ServiceController<?> controller = context.getServiceRegistry(false).getService(
                                    GateInRuntimeService.SERVICE_NAME);
                            if (controller != null) {
                                GateInContext gateInContext = (GateInContext) controller.getValue();
                                for (String name : gateInContext.getPortalNames()) {
                                    PathElement pathElement = PathElement.pathElement(PORTAL, name);
                                    context.addResource(PathAddress.pathAddress(pathElement), new PortalRuntimeResource(
                                            pathElement));
                                }
                            }
                        }
                    }
                }
                context.completeStep();
            }
        }, OperationContext.Stage.MODEL);

        context.addStep(delegate, OperationContext.Stage.MODEL);

        context.completeStep();
    }
}
