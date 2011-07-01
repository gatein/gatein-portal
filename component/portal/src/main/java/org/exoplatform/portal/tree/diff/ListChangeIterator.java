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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over a list of {@link ListChangeType} computed from two list of  objects. The implementation
 * is optimized to use the LCS algorithm only when needed, for trivial list no LCS computation should be
 * required.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ListChangeIterator<L1, L2, E> implements Iterator<ListChangeType>
{

   /** . */
   private static final int[] EMPTY = new int[0];

   /** . */
   private static final int TRIVIAL_MODE = 0;

   /** . */
   private static final int LCS_MODE = 1;

   /** . */
   ListDiff<L1, L2, E> diff;

   /** . */
   private final L1 elements1;

   /** . */
   private final L2 elements2;

   /** . */
   private final Iterator<E> it1;

   /** . */
   private final Iterator<E> it2;

   /** . */
   private int index1;

   /** . */
   private int index2;

   /** . */
   private E next1;

   /** . */
   private E next2;

   /** . */
   private E element;

   /** . */
   private ListChangeType type;

   /** . */
   private int mode;

   /** . */
   private boolean buffered;

   // LCS state

   /** . */
   private int[] matrix;

   /** . */
   private int m;

   /** . */
   private int n;

   ListChangeIterator(ListDiff<L1, L2, E> diff, L1 elements1, L2 elements2) {
      this.diff = diff;
      this.elements1 = elements1;
      this.elements2 = elements2;
      this.it1 = elements1 != null ? diff.adapter1.iterator(elements1, false) : null;
      this.it2 = elements2 != null ? diff.adapter2.iterator(elements2, false) : null;
      this.mode = TRIVIAL_MODE;

      //
      this.index1 = 0;
      this.index2 = 0;
      this.buffered = false;
      this.next1 = null;
      this.next2 = null;
      this.type = null;
      this.element = null;

      //
      if (it1 != null && it1.hasNext()) {
         next1 = it1.next();
      }
      if (it2 != null && it2.hasNext()) {
         next2 = it2.next();
      }

      //
      this.m = 0;
      this.n = 0;
      this.matrix = EMPTY;
   }

   private void next1() {
      index1++;
      if (it1 != null && it1.hasNext()) {
         next1 = it1.next();
      } else {
         next1 = null;
      }
   }

   private void next2() {
      index2++;
      if (it2 != null && it2.hasNext()) {
         next2 = it2.next();
      } else {
         next2 = null;
      }
   }

   public boolean hasNext() {

      while (!buffered) {
         if (mode == TRIVIAL_MODE) {
            if (next1 != null) {
               if (next2 != null) {
                  if (diff.equals(next1, next2)) {
                     type = ListChangeType.SAME;
                     element = next1;
                     buffered = true;
                     next1();
                     next2();
                  } else {
                     lcs(index1, elements1, elements2);
                     mode = LCS_MODE;
                  }
               } else {
                  type = ListChangeType.REMOVE;
                  element = next1;
                  buffered = true;
                  next1();
               }
            } else {
               if (next2 != null) {
                  type = ListChangeType.ADD;
                  element = next2;
                  buffered = true;
                  next2();
               } else {
                  // Force a break with buffered to false
                  break;
               }
            }
         } else if (mode == LCS_MODE) {
            E elt1 = null;
            E elt2 = null;
            int i = diff.adapter1.size(elements1) - index1;
            int j = diff.adapter2.size(elements2) - index2;
            if (i > 0 && j > 0 && diff.equals(elt1 = next1, elt2 = next2)) {
               type = ListChangeType.SAME;
               element = elt1;
               next1();
               next2();
               buffered = true;
            } else {
               int index1 = i + (j - 1) * m;
               int index2 = i - 1 + j * m;
               if (j > 0 && (i == 0 || matrix[index1] >= matrix[index2])) {
                  type = ListChangeType.ADD;
                  element = elt2 == null ? next2 : elt2;
                  next2();
                  buffered = true;
               } else if (i > 0 && (j == 0 || matrix[index1] < matrix[index2])) {
                  type = ListChangeType.REMOVE;
                  element = elt1 == null ? next1 : elt1;
                  next1();
                  buffered = true;
               } else {
                  // Force a break with buffered to false
                  break;
               }
            }
         } else {
            throw new AssertionError();
         }
      }

      //
      return buffered;
   }

   public ListChangeType next() {
      if (!hasNext()) {
         throw new NoSuchElementException();
      } else {
         buffered = false;
         return type;
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }

   public E getElement() {
      return element;
   }

   public int getIndex1() {
      return index1;
   }

   public int getIndex2() {
      return index2;
   }

   /**
    * Compute the LCS matrix from the specified offset. It updates the state of this object
    * with the relevant state. The LCS matrix is computed using the LCS algorithm
    * (see http://en.wikipedia.org/wiki/Longest_common_subsequence_problem).
    *
    * @param offset the offset
    * @param elements1 the elements 1
    * @param elements2 the elements 2
    */
   private void lcs(int offset, L1 elements1, L2 elements2) {
      m = 1 + diff.adapter1.size(elements1) - offset;
      n = 1 + diff.adapter2.size(elements2) - offset;

      //
      int s = m * n;
      matrix = new int[s];

      // Compute the lcs matrix
      Iterator<E> itI = diff.adapter1.iterator(elements1, true);
      for (int i = 1; i < m; i++) {
         E abc = itI.next();
         Iterator<E> itJ = diff.adapter2.iterator(elements2, true);
         for (int j = 1; j < n; j++) {
            int index = i + j * m;
            int v;
            E def = itJ.next();
            if (diff.equals(abc, def)) {
               v = matrix[index - m - 1] + 1;
            } else {
               int v1 = matrix[index - 1];
               int v2 = matrix[index - m];
               v = v1 < v2 ? v2 : v1;
            }
            matrix[index] = v;
         }
      }
   }

   // For unit testing purpose
   String getMatrix() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < m; i++) {
         sb.append('[');
         for (int j = 0; j < n; j++) {
            if (j > 0) {
               sb.append(',');
            }
            sb.append(matrix[i + j * m]);
         }
         sb.append("]\n");
      }
      return sb.toString();
   }
}
