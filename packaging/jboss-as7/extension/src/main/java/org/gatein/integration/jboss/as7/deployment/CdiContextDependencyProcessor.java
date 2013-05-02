/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.vfs.VirtualFile;

/**
 * Check whether the deployment is a portlet that is CDI enabled, determined by presence of beans.xml.
 * If it is, then add the module dependency for CDI Contexts.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class CdiContextDependencyProcessor implements DeploymentUnitProcessor {
    private static final String WEB_INF_BEANS_XML = "WEB-INF/beans.xml";

    private static final ModuleIdentifier CDI_CONTEXTS = ModuleIdentifier.create("org.gatein.cdi-contexts");

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return; // Skip non web deployments
        }

        if (!GateInConfiguration.isPortletArchive(deploymentUnit)) {
            return; // Skip non portlet deployments
        }

        ResourceRoot deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        if (null == deploymentRoot) {
            return;
        }

        ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        ModuleLoader moduleLoader = Module.getBootModuleLoader();

        if (DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            // look for WEB-INF/beans.xml
            final VirtualFile rootBeansXml = deploymentRoot.getRoot().getChild(WEB_INF_BEANS_XML);
            if (rootBeansXml.exists() && rootBeansXml.isFile()) {
                ModuleDependency cdiContexts = new ModuleDependency(moduleLoader, CDI_CONTEXTS, false, false, false, false);
                moduleSpecification.addSystemDependency(cdiContexts);
            }
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
