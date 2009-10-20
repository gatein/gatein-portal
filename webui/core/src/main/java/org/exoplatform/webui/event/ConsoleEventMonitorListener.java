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

package org.exoplatform.webui.event;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.Date;

/**
 * Created by The eXo Platform SAS
 * Jun 10, 2006
 * 
 * This class is listening to the events:
 *  - portal.application.lifecycle.event
 *  - portal.execution.lifecycle.event
 *  
 * The events are sent by the portal platform from the MonitorApplicationLifecycle class
 * 
 * Here we simply put in the log some response time information
 */
public class ConsoleEventMonitorListener extends EventListener
{

   protected static Log log = ExoLogger.getLogger("portal:ConsoleEventMonitorListener");

   public void execute(Event event) throws Exception
   {
      MonitorEvent mevent = (MonitorEvent)event;
      StringBuilder b = new StringBuilder();
      b.append("\nComponent ").append(event.getSource().getClass().getName()).append(
         ", phase " + event.getExecutionPhase()).append("\n  Start event ").append(mevent.getName()).append(" at ")
         .append(new Date(mevent.getStartExecutionTime()));
      if (mevent.getEndExecutionTime() > 0)
      {
         b.append("\n  End event ").append(mevent.getName()).append(" at ").append(
            new Date(mevent.getEndExecutionTime()));
         b.append("\n  Execute the event in ").append(mevent.getEndExecutionTime() - mevent.getStartExecutionTime());
      }
      b.append("\n");
      log.debug(b.toString());
   }

}