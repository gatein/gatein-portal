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
   private static Map<Class, ValueRenderer> DEFAULT_RENDERERS;
   private Map<Class, ValueRenderer> renderers;

   static
   {
      FormattableValueRenderer numberRenderer = new FormattableValueRenderer(null, "number");
      registerDefaultRendererFor(numberRenderer, Number.class);
      registerDefaultRendererFor(numberRenderer, Byte.class);
      registerDefaultRendererFor(numberRenderer, Double.class);
      registerDefaultRendererFor(numberRenderer, Float.class);
      registerDefaultRendererFor(numberRenderer, Integer.class);
      registerDefaultRendererFor(numberRenderer, Long.class);
      registerDefaultRendererFor(numberRenderer, Short.class);

      FormattableValueRenderer dateRenderer = new FormattableValueRenderer(new SimpleDateFormat("HH:mm:ss yyyy-MM-dd"), "Datetime");
      registerDefaultRendererFor(dateRenderer, Date.class);
      registerDefaultRendererFor(dateRenderer, java.sql.Date.class);
      registerDefaultRendererFor(dateRenderer, Time.class);
      registerDefaultRendererFor(dateRenderer, Timestamp.class);
   }


   public <ValueType> ValueRenderer<ValueType> getRendererFor(Class<? extends ValueType> valueType)
   {
      if (valueType == null)
      {
         return ValueRenderer.NULL_RENDERER;
      }

      // first check local renderers
      ValueRenderer renderer = getRendererIn(valueType, renderers);
      if (renderer == null)
      {
         // then globally registered ones
         renderer = getRendererIn(valueType, DEFAULT_RENDERERS);

         // if we haven't found a match, check inheritance and return first match
         if (renderer == null)
         {
            for (Map.Entry<Class, ValueRenderer> entry : DEFAULT_RENDERERS.entrySet())
            {
               Class type = entry.getKey();
               if (type.isAssignableFrom(valueType))
               {
                  renderer = entry.getValue();

                  // add the found renderers to the default ones so that further look-ups will be faster
                  registerDefaultRendererFor(renderer, type);

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

   public static <ValueType> void registerDefaultRendererFor(ValueRenderer<ValueType> renderer, Class<? extends ValueType> type)
   {
      DEFAULT_RENDERERS = registerIn(renderer, type, DEFAULT_RENDERERS);
   }

   public <ValueType> void registerRendererFor(ValueRenderer<ValueType> renderer, Class<? extends ValueType> type)
   {
      renderers = registerIn(renderer, type, renderers);
   }

   private static <ValueType> Map<Class, ValueRenderer> registerIn(ValueRenderer<ValueType> renderer, Class<? extends ValueType> type, Map<Class, ValueRenderer> renderers)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(type, "Value class");
      ParameterValidation.throwIllegalArgExceptionIfNull(renderer, "Renderer");

      if (renderers == null)
      {
         renderers = new HashMap<Class, ValueRenderer>(7);
      }

      renderers.put(type, renderer);

      return renderers;
   }

   private static ValueRenderer getRendererIn(Class valueType, Map<Class, ValueRenderer> renderers)
   {
      if (renderers == null)
      {
         return null;
      }
      else
      {
         return renderers.get(valueType);
      }
   }
}
