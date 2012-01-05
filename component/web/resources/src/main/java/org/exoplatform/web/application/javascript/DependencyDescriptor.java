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

package org.exoplatform.web.application.javascript;

import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.script.FetchMode;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DependencyDescriptor
{

   /** . */
   private final ResourceId resourceId;

   /** . */
   private final FetchMode fetchMode;

   public DependencyDescriptor(ResourceId resourceId, FetchMode fetchMode) throws NullPointerException
   {
      if (resourceId == null)
      {
         throw new NullPointerException("No null resource id accepted");
      }

      //
      this.resourceId = resourceId;
      this.fetchMode = fetchMode;
   }

   public ResourceId getResourceId()
   {
      return resourceId;
   }

   public FetchMode getFetchMode()
   {
      return fetchMode;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof DependencyDescriptor)
      {
         DependencyDescriptor that = (DependencyDescriptor)obj;
         return resourceId.equals(that.resourceId) && fetchMode == that.fetchMode;
      }
      return false;
   }
}
