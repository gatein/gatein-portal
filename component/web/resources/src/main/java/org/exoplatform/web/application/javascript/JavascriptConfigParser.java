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

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.controller.resource.script.FetchMode;
import org.gatein.common.xml.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 * 
 */
public class JavascriptConfigParser
{

   final public static String JAVA_SCRIPT_TAG = "javascript";

   final public static String JAVA_SCRIPT_PARAM = "param";

   final public static String JAVA_SCRIPT_MODULE = "js-module";

   final public static String JAVA_SCRIPT_PATH = "js-path";

   final public static String JAVA_SCRIPT_PRIORITY = "js-priority";
   
   final public static String JAVA_SCRIPT_PORTAL_NAME = "portal-name";

   /** . */
   final public static String SCRIPTS_TAG = "scripts";

   /** . */
   final public static String PORTLET_TAG = "portlet";

   /** . */
   final public static String PORTAL_TAG = "portal";

   /** . */
   final public static String RESOURCE_TAG = "resource";

   /** . */
   final public static String SCOPE_TAG = "scope";

   /** . */
   final public static String NAME_TAG = "name";

   /** . */
   final public static String MODULE_TAG = "module";

   /** . */
   final public static String PATH_TAG = "path";

   /** . */
   final public static String DEPENDS_TAG = "depends";

   /** . */
   private final String contextPath;

   public JavascriptConfigParser(String contextPath)
   {
      this.contextPath = contextPath;
   }

   public static void processConfigResource(InputStream is, JavascriptConfigService service, ServletContext scontext)
   {
      JavascriptConfigParser parser = new JavascriptConfigParser(scontext.getContextPath());
      JavascriptTask task = new JavascriptTask();
      for (ScriptResourceDescriptor script : parser.parseConfig(is))
      {
         task.addDescriptor(script);
      }
      task.execute(service, scontext);
   }

   public List<ScriptResourceDescriptor> parseConfig(InputStream is)
   {
      try
      {
         DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document document = docBuilder.parse(is);
         return parseScripts(document);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return null;
      }
   }

   private List<ScriptResourceDescriptor> parseScripts(Document document)
   {
      List<ScriptResourceDescriptor> tasks = new ArrayList<ScriptResourceDescriptor>();
      Element element = document.getDocumentElement();
      for (String tagName : Arrays.asList(JAVA_SCRIPT_TAG, SCRIPTS_TAG, PORTLET_TAG, PORTAL_TAG))
      {
         for (Element childElt : XMLTools.getChildren(element, tagName))
         {
            Collection<ScriptResourceDescriptor> task = parseScripts(childElt);
            if (task != null)
            {
               tasks.addAll(task);
            }
         }
      }
      return tasks;
   }

