package org.gatein.integration.jboss.as7.deployment;

import org.gatein.cdi.CDIPortletContextExtension;
import org.gatein.integration.jboss.as7.GateInConfiguration;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.reflect.DeploymentReflectionIndex;
import org.jboss.as.weld.WeldDeploymentMarker;
import org.jboss.as.weld.deployment.WeldPortableExtensions;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;

import javax.enterprise.inject.spi.Extension;
import java.lang.reflect.Constructor;

/**
 * Need to forcibly add our CDI extension into the list created by Weld subsystem as
 * any dependencies don't have their services files read by container.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class CdiContextExtensionProcessor implements DeploymentUnitProcessor {
    private static final Logger log = Logger.getLogger(CdiContextExtensionProcessor.class);

    private static final String[] EMPTY_STRING_ARRAY = {};

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (!GateInConfiguration.isPortletArchive(deploymentUnit)) {
            return;
        }

        if (!WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
            return; // skip non weld deployments
        }

        final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        final DeploymentReflectionIndex index = deploymentUnit.getAttachment(Attachments.REFLECTION_INDEX);
        final Extension extension = loadExtension(CDIPortletContextExtension.class.getName(), index,  module.getClassLoader());

        log.debug("Loaded portable extension " + extension);

        WeldPortableExtensions wpe = WeldPortableExtensions.getPortableExtensions(deploymentUnit);
        wpe.registerExtensionInstance(extension, deploymentUnit);
    }

    private Extension loadExtension(String serviceClassName, final DeploymentReflectionIndex index, final ClassLoader loader) throws DeploymentUnitProcessingException {
        Class<?> clazz;
        Class<Extension> serviceClass;
        try {
            clazz = loader.loadClass(serviceClassName);
            serviceClass = (Class<Extension>) clazz;
            final Constructor<Extension> ctor = index.getClassIndex(serviceClass).getConstructor(EMPTY_STRING_ARRAY);
            return ctor.newInstance();
        }  catch (ClassCastException e) {
            throw new DeploymentUnitProcessingException(
                    "Service class " + serviceClassName + " didn't implement the javax.enterprise.inject.spi.Extension interface", e);
        } catch (Exception e) {
            log.warnf("Could not load portable extension class %s: %s", serviceClassName, e.getMessage());
        }
        return null;
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
