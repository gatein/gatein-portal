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

package org.exoplatform.webui.replication;

import junit.framework.TestCase;
import org.exoplatform.webui.application.replication.model.TypeDomain;
import org.exoplatform.webui.application.replication.serial.ObjectReader;
import org.exoplatform.webui.application.replication.serial.ObjectWriter;

import java.io.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestSerialization extends TestCase
{

   public void testBar() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(A.class);
      A a = new A();
      a.a = "foo";
      a.b = 2;
      a.c = true;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectWriter writer = new ObjectWriter(domain, baos);
      writer.writeObject(a);
      writer.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectReader in = new ObjectReader(domain, bais);
      a = (A)in.readObject();
      assertEquals("foo", a.a);
      assertEquals(2, a.b);
      assertEquals(true, a.c);
   }

   public void testFoo() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(B.class);
      B b = new B();
      b.ref = new B(b);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectWriter writer = new ObjectWriter(domain, baos);
      writer.writeObject(b);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectReader in = new ObjectReader(domain, bais);
      b = (B)in.readObject();
      assertNotNull(b.ref);
      assertSame(b, b.ref.ref);
   }

   public void testAAA() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(E.class);
      E e = new E();
      e.left = new E();
      e.left.left = new E();
      e.left.right = new E();
      e.right = new E();
      e.right.left = e.left.left;
      e.right.right = e.left.right;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectWriter writer = new ObjectWriter(domain, baos);
      writer.writeObject(e);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectReader in = new ObjectReader(domain, bais);
      e = (E)in.readObject();
      assertSame(e.left.left, e.right.left);
      assertSame(e.left.right, e.right.right);
   }
}
