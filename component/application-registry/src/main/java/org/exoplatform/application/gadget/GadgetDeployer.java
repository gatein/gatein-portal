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
package org.exoplatform.application.gadget;

import org.exoplatform.application.gadget.impl.GadgetRegistryServiceImpl;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.xml.XMLTools;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GadgetDeployer implements WebAppListener, Startable
{

   /** . */
   private final Logger log = LoggerFactory.getLogger(GadgetDeployer.class);

   /** . */
   private GadgetRegistryServiceImpl gadgetRegistryService;

   public GadgetDeployer(GadgetRegistryService gadgetRegistryService)
   {
      this.gadgetRegistryService = (GadgetRegistryServiceImpl)gadgetRegistryService;
   }

   public void onEvent(WebAppEvent webAppEvent)
   {
      if (webAppEvent instanceof WebAppLifeCycleEvent)
      {
         WebAppLifeCycleEvent lfEvent = (WebAppLifeCycleEvent)webAppEvent;
         if (lfEvent.getType() == WebAppLifeCycleEvent.ADDED)
         {
            WebApp webApp = webAppEvent.getWebApp();
            ServletContext scontext = webApp.getServletContext();
            try
            {
               final URL url = scontext.getResource("/WEB-INF/gadget.xml");
               if (url != null)
               {
                  final RootContainer.PortalContainerPostInitTask task = new RootContainer.PortalContainerPostInitTask()
                  {
                     public void execute(ServletContext context, PortalContainer portalContainer)
                     {
                        handle(context, url);
                     }
                  };
                  PortalContainer.addInitTask(scontext, task);
               }
            }
            catch (MalformedURLException e)
            {
               log.error("Could not read the content of the gadget file", e);
            }
         }
      }
   }

   public void start()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(this);
   }

   public void stop()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(this);
   }

   private void handle(ServletContext scontext, URL gadgetsURL)
   {
      ChromatticLifeCycle lifeCycle = gadgetRegistryService.getChromatticLifeCycle();
      SessionContext context = lifeCycle.openContext();
      InputStream in;
      try
      {
         in = gadgetsURL.openStream();
         DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document docXML = db.parse(in);
         NodeList nodeList = docXML.getElementsByTagName("gadget");
         for (int i = 0; i < nodeList.getLength(); i++)
         {
            Element gadgetElement = (Element)nodeList.item(i);
            String gadgetName = gadgetElement.getAttribute("name");
            log.info("About to import gadget " + gadgetName);
            Element pathElt = XMLTools.getUniqueChild(gadgetElement, "path", false);
            if (pathElt != null)
            {
               String path = XMLTools.asString(pathElt, true);
               ServletLocalImporter importer = new ServletLocalImporter(
                  gadgetName,
                  gadgetRegistryService.getRegistry(),
                  path,
                  scontext,
                  true);
               importer.doImport();
            }
            else
            {
               Element urlElt = XMLTools.getUniqueChild(gadgetElement, "url", false);
               if (urlElt != null)
               {
                  String url = XMLTools.asString(urlElt, true);
                  ServletLocalImporter importer = new ServletLocalImporter(
                     gadgetName,
                     gadgetRegistryService.getRegistry(),
                     url, 
                     scontext,
                     false);
                  importer.doImport();
               }
            }
         }
      }
      catch (Exception e)
      {
         log.error("Could not process gadget file " + gadgetsURL, e);
      }
      finally
      {
         lifeCycle.closeContext(context, true);
      }
   }
}
