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
import org.exoplatform.webui.application.replication.SerializationContext;
import org.exoplatform.webui.application.replication.model.TypeDomain;

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
      SerializationContext context = new SerializationContext(domain);
      a = context.clone(a);
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
      SerializationContext context = new SerializationContext(domain);
      b = context.clone(b);
      assertNotNull(b.ref);
      assertSame(b, b.ref.ref);
   }

   public void testStaticField() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(D.class);
      D d = new D();
      d.b = "bar";
      SerializationContext context = new SerializationContext(domain);
      byte[] bytes =  context.write(d);
      D.a = "foo";
      d = (D)context.read(bytes);
      assertEquals("foo", D.a);
      assertEquals("bar", d.b);
   }

   public void testAAA() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(E2.class);
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

/*
   public void testListOfReplicatable() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.add(F1.class);
      domain.add(F2.class);

      //
      F1 f1 = new F1();
      F2 f2 = new F2();
      f1.values.add(f2);

      //
      SerializationContext context = new SerializationContext(domain);
      f1 = context.clone(f1);

      //
      assertNotNull(f1.values);
      assertEquals(1, f1.values.size());
   }
*/
}
