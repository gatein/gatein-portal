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

import org.exoplatform.webui.Util;
import org.exoplatform.commons.serialization.api.annotations.Converted;
import org.exoplatform.webui.config.metadata.ComponentMetaData;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net May 4, 2006 */
@Converted(ComponentConfigConverter.class)
public class Component
{

   final ComponentHandle handle;

   private final String id;

   private final String type;

   private final String lifecycle;

   private final String template;

   private final String decorator;

   private final InitParams initParams;

   private final List<Validator> validators;

   private final List<Event> events;

   private final List<EventInterceptor> eventInterceptors;

   /** Declare this map as volatile to make double-check work properly **/
   private volatile Map<String, Event> eventMap;

   private Lifecycle<UIComponent> componentLifecycle;

   public Component(ComponentMetaData metaData)
   {
      this(
         new ComponentHandle(null, metaData.getId() == null ? metaData.getType() : metaData.getType() + metaData.getId()),
         metaData.getId(),
         metaData.getType(),
         metaData.getLifecycle(),
         metaData.getTemplate(),
         metaData.getDecorator(),
         metaData.getInitParams(),
         metaData.getValidators(),
         metaData.getEvents(),
         metaData.getEventInterceptors());
   }

   public Component(
      Class<?> owner,
      String id,
      String type,
      String lifecycle,
      String template,
      String decorator,
      InitParams initParams,
      List<Validator> validators,
      List<Event> events,
      List<EventInterceptor> eventInterceptors)
   {
      this(
         new ComponentHandle(owner, id == null ? type : type + ":" + id),
         id,
         type,
         lifecycle,
         template,
         decorator,
         initParams,
         validators,
         events,
         eventInterceptors);
   }

   private Component(
      ComponentHandle handle,
      String id,
      String type,
      String lifecycle,
      String template,
      String decorator,
      InitParams initParams,
      List<Validator> validators,
      List<Event> events,
      List<EventInterceptor> eventInterceptors)
   {
      this.handle = handle;
      this.id = id;
      this.type = type;
      this.lifecycle = lifecycle;
      this.template = template;
      this.decorator = decorator;
      this.initParams = initParams;
      this.validators = validators;
      this.events = events;
      this.eventInterceptors = eventInterceptors;
   }

   public String getKey()
   {
      return handle.getKey();
   }

   public String getId()
   {
      return id;
   }

   public String getType()
   {
      return type;
   }

   public String getLifecycle()
   {
      return lifecycle;
   }

   public String getTemplate()
   {
      return template;
   }

   public String getDecorator()
   {
      return decorator;
   }

   public InitParams getInitParams()
   {
      return initParams;
   }

   public List<Validator> getValidators()
   {
      return validators;
   }

   public List<Event> getEvents()
   {
      return events;
   }

   public List<EventInterceptor> getEventInterceptors()
   {
      return eventInterceptors;
   }

   public Event getUIComponentEventConfig(String eventName) throws Exception
   {
      if(eventMap == null)
      {
         synchronized(this)
         {
            if(eventMap == null)
            {
               Map<String, Event> temporaryMap = new HashMap<String, Event>();

               if (events == null)
               {
                  eventMap = temporaryMap;
                  return null;
               }

               for (Event event : events)
               {
                  createCachedEventListeners(event);
                  temporaryMap.put(event.getName(), event);
               }
               
               eventMap = temporaryMap;
            }
            
            return eventMap.get(eventName);
         }
      }
      else
      {
         return eventMap.get(eventName);
      }
   }

   public List<EventListener> getUIComponentEventListeners(String eventName) throws Exception
   {
      Event event = getUIComponentEventConfig(eventName);
      if (event == null)
      {
         return null;
      }
      List<EventListener> cachedListeners = event.getCachedEventListeners();
      if (cachedListeners != null)
      {
         return cachedListeners;
      }
      cachedListeners = new ArrayList<EventListener>();
      for (String listener : event.getListeners())
      {
         if (listener.indexOf(".") < 0)
         {
            listener = type + "$" + listener;
         }
         EventListener eventListener = (EventListener)Util.createObject(listener, event.getInitParams());
         cachedListeners.add(eventListener);
      }
      event.setCachedEventListeners(cachedListeners);
      return cachedListeners;
   }

   private void createCachedEventListeners(Event event) throws Exception
   {
      List<EventListener> cachedListeners = new ArrayList<EventListener>();
      for (String listener : event.getListeners())
      {
         if (listener.indexOf(".") < 0)
         {
            listener = type + "$" + listener;
         }
         EventListener eventListener = (EventListener)Util.createObject(listener, event.getInitParams());
         cachedListeners.add(eventListener);
      }
      event.setCachedEventListeners(cachedListeners);
   }

   public Lifecycle<UIComponent> getUIComponentLifecycle() throws Exception
   {
      if (componentLifecycle != null)
      {
         return componentLifecycle;
      }
      if (lifecycle != null)
      {
         componentLifecycle = (Lifecycle<UIComponent>)Util.createObject(lifecycle, null);
      }
      else
      {
         componentLifecycle = (Lifecycle<UIComponent>)Util.createObject(Lifecycle.class, null);
      }
      return componentLifecycle;
   }

}