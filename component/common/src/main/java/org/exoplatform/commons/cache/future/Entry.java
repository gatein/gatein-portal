/*
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

package org.exoplatform.commons.cache.future;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Entry<V> implements Serializable
{

   public static <V> Entry<V> create(V v)
   {
      return new Entry<V>(v);
   }

   /** . */
   private final V value;

   private Entry(V value)
   {
      if (value == null)
      {
         throw new NullPointerException();
      }
      this.value = value;
   }

   public V getValue()
   {
      return value;
   }

   @Override
   public String toString()
   {
      return "Entry[" + value + "]";
   }
}
