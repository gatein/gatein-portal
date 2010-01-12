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

import java.text.Format;

/**
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class FormattableValueRenderer<V> extends ValueRenderer<V>
{

   private String cssClass;

   private Format formatPrototype;

   private final ThreadLocal<Format> format = new ThreadLocal<Format>()
   {
      @Override
      public Format get()
      {
         return (Format)formatPrototype.clone();
      }
   };

   public FormattableValueRenderer(Format format, String cssClass)
   {
      this.formatPrototype = format;
      this.cssClass = cssClass;
   }

   @Override
   public String render(V value)
   {
      if (formatPrototype != null)
      {
         return format.get().format(value);
      }
      else
      {
         return super.render(value);
      }
   }

   @Override
   public String getCSSClassFor(V value)
   {
      if (cssClass != null)
      {
         return cssClass;
      }
      else
      {
         return super.getCSSClassFor(value);
      }
   }
}
