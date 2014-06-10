/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.web.application.javascript;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CompositeReader;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.InvalidResourceException;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.URIWriter;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.BaseScriptResource;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.Module;
import org.gatein.portal.controller.resource.script.ScriptGraph;
import org.gatein.portal.controller.resource.script.ScriptGroup;
import org.gatein.portal.controller.resource.script.ScriptResource;
import org.gatein.portal.controller.resource.script.ScriptResource.DepInfo;
import org.gatein.portal.controller.resource.script.StaticScriptResource;
import org.gatein.wci.WebApp;
import org.json.JSONArray;
import org.json.JSONObject;

public class JavascriptConfigService extends AbstractResourceService {
    /**
     * <a href="http://requirejs.org/docs/api.html#config-paths">require.js path mappings</a>
     * for module names not found directly under require.js's {@code baseUrl}.
     * For a given {@link #pathMappings} entry, the key is the prefix not found in under {@code baseUrl}
     * and the value is (possibly a portal-external) path to be used instead of the prefix.
     * The value of an entry is actually a {@link List} of substitute paths, to mirror the
     * <a href="http://requirejs.org/docs/api.html#pathsfallbacks">fallback paths</a>
     * feature of require.js.
     * <p>
     * Internally, a {@link LinkedHashMap} is used is used to store the prefix to target path mapping,
     * because the order of paths matters - they represent a fallback sequence tried in the given
     * order by require.js.
     * <p>
     * This class is deeply immutable, {@link #add(String, Map)} and {@link #remove(String)} methods
     * return a new {@link PathMappings} instance or {@code this} if there is nothing to change.
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     */
    static class PathMappings {
        private static final PathMappings EMPTY = new PathMappings();
        public static PathMappings empty() {
            return EMPTY;
        }

        /** Path prefixes are mapped to target paths. Always a {@link LinkedHashMap}
         * because the order matters - see above. */
        private final Map<String, List<String>> entries;

        /** A place to store which prefixes were registered from which servlet context. */
        private final Map<String, Set<String>> prefixesToContextPaths;

        /**
         * Both parameters must be immutable.
         *
         * @param entries
         * @param prefixesToContextPaths
         */
        private PathMappings(Map<String, List<String>> entries, Map<String, Set<String>> prefixesToContextPaths) {
            super();
            this.entries = entries;
            this.prefixesToContextPaths = prefixesToContextPaths;
        }

        public PathMappings() {
            this.entries = Collections.emptyMap();
            this.prefixesToContextPaths = Collections.emptyMap();
        }

