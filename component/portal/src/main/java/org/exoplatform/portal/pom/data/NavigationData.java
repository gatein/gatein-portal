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

import java.util.List;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
* @version $Revision$
*/
public class NavigationData extends NavigationNodeContainerData
{

   /** . */
   private final NavigationKey key;

   /** . */
   private final int priority;

   public NavigationData(
      String ownerType,
      String ownerId,
      Integer priority,
      List<NavigationNodeData> children)
   {
      this(null, ownerType, ownerId, priority, children);
   }

   public NavigationData(
      String storageId,
      String ownerType,
      String ownerId,
      Integer priority,
      List<NavigationNodeData> children)
   {
      super(storageId, children);

      //
      if (ownerType == null)
      {
         throw new NullPointerException("No null owner type");
      }
      if (ownerId == null)
      {
         throw new NullPointerException("No null owner id");
      }

      //
      this.key = new NavigationKey(ownerType, ownerId);
      this.priority = priority != null ? priority : 1;
   }

   public NavigationKey getKey()
   {
      return key;
   }

   public String getOwnerType()
   {
      return key.getType();
   }

   public String getOwnerId()
   {
      return key.getId();
   }

   public int getPriority()
   {
      return priority;
   }
}
