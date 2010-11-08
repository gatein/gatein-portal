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

package org.exoplatform.web;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.container.util.EnvSpecific;
import org.exoplatform.container.web.AbstractHttpSessionListener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.EventObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * This class is used to broadcast any "HttpEvent" to allow the rest of the platform to be notified
 * without changing the web.xml file. 
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 25 sept. 2009  
 */
public class GenericHttpListener extends AbstractHttpSessionListener implements ServletContextListener
{

   /**
    * The name of the "session created" event
    */
   public static final String SESSION_CREATED = "org.exoplatform.web.GenericHttpListener.sessionCreated";

   /**
    * The name of the "session destroyed" event
    */
   public static final String SESSION_DESTROYED = "org.exoplatform.web.GenericHttpListener.sessionDestroyed";

   /**
    * The name of the "context initialized" event
    */
   public static final String CONTEXT_INITIALIZED = "org.exoplatform.web.GenericHttpListener.contextInitialized";

   /**
    * The name of the "context destroyed" event
    */
   public static final String CONTEXT_DESTROYED = "org.exoplatform.web.GenericHttpListener.contextDestroyed";

   /**
    * Logger.
    */
   private static Log log = ExoLogger.getLogger("portal:GenericHttpListener");

   /**
    * This method is called when a HTTP session of a Portal instance is created. 
    * 
    * In this method, we:
    * 1) first get the portal instance name for which the session is created.
    * 2) Put the portal instance in the Portal ThreadLocal
    * 3) Broadcast the SESSION_CREATED event
    * 4) Flush the {@link ThreadLocal} for the PortalContainer
    */
   @Override
   protected void onSessionCreated(ExoContainer container, HttpSessionEvent event)
   {
      try
      {
         broadcast((PortalContainer)container, SESSION_CREATED, event);
      }
      catch (Exception ex)
      {
         log.error("Error on sessionCreated", ex);
      }
   }

   /**
    * This method is called when a HTTP session of a Portal instance is destroyed. 
    * 
    * In this method, we:
    * 1) first get the portal instance name for which the session is created.
    * 2) Put the portal instance in the Portal ThreadLocal
    * 3) Broadcast the SESSION_DESTROYED event
    * 4) Flush the {@link ThreadLocal} for the PortalContainer
    */
   @Override
   protected void onSessionDestroyed(ExoContainer container, HttpSessionEvent event)
   {
      try
      {
         broadcast((PortalContainer)container, SESSION_DESTROYED, event);
      }
      catch (Exception ex)
      {
         log.error("Error on sessionDestroyed", ex);
      }
   }

   /**
    * This method is called when the {@link ServletContext} of the Portal is destroyed.  
    * 
    * In this method, we:
    * 1) first get the portal instance name for which the session is created.
    * 2) Put the portal instance in the Portal ThreadLocal
    * 3) Broadcast the CONTEXT_DESTROYED event
    * 4) Flush the {@link ThreadLocal} for the PortalContainer
    */
   public void contextDestroyed(ServletContextEvent event)
   {
      boolean hasBeenSet = false;
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
      try
      {
         PortalContainer portalContainer = PortalContainer.getInstanceIfPresent();
         if (portalContainer == null)
         {
            portalContainer = PortalContainer.getCurrentInstance(event.getServletContext());
            PortalContainer.setInstance(portalContainer);
            hasBeenSet = true;
         }
         final String ctxName = event.getServletContext().getServletContextName();
         if (!PortalContainer.isPortalContainerNameDisabled(ctxName) && portalContainer instanceof PortalContainer)
         {
            // The portal container corresponding to the current servlet context could be found
            broadcast(portalContainer, CONTEXT_DESTROYED, event);
         }
         else if (PropertyManager.isDevelopping())
         {
            log.info("The portal environment could not be set for the webapp '" + ctxName
                  + "' because this servlet context has not been defined as a "
                  + "dependency of any portal container or it is a disabled portal"
                  + " container, the contextDestroyed event will be ignored");
         }
      }
      catch (Exception ex)
      {
         log.error("Error on contextDestroyed", ex);
      }
      finally
      {
         if (hasBeenSet)
         {
            PortalContainer.setInstance(null);
            ExoContainerContext.setCurrentContainer(oldContainer);
         }
      }
   }

   /**
    * This method is called when the {@link ServletContext} of the Portal is initialized. 
    * 
    * In this method, we:
    * 1) first get the portal instance name for which the session is created.
    * 2) Put the portal instance in the Portal ThreadLocal
    * 3) Broadcast the CONTEXT_INITIALIZED event
    * 4) Flush the {@link ThreadLocal} for the PortalContainer
    */
   public void contextInitialized(final ServletContextEvent event)
   {
      final PortalContainerPostInitTask task = new PortalContainerPostInitTask()
      {
         public void execute(ServletContext scontext, PortalContainer portalContainer)
         {
            try
            {
               broadcast(portalContainer, CONTEXT_INITIALIZED, event);
            }
            catch (Exception ex)
            {
               log.error("Error on contextInitialized", ex);
            }
         }
      };
      ServletContext ctx = event.getServletContext();
      try
      {
         EnvSpecific.initThreadEnv(ctx);
         RootContainer.getInstance().addInitTask(event.getServletContext(), task);
      }
      finally
      {
         EnvSpecific.cleanupThreadEnv(ctx);      
      }
      
   }

   /**
    * @see org.exoplatform.container.web.AbstractHttpSessionListener#requirePortalEnvironment()
    */
   @Override
   protected boolean requirePortalEnvironment()
   {
      return true;
   }

   /**
    * Allow the rest of the application to be notified when an HttpEvent happens
    * @param portalContainer the related portal container
    * @param eventName the name of the event to broadcast
    * @param event the notification event
    */
   private <T extends EventObject> void broadcast(PortalContainer portalContainer, String eventName, T event)
   {
      try
      {
         ListenerService listenerService =
            (ListenerService)portalContainer.getComponentInstanceOfType(ListenerService.class);
         listenerService.broadcast(eventName, portalContainer, event);
      }
      catch (Exception e)
      {
         log.warn("Cannot broadcast the event '" + eventName + "'", e);
      }
   }
}
