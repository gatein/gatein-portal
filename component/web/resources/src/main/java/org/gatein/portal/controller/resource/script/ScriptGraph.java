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
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.exoplatform.portal.resource.InvalidResourceException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.DependencyDescriptor;
import org.exoplatform.web.application.javascript.DuplicateResourceKeyException;
import org.exoplatform.web.application.javascript.Javascript;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.ScriptGroup.ScriptGroupBuilder;
import org.gatein.portal.controller.resource.script.ScriptResource.ScriptResourceBuilder;

/**
 * Scripts and the dependencies among them.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 */
public class ScriptGraph {

    static class ScriptGraphBuilder {
        private final Map<ResourceScope, Map<String, ScriptResourceBuilder>> resourceBuilders;
        private final Map<String, ScriptGroupBuilder> loadGroupBuilders;

        private ScriptGraphBuilder(Map<ResourceScope, Map<String, ScriptResource>> resources, Map<String, ScriptGroup> loadGroups) {
            int groupsCount = loadGroups.size();
            this.loadGroupBuilders = new HashMap<String, ScriptGroupBuilder>(groupsCount + groupsCount/2 + 1);
            for (ScriptGroup group : loadGroups.values()) {
                loadGroupBuilders.put(group.getId().getName(), group.newBuilder(this));
            }

            this.resourceBuilders =
                    new EnumMap<ResourceScope, Map<String, ScriptResourceBuilder>>(ResourceScope.class);
            for (ResourceScope scope : ResourceScope.values()) {
                Map<String, ScriptResource> scopeMap = resources.get(scope);
                int resourcesCount = scopeMap.size();
                Map<String, ScriptResourceBuilder> newScopeMap = new HashMap<String, ScriptResourceBuilder>(resourcesCount  + resourcesCount/2 + 1);
                for (ScriptResource scriptResource : scopeMap.values()) {
                    ScriptGroup group = scriptResource.getGroup();
                    ScriptGroupBuilder groupBuilder = group != null ? loadGroupBuilders.get(group.getId().getName()) : null;
                    ScriptResourceBuilder newBuilder = scriptResource.newBuilder(this, groupBuilder);
                    newScopeMap.put(scriptResource.getId().getName(), newBuilder);
                }
                resourceBuilders.put(scope, newScopeMap);
            }
        }

        ScriptResourceBuilder getResource(ResourceId id) {
            return resourceBuilders.get(id.getScope()).get(id.getName());
        }

        void addResource(String contextPath, ScriptResourceDescriptor desc) throws InvalidResourceException {
            ResourceId id = desc.getId();
            FetchMode fetchMode = desc.getFetchMode();
            String alias = desc.getAlias();
            String groupName = desc.getGroup();
            boolean nativeAmd = desc.isNativeAmd();
            if (id == null) {
                throw new NullPointerException("No null resource accepted");
            }
            if (fetchMode == null) {
                throw new NullPointerException("No null fetch mode accepted");
            }

            //
            if (ResourceScope.SHARED.equals(id.getScope())) {
                for (Map<String, ScriptResourceBuilder> mp : resourceBuilders.values()) {
                    for (ScriptResourceBuilder rs : mp.values()) {
                        rs.checkDependencyFetchMode(id, fetchMode);
                    }
                }

                if (JavascriptConfigService.RESERVED_MODULE.contains(id.getName())) {
                    throw new InvalidResourceException("Cannot add " + id + ". The name " + id.getName() + " is a reserved name");
                }
            }

            //
            Map<String, ScriptResourceBuilder> map = resourceBuilders.get(id.getScope());
            String name = id.getName();
            ScriptResourceBuilder resourceBuilder = map.get(name);
            if (resourceBuilder == null) {
                ScriptGroupBuilder group = null;
                if (groupName != null && contextPath != null) {
                    group = loadGroupBuilders.get(groupName);
                    if (group == null) {
                        ResourceId grpId = new ResourceId(ResourceScope.GROUP, groupName);
                        group = new ScriptGroupBuilder(this, grpId, contextPath);
                        group.addDependency(id);
                        loadGroupBuilders.put(groupName, group);
                    } else if (!group.getContextPath().equals(contextPath)) {
                        log.warn("Cannot add resource {} from context {} to group {} in another context {}.", id, contextPath, groupName, group, group.getContextPath());
                        group = null;
                    } else {
                        group.addDependency(id);
                    }
                }

                resourceBuilder = new ScriptResourceBuilder(this, id, contextPath, fetchMode, alias, group, nativeAmd);
                map.put(name, resourceBuilder);
            } else if (!(id.getScope().equals(ResourceScope.SHARED) && JavascriptConfigParser.LEGACY_JAVA_SCRIPT.equals(name))) {
                throw new DuplicateResourceKeyException("Duplicate ResourceId : " + id + ", later resource definition will be ignored");
            }


            for (Javascript module : desc.getModules()) {
                if (module instanceof Javascript.Local) {
                    Javascript.Local localModule = (Javascript.Local) module;
                    resourceBuilder.addLocalModule(contextPath, localModule.getContents(), localModule.getResourceBundle(), localModule.getPriority());
                } else if (module instanceof Javascript.Remote) {
                    Javascript.Remote remoteModule = (Javascript.Remote) module;
                    resourceBuilder.addRemoteModule(remoteModule.getUri(), remoteModule.getPriority());
                } else {
                    throw new IllegalStateException("Unexpected type "+ module.getClass().getName());
                }
            }
            for (Locale locale : desc.getSupportedLocales()) {
                resourceBuilder.addSupportedLocale(locale);
            }
            for (DependencyDescriptor dependency : desc.getDependencies()) {
                resourceBuilder.addDependency(contextPath, dependency.getResourceId(), dependency.getAlias(), dependency.getPluginResource());
            }
        }

