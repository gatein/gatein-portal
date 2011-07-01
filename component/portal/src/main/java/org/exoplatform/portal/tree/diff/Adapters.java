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

package org.exoplatform.portal.tree.diff;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Adapters
{

   /** . */
   private static final ArrayAdapter ARRAY_INSTANCE = new ArrayAdapter();

   public static <E> ListAdapter<E[], E> list()
   {
      @SuppressWarnings("unchecked")
      ListAdapter<E[], E> adapter = (ListAdapter<E[], E>)ARRAY_INSTANCE;
      return adapter;
   }

   private static class ArrayAdapter<E> implements ListAdapter<E[], E>
   {
      public int size(E[] list)
      {
         return list.length;
      }

      public Iterator<E> iterator(final E[] list, final boolean reverse)
      {
         return new Iterator<E>()
         {
            /** . */
            int count = 0;

            public boolean hasNext()
            {
               return count < list.length;
            }

            public E next()
            {
               if (!hasNext())
               {
                  throw new NoSuchElementException();
               }
               int index = count++;
               if (reverse)
               {
                  index = list.length - index - 1;
               }
               return list[index];
            }

            public void remove()
            {
               throw new UnsupportedOperationException();
            }
         };
      }
   }
}
