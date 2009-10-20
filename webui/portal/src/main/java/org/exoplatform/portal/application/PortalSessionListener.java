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

package org.exoplatform.portal.application;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.web.AbstractHttpSessionListener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.WebAppController;

import javax.servlet.http.HttpSessionEvent;

/**
 * Created by The eXo Platform SAS        
 * Date: Jan 25, 2003
 * Time: 5:25:52 PM
 */
public class PortalSessionListener extends AbstractHttpSessionListener
{

   protected static Log log = ExoLogger.getLogger("portal:PortalSessionListener");

   public PortalSessionListener()
   {
   }

   @Override
   protected void onSessionDestroyed(ExoContainer container, HttpSessionEvent event)
   {
   }

   /**
    * This method is called when a HTTP session of a Portal instance is destroyed. 
    * By default the session time is 30 minutes.
    * 
    * In this method, we:
    * 1) first get the portal instance name from where the session is removed.
    * 2) Put the portal instance in the Portal ThreadLocal
    * 3) Get the main entry point (WebAppController) from the current portal container 
    * 4) Extract from the WebAppController the PortalApplication object which is the entry point to
    *    the StateManager object
    * 5) Expire the portal session stored in the StateManager
    * 6) Finally, removes the WindowInfos object from the WindowInfosContainer container
    * 7) Flush the {@link ThreadLocal} for the PortalContainer
    * 
    */
   @Override
   protected void onSessionCreated(ExoContainer container, HttpSessionEvent event)
   {
      try
      {
         if (log.isInfoEnabled())
            log.info("Destroy session from '" + container == null ? "unknown" : ((PortalContainer)container).getName()
               + "' portal");
         WebAppController controller = (WebAppController)container.getComponentInstanceOfType(WebAppController.class);
         PortalApplication portalApp = controller.getApplication(PortalApplication.PORTAL_APPLICATION_ID);
         portalApp.getStateManager().expire(event.getSession().getId(), portalApp);
      }
      catch (Exception ex)
      {
         log.error("Error while destroying a portal session", ex);
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
}
