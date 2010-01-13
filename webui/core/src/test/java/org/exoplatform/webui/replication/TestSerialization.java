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
import org.exoplatform.webui.application.replication.serial.DataKind;
import org.exoplatform.webui.application.replication.serial.ObjectWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestSerialization extends TestCase
{

   public void testBar()
   {
      TypeDomain domain = new TypeDomain();
      domain.add(A.class);
      A a = new A();
      ObjectWriter writer = new ObjectWriter(domain);
      DataOutputImpl output = new DataOutputImpl();
      writer.write(a, output);
      output.assertInt(DataKind.OBJECT);
      output.assertInt(0);
      output.assertSerializable(A.class);
      output.assertInt(DataKind.SERIALIZABLE_OBJECT);
      output.assertSerializable("a");
      output.assertInt(DataKind.SERIALIZABLE_OBJECT);
      output.assertSerializable(2);
      output.assertInt(DataKind.SERIALIZABLE_OBJECT);
      output.assertSerializable(true);
      output.assertEmpty();

      //
      writer.write(a, output);
      output.assertInt(DataKind.OBJECT_REF);
      output.assertInt(0);
      output.assertEmpty();
   }

   public void testFoo()
   {
      TypeDomain domain = new TypeDomain();
      domain.add(B.class);

      B b = new B();
      B b2 = new B(b);
      b.ref = b2;

      ObjectWriter writer = new ObjectWriter(domain);
      DataOutputImpl output = new DataOutputImpl();
      writer.write(b, output);

      output.assertInt(DataKind.OBJECT);
      output.assertInt(0);
      output.assertSerializable(B.class);

      output.assertInt(DataKind.OBJECT);
      output.assertInt(1);
      output.assertSerializable(B.class);

      output.assertInt(DataKind.OBJECT_REF);
      output.assertInt(0);
      output.assertEmpty();
   }

   public void testJuu()
   {
      TypeDomain domain = new TypeDomain();
      domain.add(C2.class);

      C2 c = new C2();

      ObjectWriter writer = new ObjectWriter(domain);
      DataOutputImpl output = new DataOutputImpl();
      writer.write(c, output);

      output.assertInt(DataKind.OBJECT);
      output.assertInt(0);
      output.assertSerializable(C2.class);

      output.assertInt(DataKind.SERIALIZABLE_OBJECT);
      output.assertSerializable("a2");
      output.assertInt(DataKind.SERIALIZABLE_OBJECT);
      output.assertSerializable("a1");
      output.assertEmpty();
   }

   public void testDaa()
   {
      TypeDomain domain = new TypeDomain();
      domain.add(D.class);

      D c = new D();

      ObjectWriter writer = new ObjectWriter(domain);
      DataOutputImpl output = new DataOutputImpl();
      writer.write(c, output);

      output.assertInt(DataKind.OBJECT);
      output.assertInt(0);
      output.assertSerializable(D.class);

      output.assertInt(DataKind.SERIALIZABLE_OBJECT);
      output.assertSerializable("a");
      output.assertInt(DataKind.SERIALIZABLE_OBJECT);
      output.assertSerializable(c);
      output.assertEmpty();
   }
}
