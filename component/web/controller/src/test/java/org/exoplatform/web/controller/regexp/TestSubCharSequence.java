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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestSubCharSequence extends TestCase
{

   private final CharSequence seq = new SubCharSequence("abcdef", 1, 5);

   public void testState()
   {
      assertEquals(4, seq.length());
      assertEquals('b', seq.charAt(0));
      assertEquals('c', seq.charAt(1));
      assertEquals('d', seq.charAt(2));
      assertEquals('e', seq.charAt(3));
      assertEquals("bcde", seq.toString());
   }

   public void testSubSequence()
   {
      CharSequence sub = seq.subSequence(1, 3);
      assertEquals(2, sub.length());
      assertEquals('c', sub.charAt(0));
      assertEquals('d', sub.charAt(1));
      assertEquals("cd", sub.toString());
   }

   public void testSubSequenceThrowsIOOBE()
   {
      assertSubSequenceThrowsIIOBE(-1, 3);
      assertSubSequenceThrowsIIOBE(1, 5);
      assertSubSequenceThrowsIIOBE(1, -1);
      assertSubSequenceThrowsIIOBE(5, 1);
   }

   private void assertSubSequenceThrowsIIOBE(int start, int end)
   {
      try
      {
         seq.subSequence(start, end);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
      }
   }

   public void testCharAtThrowsIOOBE()
   {
      try
      {
         seq.charAt(-1);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
      }
      try
      {
         seq.charAt(4);
         fail();
      }
      catch (IndexOutOfBoundsException e)
      {
      }
   }

   public void testCtorThrowsNPE()
   {
      try
      {
         new SubCharSequence(null, 1, 5);
         fail();
      }
      catch (NullPointerException e)
      {
      }
   }

   public void testCtorThrowsIAE()
   {
      assertCtorThrowsIAE("a", -1, 1);
      assertCtorThrowsIAE("a", 0, 2);
      assertCtorThrowsIAE("a", 1, 0);
   }

   private void assertCtorThrowsIAE(String s, int from, int to)
   {
      try
      {
         new SubCharSequence(s, from, to);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }
}