        void dependencyAdded(ResourceId id, Set<ResourceId> closure) {
            for (Map<String, ScriptResourceBuilder> scopeMap : resourceBuilders.values()) {
                for (ScriptResourceBuilder resource : scopeMap.values()) {
                    resource.dependencyAdded(id, closure);
                }
            }
        }

        ScriptGraphBuilder removeResourcesByContextPath(String contextPath) {
            HashSet<ResourceId> removedIds = new HashSet<ResourceId>();
            for (Map<String, ScriptResourceBuilder> scopeMap : resourceBuilders.values()) {
                for (Iterator<ScriptResourceBuilder> it = scopeMap.values().iterator(); it.hasNext();) {
                    ScriptResourceBuilder resource = it.next();
                    if (resource.getContextPath().equals(contextPath)) {
                        it.remove();
                        removedIds.add(resource.getId());
                    }
                }
            }

            for (Map<String, ScriptResourceBuilder> scopeMap : resourceBuilders.values()) {
                for (ScriptResourceBuilder resource : scopeMap.values()) {
                    resource.resourcesRemoved(contextPath, removedIds);
                }
            }

            for (Iterator<ScriptGroupBuilder> it = loadGroupBuilders.values().iterator(); it.hasNext();) {
                ScriptGroupBuilder group = it.next();
                if (group.getContextPath().equals(contextPath)) {
                    it.remove();
                }
            }

            return this;
        }

        ScriptGraph build() {

            int groupsCount = loadGroupBuilders.size();
            Map<String, ScriptGroup> loadGroups = new HashMap<String, ScriptGroup>(groupsCount + groupsCount/2 + 1);
            for (Entry<String, ScriptGroupBuilder> en : loadGroupBuilders.entrySet()) {
                ScriptGroupBuilder groupBuilder = en.getValue();
                if (groupBuilder.hasDependencies()) {
                    /* do not add empty groups */
                    loadGroups.put(en.getKey(), groupBuilder .build());
                }
            }

            EnumMap<ResourceScope, Map<String, ScriptResource>> resources =
                    new EnumMap<ResourceScope, Map<String, ScriptResource>>(ResourceScope.class);
            for (Entry<ResourceScope, Map<String, ScriptResourceBuilder>> en : resourceBuilders.entrySet()) {
                ResourceScope scope = en.getKey();
                Map<String, ScriptResourceBuilder> scopeBuilders = en.getValue();
                int resourcesCount = scopeBuilders.size();
                HashMap<String, ScriptResource> scopeMap = new HashMap<String, ScriptResource>(resourcesCount + resourcesCount/2 + 1);
                for (ScriptResourceBuilder resourceBuilder : scopeBuilders.values()) {
                    ScriptResource resource = resourceBuilder.build();
                    scopeMap.put(resource.getId().getName(), resource);
                }
                resources.put(scope, scopeMap);
            }

            return new ScriptGraph(Collections.unmodifiableMap(resources), Collections.unmodifiableMap(loadGroups));
        }

        /**
         * @return
         * @throws InvalidResourceException
         */
        public ScriptGraphBuilder validateDependencies(ResourceId id) throws InvalidResourceException {
            getResource(id).validateDependencies();
            return this;
        }
    }

    /** . */
    private final Map<ResourceScope, Map<String, ScriptResource>> resources;

