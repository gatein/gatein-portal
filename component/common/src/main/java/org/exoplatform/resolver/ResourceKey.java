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

package org.exoplatform.resolver;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ResourceKey implements Serializable
{

   /** . */
   private final int resolverId;

   /** . */
   private final String url;

   public ResourceKey(int resolverId, String url)
   {
      if (url == null)
      {
         throw new NullPointerException("no null URL accepted");
      }
      this.resolverId = resolverId;
      this.url = url;
   }

   public String getURL()
   {
      return url;
   }

   @Override
   public int hashCode()
   {
      return resolverId ^ url.hashCode();
   }

   @Override
   public boolean equals(Object o)
   {
      if (o == this)
      {
         return true;
      }
      if (o instanceof ResourceKey)
      {
         ResourceKey that = (ResourceKey)o;
         return resolverId == that.resolverId && url.equals(that.url);
      }
      return false;
   }
}
