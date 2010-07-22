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

package org.exoplatform.webui.config.metadata;

import org.exoplatform.webui.config.Event;
import org.exoplatform.webui.config.EventInterceptor;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.Validator;

import java.util.ArrayList;

/** Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net May 4, 2006 */
public class ComponentMetaData
{

   private String id;

   private String type;

   private String lifecycle;

   private String template;

   private String decorator;

   private InitParams initParams;

   private ArrayList<Validator> validators;

   private ArrayList<Event> events;

   private ArrayList<EventInterceptor> eventInterceptors;

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

   public void setId(String id)
   {
      this.id = id;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public void setLifecycle(String lifecycle)
   {
      this.lifecycle = lifecycle;
   }

   public void setTemplate(String template)
   {
      this.template = template;
   }

   public void setDecorator(String decorator)
   {
      this.decorator = decorator;
   }

   public InitParams getInitParams()
   {
      return initParams;
   }

   public void setInitParams(InitParams initParams)
   {
      this.initParams = initParams;
   }

   public ArrayList<Validator> getValidators()
   {
      return validators;
   }

   public void setValidators(ArrayList<Validator> validators)
   {
      this.validators = validators;
   }

   public ArrayList<Event> getEvents()
   {
      return events;
   }

   public void setEvents(ArrayList<Event> events)
   {
      this.events = events;
   }

   public ArrayList<EventInterceptor> getEventInterceptors()
   {
      return eventInterceptors;
   }

   public void setEventInterceptors(ArrayList<EventInterceptor> events)
   {
      eventInterceptors = events;
   }
}