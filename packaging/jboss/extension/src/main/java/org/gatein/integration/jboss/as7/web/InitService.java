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
package org.gatein.integration.jboss.as7.web;

import org.exoplatform.container.RootContainer;
import org.gatein.integration.jboss.as7.GateInConfiguration;
import org.gatein.integration.jboss.as7.deployment.wsrp.WSRPPostModuleDeploymentProcessor;
import org.gatein.integration.wsrp.plugins.AS7Plugins;
import org.jboss.as.server.moduleservice.ModuleLoadService;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/** @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a> */
public class InitService implements Service<InitService> {

    private GateInConfiguration config;

    public InitService(GateInConfiguration config) {
        this.config = config;
    }

    @Override
    public void start(StartContext context) throws StartException {
        System.out.println("InitService START");
        ServiceController<?> svc = context.getController().getServiceContainer()
                .getRequiredService(ServiceModuleLoader.moduleServiceName(config.getGateInEarModule()));

        ModuleLoadService modService = (ModuleLoadService) svc.getService();
        Module module = modService.getValue();

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // set TCCL to this module's CL
            Thread.currentThread().setContextClassLoader(module.getClassLoader());
            final RootContainer rootContainer = RootContainer.getInstance();

            // register WSRP plugins service so that it's available when the WSRP service starts
            rootContainer.registerComponentInstance(AS7Plugins.class, WSRPPostModuleDeploymentProcessor.plugins);
        } finally {
            if (Thread.currentThread().getContextClassLoader() != oldCl) {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public InitService getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
