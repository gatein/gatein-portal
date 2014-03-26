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

package org.gatein.integration.jboss.as7.portal.resources;

import java.util.Collections;
import java.util.Set;

import org.jboss.as.controller.PathElement;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ApplicationRuntimeResource extends AbstractPortalResource {
    private final String applicationName;

    protected ApplicationRuntimeResource(PathElement pathElement, String portalContainerName) {
        super(pathElement, portalContainerName);
        this.applicationName = pathElement.getValue();
    }

    @Override
    protected Set<String> getChildrenNames() {
        return Collections.emptySet();
    }

    @Override
    protected ResourceEntry getChildEntry(String name) {
        return null;
    }

    @Override
    public Set<String> getChildTypes() {
        return Collections.emptySet();
    }
}
