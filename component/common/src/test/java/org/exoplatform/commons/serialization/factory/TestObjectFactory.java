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

package org.exoplatform.commons.serialization.factory;

import org.exoplatform.commons.serialization.SerializationContext;
import org.exoplatform.commons.serialization.model.TypeDomain;
import org.exoplatform.component.test.AbstractGateInTest;

import java.io.InvalidClassException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestObjectFactory extends AbstractGateInTest
{

   public void testCustomFactory() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(A2.class);
      SerializationContext context = new SerializationContext(domain);
      context.addFactory(new A1());
      A2 a2 = new A2();
      assertSame(A1.instance, context.clone(a2));
   }

   public void testFactoryThrowsException() throws Exception
   {
      TypeDomain domain = new TypeDomain();
      domain.addTypeModel(B.class);
      SerializationContext context = new SerializationContext(domain);
      B b = new B(false);
      try
      {
         context.clone(b);
         fail();
      }
      catch (InvalidClassException e)
      {
      }
   }
}
