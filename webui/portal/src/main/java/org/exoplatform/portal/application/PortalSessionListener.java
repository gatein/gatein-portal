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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This session listener was created for purpose of expiring the portal session stored in the StateManager
 * and removes the WindowInfos object from the WindowInfosContainer container
 * <p>
 * Created by The eXo Platform SAS        
 * Date: Jan 25, 2003
 * Time: 5:25:52 PM
 * 
 * @deprecated Currently we do not store anything outside of the session,
 * that's why we do not need to clean anything when a session is destroyed.
 */
@Deprecated
public class PortalSessionListener implements HttpSessionListener
{

   protected static Log log = ExoLogger.getLogger("portal.PortalSessionListener");

   public PortalSessionListener()
   {
      log.debug("This session listener is not useful anymore and it is left empty for now to be compatible with older versions");
   }

   @Override
   public void sessionCreated(HttpSessionEvent se)
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
   public void sessionDestroyed(HttpSessionEvent se)
   {
   }
}
