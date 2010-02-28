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
package org.exoplatform.commons.utils;

import org.exoplatform.component.test.AbstractGateInTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestBufferingOutputStream extends AbstractGateInTest
{

   private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
   private BufferingOutputStream out = new BufferingOutputStream(bytes, 5);

   @Override
   protected void setUp() throws Exception
   {
      bytes = new ByteArrayOutputStream();
      out = new BufferingOutputStream(bytes, 5);
   }

   @Override
   protected void tearDown() throws Exception
   {
      bytes = null;
      out = null;
   }

   public void testCtorIAE()
   {
      try
      {
         new BufferingOutputStream(bytes, 0);
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new BufferingOutputStream(bytes, -1);
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testCtorNPE()
   {
      try
      {
         new BufferingOutputStream(null, 1);
      }
      catch (NullPointerException e)
      {
      }
   }

   public void testStreamClose() throws Exception
   {
      final AtomicBoolean closed = new AtomicBoolean(false);
      BufferingOutputStream out = new BufferingOutputStream(new ByteArrayOutputStream()
      {
         @Override
         public void close() throws IOException
         {
            closed.set(true);
            super.close();
         }
      }, 1);
      out.close();
      assertTrue(closed.get());
      try
      {
         out.write(0);
      }
      catch (IOException ignore)
      {
      }
      try
      {
         out.write(new byte[]{0});
      }
      catch (IOException ignore)
      {
      }
      try
      {
         out.flush();
      }
      catch (IOException ignore)
      {
      }
      try
      {
         out.close();
      }
      catch (IOException ignore)
      {
      }
   }

   public void testFlush() throws Exception
   {
      out.write(0);
      assertBytes();
      out.flush();
      assertBytes(0);
      out.write(new byte[]{1, 2, 3, 4});
      assertBytes();
      out.close();
      assertBytes(1, 2, 3, 4);
   }

   public void testWriteByte() throws Exception
   {
      out.write(0);
      assertBytes();
      out.close();
      assertBytes(0);
   }

   public void testAlmostFill() throws Exception
   {
      out.write(new byte[]{0, 1, 2, 3});
      assertBytes();
      out.close();
      assertBytes(0, 1, 2, 3);
   }
   
   public void testFill() throws Exception
   {
      out.write(new byte[]{0, 1, 2, 3, 4});
      assertBytes(0, 1, 2, 3, 4);
      out.close();
      assertBytes();
   }

   public void testBufferOverflowWithByte() throws Exception
   {
      out.write(new byte[]{0, 1, 2, 3});
      assertBytes();
      out.write(4);
      assertBytes();
      out.write(5);
      assertBytes(0, 1, 2, 3, 4);
      out.close();
      assertBytes(5);
   }
   public void testBufferOverflowWithArray() throws Exception
   {
      out.write(new byte[]{0, 1, 2, 3});
      assertBytes();
      out.write(new byte[]{4});
      assertBytes(0, 1, 2, 3);
      out.close();
      assertBytes(4);
   }

   public void testBufferOverflowWithLongArray() throws Exception
   {
      out.write(new byte[]{0, 1, 2, 3});
      assertBytes();
      out.write(new byte[]{4, 5, 6, 7, 8, 9});
      assertBytes(0, 1, 2, 3, 4, 5, 6, 7, 8);
      out.close();
      assertBytes(9);
   }

   public void testBufferOverflowWithVeryLongArray() throws Exception
   {
      out.write(new byte[]{0, 1, 2, 3});
      assertBytes();
      out.write(new byte[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
      assertBytes(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
      out.close();
      assertBytes(14);
   }

   private void assertBytes(int... expectedBytes)
   {
      int len = expectedBytes.length;
      assertEquals(len, bytes.size());
      if (len > 0)
      {
         byte[] actualBytes = bytes.toByteArray();
         for (int i = 0;i < len;i++)
         {
            int expectedByte = expectedBytes[i];
            byte actualByte = actualBytes[i];
            assertEquals("Was expecting byte at index " + i + " to be equals to " + expectedByte + " instead of " + actualByte, expectedByte, actualByte);
         }
         bytes.reset();
      }
   }

}
