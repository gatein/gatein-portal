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

package org.exoplatform.webui.test;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.EventInterceptorConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.config.annotation.ValidatorConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

@ComponentConfig(type = UIComponentAnnotation.class, lifecycle = UILifecycle.class, template = ".....................", decorator = "....................", events = {@EventConfig(name = "name", phase = Phase.DECODE, listeners = UIComponentAnnotation.UIComponentEvent.class, initParams = {@ParamConfig(name = "name", value = "value")})}, initParams = {@ParamConfig(name = "name", value = "value")}, validators = {@ValidatorConfig(type = UIComponentAnnotation.UIComponentValidator.class, initParams = {@ParamConfig(name = "name", value = "value")})}, eventInterceptors = {@EventInterceptorConfig(type = UIComponentAnnotation.UIComponentEventInterceptor.class, interceptors = {"inter1"}, initParams = {@ParamConfig(name = "name", value = "value")})}

)
public class UIComponentAnnotation
{

   static public class UIComponentValidator
   {

   }

   static public class UIComponentEventInterceptor
   {

   }

   static public class UIComponentEvent extends EventListener
   {
      @SuppressWarnings("unused")
      public void execute(Event event) throws Exception
      {

      }
   }
}
