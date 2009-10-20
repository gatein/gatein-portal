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

package org.exoplatform.services.common.util;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * Sep 19, 2006  
 */
public class Queue<T>
{

   protected Node<T> current = null;

   protected Node<T> first = null;

   protected Node<T> last = null;

   public T pop()
   {
      T result = first.value;
      first = first.next;
      if (first == null)
         current = null;
      return result;
   }

   public boolean hasNext()
   {
      return first != null;
   }

   public void push(T v)
   {
      Node<T> newNode = new Node<T>(v);
      if (current != null)
      {
         current.next = newNode;
         current = newNode;
         return;
      }
      current = newNode;
      first = current;
   }
}
