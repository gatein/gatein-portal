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

package org.gatein.integration.jboss.as7.portal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.DataStorage;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalContext {
    private static Logger log = Logger.getLogger("org.gatein.as7.portal");

    private final String portalContainerName;

    public PortalContext(String portalContainerName) {
        this.portalContainerName = portalContainerName;
    }

    public String getPortalContainerName() {
        return portalContainerName;
    }

    public static Logger getLog() {
        return log;
    }

    public Set<String> getSites() {
        try {
            return execute(new Request<Set<String>>() {
                @Override
                public Set<String> within(PortalContainer container) throws Exception {
                    DataStorage dataStorage = getComponent(DataStorage.class, container);
                    List<String> names = dataStorage.getAllPortalNames();
                    return new LinkedHashSet<String>(names);
                }
            });
        } catch (Exception e) {
            log.error("Could not retrieve list of sites.", e);
            return Collections.emptySet();
        }
    }

    public <T> T execute(Request<T> request) throws Exception {
        return doExecute(request);
    }

    private <T> T doExecute(Request<T> request) throws Exception {
        final PortalContainer original = PortalContainer.getInstance();
        final RootContainer root = RootContainer.getInstance();

        PortalContainer container = null;
        if (root != null) {
            container = root.getPortalContainer(portalContainerName);
        }
        PortalContainer.setInstance(container);

        if (container != null) {
            RequestLifeCycle.begin(container);
        }
        try {
            return request.within(container);
        } finally {
            if (container != null) {
                RequestLifeCycle.end();
            }
            PortalContainer.setInstance(original);
        }
    }

    public abstract static class Request<T> {
        public <S> S getComponent(Class<S> type, PortalContainer container) {
            if (container == null)
                return null;

            Object service = container.getComponentInstanceOfType(type);
            return (service != null) ? type.cast(service) : null;
        }

        public abstract T within(PortalContainer container) throws Exception;
    }
}
