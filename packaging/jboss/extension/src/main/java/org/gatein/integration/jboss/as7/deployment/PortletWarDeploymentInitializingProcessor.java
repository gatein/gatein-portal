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
package org.gatein.integration.jboss.as7.deployment;

import org.gatein.integration.jboss.as7.GateInConfiguration;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VirtualFile;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class PortletWarDeploymentInitializingProcessor implements DeploymentUnitProcessor {
    private static final String PORTLET_XML = "WEB-INF/portlet.xml";

    private GateInConfiguration config;

    public PortletWarDeploymentInitializingProcessor(GateInConfiguration config) {
        this.config = config;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit du = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, du)) {
            return; // Skip non web deployments
        }
        final ResourceRoot deploymentRoot = du.getAttachment(Attachments.DEPLOYMENT_ROOT);
        final VirtualFile portletXml = deploymentRoot.getRoot().getChild(PORTLET_XML);
        if (portletXml.exists()) {
            // we don't care about the contents, its existence is enough to mark it as a portlet app
            du.putAttachment(PortletWarKey.INSTANCE, Boolean.TRUE);
            du.putAttachment(GateInConfigurationKey.KEY, config);
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
