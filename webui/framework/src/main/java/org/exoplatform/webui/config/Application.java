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

package org.exoplatform.webui.config;

import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.webui.Util;
import org.exoplatform.webui.config.Event;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * May 7, 2006
 */
public class Application
{

   private InitParams initParams;

   private String uiroot;

   private String stateManager;

   public InitParams getInitParams()
   {
      return initParams;
   }

   private ArrayList<String> lifecycleListeners;

   private ArrayList<Event> events;

   transient private Map<String, Event> eventMap;

   public String getUIRootComponent()
   {
      return uiroot;
   }

   public String getStateManager()
   {
      return stateManager;
   }

   public ArrayList<String> getLifecyleListeners()
   {
      return lifecycleListeners;
   }

   public ArrayList<Event> getEvents()
   {
      return events;
   }

   public Event getApplicationEventConfig(String eventName)
   {
      if (eventMap != null)
         return eventMap.get(eventName);
      eventMap = new HashMap<String, Event>();
      if (events == null)
         return null;
      for (Event event : events)
      {
         eventMap.put(event.getName(), event);
      }
      return eventMap.get(eventName);
   }

   public List<EventListener> getApplicationEventListeners(String eventName) throws Exception
   {
      Event event = getApplicationEventConfig(eventName);
      if (event == null)
         return null;
      List<EventListener> cachedListeners = event.getCachedEventListeners();
      if (cachedListeners != null)
         return cachedListeners;
      cachedListeners = new ArrayList<EventListener>();
      for (String listener : event.getListeners())
      {
         EventListener eventListener = (EventListener)Util.createObject(listener, null);
         cachedListeners.add(eventListener);
      }
      event.setCachedEventListeners(cachedListeners);
      return cachedListeners;
   }

   public List<ApplicationLifecycle> getApplicationLifecycleListeners() throws Exception
   {
      List<ApplicationLifecycle> appLifecycles = new ArrayList<ApplicationLifecycle>();
      if (lifecycleListeners == null)
         return appLifecycles;
      for (String type : lifecycleListeners)
      {
         ApplicationLifecycle instance = (ApplicationLifecycle)Util.createObject(type, null);
         appLifecycles.add(instance);
      }
      return appLifecycles;
   }
}