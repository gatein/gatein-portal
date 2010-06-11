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

package org.exoplatform.portal.resource.config.xml;

import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.commons.xml.XMLValidator;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.resource.config.tasks.SkinConfigTask;
import org.exoplatform.portal.resource.config.tasks.PortalSkinTask;
import org.exoplatform.portal.resource.config.tasks.PortletSkinTask;
import org.exoplatform.portal.resource.config.tasks.ThemeTask;
import org.exoplatform.web.application.javascript.JavascriptTask;
import org.exoplatform.web.resource.config.xml.GateinResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */

public class SkinConfigParser
{

   /** . */
   public static final String GATEIN_RESOURCES_1_0_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_0";

   /** . */
   public static final String GATEIN_RESOURCES_1_1_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_1";

   /** . */
   private static final String GATEIN_RESOURCE_1_0_XSD_PATH = "gatein_resources_1_0.xsd";

   /** . */
   private static final String GATEIN_RESOURCE_1_1_XSD_PATH = "gatein_resources_1_1.xsd";

   /** . */
   private static final XMLValidator VALIDATOR;

   static
   {
      Map<String, String> systemIdToResourcePath = new HashMap<String, String>();
      systemIdToResourcePath.put(GATEIN_RESOURCES_1_0_SYSTEM_ID, GATEIN_RESOURCE_1_0_XSD_PATH);
      systemIdToResourcePath.put(GATEIN_RESOURCES_1_1_SYSTEM_ID, GATEIN_RESOURCE_1_1_XSD_PATH);
      VALIDATOR = new XMLValidator(SkinConfigParser.class, systemIdToResourcePath);
   }

   public static void processConfigResource(DocumentSource source, SkinService skinService, ServletContext scontext)
   {
      List<SkinConfigTask> allTasks = fetchTasks(source);
      if (allTasks != null)
      {
         for (SkinConfigTask task : allTasks)
         {
            task.execute(skinService, scontext);
         }
      }
   }

   public static List<SkinConfigTask> fetchTasks(DocumentSource source)
   {
      try
      {
         Document document = VALIDATOR.validate(source);
         
         List<SkinConfigTask> tasks = new ArrayList<SkinConfigTask>();
         Element docElement = document.getDocumentElement();

         fetchTasksByTagName(GateinResource.PORTAl_SKIN_TAG, docElement, tasks);
         fetchTasksByTagName(GateinResource.PORTLET_SKIN_TAG, docElement, tasks);
         fetchTasksByTagName(GateinResource.WINDOW_STYLE_TAG, docElement, tasks);

         return tasks;
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   private static void fetchTasksByTagName(String tagName, Element rootElement, List<SkinConfigTask> tasks)
   {
      NodeList nodes = rootElement.getElementsByTagName(tagName);
      GateinResource task;

      for (int i = nodes.getLength() - 1; i >= 0; i--)
      {
         task = elemtToTask(tagName);
         if (task != null)
         {
            task.binding((Element)nodes.item(i));
            tasks.add((SkinConfigTask)task);
         }
      }
   }
   
   /**
    * Return a skin task associated to the <code>tagName</code> of an XML element
    */
   private static GateinResource elemtToTask(String tagName)
   {
      if (tagName.equals(GateinResource.PORTAl_SKIN_TAG))
      {
         return new PortalSkinTask();
      }
      else if (tagName.equals(GateinResource.WINDOW_STYLE_TAG))
      {
         return new ThemeTask();
      }
      else if (tagName.equals(GateinResource.PORTLET_SKIN_TAG))
      {
         return new PortletSkinTask();
      }
      return null;
   }
}
