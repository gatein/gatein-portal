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

package org.exoplatform.portal.controller.resource;

/**
 * Identify a resource.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ResourceId
{

   /** . */
   private final ResourceScope scope;

   /** . */
   private final String name;

   public ResourceId(ResourceScope scope, String name)
   {
      this.scope = scope;
      this.name = name;
   }

   public ResourceScope getScope()
   {
      return scope;
   }

   public String getName()
   {
      return name;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof ResourceId)
      {
         ResourceId that = (ResourceId)obj;
         return scope == that.scope && name.equals(that.name);
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return scope.hashCode() ^ name.hashCode();
   }

   @Override
   public String toString()
   {
      return "ResourceId[type=" + scope.name() + ",id=" + name + "]";
   }
}
