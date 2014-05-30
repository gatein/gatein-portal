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
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.javascript.ScriptResources.ImmutableScriptResources;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.URIWriter;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceRequestHandler;
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
     * A builder for producing immutable paths {@link Map}s.
     *
     * @see {@link ScriptResources#paths}
     * @see {@link JavascriptConfigService#paths}
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     */
    static class ImmutablePathsBuilder {
        /**
         * @return an empty immutable {@link Map}.
         */
        public static Map<String, List<String>> buildEmpty() {
            return Collections.emptyMap();
        }

        /**
         * The result for {@link #build()} is collected here.
         */
        private final Map<String, List<String>> paths;

        /**
         * Creates a new builder based on the given {@code paths}. The values from {@code paths}
         * are deeply copied into a new {@link LinkedHashMap}. It is a {@link LinkedHashMap} because
         * the order of paths matters - they represent a fallback sequence tried in the given order.
         *
         * @param paths the initial {@link #paths}
         */
        public ImmutablePathsBuilder(Map<String, List<String>> paths) {
            super();
            this.paths = new LinkedHashMap<String, List<String>>(paths);
            for (Map.Entry<String, List<String>> en : this.paths.entrySet()) {
                en.setValue(Collections.unmodifiableList(new ArrayList<String>(en.getValue())));
            }
        }

        /**
         * Adds the all elements from {@code pathEntries} to {@link #paths}. If a key of an added entry
         * is available in {@link #paths}, a {@link DuplicateResourceKeyException} is thrown.
         *
         * @param pathEntries
         * @return see above
         * @throws DuplicateResourceKeyException if a key of an added entry is available in {@link #paths}
         */
        public ImmutablePathsBuilder add(Map<String, List<String>> pathEntries) throws DuplicateResourceKeyException {
            for (Entry<String, List<String>> en : pathEntries.entrySet()) {
                List<String> availableValue = this.paths.get(en.getKey());
                if (availableValue != null) {
                    throw new DuplicateResourceKeyException("Ignoring path entry " + en + " because the given resource path was already provided: "
                            + availableValue);
                } else {
                    /* add only if not there already */
                    if (log.isDebugEnabled()) {
                        log.debug("Adding path entry " + en);
                    }
                    this.paths.put(en.getKey(), Collections.unmodifiableList(new ArrayList<String>(en.getValue())));
                }
            }
            return this;
        }

        /**
         * @return a new immutable paths {@link Map} that can be used in {@link JavascriptConfigService#paths}.
         */
        public Map<String, List<String>> build() {
            return Collections.unmodifiableMap(paths);
        }

        /**
         * Removes all keys given in {@code keysToRemove} from the underlying {@link #paths}.
         *
         * @param keysToRemove
         * @return
         */
        public ImmutablePathsBuilder removeAll(Collection<String> keysToRemove) {
            for (String prefix : keysToRemove) {
                paths.remove(prefix);
            }
            return this;
        }
    }

    /**
     * A builder for producing immutable static script resources {@link Map}s.
     *
     * @see {@link ScriptResources#staticScriptResources}
     * @see {@link JavascriptConfigService#staticScriptResources}
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     *
     */
    static class ImmutableStaticScriptResourcesBuilder {
        /**
         * @return an empty immutable {@link Map}.
         */
        public static Map<String, StaticScriptResource> buildEmpty() {
            return Collections.emptyMap();
        }

        /**
         * The result for {@link #build()} is collected here.
         */
        private final Map<String, StaticScriptResource> staticScriptResources;

        /**
         * Creates a new builder based on the given {@code staticScriptResources}. The values from {@code paths}
         * are copied into a new {@link HashMap}.
         *
         * @param staticScriptResources
         */
        public ImmutableStaticScriptResourcesBuilder(Map<String, StaticScriptResource> staticScriptResources) {
            super();
            this.staticScriptResources = new HashMap<String, StaticScriptResource>(staticScriptResources);
        }

        /**
         * Adds the all elements from {@code toAdd} to {@link #staticScriptResources}. If a {@code resourcePath}
         * of an added entry is available in {@link #staticScriptResources} as a key,
         * a {@link DuplicateResourceKeyException} is thrown.
         *
         * @param toAdd entries to add
         * @return
         * @throws DuplicateResourceKeyException if a {@code resourcePath} of an added entry is
         *          available in {@link #staticScriptResources} as a key, a {@link DuplicateResourceKeyException} is thrown.
         */
        public ImmutableStaticScriptResourcesBuilder add(Collection<StaticScriptResource> toAdd) throws DuplicateResourceKeyException {
            for (StaticScriptResource staticScriptResource : toAdd) {
                String resourcePath = staticScriptResource.getResourcePath();
                StaticScriptResource availableValue = staticScriptResources.get(resourcePath);
                if (availableValue != null) {
                    throw new DuplicateResourceKeyException("Ignoring " + StaticScriptResource.class.getSimpleName() + " " + staticScriptResource
                            + " because the given resource path was already provided by " + availableValue);
                } else {
                    /* add only if not there already */
                    if (log.isDebugEnabled()) {
                        log.debug("Adding " + staticScriptResource);
                    }
                    staticScriptResources.put(resourcePath, staticScriptResource);
                }
            }
            return this;
        }

        /**
         * @return a new immutable staticScriptResources {@link Map} that can be used in {@link JavascriptConfigService#staticScriptResources}.
         */
        public Map<String, StaticScriptResource> build() {
            return Collections.unmodifiableMap(staticScriptResources);
        }

        /**
         * Removes all entries given in {@code toRemove} from the underlying {@link #staticScriptResources}.
         * @param toRemove
         * @return
         */
        public ImmutableStaticScriptResourcesBuilder removeAll(Collection<StaticScriptResource> toRemove) {
            for (StaticScriptResource staticScriptResource : toRemove) {
                staticScriptResources.remove(staticScriptResource.getResourcePath());
            }
            return this;
        }
    }

    /** Our logger. */
    private static final Log log = ExoLogger.getLogger(JavascriptConfigService.class);

    /** The scripts. */
    private final ScriptGraph scripts;

    /**
     * <a href="http://requirejs.org/docs/api.html#config-paths">require.js path mappings</a>
     *  for module names not found directly under require.js's {@code baseUrl}.
     * For a given {@link #paths} entry, the key is the prefix not found in under {@code baseUrl}
     * and the value is (possibly a portal-external) path to be used instead of the prefix.
     * The value of an entry is actually a {@link List} of substitute paths, to mirror the
     * <a href="http://requirejs.org/docs/api.html#pathsfallbacks">fallback paths</a>
     * feature of require.js.
     * <p>
     * Internally, a {@link LinkedHashMap} is used, because the order of paths matters - they
     * represent a fallback sequence tried in the given order by require.js.
     * <p>
     * Within {@link JavascriptConfigService}, {@link #paths} is always assigned a deeply immutable instance.
     *
     * {@link Map}. This is because there may happen concurrent invocations of say
     * {@link #remove(ImmutableScriptResources, String)} and {@link #getJSConfig(ControllerContext, Locale)}.
     */
    private Map<String, List<String>> paths;

    /** A collection of {@link StaticScriptResource}s keyed by the given
     * {@link StaticScriptResource#getResourcePath()}
     * <p>
     * Within {@link JavascriptConfigService}, {@link #paths} is always assigned a deeply immutable
     * {@link Map}. This is because there may happen concurrent invocations of say
     * {@link #remove(ImmutableScriptResources, String)} and {@link #getJSConfig(ControllerContext, Locale)}.
     */
    private Map<String, StaticScriptResource> staticScriptResources;

    /**
     * @see #getSharedBaseUrl(ControllerContext)
     */
    private volatile String sharedBaseUrl;

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

        //
        this.scripts = new ScriptGraph();
        this.paths = ImmutablePathsBuilder.buildEmpty();
        this.staticScriptResources = ImmutableStaticScriptResourcesBuilder.buildEmpty();
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

                //
                for (Module js : modules) {
                    Reader jScript = getJavascript(js, locale);
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

        for (Entry<String, List<String>> en : this.paths.entrySet()) {
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
                        deps.put(getResource(id).getId());
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

    private Reader getJavascript(Module module, Locale locale) {
        if (module instanceof Module.Local) {
            Module.Local localModule = (Module.Local) module;
            final WebApp webApp = contexts.get(localModule.getContextPath());
            if (webApp != null) {
                ServletContext sc = webApp.getServletContext();
                return localModule.read(locale, sc, webApp.getClassLoader());
            }
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
     * {@link BaseScriptResource#createBaseParameters()} and delegates to
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

            Map<QualifiedName, String> baseParams = BaseScriptResource.createBaseParameters();
            baseParams.put(ResourceRequestHandler.SCOPE_QN, ResourceScope.SHARED.name());
            baseParams.put(ResourceRequestHandler.RESOURCE_QN, "fake");

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
     * Equivalent to {@code staticScriptResources.get(resourcePath)}.
     * See {@link #staticScriptResources} and {@link StaticScriptResource#getResourcePath()}
     *
     * @param resourcePath see {@link StaticScriptResource#getResourcePath()}
     * @return
     */
    public StaticScriptResource getStaticScriptResource(String resourcePath) {
        return staticScriptResources.get(resourcePath);
    }

    /**
     * Adds the entities provided in the given {@link ScriptResources} to this
     * {@link JavascriptConfigService}. The object provided in the {@code scriptResources} parameter can
     * be modified during the call. Namely, the values (paths and static script resources) already
     * available in this {@link JavascriptConfigService} are removed from the object. The purpose of
     * this is to inform the caller which entities were actually added (and can thus be safely
     * removed later).
     *
     * @param scriptResources entities to add to this {@link JavascriptConfigService}
     */
    public void add(ScriptResources scriptResources) throws DuplicateResourceKeyException {
        Map<String, StaticScriptResource> newStaticScriptResources = null;
        Map<String, List<String>> newPaths = null;

        List<StaticScriptResource> toAddStaticScriptResources = scriptResources.getStaticScriptResources();
        if (toAddStaticScriptResources != null && !toAddStaticScriptResources.isEmpty()) {
            newStaticScriptResources = new ImmutableStaticScriptResourcesBuilder(
                    this.staticScriptResources).add(toAddStaticScriptResources).build();
        }

        Map<String, List<String>> toAddPaths = scriptResources.getPaths();
        if (toAddPaths != null && !toAddPaths.isEmpty()) {
            newPaths = new ImmutablePathsBuilder(this.paths)
                    .add(toAddPaths).build();
        }

        for (ScriptResourceDescriptor desc : scriptResources.getScriptResourceDescriptors()) {
            String contextPath = null;
            if (desc.modules.size() > 0) {
                contextPath = desc.modules.get(0).getContextPath();
            }

            ScriptResource resource = scripts.addResource(desc.id, desc.fetchMode, desc.alias, desc.group, contextPath, desc.nativeAmd);
            if (resource != null) {
                for (Javascript module : desc.modules) {
                    module.addModuleTo(resource);
                }
                for (Locale locale : desc.getSupportedLocales()) {
                    resource.addSupportedLocale(locale);
                }
                for (DependencyDescriptor dependency : desc.dependencies) {
                    resource.addDependency(dependency.getResourceId(), dependency.getAlias(), dependency.getPluginResource());
                }
            }
        }

        /* No exception was thrown, now we can at once assign these two local variables to service fields */
        if (newStaticScriptResources != null) {
            this.staticScriptResources = newStaticScriptResources;
        }
        if (newPaths != null) {
            this.paths = newPaths;
        }

    }

    /**
     * Removes the entities provided in the given {@link ScriptResources} from this
     * {@link JavascriptConfigService}.
     *
     * @param scriptResources entities to remove from this {@link JavascriptConfigService}
     * @param contextPath for which which context path the script resources should be removed
     */
    public void remove(ImmutableScriptResources scriptResources, String contextPath) {
        for (ScriptResourceDescriptor desc : scriptResources.getScriptResourceDescriptors()) {
            scripts.removeResource(desc.id, contextPath);
        }

        List<StaticScriptResource> toRemoveStaticScriptResources = scriptResources.getStaticScriptResources();
        if (toRemoveStaticScriptResources != null && !toRemoveStaticScriptResources.isEmpty()) {
            Map<String, StaticScriptResource> newStaticScriptResources = new ImmutableStaticScriptResourcesBuilder(
                    this.staticScriptResources).removeAll(toRemoveStaticScriptResources).build();
            this.staticScriptResources = newStaticScriptResources;
        }

        Map<String, List<String>> toRemovePaths = scriptResources.getPaths();
        if (toRemovePaths != null && !toRemovePaths.isEmpty()) {
            Map<String, List<String>> newPaths = new ImmutablePathsBuilder(this.paths)
                    .removeAll(toRemovePaths.keySet()).build();
            this.paths = newPaths;
        }

    }

}
