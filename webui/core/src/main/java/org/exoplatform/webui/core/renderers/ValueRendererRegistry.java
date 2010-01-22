/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.exoplatform.webui.core.renderers;

import org.gatein.common.util.ParameterValidation;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ValueRendererRegistry
{
   private static Map<Class<?>, ValueRenderer<?>> DEFAULT_RENDERERS;
   private Map<Class<?>, ValueRenderer<?>> renderers;

   static
   {
      registerDefaultRendererFor(ValueRenderer.DEFAULT_RENDERER, String.class);

      FormattableValueRenderer<Number> numberRenderer = new FormattableValueRenderer<Number>(null, "number");
      registerDefaultRendererFor(numberRenderer, Number.class);
      registerDefaultRendererFor(numberRenderer, Byte.class);
      registerDefaultRendererFor(numberRenderer, Double.class);
      registerDefaultRendererFor(numberRenderer, Float.class);
      registerDefaultRendererFor(numberRenderer, Integer.class);
      registerDefaultRendererFor(numberRenderer, Long.class);
      registerDefaultRendererFor(numberRenderer, Short.class);

      FormattableValueRenderer<Date> dateRenderer = new FormattableValueRenderer<Date>(new SimpleDateFormat("HH:mm:ss yyyy-MM-dd"), "Datetime");
      registerDefaultRendererFor(dateRenderer, Date.class);
      registerDefaultRendererFor(dateRenderer, java.sql.Date.class);
      registerDefaultRendererFor(dateRenderer, Time.class);
      registerDefaultRendererFor(dateRenderer, Timestamp.class);
   }

   public <V> ValueRenderer<? super V> getRendererFor(V value)
   {
      if (value == null)
      {
         return ValueRenderer.NULL_RENDERER;
      }
      else
      {
         Class<?> valueType = value.getClass();
         // This is almost OK
         @SuppressWarnings("unchecked")
         ValueRenderer<? super V> renderer = (ValueRenderer<? super V>)getRendererFor(valueType);
         return renderer;
      }
   }

   public <V> ValueRenderer<? super V> getRendererFor(Class<V> valueType)
   {
      if (valueType == null)
      {
         return ValueRenderer.NULL_RENDERER;
      }

      // first check local renderers
      ValueRenderer<? super V> renderer = getRendererIn(valueType, renderers);
      if (renderer == null)
      {
         // then globally registered ones
         renderer = getRendererIn(valueType, DEFAULT_RENDERERS);

         // if we haven't found a match, check inheritance and return first match
         if (renderer == null)
         {
            for (Map.Entry<Class<?>, ValueRenderer<?>> entry : DEFAULT_RENDERERS.entrySet())
            {
               Class<?> type = entry.getKey();
               if (type.isAssignableFrom(valueType))
               {
                  // the valueType class is assignable to the type class which mean that
                  // the type class is a super class of the valueType class
                  // This cast is OK
                  @SuppressWarnings("unchecked")
                  ValueRenderer<? super V> tmp = (ValueRenderer<? super V>)entry.getValue();
                  renderer = tmp;

                  // OK
                  @SuppressWarnings("unchecked")
                  Class<? extends V> asSubclassOfV = (Class<? extends V>)type;

                  // add the found renderers to the default ones so that further look-ups will be faster
                  registerDefaultRendererFor(renderer, asSubclassOfV);

                  break;
               }
            }

            // if we still haven't found one, use the default
            if (renderer == null)
            {
               renderer = ValueRenderer.DEFAULT_RENDERER;
            }
         }

      }

      return renderer;
   }

   public static <V> void registerDefaultRendererFor(ValueRenderer<? super V> renderer, Class<? extends V> type)
   {
      DEFAULT_RENDERERS = registerIn(renderer, type, DEFAULT_RENDERERS);
   }

   public <V> void registerRendererFor(ValueRenderer<V> renderer, Class<? extends V> type)
   {
      renderers = registerIn(renderer, type, renderers);
   }

   private static <V> Map<Class<?>, ValueRenderer<?>> registerIn(
      ValueRenderer<? super V> renderer,
      Class<? extends V> type,
      Map<Class<?>, ValueRenderer<?>> renderers)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(type, "Value class");
      ParameterValidation.throwIllegalArgExceptionIfNull(renderer, "Renderer");

      if (renderers == null)
      {
         renderers = new HashMap<Class<?>, ValueRenderer<?>>(7);
      }
      else
      {
         // Copy on write for thread safety
         renderers = new HashMap<Class<?>, ValueRenderer<?>>(renderers);
      }

      renderers.put(type, renderer);

      return renderers;
   }

   private static <V> ValueRenderer<? super V> getRendererIn(Class<V> valueType, Map<Class<?>, ValueRenderer<?>> renderers)
   {
      if (renderers == null)
      {
         return null;
      }
      else
      {
         // this cast is OK
         @SuppressWarnings("unchecked")
         ValueRenderer<? super V> renderer = (ValueRenderer<? super V>)renderers.get(valueType);
         return renderer;
      }
   }
}