        /**
         * Creates a new {@link PathMappings} instance by first copying {@link #entries} from
         * {@code this}, then adding all elements from {@code pathEntries} parameter to the
         * {@link #entries} of the new instance, returning the new instance.
         *
         * In this method, we are adding a map of path entries like
         * {@code ['/dojo' -> 'http://cdn.com/dojo', '/whatever' -> 'http://cdn.com/whatever']}.
         * Keys in that map are called <i>prefixes</i> and values are called <i>target paths</i>.
         * We are adding these entries to a map of entries that have been registered before and for
         * each of the entries that are being added, we check, if it breaks the internal consistency
         * of the newly created PathMappings. There are three cases possible:
         * <ol>
         * <li>If the prefix is still not available in {@link #entries}, the entry is
         * added to {@link #entries}</li>
         * <li>If the prefix is already available in {@link #entries} and the available
         * target path is equal to the one being added, the entry is also added to {@link #entries}</li>
         * <li>If the prefix is already available in {@link #entries} and the available
         * target path is not equal to the one being added, a {@link DuplicateResourceKeyException} is thrown.</li>
         * </ol>
         * @param contextPath the servlet context path
         * @param pathEntries the entries to add
         * @return see above
         * @throws DuplicateResourceKeyException if a key of an added entry is available
         *                          in {@link #pathMappings} and the available
         *                          target path is not equal to the one being added
         */
        public PathMappings add(String contextPath, Map<String, List<String>> pathEntries) throws DuplicateResourceKeyException {
            if (pathEntries == null || pathEntries.isEmpty()) {
                return this;
            } else {
                final Map<String, List<String>> newPrefixesToTargetPaths = new LinkedHashMap<String, List<String>>(this.entries);
                final Map<String, Set<String>> newPrefixesToContextPaths = new HashMap<String, Set<String>>(this.prefixesToContextPaths);
                for (Entry<String, List<String>> en : pathEntries.entrySet()) {
                    String prefix = en.getKey();
                    List<String> availableValue = newPrefixesToTargetPaths.get(prefix);
                    if (availableValue != null) {
                        if (availableValue.equals(en.getValue())) {
                            /* no need to add to newPrefixesToTargetPaths because it is already there
                             * just remember the present context path */
                            Set<String> newContextPaths = new HashSet<String>(newPrefixesToContextPaths.get(prefix));
                            newContextPaths.add(contextPath);
                            newPrefixesToContextPaths.put(prefix, Collections.unmodifiableSet(newContextPaths));
                        } else {
                            Set<String> contextPaths = newPrefixesToContextPaths.get(prefix);
                            throw new DuplicateResourceKeyException("Cannot accept path mapping entry " + en
                                    + " from servlet context '"+ contextPath
                                    +"' because the given prefix '"+ prefix +"' was already registered by servlet contexts "+ contextPaths +". The registered target path is "
                                    + availableValue);
                        }
                    } else {
                        /* The prefix is not available yet. */
                        if (log.isDebugEnabled()) {
                            log.debug("Adding path entry " + en);
                        }
                        newPrefixesToTargetPaths.put(prefix, Collections.unmodifiableList(new ArrayList<String>(en.getValue())));
                        newPrefixesToContextPaths.put(prefix, Collections.singleton(contextPath));
                    }
                }
                return new PathMappings(Collections.unmodifiableMap(newPrefixesToTargetPaths), Collections.unmodifiableMap(newPrefixesToContextPaths));
            }
        }

        /**
         * Creates a new {@link PathMappings} instance by copying {@link #entries} from {@code this},
         * removes all entries that were registered for the given {@code contextPath} from the
         * new instance and returns the new instance.
         *
         * @param contextPath the servlet context path
         * @return see above
         */
        public PathMappings remove(String contextPath) {
            Map<String, List<String>> newPrefixesToTargetPaths = this.entries;
            Map<String, Set<String>> newPrefixesToContextPaths = this.prefixesToContextPaths;
            for (Entry<String, Set<String>> en : this.prefixesToContextPaths.entrySet()) {
                String prefix = en.getKey();
                Set<String> contextPaths = en.getValue();
                if (contextPaths.contains(contextPath)) {
                    if (newPrefixesToTargetPaths == this.entries) {
                        /* we hit the first change, so prepare mutable objects */
                        newPrefixesToTargetPaths = new LinkedHashMap<String, List<String>>(this.entries);
                        newPrefixesToContextPaths = new HashMap<String, Set<String>>(this.prefixesToContextPaths);
                    }
                    switch (contextPaths.size()) {
                        case 0:
                            /* should never happen */
                            throw new IllegalStateException("contextPaths set should never have size 0");
                        case 1:
                            /* we are removing the last context that relied on this prefix
                             * hence we can remove the entry from newPrefixesToTargetPaths */
                            newPrefixesToTargetPaths.remove(prefix);
                            newPrefixesToContextPaths.remove(prefix);
                            break;
                        default:
                            /* copy the set and remove the present context path from it */
                            Set<String> newContextPaths = new HashSet<String>(newPrefixesToContextPaths.get(prefix));
                            newContextPaths.remove(contextPath);
                            newPrefixesToContextPaths.put(prefix, Collections.unmodifiableSet(newContextPaths));
                            break;
                    }
                }
            }
            if (newPrefixesToTargetPaths == this.entries) {
                return this;
            } else {
                return new PathMappings(Collections.unmodifiableMap(newPrefixesToTargetPaths), Collections.unmodifiableMap(newPrefixesToContextPaths));
            }
        }

