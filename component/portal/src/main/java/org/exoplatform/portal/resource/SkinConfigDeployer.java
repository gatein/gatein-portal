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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

/**
 * Created by The eXo Platform SAS
 * Jan 19, 2007
 */

public class SkinConfigDeployer extends AbstractResourceHandler
{

   /**
    * Logger
    */
   private static final Log LOG = ExoLogger.getLogger(SkinConfigDeployer.class);

   /** . */
   private final SkinService skinService;

   /**
    * The name of the portal container
    */
   private final String portalContainerName;

   public SkinConfigDeployer(String portalContainerName, SkinService skinService)
   {
      this.skinService = skinService;
      this.portalContainerName = portalContainerName;
   }

   public void onEvent(WebAppEvent event)
   {
      if (event instanceof WebAppLifeCycleEvent)
      {
         WebAppLifeCycleEvent waEvent = (WebAppLifeCycleEvent)event;
         if (waEvent.getType() == WebAppLifeCycleEvent.ADDED)
         {
            ServletContext scontext = null;
            InputStream is = null;
            try
            {
               scontext = event.getWebApp().getServletContext();
               is = scontext.getResourceAsStream("/WEB-INF/conf/script/groovy/SkinConfigScript.groovy");
               final PortalContainerPostInitTask task = new PortalContainerPostInitTask()
               {

                  public void execute(ServletContext scontext, PortalContainer portalContainer)
                  {
                     register(scontext, portalContainer);
                  }
               };
               PortalContainer.addInitTask(scontext, task, portalContainerName);
            }
            catch (Exception ex)
            {
               LOG.error("An error occurs while registering 'SkinConfigScript.groovy' from the context '"
                  + (scontext == null ? "unknown" : scontext.getServletContextName()) + "'", ex);
            }
            finally
            {
               Safe.close(is);
            }
         }
      }
   }

   private void register(ServletContext scontext, PortalContainer container)
   {
      InputStream is = null;
      try
      {
         is = scontext.getResourceAsStream("/WEB-INF/conf/script/groovy/SkinConfigScript.groovy");
         Binding binding = new Binding();
         binding.setVariable("SkinService", skinService);
         binding.setVariable("ServletContext", scontext);
         binding.setVariable("ServletContextName", scontext.getServletContextName());
         binding.setVariable("PortalContainerName", container.getName());
         GroovyShell shell = new GroovyShell(binding);
         shell.evaluate(is);
      }
      catch (Exception ex)
      {
         LOG.error("An error occurs while processing 'SkinConfigScript.groovy' from the context '"
            + scontext.getServletContextName() + "'", ex);
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