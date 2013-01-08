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

import static org.gatein.integration.jboss.as7.portal.PortalResourceConstants.APPLICATION;
import static org.gatein.integration.jboss.as7.portal.PortalResourceConstants.SITE;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.ApplicationStatisticService;
import org.gatein.integration.jboss.as7.portal.PortalContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.registry.Resource;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalRuntimeResource extends AbstractPortalResource {
    private static final Set<String> childTypes;

    static {
        Set<String> set = new LinkedHashSet<String>(2);
        set.add(SITE);
        set.add(APPLICATION);
        childTypes = set;
    }

    public PortalRuntimeResource(PathElement pathElement) {
        super(pathElement, pathElement.getValue());
    }

    @Override
    protected Set<String> getChildrenNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getChildrenNames(String childType) {
        if (childType.equals(SITE)) {
            return portalContext.getSites();
        } else if (childType.equals(APPLICATION)) {
            try {
                // For now we will return the list of applications that have statistics registered for them. This
                // should probably use the ApplicationRegistryService at some point, but our statistics are not in sync with
                // this
                return portalContext.execute(new PortalContext.Request<Set<String>>() {
                    @Override
                    public Set<String> within(PortalContainer container) throws Exception {
                        ApplicationStatisticService service = getComponent(ApplicationStatisticService.class, container);
                        if (service == null)
                            return Collections.emptySet();

                        return new LinkedHashSet<String>(Arrays.asList(service.getApplicationList()));
                    }
                });
            } catch (Exception e) {
                getLogger().error("Exception retrieving list of applications for runtime portal resource.", e);
                return Collections.emptySet();
            }
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<ResourceEntry> getChildren(String childType) {
        if (!hasChildren(childType)) {
            return Collections.emptySet();
        } else {
            Set<Resource.ResourceEntry> result = new LinkedHashSet<ResourceEntry>();
            for (String name : getChildrenNames(childType)) {
                result.add(getChildEntry(childType, name));
            }
            return result;
        }
    }

    @Override
    public Resource getChild(PathElement element) {
        if (hasChildren(element.getKey())) {
            return getChildEntry(element.getKey(), element.getValue());
        } else {
            return null;
        }
    }

    @Override
    protected ResourceEntry getChildEntry(String name) {
        throw new UnsupportedOperationException();
    }

    private ResourceEntry getChildEntry(String childType, String name) {
        if (childType.equals(SITE)) {
            return new SiteRuntimeResource(PathElement.pathElement(SITE, name), portalContext.getPortalContainerName());
        } else if (childType.equals(APPLICATION)) {
            return new ApplicationRuntimeResource(PathElement.pathElement(APPLICATION, name),
                    portalContext.getPortalContainerName());
        }

        return null;
    }

    @Override
    public Set<String> getChildTypes() {
        return childTypes;
    }
}