        /**
         * @return the {@link #entries}
         */
        public Map<String, List<String>> getEntries() {
            return entries;
        }

        /**
         * Returns a {@link Set} of servlet context paths which registered the given {@code prefix}.
         * For testing purposes only, therefore the package visibility.
         *
         * @return {@link #prefixesToContextPaths}
         */
        Map<String, Set<String>> getPrefixesToContextPaths() {
            return prefixesToContextPaths;
        }
    }

    /**
     * A immutable collection of {@link StaticScriptResource}s.
     * <p>
     * Immutable because there may happen concurrent invocations of say
     * {@link JavascriptConfigService#remove(String)} and {@link JavascriptConfigService#getJSConfig(ControllerContext, Locale)}
     *
     * @see {@link ScriptResources#staticScriptResources}
     * @see {@link JavascriptConfigService#staticScriptResources}
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     *
     */
    static class StaticScriptResources {
        private static final StaticScriptResources EMPTY = new StaticScriptResources();

        /**
         * @return an empty immutable {@link Map}.
         */
        public static StaticScriptResources empty() {
            return EMPTY;
        }

        /**
         * A collection of {@link StaticScriptResource}s keyed by the given
         * {@link StaticScriptResource#getResourcePath()}
         */
        private final Map<String, StaticScriptResource> entries;

        /**
         * Creates a new builder based on the given {@code entries}. The values from {@code paths}
         * are copied into a new {@link HashMap}.
         *
         * @param entries
         */
        private StaticScriptResources(Map<String, StaticScriptResource> entries) {
            this.entries = entries;
        }

        private StaticScriptResources() {
            this.entries = Collections.emptyMap();
        }

        /**
         * Adds the all elements from {@code toAdd} to {@link #entries}. If a {@code resourcePath}
         * of an added entry is available in {@link #entries} as a key,
         * a {@link DuplicateResourceKeyException} is thrown.
         *
         * @param toAdd entries to add
         * @return
         * @throws DuplicateResourceKeyException if a {@code resourcePath} of an added entry is
         *          available in {@link #entries} as a key, a {@link DuplicateResourceKeyException} is thrown.
         */
        public StaticScriptResources add(Collection<StaticScriptResource> toAdd) throws DuplicateResourceKeyException {
            if (toAdd == null || toAdd.isEmpty()) {
                return this;
            } else {
                Map<String, StaticScriptResource> newStaticScriptResources = new HashMap<String, StaticScriptResource>(this.entries);
                for (StaticScriptResource staticScriptResource : toAdd) {
                    String resourcePath = staticScriptResource.getResourcePath();
                    StaticScriptResource availableValue = entries.get(resourcePath);
                    if (availableValue != null) {
                        throw new DuplicateResourceKeyException("Ignoring " + StaticScriptResource.class.getSimpleName() + " " + staticScriptResource
                                + " because the given resource path was already provided by " + availableValue);
                    } else {
                        /* add only if not there already */
                        if (log.isDebugEnabled()) {
                            log.debug("Adding " + staticScriptResource);
                        }
                        newStaticScriptResources.put(resourcePath, staticScriptResource);
                    }
                }
                return new StaticScriptResources(Collections.unmodifiableMap(newStaticScriptResources));
            }
        }

        /**
         * Copies this into a new {@link StaticScriptResources} instance, removes all entries
         * with the given {@code contextPath} from {@link #entries} of the new instance and returns
         * the new instance. Returns {@code this} if there is nothing to change.
         *
         * @param contextPath a servlet context path
         * @return
         */
        public StaticScriptResources remove(String contextPath) {
            Map<String, StaticScriptResource> newStaticScriptResources = this.entries;
            for (StaticScriptResource staticScriptResource : this.entries.values()) {
                if (staticScriptResource.getContextPath().equals(contextPath)) {
                    if (newStaticScriptResources == this.entries) {
                        /* we hit the first change, so prepare a mutable object */
                        newStaticScriptResources = new HashMap<String, StaticScriptResource>(this.entries);
                    }
                    newStaticScriptResources.remove(staticScriptResource.getResourcePath());
                }
            }
            if (newStaticScriptResources == this.entries) {
                return this;
            } else {
                return new StaticScriptResources(Collections.unmodifiableMap(newStaticScriptResources));
            }
        }

