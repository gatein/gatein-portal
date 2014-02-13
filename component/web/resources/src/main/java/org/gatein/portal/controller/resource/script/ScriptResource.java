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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.script.Module.Local.Content;

/**
 * <p>
 * </p>
 *
 * <p>
 * </p>
 * This class implements the {@link Comparable} interface, however the natural ordering provided here is not consistent with
 * equals, therefore this class should not be used as a key in a {@link java.util.TreeMap} for instance.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScriptResource extends BaseScriptResource<ScriptResource> implements Comparable<ScriptResource> {

    private final Log log = ExoLogger.getLogger(ScriptResource.class);

    /** . */
    private final List<Module> modules;

    /** . */
    final HashMap<ResourceId, Set<DepInfo>> dependencies;

    /** . */
    final HashSet<ResourceId> closure;

    /** . */
    FetchMode fetchMode;

    /** . */
    final String alias;

    /** . */
    final ScriptGroup group;

    ScriptResource(ScriptGraph graph, ResourceId id, FetchMode fetchMode) {
        this(graph, id, fetchMode, null, null);
    }

    ScriptResource(ScriptGraph graph, ResourceId id, FetchMode fetchMode, String alias, ScriptGroup group) {
        super(graph, id);

        this.modules = new ArrayList<Module>();
        this.closure = new HashSet<ResourceId>();
        this.dependencies = new LinkedHashMap<ResourceId, Set<DepInfo>>();
        this.fetchMode = fetchMode;

        if (alias == null) {
            String resName = id.getName();
            alias = resName.substring(resName.lastIndexOf("/") + 1);
        }
        if (FetchMode.ON_LOAD.equals(fetchMode)) {
            Matcher validMatcher = JavascriptConfigService.JS_ID_PATTERN.matcher(alias);
            if (!validMatcher.matches()) {
                log.warn("alias {} is not valid JS identifier", alias);
            }
        }
        this.alias = alias;
        this.group = group;
    }

    public boolean isEmpty() {
        return modules.isEmpty();
    }

    public FetchMode getFetchMode() {
        return fetchMode;
    }

    public void addDependency(ResourceId dependencyId) {
        addDependency(dependencyId, null, null);
    }

    public void addDependency(ResourceId dependencyId, String alias, String pluginRS) {
        ScriptResource dependency = graph.getResource(dependencyId);

        if (dependency != null) {
            if (!fetchMode.equals(dependency.getFetchMode())) {
                throw new IllegalStateException("ScriptResource " + id + " can't depend on " + dependency.getId()
                        + ". They have difference fetchMode");
            } else if (dependency.closure.contains(id)) {
                // Detect cycle
                throw new IllegalStateException("Adding script dependency "+ dependency.id +" to "+ id +" would introduce a circle");
            }
        }

        // That is important to make closure independent from building order of graph nodes.
        if (dependency != null) {
            closure.addAll(dependency.getClosure());
        }

        // Update the source's closure
        closure.add(dependencyId);

        // Update any entry that points to the source
        for (Map<String, ScriptResource> resources : graph.resources.values()) {
            for (ScriptResource resource : resources.values()) {
                if (resource.closure.contains(id)) {
                    resource.closure.addAll(closure);
                }
            }
        }

        //
        Set<DepInfo> infos = dependencies.get(dependencyId);
        if (infos == null) {
            dependencies.put(dependencyId, infos = new LinkedHashSet<DepInfo>());
        }
        infos.add(new DepInfo(alias, pluginRS));
    }

    public Set<ResourceId> getClosure() {
        return closure;
    }

    public Module.Local addLocalModule(String contextPath, String path, String resourceBundle, int priority) {
        return addLocalModule(contextPath, new Content[] { new Content(path) }, resourceBundle, priority);
    }

    public Module.Local addLocalModule(String contextPath, Content[] contents, String resourceBundle, int priority) {
        Module.Local module = new Module.Local(this, contextPath, contents, resourceBundle, priority);
        modules.add(module);
        return module;
    }

    public Module.Remote addRemoteModule(String contextPath, String path, int priority) {
        Module.Remote module = new Module.Remote(this, contextPath, path, priority);
        modules.add(module);
        return module;
    }

    @Override
    public void addSupportedLocale(Locale locale) {
        super.addSupportedLocale(locale);
        if (group != null) {
            group.addSupportedLocale(locale);
        }
    }

    public List<Module> removeModuleByContextPath(String contextPath) {
        ArrayList<Module> removed = new ArrayList<Module>();
        for (Iterator<Module> i = modules.iterator(); i.hasNext();) {
            Module module = i.next();
            if (module.getContextPath().equals(contextPath)) {
                removed.add(module);
                i.remove();
            }
        }
        return removed;
    }

    public List<Module> getModules() {
        return modules;
    }

    public int compareTo(ScriptResource o) {
        if (closure.contains(o.id)) {
            return 1;
        } else if (o.closure.contains(id)) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public Set<ResourceId> getDependencies() {
        return dependencies.keySet();
    }

    public Set<DepInfo> getDepInfo(ResourceId id) {
        return dependencies.get(id);
    }

    /**
     * If no alias was set, return the last part of the resource name If resourceID is null, return null
     */
    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "ScriptResource[id=" + id + "]";
    }

    public ScriptGroup getGroup() {
        return group;
    }

    public class DepInfo {
        final String alias;
        final String pluginRS;

        DepInfo(String alias, String pluginRS) {
            this.alias = alias;
            this.pluginRS = pluginRS;
        }

        public String getAlias() {
            return alias;
        }

        public String getPluginRS() {
            return pluginRS;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((alias == null) ? 0 : alias.hashCode());
            result = prime * result + ((pluginRS == null) ? 0 : pluginRS.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || !(obj instanceof DepInfo))
                return false;
            DepInfo other = (DepInfo) obj;
            return ((alias == other.alias || alias != null && alias.equals(other.alias)) && (pluginRS == other.pluginRS || pluginRS != null
                    && pluginRS.equals(other.pluginRS)));
        }
    }
}
