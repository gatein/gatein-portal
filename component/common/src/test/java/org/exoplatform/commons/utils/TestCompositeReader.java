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

import org.exoplatform.component.test.AbstractGateInTest;
import org.gatein.common.io.IOTools;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestCompositeReader extends AbstractGateInTest
{
   
   private static class MyReader extends StringReader
   {
      
      boolean closed = false;
      
      private MyReader(String s)
      {
         super(s);
      }

      @Override
      public void close()
      {
         closed = true;
         super.close();
      }
   }

   public void testMono()
   {
      MyReader r = new MyReader("foo");
      CompositeReader reader = new CompositeReader(r);
      assertEquals(reader, "foo");
      assertTrue(r.closed);
   }

   public void testPair()
   {
      MyReader r1 = new MyReader("foo");
      MyReader r2 = new MyReader("bar");
      CompositeReader reader = new CompositeReader(r1, r2);
      assertEquals(reader, "foobar");
      assertTrue(r1.closed);
      assertTrue(r2.closed);
   }

   public void testEmpty()
   {
      MyReader r1 = new MyReader("");
      MyReader r2 = new MyReader("bar");
      CompositeReader reader = new CompositeReader(r1, r2);
      assertEquals(reader, "bar");

      r1 = new MyReader("foo");
      r2 = new MyReader("");
      reader = new CompositeReader(r1, r2);
      assertEquals(reader, "foo");
   }
   
   public void testClose() throws IOException
   {
      MyReader r1 = new MyReader("foo");
      MyReader r2 = new MyReader("bar");
      MyReader r3 = new MyReader("juu");
      CompositeReader reader = new CompositeReader(r1, r2, r3);
      reader.read(new char[4], 0, 4);
      assertTrue(r1.closed);
      assertFalse(r2.closed);
      assertFalse(r3.closed);
      reader.close();
      assertTrue(r1.closed);
      assertTrue(r2.closed);
      assertTrue(r3.closed);
   }

   private void assertEquals(Reader reader, String expected)
   {
      try
      {
         StringWriter writer = new StringWriter();
         IOTools.copy(reader, writer);
         String test = writer.toString();
         assertEquals(expected, test);
      }
      catch (IOException e)
      {
         fail(e);
      }
   }


}
