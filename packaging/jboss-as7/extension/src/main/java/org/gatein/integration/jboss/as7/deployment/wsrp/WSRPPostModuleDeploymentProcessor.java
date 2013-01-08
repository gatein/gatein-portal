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

import java.util.List;

import org.gatein.integration.wsrp.plugins.AS7Plugins;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.ServicesAttachment;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class WSRPPostModuleDeploymentProcessor implements DeploymentUnitProcessor {
    public static final AS7Plugins plugins = new AS7Plugins();
    static final List<String> KNOWN_PLUGIN_INTERFACE_NAMES = plugins.getKnownPluginInterfaceNames();

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit du = phaseContext.getDeploymentUnit();
        if (GateInWSRPKey.isGateInWSRPArchive(du)) {
            // Only process if we have a META-INF/services ServiceLoader directory in the archive
            final ServicesAttachment services = du.getAttachment(Attachments.SERVICES);
            if (services != null) {
                // get the module class loader
                final Module module = du.getAttachment(Attachments.MODULE);
                final ModuleClassLoader classLoader;
                if (module != null) {
                    classLoader = module.getClassLoader();
                } else {
                    classLoader = null;
                }

                for (String interfaceName : KNOWN_PLUGIN_INTERFACE_NAMES) {
                    // retrieve all service implementations for each known plugin interface and add them if they exist
                    final List<String> serviceImplementations = services.getServiceImplementations(interfaceName);
                    plugins.addPluginImplementations(interfaceName, serviceImplementations);

                    if (classLoader != null) {
                        // remember the module for each service implementation
                        for (String implementation : serviceImplementations) {
                            plugins.registerClassloader(implementation, classLoader);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // nothing to do for now. If we supported hot redeploy, we would need to update AS7Plugins to remove the undeployed
        // implementations
    }
}
