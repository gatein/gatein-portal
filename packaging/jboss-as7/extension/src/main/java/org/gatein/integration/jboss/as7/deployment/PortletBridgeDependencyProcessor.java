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

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.gatein.integration.jboss.as7.GateInConfiguration;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.spec.WebFragmentMetaData;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;

/**
 * @author <a href="mailto:stian@redhat.com">Stian Thorgersen</a>
 */
public class PortletBridgeDependencyProcessor implements DeploymentUnitProcessor {

    private static final Logger log = Logger.getLogger(PortletBridgeDependencyProcessor.class);
    
    private static final ModuleIdentifier COMMONS_FILEUPLOAD = ModuleIdentifier.create("org.apache.commons-fileupload");
    private static final ModuleIdentifier JSF_API = ModuleIdentifier.create("javax.faces.api");
    private static final ModuleIdentifier JSF_IMPL = ModuleIdentifier.create("com.sun.jsf-impl");
    private static final ModuleIdentifier PORTLETBRIDGE_API = ModuleIdentifier.create("org.jboss.portletbridge.api");
    private static final ModuleIdentifier PORTLETBRIDGE_IMPL = ModuleIdentifier.create("org.jboss.portletbridge.impl");

    private static final String portletBridgeVersion = getPortletBridgeVersion();

    public static final String WAR_BUNDLES_PORTLETBRIDGE_PARAM = "org.gatein.portletbridge.WAR_BUNDLES_PORTLETBRIDGE";

    private static String getPortletBridgeVersion() {
        try {
            Module module = Module.getBootModuleLoader().loadModule(PORTLETBRIDGE_IMPL);
            Manifest mf = new Manifest(module.getClassLoader().getResourceAsStream("/META-INF/MANIFEST.MF"));
            return mf.getMainAttributes().getValue("Implementation-Version");
        } catch (Exception e) {
            return "";
        }
    }

    private void addPortletBridgeApi(DeploymentUnit deploymentUnit, ModuleSpecification moduleSpecification,
            ModuleLoader moduleLoader) {
        ModuleDependency pbr = new ModuleDependency(moduleLoader, PORTLETBRIDGE_API, false, false, false, false);
        moduleSpecification.addSystemDependency(pbr);
    }

    private void addPortletBridgeDependencies(DeploymentUnit deploymentUnit, ModuleSpecification moduleSpecification,
            ModuleLoader moduleLoader) {
        ModuleDependency pbr = new ModuleDependency(moduleLoader, COMMONS_FILEUPLOAD, false, false, false, false);
        moduleSpecification.addSystemDependency(pbr);
    }

    private void addPortletBridgeImpl(DeploymentUnit deploymentUnit, ModuleSpecification moduleSpecification,
            ModuleLoader moduleLoader) {
        try {
            Module module = moduleLoader.loadModule(PORTLETBRIDGE_IMPL);
            URL url = module.getClassLoader().getResource("org/jboss/portletbridge/PortletBridgeImpl.class");
            URLConnection connection = url.openConnection();

            if (!(connection instanceof JarURLConnection)) {
                throw new RuntimeException("portletbridge-impl not found");
            }

            JarFile jarFile = ((JarURLConnection) connection).getJarFile();

            moduleSpecification.addResourceLoader(ResourceLoaderSpec.createResourceLoaderSpec(ResourceLoaders
                    .createJarResourceLoader("portletbridge-impl", jarFile)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (!GateInConfiguration.isPortletArchive(deploymentUnit)) {
            return;
        }

        ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        ModuleLoader moduleLoader = Module.getBootModuleLoader();

        if (isJsfDependencies(moduleSpecification) && !isPortletBridgeBundled(deploymentUnit)) {
            log.infof("Adding JBoss Portlet Bridge %s to \"%s\"", portletBridgeVersion, deploymentUnit.getName());

            addPortletBridgeApi(deploymentUnit, moduleSpecification, moduleLoader);
            addPortletBridgeImpl(deploymentUnit, moduleSpecification, moduleLoader);
            addPortletBridgeDependencies(deploymentUnit, moduleSpecification, moduleLoader);
        }
    }

    private boolean isJsfDependencies(ModuleSpecification moduleSpecification) {
        boolean jsfApi = false;
        boolean jsfImpl = false;
        for (ModuleDependency d : moduleSpecification.getSystemDependencies()) {
            if (d.getIdentifier().equals(JSF_API)) {
                jsfApi = true;
            }
            if (d.getIdentifier().equals(JSF_IMPL)) {
                jsfImpl = true;
            }
        }
        return jsfApi && jsfImpl;
    }

    private boolean isPortletBridgeBundled(DeploymentUnit deploymentUnit) {
        WarMetaData metaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);

        if (metaData == null) {
            return false;
        }

        List<ParamValueMetaData> contextParams = new ArrayList<ParamValueMetaData>();

        if ((metaData.getWebMetaData() != null) && (metaData.getWebMetaData().getContextParams() != null)) {
            contextParams.addAll(metaData.getWebMetaData().getContextParams());
        }

        if (metaData.getWebFragmentsMetaData() != null) {
            for (WebFragmentMetaData fragmentMetaData : metaData.getWebFragmentsMetaData().values()) {
                if (fragmentMetaData.getContextParams() != null) {
                    contextParams.addAll(fragmentMetaData.getContextParams());
                }
            }
        }

        for (ParamValueMetaData param : contextParams) {
            if ((param.getParamName().equals(WAR_BUNDLES_PORTLETBRIDGE_PARAM) && (param.getParamValue() != null) && (param
                    .getParamValue().toLowerCase(Locale.ENGLISH).equals("true")))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
