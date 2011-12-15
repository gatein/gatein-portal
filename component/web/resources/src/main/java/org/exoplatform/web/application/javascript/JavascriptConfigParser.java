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

import org.exoplatform.portal.controller.resource.Resource;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
   private ServletContext context;

   private JavascriptConfigParser(ServletContext context)
   {
      this.context = context;
   }

   public static void processConfigResource(InputStream is, JavascriptConfigService service, ServletContext scontext)
   {
      JavascriptConfigParser parser = new JavascriptConfigParser(scontext);
      List<JavascriptTask> tasks = parser.fetchTasks(is);
      if (tasks != null)
      {
         for (JavascriptTask task : tasks)
         {
            task.execute(service, scontext);
         }
      }
   }

   private List<JavascriptTask> fetchTasks(InputStream is)
   {
      try
      {
         DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document document = docBuilder.parse(is);
         return fetchTasksFromXMLConfig(document);
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   private List<JavascriptTask> fetchTasksFromXMLConfig(Document document)
   {
      List<JavascriptTask> tasks = new ArrayList<JavascriptTask>();
      Element element = document.getDocumentElement();
      NodeList nodes = element.getElementsByTagName(JAVA_SCRIPT_TAG);
      int length = nodes.getLength();
      for (int i = 0; i < length; i++)
      {
         JavascriptTask task = xmlToTask((Element)nodes.item(i));
         if (task != null)
         {
            tasks.add(task);
         }
      }
      return tasks;
   }

   private JavascriptTask xmlToTask(Element element)
   {
      if (!JAVA_SCRIPT_TAG.equals(element.getTagName()))
      {
         return null;
      }
      try
      {
         JavascriptTask task = new JavascriptTask();
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
               js = Javascript.create(new Resource(ResourceScope.SHARED, "common"), js_module, js_path, context.getContextPath(), priority);
            }
            else
            {
               js = Javascript.create(new Resource(ResourceScope.PORTAL, portalName), js_module, js_path, context.getContextPath(), priority);
            }
            task.addScript(js);
         }
         return task;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return null;
      }
   }
}
