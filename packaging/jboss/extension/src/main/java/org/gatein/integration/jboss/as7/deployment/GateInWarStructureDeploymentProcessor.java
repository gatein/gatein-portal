package org.gatein.integration.jboss.as7.deployment;

import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.metadata.web.spec.CookieConfigMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;
import org.jboss.metadata.web.spec.WebMetaData;

/**
 * This deployment processor adds to the sharedWarMetaData for all web (war) applications. For example it's used to set the
 * cookie path to "/" of the session-config.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class GateInWarStructureDeploymentProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return;
        }
        final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null) {
            return;
        }
        final WebMetaData sharedWebMetaData = warMetaData.getSharedWebMetaData();
        if (sharedWebMetaData == null) {
            return;
        }

        // Set cookie-path to "/" for all web applications
        SessionConfigMetaData sessionConfig = sharedWebMetaData.getSessionConfig();
        if (sessionConfig == null) {
            sessionConfig = new SessionConfigMetaData();
            warMetaData.getWebMetaData().setSessionConfig(sessionConfig);
        }
        CookieConfigMetaData cookieConfig = sessionConfig.getCookieConfig();
        if (cookieConfig == null) {
            cookieConfig = new CookieConfigMetaData();
            sessionConfig.setCookieConfig(cookieConfig);
        }
        cookieConfig.setPath("/");
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
