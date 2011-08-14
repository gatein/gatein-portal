/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.web.controller.regexp;

/**
 * Should make it to org.gatein.common somehow.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SubCharSequence implements CharSequence
{

   /** . */
   private final CharSequence s;

   /** . */
   private final int from;

   /** . */
   private final int to;

   public SubCharSequence(CharSequence s, int from, int to)
   {
      if (s == null)
      {
         throw new NullPointerException("No null string accepted");
      }
      if (from < 0)
      {
         throw new IllegalArgumentException("No negative lower bound accepted");
      }
      if (to > s.length())
      {
         throw new IllegalArgumentException("Upper bound cannot be greater than the sequence length");
      }
      if (from > to)
      {
         throw new IllegalArgumentException("Upper bound cannot be lesser than the lower bound");
      }

      //
      this.s = s;
      this.from = from;
      this.to = to;
   }

   public int length()
   {
      return to - from;
   }

   public char charAt(int index)
   {
      if (index < 0)
      {
         throw new IndexOutOfBoundsException("Index cannot be negative");
      }
      index += from;
      if (index >= to)
      {
         throw new IndexOutOfBoundsException("Index cannot be negative");
      }
      return s.charAt(index);
   }

   public CharSequence subSequence(int start, int end)
   {
      if (start < 0)
      {
         throw new IndexOutOfBoundsException("The start argument cannot be negative");
      }
      if (end < 0)
      {
         throw new IndexOutOfBoundsException("The start argument cannot be negative");
      }
      if (start > end)
      {
         throw new IndexOutOfBoundsException("The start argument cannot greater than the end argument");
      }
      end += from;
      if (end > to)
      {
         throw new IndexOutOfBoundsException("The end argument cannot greater than the length");
      }
      return new SubCharSequence(s, from + start, end);
   }

   @Override
   public String toString()
   {
      return s.subSequence(from, to).toString();
   }
}
