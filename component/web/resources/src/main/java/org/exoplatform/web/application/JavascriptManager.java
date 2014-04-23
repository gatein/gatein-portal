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

package org.exoplatform.web.application;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMap;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.ScriptResource;

/**
 * Created by The eXo Platform SAS Mar 27, 2007
 */
public class JavascriptManager {
    Log log = ExoLogger.getLogger("portal:JavascriptManager");

    /** . */
    private FetchMap<ResourceId> resourceIds = new FetchMap<ResourceId>();

    /** . */
    private Set<String> extendedScriptURLs = new LinkedHashSet<String>();

    /** . */
    private StringBuilder scripts = new StringBuilder();

    /** . */
    private StringBuilder customizedOnloadJavascript = new StringBuilder();

    private RequireJS requireJS;

    public JavascriptManager() {
        requireJS = new RequireJS();
        requireJS.require("SHARED/base", "base");
    }

    /**
     * Add a valid javascript code
     *
     * @param s a valid javascript code
     */
    public void addJavascript(CharSequence s) {
        if (s != null) {
            scripts.append(s.toString().trim());
            scripts.append(";\n");
        }
    }

    /**
     * Register a SHARE Javascript resource that will be loaded in Rendering phase Script FetchMode is ON_LOAD by default
     */
    public void loadScriptResource(String name) {
        loadScriptResource(ResourceScope.SHARED, name);
    }

    /**
     * Register a Javascript resource that will be loaded in Rendering phase If mode is null, script will be loaded with mode
     * defined in gatein-resources.xml
     */
    public void loadScriptResource(ResourceScope scope, String name) {
        if (scope == null) {
            throw new IllegalArgumentException("scope can't be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name can't be null");
        }
        ResourceId id = new ResourceId(scope, name);
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        JavascriptConfigService service = (JavascriptConfigService) container
                .getComponentInstanceOfType(JavascriptConfigService.class);
        ScriptResource resource = service.getResource(id);
        if (resource != null) {
            if (FetchMode.IMMEDIATE.equals(resource.getFetchMode())) {
                resourceIds.add(id, null);
            } else {
                Map<ResourceId, FetchMode> tmp = new HashMap<ResourceId, FetchMode>();
                tmp.put(id, null);
                for (ScriptResource res : service.resolveIds(tmp).keySet()) {
                    if (res.isNativeAmd()) {
                        /* Require scopeless id for native AMD modules. They are implicitly SHARED
                         * and baseUrl of requirejs ends with SHARED. Therefore there is no need
                         * to prepend the scope to the name here. */
                        require(res.getId().getName());
                    } else {
                        require(res.getId().toString());
                    }
                }
            }
        }
    }

    public FetchMap<ResourceId> getScriptResources() {
        return resourceIds;
    }

    public List<String> getExtendedScriptURLs() {
        return new LinkedList<String>(extendedScriptURLs);
    }

    public void addExtendedScriptURLs(String url) {
        this.extendedScriptURLs.add(url);
    }

    public void addOnLoadJavascript(CharSequence s) {
        if (s != null) {
            String id = Integer.toString(Math.abs(s.hashCode()));
            StringBuilder script = new StringBuilder("base.Browser.addOnLoadCallback('mid");
            script.append(id);
            script.append("',");
            script.append(s instanceof String ? (String) s : s.toString());
            script.append(");");
            requireJS.addScripts(script.toString());
        }
    }

    public void addOnResizeJavascript(CharSequence s) {
        if (s != null) {
            String id = Integer.toString(Math.abs(s.hashCode()));
            StringBuilder script = new StringBuilder();
            script.append("base.Browser.addOnResizeCallback('mid");
            script.append(id);
            script.append("',");
            script.append(s instanceof String ? (String) s : s.toString());
            script.append(");");
            requireJS.addScripts(script.toString());
        }
    }

    public void addOnScrollJavascript(CharSequence s) {
        if (s != null) {
            String id = Integer.toString(Math.abs(s.hashCode()));
            StringBuilder script = new StringBuilder();
            script.append("base.Browser.addOnScrollCallback('mid");
            script.append(id);
            script.append("',");
            script.append(s instanceof String ? (String) s : s.toString());
            script.append(");");
            requireJS.addScripts(script.toString());
        }
    }

    public void addCustomizedOnLoadScript(CharSequence s) {
        if (s != null) {
            customizedOnloadJavascript.append(s.toString().trim());
            customizedOnloadJavascript.append(";\n");
        }
    }

    /**
     * Returns javascripts which were added by {@link #addJavascript(CharSequence)}, {@link #addOnLoadJavascript(CharSequence)},
     * {@link #addOnResizeJavascript(CharSequence)}, {@link #addOnScrollJavascript(CharSequence)},
     * {@link #addCustomizedOnLoadScript(CharSequence)}, {@link #requireJS}
     *
     * @return
     */
    public String getJavaScripts() {
        StringBuilder callback = new StringBuilder();
        callback.append(scripts);
        callback.append(requireJS.addScripts("base.Browser.onLoad();").addScripts(customizedOnloadJavascript.toString())
                .toString());
        return callback.toString();
    }

    public RequireJS require(String moduleId) {
        return require(moduleId, null);
    }

    public RequireJS require(String moduleId, String alias) {
        return requireJS.require(moduleId, alias);
    }

    public RequireJS getRequireJS() {
        return requireJS;
    }

    public String generateUUID() {
        return "uniq-" + UUID.randomUUID().toString();
    }
}