   private Collection<ScriptResourceDescriptor> parseScripts(Element element)
   {
      LinkedHashMap<ResourceId, ScriptResourceDescriptor> scripts = new LinkedHashMap<ResourceId, ScriptResourceDescriptor>();
      if (JAVA_SCRIPT_TAG.equals(element.getTagName()))
      {
         try
         {
            NodeList nodes = element.getElementsByTagName(JAVA_SCRIPT_PARAM);
            int length = nodes.getLength();
            for (int i = 0; i < length; i++)
            {
               Element param_ele = (Element)nodes.item(i);
               String js_module =
                  param_ele.getElementsByTagName(JAVA_SCRIPT_MODULE).item(0).getFirstChild().getNodeValue();
               String js_path =
                  param_ele.getElementsByTagName(JAVA_SCRIPT_PATH).item(0).getFirstChild().getNodeValue();
               int priority;
               try
               {
                  priority =
                     Integer.valueOf(param_ele.getElementsByTagName(JAVA_SCRIPT_PRIORITY).item(0)
                        .getFirstChild().getNodeValue()).intValue();
               }
               catch (Exception e)
               {
                  priority = Integer.MAX_VALUE;
               }
               String portalName = null;
               try
               {
                  portalName = param_ele.getElementsByTagName(JAVA_SCRIPT_PORTAL_NAME).item(0)
                  .getFirstChild().getNodeValue();
               }
               catch (Exception e)
               {
                  // portal-name is null
               }

               Javascript js;
               if (portalName == null)
               {
                  js = Javascript.create(new ResourceId(ResourceScope.SHARED, "common"), js_module, js_path, contextPath, priority);
               }
               else
               {
                  js = Javascript.create(new ResourceId(ResourceScope.PORTAL, portalName), js_module, js_path, contextPath, priority);
               }
               
               //
               ScriptResourceDescriptor desc = scripts.get(js.getResource());
               if (desc == null)
               {
                  scripts.put(js.getResource(), desc = new ScriptResourceDescriptor(js.getResource(), FetchMode.IMMEDIATE));
               }
               desc.modules.add(js);
            }
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
         }
      }
      else if (PORTAL_TAG.equals(element.getTagName()) || PORTLET_TAG.equals(element.getTagName()))
      {
         String resourceName = XMLTools.asString(XMLTools.getUniqueChild(element, "name", true));
         ResourceScope resourceScope;
         if (PORTLET_TAG.equals(element.getTagName()))
         {
            resourceName = contextPath.substring(1) + "/" + resourceName;
            resourceScope = ResourceScope.PORTLET;
         }
         else
         {
            resourceScope = ResourceScope.PORTAL;
         }
         ResourceId id = new ResourceId(resourceScope, resourceName);
         Element scriptsElt = XMLTools.getUniqueChild(element, SCRIPTS_TAG, false);
         if (scriptsElt != null)
         {
            Element modeElt = XMLTools.getUniqueChild(scriptsElt, "mode", false);
            FetchMode fetchMode = modeElt == null ? FetchMode.IMMEDIATE : FetchMode.decode(XMLTools.asString(modeElt));
            ScriptResourceDescriptor desc = scripts.get(id);
            if (desc == null)
            {
               scripts.put(id, desc = new ScriptResourceDescriptor(id, fetchMode));
            }
            else if (fetchMode == FetchMode.IMMEDIATE)
            {
               desc.fetchMode = fetchMode;
            }
            parseDesc(scriptsElt, desc);
         }
      }
      else if (SCRIPTS_TAG.equals(element.getTagName()))
      {
         String resourceName = XMLTools.asString(XMLTools.getUniqueChild(element, "name", true));
         ResourceId id = new ResourceId(ResourceScope.SHARED, resourceName);
         ScriptResourceDescriptor desc = scripts.get(id);
         if (desc == null)
         {
            scripts.put(id, desc = new ScriptResourceDescriptor(id, FetchMode.ON_LOAD));
         }
         parseDesc(element, desc);
      }
      else
      {
         // ???
      }

      //
      return scripts.values();
   }
   
   private void parseDesc(Element element, ScriptResourceDescriptor desc)
   {
      for (Element localeElt : XMLTools.getChildren(element, "supported-locale"))
      {
         String localeValue = XMLTools.asString(localeElt);
         Locale locale = I18N.parseTagIdentifier(localeValue);
         desc.supportedLocales.add(locale);
      }
      for (Element moduleElt : XMLTools.getChildren(element, "module"))
      {
         String moduleName = XMLTools.asString(XMLTools.getUniqueChild(moduleElt, "name", true));
         Javascript script;
         Element path = XMLTools.getUniqueChild(moduleElt, "path", false);
         if (path != null)
         {
            String resourceBundle = null;
            Element bundleElt = XMLTools.getUniqueChild(moduleElt, "resource-bundle", false);
            if (bundleElt != null)
            {
               resourceBundle = XMLTools.asString(bundleElt);
            }
            String modulePath = XMLTools.asString(path);
            script = new Javascript.Local(desc.id, moduleName, contextPath, modulePath, resourceBundle, 0);
         }
         else
         {
            String moduleURI = XMLTools.asString(XMLTools.getUniqueChild(moduleElt, "uri", true));
            script = new Javascript.Remote(desc.id, moduleName, contextPath, moduleURI, 0);
         }
         desc.modules.add(script);
      }
      for (Element moduleElt : XMLTools.getChildren(element, "depends"))
      {
         String dependencyName = XMLTools.asString(XMLTools.getUniqueChild(moduleElt, "scripts", true));
         ResourceId resourceId = new ResourceId(ResourceScope.SHARED, dependencyName);
         DependencyDescriptor dependency = new DependencyDescriptor(resourceId);
         desc.dependencies.add(dependency);
      }
   }
}
