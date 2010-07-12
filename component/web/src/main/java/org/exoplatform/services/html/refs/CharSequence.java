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

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 8, 2006
 */
class CharsSequence
{

   private int index;

   private char[] values;

   CharsSequence(int max)
   {
      values = new char[max];
      index = 0;
   }

   void append(char c)
   {
      if (index >= values.length)
         return;
      values[index] = c;
      index++;
   }

   void append(String string)
   {
      char[] cs = string.toCharArray();
      for (int i = 0; i < cs.length; i++)
      {
         if (index >= values.length)
            return;
         values[index] = cs[i];
         index++;
      }
   }

   char[] getValues()
   {
      char[] newValues = new char[index];
      System.arraycopy(values, 0, newValues, 0, index);
      return newValues;
   }
}