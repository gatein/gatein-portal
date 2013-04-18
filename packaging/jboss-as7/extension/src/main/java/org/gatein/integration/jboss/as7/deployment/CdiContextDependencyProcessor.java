package org.gatein.integration.jboss.as7.deployment;

import org.gatein.cdi.contexts.CDIServletListener;
import org.gatein.integration.jboss.as7.GateInConfiguration;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.as.weld.WeldDeploymentMarker;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.jar.Manifest;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class CdiContextDependencyProcessor implements DeploymentUnitProcessor {
    private static final Logger log = Logger.getLogger(CdiContextDependencyProcessor.class);

    private static final ModuleIdentifier CDI_CONTEXTS = ModuleIdentifier.create("org.gatein.cdi-contexts");

    private static final String CDI_LISTENER = CDIServletListener.class.getName();

    private static final String cdiContextsVersion = getCdiContextsVersion();

    private static String getCdiContextsVersion() {
        try {
            Module module = Module.getBootModuleLoader().loadModule(CDI_CONTEXTS);
            Manifest mf = new Manifest(module.getClassLoader().getResourceAsStream("/META-INF/MANIFEST.MF"));
            return mf.getMainAttributes().getValue("Implementation-Version");
        } catch (Exception e) {
            return "";
        }
    }

    private final ListenerMetaData cdiListener;

    public CdiContextDependencyProcessor() {
        cdiListener = new ListenerMetaData();
        cdiListener.setListenerClass(CDI_LISTENER);
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (!GateInConfiguration.isPortletArchive(deploymentUnit)) {
            return;
        }

        if (!WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
            return; // skip non weld deployments
        }

        ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        ModuleLoader moduleLoader = Module.getBootModuleLoader();

        log.infof("Adding CDI Contexts Extension %s to \"%s\"", cdiContextsVersion, deploymentUnit.getName());

        addCdiContexts(moduleSpecification, moduleLoader);

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
    }

    private void addCdiContexts(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader) {
        ModuleDependency cdiContexts = new ModuleDependency(moduleLoader, CDI_CONTEXTS, false, false, true, false);
        moduleSpecification.addSystemDependency(cdiContexts);
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
