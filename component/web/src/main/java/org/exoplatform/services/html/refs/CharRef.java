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

package org.exoplatform.services.html.refs;

import java.util.Comparator;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 8, 2006
 */
class CharRef
{

   static final Comparator<CharRef> comparator = new Comparator<CharRef>()
   {
      public int compare(CharRef o1, CharRef o2)
      {
         return o1.getName().compareTo(o2.getName());
      }
   };

   private int value;

   private String name;

   CharRef(String n, int v)
   {
      name = n;
      value = v;
      if (name == null)
         name = "";
   }

   String getName()
   {
      return name;
   }

   void setName(String name)
   {
      this.name = name;
   }

   int getValue()
   {
      return value;
   }

   void setValue(int value)
   {
      this.value = value;
   }

   int compare(CharRef r)
   {
      return getName().compareTo(r.getName());
   }
}