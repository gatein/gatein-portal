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

package org.exoplatform.application.gadget;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.gadget.GadgetApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by The eXo Platform SAS
 * Author : dang.tung
 *          tungcnw@gmail.com
 * May 15, 2008   
 */
/**
 * This class represents an registry for gadget application, it hear from context and then deployed
 * gadget
 */
public class GadgetRegister implements ServletContextListener
{
   protected static Log log = ExoLogger.getLogger("gadget:GadgetRegister");

   /**
    * Initializes the listener and each time a new gadget application war is deployed the gadgets
    * are added into the JCR node by GadgetRegistryService
    * @throws Exception when can't parse xml file
    */
   public void contextInitialized(ServletContextEvent event)
   {
      final PortalContainerPostInitTask task = new PortalContainerPostInitTask()
      {

         public void execute(ServletContext context, PortalContainer portalContainer)
         {
            contextInitialized(context, portalContainer);
         }
      };
      PortalContainer.addInitTask(event.getServletContext(), task);
   }

   private void contextInitialized(ServletContext context, PortalContainer pcontainer)
   {
      try
      {
         SourceStorage sourceStorage = (SourceStorage)pcontainer.getComponentInstanceOfType(SourceStorage.class);
         GadgetRegistryService gadgetService =
            (GadgetRegistryService)pcontainer.getComponentInstanceOfType(GadgetRegistryService.class);
         String confLocation = "/WEB-INF/gadget.xml";
         DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         InputStream in = context.getResourceAsStream(confLocation);
         Document docXML = db.parse(in);
         NodeList nodeList = docXML.getElementsByTagName("gadget");
         String gadgetName = null, address = null;
         for (int i = 0; i < nodeList.getLength(); i++)
         {
            Element gadgetElement = (Element)nodeList.item(i);
            gadgetName = gadgetElement.getAttribute("name");
            if (gadgetService.getGadget(gadgetName) != null)
               continue;
            try
            {
               NodeList nodeChild = gadgetElement.getChildNodes();
               for (int j = 0; j < nodeChild.getLength(); j++)
               {
                  Node node = nodeChild.item(j);
                  address = node.getTextContent();
                  if (node.getNodeName().equals("path"))
                  {
                     InputStream sourceIs = context.getResourceAsStream(address);
                     String realPath = context.getRealPath(address);
                     File sourceFile = new File(realPath);
                     File homeDir = sourceFile.getParentFile();
                     String fileName = sourceFile.getName();
                     //Saves source of gadget
                     Source source = new Source(fileName, getMimeType(context, fileName));
//                     source.setStreamContent(sourceIs);
                     source.setLastModified(Calendar.getInstance());
                     String homeName = homeDir.getName();
                     // sourceStorage.saveSource(homeName, source);
                     //Saves gadget
                     ModulePrefs prefs =
                        GadgetApplication.getModulePreferences(Uri.parse("http://www.exoplatform.org"), source
                           .getTextContent());
                     Gadget gadget = new Gadget();
                     gadget.setName(gadgetName);
                     // gadget.setUrl(sourceStorage.getSourceURI(homeName + "/" + fileName));
                     gadget.setTitle(getGadgetTitle(prefs, gadget.getName()));
                     gadget.setDescription(prefs.getDescription());
                     gadget.setThumbnail(prefs.getThumbnail().toString());
                     gadget.setReferenceUrl(prefs.getTitleUrl().toString());
                     gadget.setLocal(true);
                     gadgetService.saveGadget(gadget);
                     //Saves source's included
                     //              int dotIdx = address.lastIndexOf('.'); 
                     //              if(dotIdx < 0) continue;
                     //              String dirPath = address.substring(0, dotIdx);
                     if (homeDir.exists() && homeDir.isDirectory())
                     {
                        File[] files = homeDir.listFiles();
                        for (int k = 0; k < files.length; k++)
                        {
                           saveTree(files[k], homeName, context, sourceStorage);
                        }
                     }
                  }
                  else if (node.getNodeName().equals("url"))
                  {
                     URL urlObj = new URL(address);
                     URLConnection conn = urlObj.openConnection();
                     InputStream is = conn.getInputStream();
                     String source = IOUtils.toString(is, "UTF-8");
                     ModulePrefs prefs = GadgetApplication.getModulePreferences(Uri.parse(address), source);
                     Gadget gadget = new Gadget();
                     gadget.setName(gadgetName);
                     gadget.setUrl(address);
                     gadget.setTitle(getGadgetTitle(prefs, gadget.getName()));
                     gadget.setDescription(prefs.getDescription());
                     gadget.setThumbnail(prefs.getThumbnail().toString());
                     gadget.setReferenceUrl(prefs.getTitleUrl().toString());
                     gadget.setLocal(false);
                     gadgetService.saveGadget(gadget);
                  }
               }
            }
            catch (Exception ex)
            {
               log.warn("Can not register the gadget: '" + gadgetName + "' ");
            }
         }
      }
      catch (Exception ex)
      {
         log.error("Error while deploying a gadget", ex);
      }
   }

   private void saveTree(File file, String savePath, ServletContext context, SourceStorage storage) throws Exception
   {
      if (file.isFile())
      {
         Source includedSource = new Source(file.getName(), getMimeType(context, file.getName()));
//         includedSource.setStreamContent(new FileInputStream(file));
         includedSource.setLastModified(Calendar.getInstance());
         // storage.saveSource(savePath, includedSource);
      }
      else if (file.isDirectory())
      {
         File[] files = file.listFiles();
         String childPath = savePath + "/" + file.getName();
         for (int i = 0; i < files.length; i++)
            saveTree(files[i], childPath, context, storage);
      }
   }

   private String getGadgetTitle(ModulePrefs prefs, String defaultValue)
   {
      String title = prefs.getDirectoryTitle();
      if (title == null || title.trim().length() < 1)
         title = prefs.getTitle();
      if (title == null || title.trim().length() < 1)
         return defaultValue;
      return title;
   }

   private String getMimeType(ServletContext context, String fileName)
   {
      return (context.getMimeType(fileName) != null) ? context.getMimeType(fileName) : "text/plain";
   }

   /**
    * Destroys the listener context
    */
   public void contextDestroyed(ServletContextEvent servletContextEvent)
   {
   }
}