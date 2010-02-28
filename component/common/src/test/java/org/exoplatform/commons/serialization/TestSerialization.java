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

package org.exoplatform.commons.serialization;

import org.exoplatform.commons.serialization.model.TypeDomain;
import org.exoplatform.component.test.AbstractGateInTest;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestSerialization extends AbstractGateInTest
{

   public void testState() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(A.class);
      A a = new A();
      a.a = "foo";
      a.b = 2;
      a.c = true;
      SerializationContext context = new SerializationContext(domain);
      a = context.clone(a);
      assertEquals("foo", a.a);
      assertEquals(2, a.b);
      assertEquals(true, a.c);
   }

   public void testMultipleReference1() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(B.class);
      B b = new B();
      b.ref = new B(b);
      SerializationContext context = new SerializationContext(domain);
      b = context.clone(b);
      assertNotNull(b.ref);
      assertSame(b, b.ref.ref);
   }

   public void testStaticField() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(D.class);
      D d = new D();
      d.b = "bar";
      SerializationContext context = new SerializationContext(domain);
      byte[] bytes =  context.write(d);
      D.a = "foo";
      d = (D)context.read(bytes);
      assertEquals("foo", D.a);
      assertEquals("bar", d.b);
   }

   public void testMultipleReference2() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(E2.class);
      E2 e = new E2();
      e.left = new E2();
      ((E2)e.left).left = new E1();
      ((E2)e.left).right = new E1();
      e.right = new E2();
      ((E2)e.right).left = ((E2)e.left).left;
      ((E2)e.right).right = ((E2)e.left).right;
      SerializationContext context = new SerializationContext(domain);
      e = context.clone(e);
      assertSame(((E2)e.left).left, ((E2)e.right).left);
      assertSame(((E2)e.left).right, ((E2)e.right).right);
   }

   public void testListOfReplicatable() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(F.class);

      //
      F f1 = new F();
      F f2 = new F();
      f1.children.add(f2);
      f2.parent = f1;

      //
      SerializationContext context = new SerializationContext(domain);
      f1 = context.clone(f1);

      //
      assertNotNull(f1.children);
      assertNull(f1.parent);
      assertEquals(1, f1.children.size());
      assertNotNull(f1.children.get(0));
      assertSame(f1, f1.children.get(0).parent);
   }

   public void testNotSerializable() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(ByteArrayInputStream.class);

      SerializationContext context = new SerializationContext(domain);
      try
      {
         context.write(new ByteArrayInputStream(new byte[0]));
         fail();
      }
      catch (NotSerializableException e)
      {
      }
   }

   public void testTransientField() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(G.class);
      SerializationContext context = new SerializationContext(domain);
      G g = new G();
      g.a = "foo";
      g.b = new Thread();
      g = context.clone(g);
      assertEquals("foo", g.a);
      assertEquals(null, g.b);
   }
}
