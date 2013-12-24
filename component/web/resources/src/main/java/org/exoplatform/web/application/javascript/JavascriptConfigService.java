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

import javax.servlet.ServletContext;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CompositeReader;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.ControllerContext;
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
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.picocontainer.Startable;

public class JavascriptConfigService extends AbstractResourceService implements Startable {

    /** Our logger. */
    private final Log log = ExoLogger.getLogger(JavascriptConfigService.class);

    /** The scripts. */
    final ScriptGraph scripts;

    /** . */
    private final WebAppListener deployer;

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
        this.deployer = new JavascriptConfigDeployer(context.getPortalContainerName(), this);
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

                if (isModule) {
                    JSONArray deps = new JSONArray();
                    LinkedList<String> params = new LinkedList<String>();
                    List<String> argNames = new LinkedList<String>();
                    List<String> argValues = new LinkedList<String>(params);
                    for (ResourceId id : resource.getDependencies()) {
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

                if (isModule) {
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

        Map<ResourceId, String> groupURLs = new HashMap<ResourceId, String>();
        for (ScriptResource resource : getAllResources()) {
            if (!resource.isEmpty() || ResourceScope.SHARED.equals(resource.getId().getScope())) {
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
        config.put("paths", paths);
        config.put("shim", shim);
        return config;
    }

    public ScriptResource getResource(ResourceId resource) {
        return scripts.getResource(resource);
    }

    /**
     * Start service. Registry org.exoplatform.web.application.javascript.JavascriptDeployer,
     * org.exoplatform.web.application.javascript.JavascriptRemoval into ServletContainer
     *
     * @see org.picocontainer.Startable#start()
     */
    public void start() {
        log.debug("Registering JavascriptConfigService for servlet container events");
        ServletContainerFactory.getServletContainer().addWebAppListener(deployer);
    }

    /**
     * Stop service. Remove org.exoplatform.web.application.javascript.JavascriptDeployer,
     * org.exoplatform.web.application.javascript.JavascriptRemoval from ServletContainer
     *
     * @see org.picocontainer.Startable#stop()
     */
    public void stop() {
        log.debug("Unregistering JavascriptConfigService for servlet container events");
        ServletContainerFactory.getServletContainer().removeWebAppListener(deployer);
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
}
