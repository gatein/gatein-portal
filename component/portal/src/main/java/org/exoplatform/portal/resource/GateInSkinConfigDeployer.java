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

package org.exoplatform.portal.resource;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

/**
 * 
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 *      Sep 16, 2009
 */
public class GateInSkinConfigDeployer extends AbstractResourceHandler
{

   private final SkinService skinService;

   private static final String GATEIN_CONFIG_RESOURCE = "/WEB-INF/gatein-resources.xml";

   private static Log LOG = ExoLogger.getExoLogger(GateInSkinConfigDeployer.class);

   /**
    * The name of the portal container
    */
   private final String portalContainerName;

   public GateInSkinConfigDeployer(String portalContainerName, SkinService _skinService)
   {
      this.skinService = _skinService;
      this.portalContainerName = portalContainerName;
   }

   @Override
   public void onEvent(WebAppEvent event)
   {
      if (event instanceof WebAppLifeCycleEvent)
      {
         WebAppLifeCycleEvent waEvent = (WebAppLifeCycleEvent)event;
         if (waEvent.getType() == WebAppLifeCycleEvent.ADDED)
         {
            ServletContext scontext = null;
            try
            {
               scontext = event.getWebApp().getServletContext();
               InputStream is = scontext.getResourceAsStream(GATEIN_CONFIG_RESOURCE);
               if (is == null)
                  return;
               try
               {
                  is.close();
               }
               catch (Exception ex)
               {
                  // ignore me
               }
               final PortalContainerPostInitTask task = new PortalContainerPostInitTask()
               {

                  public void execute(ServletContext scontext, PortalContainer portalContainer)
                  {
                     register(scontext, portalContainer);
                     skinService.registerContext(scontext);
                  }
               };
               PortalContainer.addInitTask(scontext, task, portalContainerName);
            }
            catch (Exception ex)
            {
               LOG.error("An error occurs while registering '" + GATEIN_CONFIG_RESOURCE + "' from the context '"
                  + (scontext == null ? "unknown" : scontext.getServletContextName()) + "'", ex);
            }
         }
      }
   }

   private void register(ServletContext scontext, PortalContainer container)
   {
      InputStream is = null;
      try
      {
         is = scontext.getResourceAsStream(GATEIN_CONFIG_RESOURCE);
         SkinConfigParser.processConfigResource(is, skinService, scontext);
      }
      catch (Exception ex)
      {
         LOG.error("An error occurs while registering '" + GATEIN_CONFIG_RESOURCE + "' from the context '"
            + (scontext == null ? "unknown" : scontext.getServletContextName()) + "'", ex);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
               // ignore me
            }
         }
      }
   }
}
