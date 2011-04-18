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
package org.exoplatform.portal.config;

import junit.framework.Assert;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class EventQueue extends Listener
{

   /** . */
   private final LinkedList<Event> events;

   public EventQueue()
   {
      this.events = new LinkedList<Event>();
   }

   @Override
   public void onEvent(Event event) throws Exception
   {
      events.add(event);
   }

   public void assertSize(int expectedSize)
   {
      Assert.assertEquals("Was expecting events size to be " + expectedSize + " instead of " + toString(), expectedSize, events.size());
   }

   public void clear()
   {
      events.clear();
   }

   @Override
   public String toString()
   {
      List<String> tmp = new ArrayList<String>(events.size());
      for (Event event : events)
      {
         tmp.add("Event[name=" + event.getEventName() + ",data" + event.getData() + "]");
      }
      return tmp.toString();
   }
}