    /** . */
    private final Map<String, ScriptGroup> loadGroups;

    /** . */
    private static final Log log = ExoLogger.getExoLogger(ScriptGraph.class);

    /** An empty singleton. */
    private static final ScriptGraph EMPTY = new ScriptGraph();
    /**
     * Returns an empty {@link ScriptGraph}.
     * @return an empty {@link ScriptGraph}
     */
    public static ScriptGraph empty() {
        return EMPTY;
    }

    /**
     * Returns a new {@link ScriptGraphBuilder} based on this {@link ScriptGraph} thus providing a
     * way to get a new {@link ScriptGraph} instance that is a modification of {@code this}.
     * @return
     */
    ScriptGraphBuilder newBuilder() {
        return new ScriptGraphBuilder(this.resources, this.loadGroups);
    }

    /**
     * Creates an empty {@link ScriptGraph}.
     */
    private ScriptGraph() {
        EnumMap<ResourceScope, Map<String, ScriptResource>> resources = new EnumMap<ResourceScope, Map<String, ScriptResource>>(
                ResourceScope.class);
        for (ResourceScope scope : ResourceScope.values()) {
            resources.put(scope, Collections.<String, ScriptResource> emptyMap());
        }

        this.resources = Collections.unmodifiableMap(resources);
        this.loadGroups = Collections.emptyMap();
    }

    /**
     * Creates a new {@link ScriptGraph} with provided {@code resources} and {@code loadGroups}.
     * Make sure you call this only with deeply immutable {@code resources} and {@code loadGroups}.
     * @param resources
     * @param loadGroups
     */
    private ScriptGraph(Map<ResourceScope, Map<String, ScriptResource>> resources, Map<String, ScriptGroup> loadGroups) {
        super();
        this.resources = resources;
        this.loadGroups = loadGroups;
    }

    /**
     * Resolve a collection of pair of resource id and fetch mode, each entry of the map will be processed in the order
     * specified by the iteration of the {@link java.util.Map#entrySet()}. For a given pair the fetch mode may be null or not.
     * When the fetch mode is null, the default fetch mode of the resource is used. When the fetch mode is not null, this fetch
     * mode may override the resource fetch mode if it implies this particular fetch mode. This algorithm tolerates the absence
     * of resourceBuilders, for instance if a resource is specified (among the pairs or by a transitive dependency) and does not exist,
     * the resource will be skipped.
     *
     * @param pairs the pairs to resolve
     * @return the resourceBuilders sorted
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
            if (mode != null && !resource.getFetchMode().equals(mode)) {
                return null;
            } else {
                mode = resource.getFetchMode();
                ScriptFetch fetch = map.get(id);
                if (fetch == null) {
                    fetch = new ScriptFetch(resource, mode);
                    if (!resource.isEmpty() || ResourceScope.SHARED.equals(resource.getId().getScope())) {
                        map.put(id, fetch);
                    }

                    // Recursively add the dependencies
                    if (FetchMode.IMMEDIATE.equals(mode) || resource.isEmpty()) {
                        for (ResourceId dependencyId : resource.getDependencies()) {
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

    public ScriptGroup getLoadGroup(String groupName) {
        return loadGroups.get(groupName);
    }


    /**
     * Creates a deep copy of this {@link ScriptGraph} then adds the given {@code scriptResourceDescriptors}
     * to the copy and finally returns the copy.
     *
     * {@link InvalidResourceException} is thrown if the addition of the given {@code scriptResourceDescriptors}
     * would break the internal consistency of the newly created {@link ScriptGraph}. Illegal Conditions include:
     * <ul>
     * <li>Adding a resource with {@link ResourceId} that is available in this {@link ScriptGraph} already
     * <li>Adding a resource with {@link ResourceId#getName()} containing {@value JavascriptConfigService.RESERVED_MODULE}
     * <li>Adding a resource with circular dependencies
     * <li>Adding a resource depending on another resource with an incompatible {@link FetchMode}
     * <li>Adding a resource depending on another resource that is not available in this {@link ScriptGraph}
     * <li>...
     * </ul>
     *
     * @param contextPath the servlet context path the {@code scriptResourceDescriptors} come from
     * @param scriptResourceDescriptors
     * @return see above
     * @throws InvalidResourceException see above
     */
    public ScriptGraph add(String contextPath, List<ScriptResourceDescriptor> scriptResourceDescriptors) throws InvalidResourceException {
        if (scriptResourceDescriptors == null || scriptResourceDescriptors.isEmpty()) {
            return this;
        } else {

            ScriptGraphBuilder graphBuilder = newBuilder();
            for (ScriptResourceDescriptor desc : scriptResourceDescriptors) {
                graphBuilder.addResource(contextPath, desc);
            }
            /* Check if the dependencies of resources just added are available */
            for (ScriptResourceDescriptor desc : scriptResourceDescriptors) {
                graphBuilder.validateDependencies(desc.getId());
            }
            return graphBuilder.build();
        }

    }

