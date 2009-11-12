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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ModelChange
{

   public static class Create extends ModelChange
   {

      /** . */
      private final String parentId;

      /** . */
      private final ModelData object;

      public Create(String parentId, ModelData object)
      {
         this.parentId = parentId;
         this.object = object;
      }

      public ModelData getObject()
      {
         return object;
      }

      @Override
      public String toString()
      {
         return "ModelChange.Create[parentId=" + parentId + "id=" + object.getStorageId() + ",name=" + object.getStorageName() + "]";
      }
   }

   public static class Update extends ModelChange
   {

      /** . */
      private final ModelData object;

      public Update(ModelData object)
      {
         this.object = object;
      }

      public ModelData getObject()
      {
         return object;
      }

      @Override
      public String toString()
      {
         return "ModelChange.Update[id=" + object.getStorageId() + "]";
      }
   }

   public static class Destroy extends ModelChange
   {

      /** . */
      private final String id;

      public Destroy(String id)
      {
         this.id = id;
      }

      public String getId()
      {
         return id;
      }

      @Override
      public String toString()
      {
         return "ModelChange.Destroy[id=" + id + "]";
      }
   }

   public static class Move extends ModelChange
   {

      /** . */
      private final String srcId;

      /** . */
      private final String dstId;

      /** . */
      private final String id;

      public Move(String srcId, String dstId, String id)
      {
         this.srcId = srcId;
         this.dstId = dstId;
         this.id = id;
      }

      public String getSrcId()
      {
         return srcId;
      }

      public String getDstId()
      {
         return dstId;
      }

      public String getId()
      {
         return id;
      }

      @Override
      public String toString()
      {
         return "ModelChange.Move[srcId=" + srcId + ",dstId=" + dstId + ",id" + id + "]";
      }
   }
}
