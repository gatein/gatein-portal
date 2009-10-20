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

package org.exoplatform.services.html.path;

import org.exoplatform.services.html.Name;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Aug 15, 2006
 */
public class NodePath
{

   protected Index[] indexs;

   public NodePath(Index[] indexs)
   {
      this.indexs = indexs;
   }

   public Index[] getIndexs()
   {
      return indexs;
   }

   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      for (Index index : indexs)
      {
         if (builder.length() > 0)
            builder.append('.');
         builder.append(index.getName()).append('[').append(index.getIdx()).append(']');
      }
      return builder.toString();
   }

   public static class Index
   {

      private Name name;

      private int idx;

      public Index(Name name, int idx)
      {
         this.name = name;
         this.idx = idx;
      }

      public Name getName()
      {
         return name;
      }

      public void setName(Name name)
      {
         this.name = name;
      }

      public int getIdx()
      {
         return idx;
      }

      public void setIdx(int idx)
      {
         this.idx = idx;
      }

   }

}
