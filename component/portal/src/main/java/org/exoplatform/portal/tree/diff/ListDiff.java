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

import java.util.Comparator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ListDiff<L1, L2, E> {

   /** . */
   final Comparator<E> comparator;

   /** . */
   final ListAdapter<L1, E> adapter1;

   /** . */
   final ListAdapter<L2, E> adapter2;

   public ListDiff(ListAdapter<L1, E> adapter1, ListAdapter<L2, E> adapter2, Comparator<E> comparator) {
      this.adapter1 = adapter1;
      this.adapter2 = adapter2;
      this.comparator = comparator;
   }

   public ListDiff(ListAdapter<L1, E> adapter1, ListAdapter<L2, E> adapter2) {
      this(adapter1, adapter2, null);
   }

   boolean equals(E e1, E e2) {
      if (comparator == null) {
         return e1.equals(e2);
      } else {
         return comparator.compare(e1, e2) == 0;
      }
   }

   public final ListChangeIterator<L1, L2, E> iterator(L1 elements1, L2 elements2) {
      return new ListChangeIterator<L1, L2, E>(this, elements1, elements2);
   }
}
