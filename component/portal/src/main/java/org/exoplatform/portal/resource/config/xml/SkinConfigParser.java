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
import org.exoplatform.portal.resource.config.tasks.AbstractSkinTask;
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
 * 
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 *      Sep 16, 2009
 */
public class SkinConfigParser
{

   /** . */
   public static final String GATEIN_RESOURCES_1_0_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_0.xsd";

   /** . */
   private static final String GATEIN_RESOURCE_1_0_XSD_PATH = "gatein_resources_1_0.xsd";

   /** . */
   private static final XMLValidator VALIDATOR = new XMLValidator(
      SkinConfigParser.class,
      GATEIN_RESOURCES_1_0_SYSTEM_ID,
      GATEIN_RESOURCE_1_0_XSD_PATH);

   private final static Map<String, AbstractTaskXMLBinding> allBindings = new HashMap<String, AbstractTaskXMLBinding>();

   static
   {
      allBindings.put(GateinResource.PORTAl_SKIN_TAG, new AbstractTaskXMLBinding.PortalSkinTaskXMLBinding());
      allBindings.put(GateinResource.PORTLET_SKIN_TAG, new AbstractTaskXMLBinding.PortletSkinTaskXMLBinding());
      allBindings.put(GateinResource.WINDOW_STYLE_TAG, new AbstractTaskXMLBinding.ThemeTaskXMLBinding());
   }

   public static void processConfigResource(DocumentSource source, SkinService skinService, ServletContext scontext)
   {
      List<AbstractSkinTask> allTasks = fetchTasks(source);
      if (allTasks != null)
      {
         for (AbstractSkinTask task : allTasks)
         {
            task.execute(skinService, scontext);
         }
      }
   }

   private static List<AbstractSkinTask> fetchTasks(DocumentSource source)
   {
      try
      {
         Document document = VALIDATOR.validate(source);
         return fetchTasksFromXMLConfig(document);
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   private static List<AbstractSkinTask> fetchTasksFromXMLConfig(Document document)
   {
      List<AbstractSkinTask> tasks = new ArrayList<AbstractSkinTask>();
      Element docElement = document.getDocumentElement();

      fetchTasksByTagName(GateinResource.PORTAl_SKIN_TAG, docElement, tasks);
      fetchTasksByTagName(GateinResource.PORTLET_SKIN_TAG, docElement, tasks);
      fetchTasksByTagName(GateinResource.WINDOW_STYLE_TAG, docElement, tasks);

      return tasks;
   }

   private static void fetchTasksByTagName(String tagName, Element rootElement, List<AbstractSkinTask> tasks)
   {
      AbstractTaskXMLBinding binding = allBindings.get(tagName);
      //If there is no binding for current tagName, then return
      if (binding == null)
      {
         return;
      }

      NodeList nodes = rootElement.getElementsByTagName(tagName);
      AbstractSkinTask task;

      for (int i = nodes.getLength() - 1; i >= 0; i--)
      {
         task = binding.xmlToTask((Element)nodes.item(i));
         if (task != null)
         {
            tasks.add(task);
         }
      }
   }
}
