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

package org.exoplatform.portal.mop.description;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class CacheKey implements Serializable
{

   /** . */
   final Locale locale;

   /** . */
   final String id;

   public CacheKey(Locale locale, String id)
   {
      if (locale == null)
      {
         throw new NullPointerException();
      }
      if (id == null)
      {
         throw new NullPointerException();
      }

      //
      this.locale = locale;
      this.id = id;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof CacheKey)
      {
         CacheKey that = (CacheKey)obj;
         return locale.equals(that.locale) && id.equals(that.id);
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return locale.hashCode() ^ id.hashCode();
   }
}
