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

package org.exoplatform.webui.replication.converter;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.exoplatform.webui.application.replication.SerializationContext;
import org.exoplatform.webui.application.replication.api.TypeConverter;
import org.exoplatform.webui.application.replication.model.TypeDomain;

import java.io.IOException;
import java.io.InvalidObjectException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestConverter extends TestCase
{

   public void testConvert() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(A1.class);
      A1 a = new A1("foo");

      //
      A3.delegate = new TypeConverter<A1, A2>()
      {
         @Override
         public A2 write(A1 input) throws Exception
         {
            return new A2(input.state);
         }
         @Override
         public A1 read(A2 output) throws Exception
         {
            return new A1(output.state);
         }
      };
      SerializationContext context = new SerializationContext(domain);
      a = context.clone(a);
      assertEquals("foo", a.state);
   }

   public void testConverterWriteThrowsException() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(A1.class);
      A1 a = new A1("foo");

      //
      final Exception e = new Exception();
      A3.delegate = new TypeConverter<A1, A2>()
      {
         @Override
         public A2 write(A1 input) throws Exception
         {
            throw e;
         }
         @Override
         public A1 read(A2 output) throws Exception
         {
            throw new AssertionFailedError();
         }
      };
      SerializationContext context = new SerializationContext(domain);
      try
      {
         a = context.clone(a);
         fail();
      }
      catch (InvalidObjectException ioe)
      {
         assertSame(e, ioe.getCause());
      }
   }

   public void testConverterWriteReturnsNull() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(A1.class);
      A1 a = new A1("foo");

      //
      A3.delegate = new TypeConverter<A1, A2>()
      {
         @Override
         public A2 write(A1 input) throws Exception
         {
            return null;
         }
         @Override
         public A1 read(A2 output) throws Exception
         {
            throw new AssertionFailedError();
         }
      };
      SerializationContext context = new SerializationContext(domain);
      try
      {
         a = context.clone(a);
         fail();
      }
      catch (InvalidObjectException e)
      {
      }
   }

   public void testConverterReadReturnsNull() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(A1.class);
      A1 a = new A1("foo");

      //
      A3.delegate = new TypeConverter<A1, A2>()
      {
         @Override
         public A2 write(A1 input) throws Exception
         {
            return new A2(input.state);
         }
         @Override
         public A1 read(A2 output) throws Exception
         {
            return null;
         }
      };
      SerializationContext context = new SerializationContext(domain);
      try
      {
         a = context.clone(a);
         fail();
      }
      catch (InvalidObjectException e)
      {
      }
   }

   public void testConverterReadThrowsException() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(A1.class);
      A1 a = new A1("foo");

      //
      final Exception e = new Exception();
      A3.delegate = new TypeConverter<A1, A2>()
      {
         @Override
         public A2 write(A1 input) throws Exception
         {
            return new A2(input.state);
         }
         @Override
         public A1 read(A2 output) throws Exception
         {
            throw e;
         }
      };
      SerializationContext context = new SerializationContext(domain);
      try
      {
         a = context.clone(a);
         fail();
      }
      catch (InvalidObjectException ioe)
      {
         assertSame(e, ioe.getCause());
      }
   }
}
