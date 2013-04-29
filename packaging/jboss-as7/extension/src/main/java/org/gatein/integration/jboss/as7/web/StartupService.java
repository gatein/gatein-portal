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
import org.gatein.version.Version;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/** @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a> */
public class StartupService implements Service<StartupService> {
    private Module module;

    public static final ServiceName SERVICE_NAME = ServiceName.of((ServiceName) null, "org", "gatein", "startup");
    private static final Logger log = Logger.getLogger("org.gatein");

    @Override
    public void start(StartContext context) throws StartException {
        try {
            // Trigger startup
            RootContainer.getInstance().createPortalContainers();
        } finally {
            // Startup message
            log.info(Version.prettyVersion + " started.");
        }
    }

    @Override
    public void stop(StopContext context) {
        RootContainer.getInstance().stop();
    }

    @Override
    public StartupService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public void setGateInModule(Module module) {
        this.module = module;
    }
}
