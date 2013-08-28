/*
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.common.xml.XMLTools;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.Module.Local.Content;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class JavascriptConfigParser {

    public static final String JAVA_SCRIPT_TAG = "javascript";

    public static final String JAVA_SCRIPT_PARAM = "param";

    public static final String JAVA_SCRIPT_MODULE = "js-module";

    public static final String JAVA_SCRIPT_PATH = "js-path";

    public static final String JAVA_SCRIPT_PRIORITY = "js-priority";

    public static final String JAVA_SCRIPT_PORTAL_NAME = "portal-name";

    public static final String LEGACY_JAVA_SCRIPT = "merged";

    /** . */
    public static final String SCRIPT_TAG = "script";

    public static final String SCRIPTS_TAG = "scripts";

    /** . */
    public static final String PORTLET_TAG = "portlet";

    /** . */
    public static final String PORTAL_TAG = "portal";

    /** . */
    public static final String RESOURCE_TAG = "resource";

    /** . */
    public static final String SCOPE_TAG = "scope";

    /** . */
    public static final String MODULE_TAG = "module";

    /** . */
    public static final String PATH_TAG = "path";

    /** . */
    public static final String DEPENDS_TAG = "depends";

    /** . */
    public static final String URL_TAG = "url";

    /** . */
    public static final String AS_TAG = "as";

    /** . */
    public static final String ADAPTER_TAG = "adapter";

    /** . */
    public static final String INCLUDE_TAG = "include";

    /** . */
    public static final String GROUP_TAG = "load-group";

    /** . */
    private final String contextPath;

    private static final String SCRIPT_RESOURCE_DESCRIPTORS_ATTR = "gatein.script.resource.descriptors";

    private static final Log log = ExoLogger.getExoLogger(JavascriptConfigParser.class);

    public JavascriptConfigParser(String contextPath) {
        this.contextPath = contextPath;
    }

    public static void processConfigResource(InputStream is, JavascriptConfigService service, ServletContext scontext) {
        JavascriptConfigParser parser = new JavascriptConfigParser(scontext.getContextPath());
        LinkedList<ScriptResourceDescriptor> descriptors = new LinkedList<ScriptResourceDescriptor>();
        JavascriptTask task = new JavascriptTask();
        for (ScriptResourceDescriptor script : parser.parseConfig(is)) {
            task.addDescriptor(script);
            descriptors.add(script);
        }
        scontext.setAttribute(SCRIPT_RESOURCE_DESCRIPTORS_ATTR, Collections.unmodifiableList(descriptors));
        task.execute(service, scontext);
    }

    public static void unregisterResources(JavascriptConfigService service, ServletContext scontext) {
        List<ScriptResourceDescriptor> descriptors = (List<ScriptResourceDescriptor>) scontext.getAttribute(SCRIPT_RESOURCE_DESCRIPTORS_ATTR);
        if (descriptors == null)
            return;

        JavascriptUnregisterTask task = new JavascriptUnregisterTask();
        for (ScriptResourceDescriptor script : descriptors) {
            task.addDescriptor(script);
        }
        task.execute(service, scontext);
        scontext.removeAttribute(SCRIPT_RESOURCE_DESCRIPTORS_ATTR);
    }

    public List<ScriptResourceDescriptor> parseConfig(InputStream is) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(is);
            return parseScripts(document);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private List<ScriptResourceDescriptor> parseScripts(Document document) {
        List<ScriptResourceDescriptor> tasks = new ArrayList<ScriptResourceDescriptor>();
        Element element = document.getDocumentElement();
        for (String tagName : Arrays.asList(JAVA_SCRIPT_TAG, MODULE_TAG, SCRIPTS_TAG, PORTLET_TAG, PORTAL_TAG)) {
            for (Element childElt : XMLTools.getChildren(element, tagName)) {
                Collection<ScriptResourceDescriptor> task = parseScripts(childElt);
                if (task != null) {
                    tasks.addAll(task);
                }
            }
        }
        return tasks;
    }

    private Collection<ScriptResourceDescriptor> parseScripts(Element element) {
        LinkedHashMap<ResourceId, ScriptResourceDescriptor> scripts = new LinkedHashMap<ResourceId, ScriptResourceDescriptor>();
        if (JAVA_SCRIPT_TAG.equals(element.getTagName())) {
            try {
                NodeList nodes = element.getElementsByTagName(JAVA_SCRIPT_PARAM);
                int length = nodes.getLength();
                for (int i = 0; i < length; i++) {
                    Element param_ele = (Element) nodes.item(i);
                    String js_path = param_ele.getElementsByTagName(JAVA_SCRIPT_PATH).item(0).getFirstChild().getNodeValue();

                    //
                    log.warn(
                            "<javascript> tag define for javascript: {} has ben deprecated, please use <scripts> or <module> instead",
                            js_path);

                    //
                    int priority;
                    try {
                        priority = Integer.valueOf(
                                param_ele.getElementsByTagName(JAVA_SCRIPT_PRIORITY).item(0).getFirstChild().getNodeValue())
                                .intValue();
                    } catch (Exception e) {
                        priority = Integer.MAX_VALUE;
                    }
                    String portalName = null;
                    try {
                        portalName = param_ele.getElementsByTagName(JAVA_SCRIPT_PORTAL_NAME).item(0).getFirstChild()
                                .getNodeValue();
                    } catch (Exception e) {
                        // portal-name is null
                    }

                    Javascript js;
                    if (portalName == null) {
                        js = Javascript.create(new ResourceId(ResourceScope.SHARED, LEGACY_JAVA_SCRIPT), js_path, contextPath,
                                priority);
                    } else {
                        js = Javascript
                                .create(new ResourceId(ResourceScope.PORTAL, portalName), js_path, contextPath, priority);
                    }

                    //
                    ScriptResourceDescriptor desc = scripts.get(js.getResource());
                    if (desc == null) {
                        scripts.put(js.getResource(),
                                desc = new ScriptResourceDescriptor(js.getResource(), FetchMode.IMMEDIATE));
                    }
                    desc.modules.add(js);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        } else if (PORTAL_TAG.equals(element.getTagName()) || PORTLET_TAG.equals(element.getTagName())) {
            String resourceName = XMLTools.asString(XMLTools.getUniqueChild(element, "name", true));
            ResourceScope resourceScope;
            if (PORTLET_TAG.equals(element.getTagName())) {
                resourceName = contextPath.substring(1) + "/" + resourceName;
                resourceScope = ResourceScope.PORTLET;
            } else {
                resourceScope = ResourceScope.PORTAL;
            }
            ResourceId id = new ResourceId(resourceScope, resourceName);
            FetchMode fetchMode;
            String group = null;

            Element resourceElt = XMLTools.getUniqueChild(element, MODULE_TAG, false);
            if (resourceElt != null) {
                fetchMode = FetchMode.ON_LOAD;
                if (XMLTools.getUniqueChild(resourceElt, URL_TAG, false) == null) {
                    group = parseGroup(resourceElt);
                }
            } else {
                resourceElt = XMLTools.getUniqueChild(element, SCRIPTS_TAG, false);
                fetchMode = FetchMode.IMMEDIATE;
            }

            if (resourceElt != null) {
                ScriptResourceDescriptor desc = scripts.get(id);
                if (desc == null) {
                    desc = new ScriptResourceDescriptor(id, fetchMode, parseOptString(element, AS_TAG), group);
                } else {
                    desc.fetchMode = fetchMode;
                }
                parseDesc(resourceElt, desc);
                scripts.put(id, desc);
            }
        } else if (MODULE_TAG.equals(element.getTagName()) || SCRIPTS_TAG.equals(element.getTagName())) {
            String resourceName = XMLTools.asString(XMLTools.getUniqueChild(element, "name", true));
            ResourceId id = new ResourceId(ResourceScope.SHARED, resourceName);
            FetchMode fetchMode;
            String group = null;

            if (MODULE_TAG.equals(element.getTagName())) {
                fetchMode = FetchMode.ON_LOAD;
                if (XMLTools.getUniqueChild(element, URL_TAG, false) == null) {
                    group = parseGroup(element);
                }
            } else {
                fetchMode = FetchMode.IMMEDIATE;
            }

            ScriptResourceDescriptor desc = scripts.get(id);
            if (desc == null) {
                desc = new ScriptResourceDescriptor(id, fetchMode, parseOptString(element, AS_TAG), group);
            }
            parseDesc(element, desc);
            scripts.put(id, desc);
        } else {
            // ???
        }

        //
        return scripts.values();
    }

    private void parseDesc(Element element, ScriptResourceDescriptor desc) {
        Element urlElement = XMLTools.getUniqueChild(element, URL_TAG, false);
        if (urlElement != null) {
            String remoteURL = XMLTools.asString(urlElement);
            desc.id.setFullId(false);
            desc.modules.add(new Javascript.Remote(desc.id, contextPath, remoteURL, 0));
        } else {
            for (Element localeElt : XMLTools.getChildren(element, "supported-locale")) {
                String localeValue = XMLTools.asString(localeElt);
                Locale locale = I18N.parseTagIdentifier(localeValue);
                desc.supportedLocales.add(locale);
            }
            for (Element scriptElt : XMLTools.getChildren(element, SCRIPT_TAG)) {
                String resourceBundle = parseOptString(scriptElt, "resource-bundle");

                List<Content> contents = new LinkedList<Content>();
                Element adapter = XMLTools.getUniqueChild(scriptElt, ADAPTER_TAG, false);
                String scriptPath = parseOptString(scriptElt, "path");
                if (scriptPath != null) {
                    contents.add(new Content(scriptPath));
                } else if (adapter != null) {
                    NodeList childs = adapter.getChildNodes();
                    for (int i = 0; i < childs.getLength(); i++) {
                        Node item = childs.item(i);
                        if (item instanceof Element) {
                            Element include = (Element) item;
                            if (INCLUDE_TAG.equals(include.getTagName())) {
                                contents.add(new Content(XMLTools.asString(include, true)));
                            }
                        } else if (item.getNodeType() == Node.TEXT_NODE) {
                            contents.add(new Content(item.getNodeValue().trim(), false));
                        }
                    }
                }
                Content[] tmp = contents.toArray(new Content[contents.size()]);

                //
                Javascript script = new Javascript.Local(desc.id, contextPath, tmp, resourceBundle, 0);
                desc.modules.add(script);
            }
        }
        for (Element moduleElt : XMLTools.getChildren(element, "depends")) {
            Element dependencyElt = XMLTools.getUniqueChild(moduleElt, "module", false);
            if (dependencyElt == null) {
                dependencyElt = XMLTools.getUniqueChild(moduleElt, "scripts", false);
            }
            ResourceId resourceId = new ResourceId(ResourceScope.SHARED, XMLTools.asString(dependencyElt));
            DependencyDescriptor dependency = new DependencyDescriptor(resourceId, parseOptString(moduleElt, AS_TAG),
                    parseOptString(moduleElt, RESOURCE_TAG));
            desc.dependencies.add(dependency);
        }
    }

    private String parseGroup(Element element) {
        Element group = XMLTools.getUniqueChild(element, GROUP_TAG, false);
        if (group != null) {
            String grpName = XMLTools.asString(group, true);
            if (grpName.isEmpty()) {
                grpName = null;
            }
            return grpName;
        } else {
            return null;
        }
    }

    private String parseOptString(Element element, String childTag) {
        Element childElt = XMLTools.getUniqueChild(element, childTag, false);
        return childElt == null ? null : XMLTools.asString(childElt, true);
    }
}
