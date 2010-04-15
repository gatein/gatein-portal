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

package org.exoplatform.webui.application;

import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.webui.event.MonitorEvent;

/**
 * Monitors the lifecycle of an application.
 * Uses MonitorEvent to do the monitoring.
 * @see MonitorEvent
 */
public class MonitorApplicationLifecycle implements ApplicationLifecycle<WebuiRequestContext>
{

   public void onInit(Application app) throws Exception
   {
      WebuiApplication webuiapp = (WebuiApplication)app;
      MonitorEvent<WebuiApplication> event =
         new MonitorEvent<WebuiApplication>(webuiapp, MonitorEvent.PORTAL_APPLICATION_LIFECYCLE_EVENT, null);
      event.setStartExecutionTime(System.currentTimeMillis());
      app.setAttribute(MonitorEvent.PORTAL_APPLICATION_LIFECYCLE_EVENT, event);
      webuiapp.broadcast(event);
   }

   @SuppressWarnings("unchecked")
   public void onDestroy(Application app) throws Exception
   {
      WebuiApplication webuiapp = (WebuiApplication)app;
      MonitorEvent event = (MonitorEvent)app.getAttribute(MonitorEvent.PORTAL_APPLICATION_LIFECYCLE_EVENT);
      event.setEndExecutionTime(System.currentTimeMillis());
      webuiapp.broadcast(event);
   }

   public void onStartRequest(Application app, WebuiRequestContext rcontext) throws Exception
   {
      WebuiApplication webuiapp = (WebuiApplication)app;
      MonitorEvent<WebuiApplication> event =
         new MonitorEvent<WebuiApplication>(webuiapp, MonitorEvent.PORTAL_EXECUTION_LIFECYCLE_EVENT, rcontext);
      event.setStartExecutionTime(System.currentTimeMillis());
      rcontext.setAttribute(MonitorEvent.PORTAL_EXECUTION_LIFECYCLE_EVENT, event);
   }
   
   @SuppressWarnings("unchecked")
   public void onFailRequest(Application app, WebuiRequestContext rcontext, RequestFailure failureType) throws Exception
   {
      
   }

   @SuppressWarnings("unchecked")
   public void onEndRequest(Application app, WebuiRequestContext rcontext) throws Exception
   {
      WebuiApplication webuiapp = (WebuiApplication)app;
      MonitorEvent event = (MonitorEvent)rcontext.getAttribute(MonitorEvent.PORTAL_EXECUTION_LIFECYCLE_EVENT);
      event.setEndExecutionTime(System.currentTimeMillis());
      webuiapp.broadcast(event);
   }

}