    /**
     * Creates a deep copy of this {@link ScriptGraph} then removes all resources from the copy
     * having the context path equal to the given {@code contextPath} and finally returns the copy.
     *
     * @param contextPath
     * @return see above
     */
    public ScriptGraph remove(String contextPath) {
        return newBuilder().removeResourcesByContextPath(contextPath).build();
    }

    /**
     * Performs several consistency checks on this {@code ScriptGraph}, such as:
     * <ul>
     * <li>Whether all {@link ScriptGroup}s registered in {@link #loadGroups} contain only
     * {@link ScriptResource}s available in {@link #resources}
     * <li>Whether all {@link ScriptGroup}s registered in {@link #loadGroups} are non-empty
     * <li>Whether all {@link ScriptResource}s registered in {@link #resources} refer to
     * {@link ScriptGroup}s available in {@link #loadGroups}
     * <li>Whether all {@link ScriptResource}s registered in {@link #resources} contain
     * only {@link ScriptResource}s in their dependencies and closures that are registered in
     * {@link #resources}
     * </ul>
     * This method actually contains test code called from {@link ScriptGraph}'s test cases.
     * This is the reason why it has default visibility. There is no need to call this method
     * from production code as no transformation doable through public methods of {@link ScriptGraph}
     * should lead to an invalid state.
     *
     * @return returns {@code this}
     * @throws InvalidResourceException if this {@link ScriptGraph} is inconsistent
     */
    ScriptGraph validate() throws InvalidResourceException {
        for (ScriptGroup group : loadGroups.values()) {
            Set<ResourceId> deps = group.getDependencies();
            for (ResourceId depId : deps) {
                if (!resources.get(depId.getScope()).containsKey(depId.getName())) {
                    throw new InvalidResourceException("Inconsistent ScriptGraph: ScriptGroup '"+ group.getId() +"' contains unavailable resource '"+ depId +"'.");
                }
            }
            if (deps.isEmpty()) {
                throw new InvalidResourceException("Inconsistent ScriptGraph: ScriptGroup '"+ group.getId() +"' is empty.");
            }
        }
        for (ResourceScope scope : ResourceScope.values()) {
            for (ScriptResource resource : resources.get(scope).values()) {
                ScriptGroup resourceGroup = resource.getGroup();
                if (resourceGroup != null) {
                    ScriptGroup loadGroup = loadGroups.get(resourceGroup.getId().getName());
                    if (loadGroup == null) {
                        throw new InvalidResourceException("Inconsistent ScriptGraph: ScriptGroup '"+ resourceGroup.getId() +"' of resource '"+ resource.getId() +"' is not registered in loadGroups map.");
                    }
                    if (!loadGroup.getDependencies().contains(resource.getId())) {
                        throw new InvalidResourceException("Inconsistent ScriptGraph: ScriptGroup '"+ resourceGroup.getId() +"' does not contain '"+ resource.getId() +"'. It definitely should because '"+ resourceGroup.getId() +"' refers to group '"+ resourceGroup.getId() +"' in its group field.");
                    }
                }
                /* deps */
                for (ResourceId depId : resource.getDependencies()) {
                    if (!resources.get(depId.getScope()).containsKey(depId.getName()) && !JavascriptConfigService.RESERVED_MODULE.contains(depId.getName())) {
                        /* This is was finally decided to be legal. See org.gatein.portal.controller.resource.script.ScriptResource.ScriptResourceBuilder.resourcesRemoved(String, HashSet<ResourceId>) */
                        if (log.isWarnEnabled()) {
                            log.warn("Inconsistent ScriptGraph: ScriptResource '"+ resource.getId() +"' depends on a non-existent resource '"+ depId +"'.");
                        }
                    }
                }
                /* closure */
                for (ResourceId depId : resource.getClosure()) {
                    if (!resources.get(depId.getScope()).containsKey(depId.getName())) {
                        throw new InvalidResourceException("Inconsistent ScriptGraph: ScriptResource '"+ resource.getId() +"' contains on a non-existent resource '"+ depId +"' in its closure.");
                    }
                }
            }
        }
        return this;
    }
}
