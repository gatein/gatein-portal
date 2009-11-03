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
package org.exoplatform.portal.resource.config.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.portal.resource.config.tasks.JavascriptTask;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class JavascriptConfigParser
{

   public static void processConfigResource(InputStream is, JavascriptConfigService service, ServletContext scontext){
      List<JavascriptTask> tasks = fetchTasks(is);
      if(tasks != null){
         for(JavascriptTask task : tasks){
            task.execute(service, scontext);
         }
      }
   }
   
   private static List<JavascriptTask> fetchTasks(InputStream is)
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
   
   private static List<JavascriptTask> fetchTasksFromXMLConfig(Document document){
      List<JavascriptTask> tasks = new ArrayList<JavascriptTask>();
      Element element = document.getDocumentElement();
      NodeList nodes = element.getElementsByTagName(GateinResource.JAVA_SCRIPT_TAG);
      
      for(int i = nodes.getLength() - 1 ; i >= 0; i--){
         JavascriptTask task = xmlToTask((Element)nodes.item(i));
         if(task != null){
            tasks.add(task);
         }
      }
      return tasks;
   }
   
   private static JavascriptTask xmlToTask(Element element){
      try{
         JavascriptTask task = new JavascriptTask();
         NodeList nodes = element.getElementsByTagName(GateinResource.JAVA_SCRIPT_PARAM);
         for(int i = nodes.getLength() - 1 ; i >= 0; i--){
            Element param_ele = (Element)nodes.item(i);
            task.addParam(param_ele.getFirstChild().getNodeValue(), param_ele.getLastChild().getNodeValue());
         }
         return task;
      }catch(Exception ex){
         return null;
      }
   }
   
   
}
