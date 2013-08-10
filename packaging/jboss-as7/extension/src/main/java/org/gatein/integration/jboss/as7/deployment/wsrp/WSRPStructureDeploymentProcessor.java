/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.gatein.integration.jboss.as7.deployment.wsrp;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;

import org.gatein.integration.jboss.as7.deployment.GateInExtKey;
import org.gatein.wsrp.api.plugins.Plugins;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.vfs.VirtualFile;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class WSRPStructureDeploymentProcessor implements DeploymentUnitProcessor {
    private static final Logger log = Logger.getLogger(WSRPStructureDeploymentProcessor.class);
    private final List<ModuleDependency> loadedPluginsAsDependencies = new ArrayList<ModuleDependency>(7);
    private AtomicReference<ResourceLoaderSpec> resourceLoaderSpec = new AtomicReference<ResourceLoaderSpec>(null);

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit du = phaseContext.getDeploymentUnit();
        if (isWSRPPlugin(du)) {
            du.putAttachment(GateInWSRPKey.KEY, GateInWSRPKey.INSTANCE);

            // boot module loader
            final ModuleLoader moduleLoader = Module.getBootModuleLoader();

            // plugin module loader so that the implicit plugin module can be found later by WSRP admin
            final ServiceModuleLoader pluginModuleLoader = du.getAttachment(Attachments.SERVICE_MODULE_LOADER);

            // create a dependency on this module to be added to WSRP admin
            final ModuleIdentifier moduleIdentifier = du.getAttachment(Attachments.MODULE_IDENTIFIER);
            synchronized (this) {
                loadedPluginsAsDependencies.add(new ModuleDependency(pluginModuleLoader, moduleIdentifier, false, false, false, false));
            }

            // add dependencies
            final ModuleSpecification moduleSpecification = du.getAttachment(Attachments.MODULE_SPECIFICATION);
            ModuleDependency dependency = new ModuleDependency(moduleLoader, ModuleIdentifier.fromString("org.gatein.wsrp"),
                    false, false, false, false);
            moduleSpecification.addSystemDependency(dependency);

            // PC dependency is needed for InvocationHandlerDelegate implementations
            dependency = new ModuleDependency(moduleLoader, ModuleIdentifier.fromString("org.gatein.pc"), false, false, false,
                    false);
            moduleSpecification.addSystemDependency(dependency);

            // Apache WS Security is needed for CallbackHandler implementations
            dependency = new ModuleDependency(moduleLoader, ModuleIdentifier.fromString("org.apache.ws.security"), false,
                    false, false, false);
            moduleSpecification.addSystemDependency(dependency);

            // GTNPORTAL-3133: WSRP integration is needed to so that plugins can access WSRP-specific CXF WS-Security integration
            dependency = new ModuleDependency(moduleLoader, ModuleIdentifier.fromString("org.gatein.wsrp.integration"), false,
                    false, false, false);
            moduleSpecification.addSystemDependency(dependency);

            log.infof("Adding WSRP, PC & Apache WS Security dependencies to %s", du.getName());
        } else {
            final String name = du.getName();
            final boolean isWSRPAdmin = name.contains("wsrp-admin-gui");
            if (isWSRPAdmin || name.contains("extension-war")) {

                ModuleSpecification moduleSpecification = du.getAttachment(Attachments.MODULE_SPECIFICATION);
                ModuleLoader moduleLoader = Module.getBootModuleLoader();

                // create resource loader to load JAX-WS catalog if it doesn't already exist
                if (resourceLoaderSpec.get() == null) {
                    try {
                        Module module = moduleLoader.loadModule(ModuleIdentifier.fromString("org.gatein.wsrp.catalog"));
                        URL url = module.getClassLoader().getResource("META-INF/jax-ws-catalog.xml");
                        URLConnection connection = url.openConnection();

                        if (!(connection instanceof JarURLConnection)) {
                            throw new RuntimeException("JAX-WS catalog not found");
                        }

                        JarFile jarFile = ((JarURLConnection) connection).getJarFile();

                        resourceLoaderSpec.set(ResourceLoaderSpec.createResourceLoaderSpec(ResourceLoaders
                                .createJarResourceLoader("wsrp-catalog", jarFile)));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }

                // add JAX-WS catalog access to WSRP admin and extension
                moduleSpecification.addResourceLoader(resourceLoaderSpec.get());

                // if we're processing the WSRP admin, add the plugins as local dependencies as well, just in case
                if (isWSRPAdmin) {
                    synchronized (this) {
                        moduleSpecification.addLocalDependencies(loadedPluginsAsDependencies);
                    }
                    log.info("Adding " + loadedPluginsAsDependencies + " dependencies to WSRP admin module");
                }
            }

            // GTNPORTAL-3133: if we're processing the WSRP producer WAR, add the plugins as local dependencies so that CXF can access them
            // GTNPORTAL-3232: also include the portal.war so that the consumer can access the plugins
            if (name.contains("wsrp-producer") || name.contains("portal.war")) {
                ModuleSpecification moduleSpecification = du.getAttachment(Attachments.MODULE_SPECIFICATION);

                synchronized (this) {
                    moduleSpecification.addLocalDependencies(loadedPluginsAsDependencies);
                }
                log.info("Adding " + loadedPluginsAsDependencies + " dependencies to WSRP producer module");
            }
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // nothing to do
    }

    private boolean isWSRPPlugin(DeploymentUnit du) {
        final String name = du.getName();

        // check first if name ends with .wsrp.jar which allows for fast marking of WSRP plugins
        if (name.endsWith(Plugins.WSRP_PLUGIN_EXTENSION_SUFFIX)) {
            return true;
        } else {
            // only process files that are JAR files and GateIn extensions
            if (name.endsWith(".jar") && du.hasAttachment(GateInExtKey.KEY)) {
                // check that the jar contains a Service provider implementing one of the known WSRP plugin interfaces
                final ResourceRoot deploymentRoot = du.getAttachment(Attachments.DEPLOYMENT_ROOT);
                final VirtualFile servicesDir = deploymentRoot.getRoot().getChild("META-INF/services");
                if (servicesDir.exists()) {
                    for (String interfaceName : WSRPPostModuleDeploymentProcessor.KNOWN_PLUGIN_INTERFACE_NAMES) {
                        if (servicesDir.getChild(interfaceName).exists()) {
                            return true;
                        }
                    }
                    log.debug("Looked at " + name + " using ServiceLoader but didn't contain any WSRP plugin marker. Ignoring.");
                }
            }

            return false;
        }
    }

}
