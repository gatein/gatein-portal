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

package org.exoplatform.commons.utils;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestQueues extends TestCase
{
   public void testLIFO()
   {
      Queue<String> lifo = Queues.lifo();
      assertEquals(0, lifo.size());
      assertTrue(lifo.add("a"));
      assertEquals(1, lifo.size());
      assertTrue(lifo.add("b"));
      assertEquals(2, lifo.size());
      assertTrue(lifo.add("c"));
      assertEquals(3, lifo.size());
      Iterator<String> it = lifo.iterator();
      assertEquals("c", it.next());
      assertEquals("b", it.next());
      assertEquals("a", it.next());
      assertFalse(it.hasNext());
      assertEquals("c", lifo.peek());
      assertEquals(3, lifo.size());
      assertEquals("c", lifo.poll());
      assertEquals(2, lifo.size());
      assertEquals("b", lifo.poll());
      assertEquals(1, lifo.size());
      assertEquals("a", lifo.poll());
      assertEquals(0, lifo.size());
      assertEquals(null, lifo.poll());
      assertEquals(null, lifo.peek());
      assertEquals(0, lifo.size());
      assertEquals(null, lifo.poll());
      assertEquals(null, lifo.peek());
      assertEquals(0, lifo.size());
   }

   public void testLIFOResize()
   {
      Queue<String> lifo = Queues.lifo(0);
      assertEquals(0, lifo.size());
      lifo.add("a");
      assertEquals(1, lifo.size());
      assertEquals("a", lifo.peek());
      assertEquals("a", lifo.poll());
      assertEquals(0, lifo.size());
   }

   public void testEmpty()
   {
      Queue<String> lifo = Queues.empty();
      assertFalse(lifo.offer(""));
      try
      {
         lifo.add("");
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
      assertEquals(0, lifo.size());
      assertNull(lifo.peek());
      assertNull(lifo.poll());
      try
      {
         lifo.element();
         fail();
      }
      catch (NoSuchElementException ignore)
      {
      }
   }
}
