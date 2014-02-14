/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.gatein.portal.controller.resource.script;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScriptGraph {

    /** . */
    final EnumMap<ResourceScope, Map<String, ScriptResource>> resources;

    /** . */
    final Map<String, ScriptGroup> loadGroups;

    /** . */
    private static final Log log = ExoLogger.getExoLogger(ScriptGraph.class);

    public ScriptGraph() {
        EnumMap<ResourceScope, Map<String, ScriptResource>> resources = new EnumMap<ResourceScope, Map<String, ScriptResource>>(
                ResourceScope.class);
        for (ResourceScope scope : ResourceScope.values()) {
            resources.put(scope, new HashMap<String, ScriptResource>());
        }

        //
        this.resources = resources;
        this.loadGroups = new HashMap<String, ScriptGroup>();
    }

    /**
     * <p>
     * </p>
     * Resolve a collection of pair of resource id and fetch mode, each entry of the map will be processed in the order
     * specified by the iteration of the {@link java.util.Map#entrySet()}. For a given pair the fetch mode may be null or not.
     * When the fetch mode is null, the default fetch mode of the resource is used. When the fetch mode is not null, this fetch
     * mode may override the resource fetch mode if it implies this particular fetch mode. This algorithm tolerates the absence
     * of resources, for instance if a resource is specified (among the pairs or by a transitive dependency) and does not exist,
     * the resource will be skipped.</p>
     *
     * @param pairs the pairs to resolve
     * @return the resources sorted
     */
    public Map<ScriptResource, FetchMode> resolve(Map<ResourceId, FetchMode> pairs) {
        // Build a fetch graph
        Map<ResourceId, ScriptFetch> determined = new HashMap<ResourceId, ScriptFetch>();
        for (Map.Entry<ResourceId, FetchMode> pair : pairs.entrySet()) {
            traverse(determined, pair.getKey(), pair.getValue());
        }

        // We remove one by one the nodes of the fetch graph having no dependencies
        // each node will build the dependency list
        LinkedHashMap<ScriptResource, FetchMode> result = new LinkedHashMap<ScriptResource, FetchMode>();
        LinkedList<ScriptFetch> all = new LinkedList<ScriptFetch>(determined.values());
        while (all.size() > 0) {
            ScriptFetch next = null;
            for (Iterator<ScriptFetch> i = all.iterator(); i.hasNext();) {
                ScriptFetch fetch = i.next();
                if (fetch.dependencies.size() == 0) {
                    i.remove();
                    next = fetch;
                    for (ScriptFetch dependent : fetch.dependsOnMe) {
                        dependent.dependencies.remove(fetch);
                    }
                    break;
                }
            }
            if (next == null) {
                // This should not happen:
                // we have an DAG, on each iteration we must have at least one node that has no dependencies
                // we remove it from the graph and update its dependencies
                // (unless the graph is not correctly constructed above)
                throw new AssertionError("This is a bug");
            } else {
                result.put(next.resource, next.mode);
            }
        }

        //
        return result;
    }

    private ScriptFetch traverse(Map<ResourceId, ScriptFetch> map, ResourceId id, FetchMode mode) {
        ScriptResource resource = getResource(id);
        if (resource != null) {
            if (mode != null && !resource.fetchMode.equals(mode)) {
                return null;
            } else {
                mode = resource.fetchMode;
                ScriptFetch fetch = map.get(id);
                if (fetch == null) {
                    fetch = new ScriptFetch(resource, mode);
                    if (!resource.isEmpty() || ResourceScope.SHARED.equals(resource.getId().getScope())) {
                        map.put(id, fetch);
                    }

                    // Recursively add the dependencies
                    if (FetchMode.IMMEDIATE.equals(mode) || resource.isEmpty()) {
                        for (ResourceId dependencyId : resource.dependencies.keySet()) {
                            ScriptFetch dependencyFetch = traverse(map, dependencyId, mode);
                            if (dependencyFetch != null) {
                                dependencyFetch.dependsOnMe.add(fetch);
                                fetch.dependencies.add(dependencyFetch);
                            }
                        }
                    }
                }
                return fetch;
            }
        } else {
            return null;
        }
    }

    public ScriptResource getResource(ResourceId id) {
        return getResource(id.getScope(), id.getName());
    }

    public ScriptResource getResource(ResourceScope scope, String name) {
        return resources.get(scope).get(name);
    }

    public Collection<ScriptResource> getResources(ResourceScope scope) {
        return resources.get(scope).values();
    }

    public ScriptResource addResource(ResourceId id) {
        return addResource(id, FetchMode.IMMEDIATE);
    }

    public ScriptResource addResource(ResourceId id, FetchMode fetchMode) throws NullPointerException {
        return addResource(id, fetchMode, null, null, null, false);
    }

    /**
     * Add a resource to the graph if available. return null if ResourceID is duplicated
     *
     * @param id the resource id
     * @param fetchMode the resource fetch mode
     * @param alias default alias
     * @return the resource
     * @throws NullPointerException if id or fetchMode is null
     */
    public ScriptResource addResource(ResourceId id, FetchMode fetchMode, String alias, String groupName, String contextPath, boolean nativeAmd)
            throws NullPointerException {
        if (id == null) {
            throw new NullPointerException("No null resource accepted");
        }
        if (fetchMode == null) {
            throw new NullPointerException("No null fetch mode accepted");
        }

        //
        if (ResourceScope.SHARED.equals(id.getScope())) {
            for (Map<String, ScriptResource> mp : resources.values()) {
                for (ScriptResource rs : mp.values()) {
                    if (rs.getDependencies().contains(id) && !rs.getFetchMode().equals(fetchMode)) {
                        throw new IllegalStateException("ScriptResource " + rs.getId() + " can't depend on " + id
                                + ". They have difference fetchMode");
                    }
                }
            }

            if (JavascriptConfigService.RESERVED_MODULE.contains(id.getName())) {
                throw new IllegalStateException("Can't not add " + id + ". The name " + id.getName() + " is reserved name");
            }
        }

        //
        Map<String, ScriptResource> map = resources.get(id.getScope());
        String name = id.getName();
        ScriptResource resource = map.get(name);
        if (resource == null) {
            ScriptGroup group = null;
            if (groupName != null && contextPath != null) {
                group = loadGroups.get(groupName);
                if (group == null) {
                    ResourceId grpId = new ResourceId(ResourceScope.GROUP, groupName);
                    loadGroups.put(groupName, group = new ScriptGroup(this, grpId, contextPath));
                    group.addDependency(id);
                } else if (!contextPath.equals(group.contextPath)) {
                    log.warn("Can't add cross context resource {} to {} group", id, groupName);
                    group = null;
                } else {
                    group.addDependency(id);
                }
            }

            map.put(name, resource = new ScriptResource(this, id, fetchMode, alias, group, nativeAmd));
        } else if (!(id.getScope().equals(ResourceScope.SHARED) && JavascriptConfigParser.LEGACY_JAVA_SCRIPT.equals(name))) {
            throw new IllegalStateException("Duplicate ResourceId : " + id + ", later resource definition will be ignored");
        }

        return resource;
    }

    public ScriptResource removeResource(ResourceId id) {
        return removeResource(id, null);
    }

    public ScriptResource removeResource(ResourceId id, String contextPath) {
        ScriptResource toRemove = resources.get(id.getScope()).get(id.getName());

        if (toRemove != null) {
            if (ResourceScope.SHARED.equals(id.getScope()) && JavascriptConfigParser.LEGACY_JAVA_SCRIPT.equals(id.getName())) {
                toRemove.removeModuleByContextPath(contextPath);

                if (toRemove.getModules().size() > 0)
                    return toRemove;
            }

            resources.get(id.getScope()).remove(id.getName());
            toRemove.graph = null;
        }

        return toRemove;
    }

    public ScriptGroup getLoadGroup(String groupName) {
        return loadGroups.get(groupName);
    }
}