        /**
         * @return the entries
         */
        public Map<String, StaticScriptResource> getEntries() {
            return entries;
        }
    }

    private class ShutDownListener implements ContainerLifecyclePlugin {

        @Override
        public void stopContainer(ExoContainer container) throws Exception {
            log.debug("Will ignore cleanup on application undeploy from now on because the container is shutting down.");
            rootContainerShuttingDown = true;
        }

        @Override
        public void startContainer(ExoContainer container) throws Exception {
        }

        @Override
        public void setName(String s) {
        }

        @Override
        public void setInitParams(InitParams params) {
        }

        @Override
        public void setDescription(String s) {
        }

        @Override
        public void initContainer(ExoContainer container) throws Exception {
        }

        @Override
        public String getName() {
            return getClass().getName();
        }

        @Override
        public InitParams getInitParams() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void destroyContainer(ExoContainer container) throws Exception {
        }
    }

    /** Our logger. */
    private static final Log log = ExoLogger.getLogger(JavascriptConfigService.class);

    /** The scripts. */
    private ScriptGraph scripts;

    /**
     * require.js path mappings.
     *
     * @see PathMappings
     */
    private PathMappings pathMappings;

    /** A collection of {@link StaticScriptResource}s. */
    private StaticScriptResources staticScriptResources;

    /**
     * @see #getSharedBaseUrl(ControllerContext)
     */
    private volatile String sharedBaseUrl;

    private boolean rootContainerShuttingDown = false;

    /** . */
    public static final List<String> RESERVED_MODULE = Arrays.asList("require", "exports", "module");

    /** . */
    private static final Pattern INDEX_PATTERN = Pattern.compile("^.+?(_([1-9]+))$");

    public static final Pattern JS_ID_PATTERN = Pattern.compile("^[a-zA-Z_$][0-9a-zA-Z_$]*$");

    /** . */
    public static final Comparator<Module> MODULE_COMPARATOR = new Comparator<Module>() {
        public int compare(Module o1, Module o2) {
            return o1.getPriority() - o2.getPriority();
        }
    };

    public JavascriptConfigService(ExoContainerContext context, ResourceCompressor compressor) {
        super(compressor);

        this.scripts = ScriptGraph.empty();
        this.pathMappings = PathMappings.empty();
        this.staticScriptResources = StaticScriptResources.empty();
        RootContainer.getInstance().addContainerLifecylePlugin(new ShutDownListener());

    }

