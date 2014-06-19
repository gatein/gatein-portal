/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.exoplatform.portal.resource;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebAppListener;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class ResourceDeploymentService implements Startable {
    private static Log log = ExoLogger.getLogger(ResourceDeploymentService.class);
    private final WebAppListener deployer;

    /**
     * @param skinService
     * @param javascriptConfigService
     */
    public ResourceDeploymentService(ExoContainerContext context, SkinService skinService, JavascriptConfigService javascriptConfigService) {
        super();
        this.deployer = new GateInResourcesDeployer(context.getPortalContainerName(), skinService, javascriptConfigService);
    }

    /**
     * Registers a {@link GateInResourcesDeployer} as a {@link WebAppListener} on the servlet container.
     *
     * @see org.picocontainer.Startable#start()
     */
    @Override
    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("Registering "+ deployer.getClass().getSimpleName() +" for servlet container events");
        }
        ServletContainerFactory.getServletContainer().addWebAppListener(deployer);
    }

    /**
     * Unregisters a {@link GateInResourcesDeployer} as a {@link WebAppListener} on the servlet container.
     *
     * @see org.picocontainer.Startable#stop()
     */
    @Override
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Registering "+ deployer.getClass().getSimpleName() +" for servlet container events");
        }
        ServletContainerFactory.getServletContainer().removeWebAppListener(deployer);
    }

}
