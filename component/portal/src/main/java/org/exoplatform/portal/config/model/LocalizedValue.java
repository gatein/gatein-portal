/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.config.model;

import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LocalizedValue<V>
{

   /** . */
   private V value;

   /** . */
   private Locale lang;

   public LocalizedValue()
   {
   }

   public LocalizedValue(V value)
   {
      this.value = value;
   }

   public LocalizedValue(V value, Locale lang)
   {
      this.value = value;
      this.lang = lang;
   }

   public final V getValue()
   {
      return value;
   }

   public final void setValue(V value)
   {
      this.value = value;
   }

   public final Locale getLang()
   {
      return lang;
   }

   public final void setLang(Locale lang)
   {
      this.lang = lang;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[value=" + value + ",lang=" + lang + "]";
   }
}
