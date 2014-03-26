package org.gatein.integration.jboss.as7.deployment;

import org.gatein.cdi.contexts.listeners.CDIServletListener;
import org.gatein.integration.jboss.as7.GateInConfiguration;
import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.EEApplicationClasses;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.as.web.deployment.WebAttachments;
import org.jboss.as.web.deployment.component.ComponentInstantiator;
import org.jboss.as.web.deployment.component.WebComponentDescription;
import org.jboss.as.web.deployment.component.WebComponentInstantiator;
import org.jboss.as.weld.WeldDeploymentMarker;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Adds #{CDIServletListener} into the list of servlet listeners, if not already found,
 * and adds the listener as a component to allow injection to be performed on it.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class CdiWebIntegrationProcessor implements DeploymentUnitProcessor {
    private static final Logger log = Logger.getLogger(CdiWebIntegrationProcessor.class);

    private static final String CDI_LISTENER = CDIServletListener.class.getName();

    private final ListenerMetaData cdiListener;

    public CdiWebIntegrationProcessor() {
        cdiListener = new ListenerMetaData();
        cdiListener.setListenerClass(CDI_LISTENER);
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final EEModuleDescription module = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
        final EEApplicationClasses applicationClasses = deploymentUnit.getAttachment(Attachments.EE_APPLICATION_CLASSES_DESCRIPTION);

        if (!GateInConfiguration.isPortletArchive(deploymentUnit)) {
            return;
        }

        if (!WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
            return; // skip non weld deployments
        }

        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null) {
            log.debug("Not installing GateIn CDI web tier integration as no war metadata found");
            return;
        }
        JBossWebMetaData webMetaData = warMetaData.getMergedJBossWebMetaData();
        if (webMetaData == null) {
            log.debug("Not installing GateIn CDI web tier integration as no merged web metadata found");
            return;
        }

        List<ListenerMetaData> listeners = webMetaData.getListeners();
        if (listeners == null) {
            listeners = new ArrayList<ListenerMetaData>();
            webMetaData.setListeners(listeners);
        } else {
            //if the cdi servlet listener is present remove it
            //this should allow wars to be portable between AS7 and servlet containers
            final ListIterator<ListenerMetaData> iterator = listeners.listIterator();
            while (iterator.hasNext()) {
                final ListenerMetaData listener = iterator.next();
                if (listener.getListenerClass().trim().equals(CDI_LISTENER)) {
                    log.debugf("Removing cdi servlet listener %s from web config, as it is not needed in EE6 environments", CDI_LISTENER);
                    iterator.remove();
                    break;
                }
            }
        }
        listeners.add(0, cdiListener);

        // This uses resource injection, so it needs to be a component
        final WebComponentDescription componentDescription = new WebComponentDescription(CDI_LISTENER, CDI_LISTENER, module, deploymentUnit.getServiceName(), applicationClasses);
        module.addComponent(componentDescription);
        final Map<String, ComponentInstantiator> instantiators = deploymentUnit.getAttachment(WebAttachments.WEB_COMPONENT_INSTANTIATORS);
        instantiators.put(CDI_LISTENER, new WebComponentInstantiator(deploymentUnit, componentDescription));
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