    public Reader getScript(ResourceId resourceId, Locale locale) throws Exception {
        if (ResourceScope.GROUP.equals(resourceId.getScope())) {
            ScriptGroup loadGroup = scripts.getLoadGroup(resourceId.getName());
            if (loadGroup != null) {
                List<Reader> readers = new ArrayList<Reader>(loadGroup.getDependencies().size());
                for (ResourceId id : loadGroup.getDependencies()) {
                    Reader rd = getScript(id, locale);
                    if (rd != null) {
                        readers.add(new StringReader("\n//Begin " + id));
                        readers.add(rd);
                        readers.add(new StringReader("\n//End " + id));
                    }
                }
                return new CompositeReader(readers);
            } else {
                return null;
            }
        } else {
            ScriptResource resource = getResource(resourceId);

            if (resource != null) {
                List<Module> modules = new ArrayList<Module>(resource.getModules());

                Collections.sort(modules, MODULE_COMPARATOR);
                ArrayList<Reader> readers = new ArrayList<Reader>(modules.size() * 2);
                StringBuilder buffer = new StringBuilder();

                //
                boolean isModule = FetchMode.ON_LOAD.equals(resource.getFetchMode());

                if (resource.isNativeAmd()) {
                    /* nothing to do for an AMD module */
                    // buffer.append("/* native AMD module */\n");
                } else if (isModule) {
                    Set<ResourceId> depResourceIds = resource.getDependencies();
                    int argCount = depResourceIds.size();
                    JSONArray deps = new JSONArray();

                    LinkedList<String> params = new LinkedList<String>();
                    List<String> argNames = new ArrayList<String>(argCount);
                    List<String> argValues = new ArrayList<String>(argCount);
                    for (ResourceId id : depResourceIds) {
                        ScriptResource dep = getResource(id);
                        if (dep != null) {
                            Set<DepInfo> depInfos = resource.getDepInfo(id);
                            for (DepInfo info : depInfos) {
                                String pluginRS = info.getPluginRS();
                                String alias = info.getAlias();
                                if (alias == null) {
                                    alias = dep.getAlias();
                                }

                                deps.put(parsePluginRS(dep.getId().toString(), pluginRS));
                                params.add(encode(params, alias));
                                argNames.add(parsePluginRS(alias, pluginRS));
                            }
                        } else if (RESERVED_MODULE.contains(id.getName())) {
                            String reserved = id.getName();
                            deps.put(reserved);
                            params.add(reserved);
                            argNames.add(reserved);
                        }
                    }
                    argValues.addAll(params);
                    int reserveIdx = argValues.indexOf("require");
                    if (reserveIdx != -1) {
                        argValues.set(reserveIdx, "eXo.require");
                    }

                    //
                    buffer.append("\ndefine('").append(resourceId).append("', ");
                    buffer.append(deps);
                    buffer.append(", function(");
                    buffer.append(StringUtils.join(params, ","));
                    buffer.append(") {\nvar require = eXo.require, requirejs = eXo.require,define = eXo.define;");
                    buffer.append("\neXo.define.names=").append(new JSONArray(argNames)).append(";");
                    buffer.append("\neXo.define.deps=[").append(StringUtils.join(argValues, ",")).append("]").append(";");
                    buffer.append("\nreturn ");
                }

                final WebApp app = contexts.get(resource.getContextPath());
                for (Module js : modules) {
                    Reader jScript = getJavascript(app, js, locale);
                    if (jScript != null) {
                        readers.add(new StringReader(buffer.toString()));
                        buffer.setLength(0);
                        readers.add(new NormalizeJSReader(jScript));
                    }
                }

                if (resource.isNativeAmd()) {
                    /* nothing to do for an AMD module */
                    //buffer.append("\n");
                } else if (isModule) {
                    buffer.append("\n});");
                } else {
                    buffer.append("\nif (typeof define === 'function' && define.amd && !require.specified('")
                            .append(resource.getId()).append("')) {");
                    buffer.append("define('").append(resource.getId()).append("');}");
                }
                readers.add(new StringReader(buffer.toString()));

                return new CompositeReader(readers);
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public String generateURL(ControllerContext controllerContext, ResourceId id, boolean merge, boolean minified, Locale locale)
            throws IOException {
        @SuppressWarnings("rawtypes")
        BaseScriptResource resource = null;
        if (ResourceScope.GROUP.equals(id.getScope())) {
            resource = scripts.getLoadGroup(id.getName());
        } else {
            resource = getResource(id);
        }

        //
        if (resource != null) {
            if (resource instanceof ScriptResource) {
                ScriptResource rs = (ScriptResource) resource;

                List<Module> modules = rs.getModules();
                if (modules.size() > 0 && modules.get(0) instanceof Module.Remote) {
                    return ((Module.Remote) modules.get(0)).getURI();
                }
            }

            StringBuilder buffer = new StringBuilder();
            URIWriter writer = new URIWriter(buffer);
            controllerContext.renderURL(resource.getParameters(minified, locale), writer);
            return buffer.toString();
        } else {
            return null;
        }
    }

    public Map<ScriptResource, FetchMode> resolveIds(Map<ResourceId, FetchMode> ids) {
        return scripts.resolve(ids);
    }


    public JSONObject getJSConfig(ControllerContext controllerContext, Locale locale) throws Exception {

        JSONObject paths = new JSONObject();
        JSONObject shim = new JSONObject();

        for (Entry<String, List<String>> en : this.pathMappings.getEntries().entrySet()) {
            String prefix = en.getKey();
            List<String> pathValues = en.getValue();
            switch (pathValues.size()) {
                case 0:
                    /* This should never happen as it is forbidden in gatein_resources XSD */
                    throw new IllegalStateException("Unexpected empty target path list for prefix '"+ prefix +"'.");
                case 1:
                    paths.put(prefix, pathValues.get(0));
                    break;
                default:
                    paths.put(prefix, pathValues);
                    break;
            }
        }

        Map<ResourceId, String> groupURLs = new HashMap<ResourceId, String>();
        for (ScriptResource resource : getAllResources()) {
            if (!resource.isNativeAmd() /* exclude native AMD modules to reduce the size
                                         * of the HTTP response as there may be thosands of them
                                         * They are always SHARED so there is no need to put their URLs here
                                         * explicitly. */
                    && (!resource.isEmpty() || ResourceScope.SHARED.equals(resource.getId().getScope()))) {
                String name = resource.getId().toString();
                List<Module> modules = resource.getModules();

                if (FetchMode.IMMEDIATE.equals(resource.getFetchMode())
                        || (modules.size() > 0 && modules.get(0) instanceof Module.Remote)) {
                    JSONArray deps = new JSONArray();
                    for (ResourceId id : resource.getDependencies()) {
                        deps.put(id);
                    }
                    if (deps.length() > 0) {
                        shim.put(name, new JSONObject().put("deps", deps));
                    }
                }

                String url;
                ScriptGroup group = resource.getGroup();
                if (group != null) {
                    ResourceId grpId = group.getId();
                    url = groupURLs.get(grpId);
                    if (url == null) {
                        url = buildURL(grpId, controllerContext, locale);
                        groupURLs.put(grpId, url);
                    }
                } else {
                    url = buildURL(resource.getId(), controllerContext, locale);
                }
                paths.put(name, url);
            }
        }

        JSONObject config = new JSONObject();
        String sharedBaseUrl = getSharedBaseUrl(controllerContext);
        if (sharedBaseUrl != null) {
            config.put("baseUrl", sharedBaseUrl);
        }
        config.put("paths", paths);
        config.put("shim", shim);
        return config;
    }

    public ScriptResource getResource(ResourceId resource) {
        return scripts.getResource(resource);
    }

    private Reader getJavascript(WebApp webApp, Module module, Locale locale) {
        if (module instanceof Module.Local) {
            Module.Local localModule = (Module.Local) module;
            ServletContext sc = webApp.getServletContext();
            return localModule.read(locale, sc, webApp.getClassLoader());
        }
        return null;
    }

    private String buildURL(ResourceId id, ControllerContext context, Locale locale) throws Exception {
        String url = generateURL(context, id, !PropertyManager.isDevelopping(), !PropertyManager.isDevelopping(), locale);

        if (url != null && url.endsWith(".js")) {
            return url.substring(0, url.length() - ".js".length());
        } else {
            return null;
        }
    }

    private List<ScriptResource> getAllResources() {
        List<ScriptResource> resources = new LinkedList<ScriptResource>();
        for (ResourceScope scope : ResourceScope.values()) {
            resources.addAll(scripts.getResources(scope));
        }
        return resources;
    }

    private String encode(LinkedList<String> params, String alias) {
        alias = alias.replace("/", "_");
        Matcher validMatcher = JS_ID_PATTERN.matcher(alias);
        if (!validMatcher.matches()) {
            log.error("alias {} is not valid, changing to default 'alias' name", alias);
            alias = "alias";
        }

        //
        int idx = -1;
        Iterator<String> iterator = params.descendingIterator();
        while (iterator.hasNext()) {
            String param = iterator.next();
            Matcher matcher = INDEX_PATTERN.matcher(param);
            if ( matcher.matches()) {
                if (param.replace(matcher.group(1), "").equals(alias)) {
                    idx = Integer.parseInt(matcher.group(2));
                    break;
                }
            } else if (alias.equals(param)) {
                idx = 0;
                break;
            }
        }
        if (idx != -1) {
            StringBuilder tmp = new StringBuilder(alias);
            tmp.append("_").append(idx + 1);
            String a = tmp.toString();
            log.warn("alias {} is duplicated, adding index: {}", alias, a);
            return a;
        } else {
            return alias;
        }
    }

    private String parsePluginRS(String name, String pluginRS) {
        StringBuilder depBuild = new StringBuilder(name);
        if (pluginRS != null) {
            depBuild.append("!").append(pluginRS);
        }
        return depBuild.toString();
    }

    private class NormalizeJSReader extends Reader {
        private boolean finished = false;
        private boolean multiComments = false;
        private boolean singleComment = false;
        private Reader sub;

        public NormalizeJSReader(Reader sub) {
            this.sub = sub;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (finished) {
                return sub.read(cbuf, off, len);
            } else {
                char[] buffer = new char[len];
                int relLen = sub.read(buffer, 0, len);
                if (relLen == -1) {
                    finished = true;
                    return -1;
                } else {
                    int r = off;

                    for (int i = 0; i < relLen; i++) {
                        char c = buffer[i];

                        char next = 0;
                        boolean skip = false, overflow = (i + 1 == relLen);
                        if (!finished) {
                            skip = true;
                            if (!singleComment && c == '/' && (next = readNext(buffer, i, overflow)) == '*') {
                                multiComments = true;
                                i++;
                            } else if (!singleComment && c == '*' && (next = readNext(buffer, i, overflow)) == '/') {
                                multiComments = false;
                                i++;
                            } else if (!multiComments && c == '/' && next == '/') {
                                singleComment = true;
                                i++;
                            } else if (c == '\n') {
                                singleComment = false;
                            } else if (!Character.isWhitespace(c) && !Character.isSpaceChar(c) && !Character.isISOControl(c)) {
                                skip = false;
                            }

                            if (!skip && !multiComments && !singleComment) {
                                if (next != 0 && overflow) {
                                    sub = new CompositeReader(new StringReader(String.valueOf(c)), sub);
                                }
                                cbuf[r++] = c;
                                finished = true;
                            }
                        } else {
                            cbuf[r++] = c;
                        }
                    }
                    return r - off;
                }
            }
        }

        private char readNext(char[] buffer, int i, boolean overflow) throws IOException {
            char c = 0;
            if (overflow) {
                int tmp = sub.read();
                if (tmp != -1) {
                    c = (char) tmp;
                }
            } else {
                c = buffer[i + 1];
            }
            return c;
        }

        @Override
        public void close() throws IOException {
            sub.close();
        }
    }

    /**
     * Returns a value equivalent to
     * <pre>"/"+ defaultPortalContext
     * + "/"+ ResourceRequestHandler.SCRIPT_HANDLER_NAME
     * + "/"+ ResourceRequestHandler.VERSION
     * + "/"+ ResourceScope.SHARED.name()</pre>
     *
     * This value is used as {@code baseUrl} in the configuration of requireJS javascript
     * loader on the client side.
     *
     * Rather than concatenating the above values this method uses
     * {@link BaseScriptResource#createBaseParameters(ResourceScope, String)} and delegates to
     * {@link ControllerContext#renderURL(Map, URIWriter)} which seems to be safer for
     * any future changes.
     *
     * The value computed once is stored in {@link JavascriptConfigService#sharedBaseUrl} and
     * re-used upon subsequent calls.
     *
     * @param controllerContext
     * @return
     * @throws Exception
     */
    private String getSharedBaseUrl(ControllerContext controllerContext) throws Exception {
        if (this.sharedBaseUrl == null) {
            /* Let's accept some harmless race conditions here rather than syncing explicitly.
             * It does not matter if this.sharedBaseUrl gets initialized several times
             * concurrently as the result will be the same every time.*/

            Map<QualifiedName, String> baseParams = BaseScriptResource.createBaseParameters(ResourceScope.SHARED, "fake");

            /* 52 is the length of /portal/scripts/3.8.0.Beta01-SNAPSHOT/SHARED/fake.js
             * it should be a little bit more than necessary in most cases */
            StringBuilder buffer = new StringBuilder(52);
            URIWriter writer = new URIWriter(buffer);
            controllerContext.renderURL(baseParams, writer);

            if (buffer.length() < 2) {
                throw new IllegalStateException("sharedBaseUrl too short: '"+ buffer.toString() +"'");
            }
            /* There is no StringBuilder.lastIndexOf(char) let's loop manually */
            int lastSlash = -1;
            for (int i = buffer.length() -1; i >= 0; i--) {
                if (buffer.charAt(i) == '/') {
                    lastSlash = i;
                    break;
                }
            }
            if (lastSlash < 0) {
                throw new IllegalStateException("No slash in '"+ buffer.toString() +"'");
            }
            this.sharedBaseUrl = buffer.substring(0, lastSlash);
        }
        return this.sharedBaseUrl;
    }

    /**
     * Equivalent to {@code staticScriptResources.getEntries().get(resourcePath)}.
     * See {@link StaticScriptResources#entries} and {@link StaticScriptResource#getResourcePath()}
     *
     * @param resourcePath see {@link StaticScriptResource#getResourcePath()}
     * @return
     */
    public StaticScriptResource getStaticScriptResource(String resourcePath) {
        return staticScriptResources.getEntries().get(resourcePath);
    }

    /**
     * Adds the entities provided in the given {@link ScriptResources} to this
     * {@link JavascriptConfigService}.
     *
     * {@link InvalidResourceException} is thrown if the addition of the given {@code scriptResources}
     * would break the internal consistency of this {@link JavascriptConfigService}. See the
     * documentation of the following methods to see which specific conditions are illegal:
     * {@link StaticScriptResources#add(Collection)}, {@link PathMappings#add(String, Map)}
     * and {@link ScriptGraph#add(String, List)}.
     *
     * In case this method throws an {@link InvalidResourceException}, the the internal state of
     * this {@link JavascriptConfigService} stays as it was before the invocation of this method.
     *
     * @param scriptResources entities to add to this {@link JavascriptConfigService}
     * @throws InvalidResourceException see above.
     */
    public void add(ScriptResources scriptResources) throws InvalidResourceException {

        /* Combine the present resources with the ones being added */
        StaticScriptResources newStaticScriptResources = this.staticScriptResources.add(scriptResources.getStaticScriptResources());

        /* Combine the present paths with the ones being added */
        PathMappings newPaths = this.pathMappings.add(scriptResources.getContextPath(), scriptResources.getPaths());

        /* We might get an exception here, if there are resources in getScriptResourceDescriptors
         * which were registered already in the past by other apps. If that is the case, the
         * deployment will be interrupted, and the state of this is the same as before the call */
        this.scripts = this.scripts.add(scriptResources.getContextPath(), scriptResources.getScriptResourceDescriptors());

        /* No exception was thrown, now we can at once assign these two local variables to the fields of this service */
        this.staticScriptResources = newStaticScriptResources;
        this.pathMappings = newPaths;

    }

    /**
     * Removes the entities provided in the given {@link ScriptResources} from this
     * {@link JavascriptConfigService}.
     *
     * @param contextPath for which which context path the script resources should be removed
     */
    public void remove(String contextPath) {
        final boolean debug = log.isDebugEnabled();
        if (this.rootContainerShuttingDown) {
            if (debug) {
                log.debug("Going without script cleanup for context '"+ contextPath +"' because the container is shutting down.");
            }
        } else {
            if (debug) {
                log.debug("Removing scripts coming from context '"+ contextPath +"'.");
            }
            this.scripts = this.scripts.remove(contextPath);
            this.staticScriptResources = this.staticScriptResources.remove(contextPath);
            this.pathMappings = this.pathMappings.remove(contextPath);
        }
    }

}
