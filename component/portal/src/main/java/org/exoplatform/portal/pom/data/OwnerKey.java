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
package org.exoplatform.portal.pom.data;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class OwnerKey implements Serializable
{

   /** . */
   private final String type;

   /** . */
   private final String id;

   public OwnerKey(String type, String id)
   {
      if (type == null)
      {
         throw new NullPointerException();
      }
      if (id == null)
      {
         throw new NullPointerException();
      }
      this.type = type;
      this.id = id;
   }

   public String getType()
   {
      return type;
   }

   public String getId()
   {
      return id;
   }

   @Override
   public int hashCode()
   {
      return id.hashCode() ^ type.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }

      // We need to use class equality here
      if (obj != null && getClass().equals(obj.getClass()))
      {
         OwnerKey that = (OwnerKey)obj;
         return type.equals(that.type) && id.equals(that.id);
      }

      //
      return false;
   }
}
