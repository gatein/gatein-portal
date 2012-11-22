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
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class WarDependenciesDeploymentProcessor implements DeploymentUnitProcessor {

    private void processWarDeployment(DeploymentUnit du) {
        WarMetaData warMetaData = du.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null)
            return;
        final JBossWebMetaData metaData = warMetaData.getMergedJBossWebMetaData();

        String pathName;
        if (metaData.getContextRoot() == null) {
            pathName = "/" + du.getName().substring(0, du.getName().length() - 4);
        } else {
            pathName = metaData.getContextRoot();
            if ("/".equals(pathName)) {
                pathName = "";
            } else if (pathName.length() > 0 && pathName.charAt(0) != '/') {
                pathName = "/" + pathName;
            }
        }

        GateInConfiguration config = du.getAttachment(GateInConfigurationKey.KEY);
        ServiceName deploymentServiceName = WebSubsystemServices.deploymentServiceName("default-host", pathName);

        config.addChildWar(deploymentServiceName);
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit du = phaseContext.getDeploymentUnit();
        DeploymentUnit parent = du.getParent();

        if (parent != null) {
            if (GateInConfiguration.isGateInArchive(parent)) {
                processWarDeployment(du);
            }
        } else if (GateInConfiguration.isGateInArchive(du)) {
            processWarDeployment(du);
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
