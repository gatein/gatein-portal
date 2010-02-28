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

import org.exoplatform.commons.serialization.model.*;
import org.exoplatform.component.test.AbstractGateInTest;

import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTypeModel extends AbstractGateInTest
{


   public void testBar()
   {
      TypeDomain domain = new TypeDomain();
      assertEquals(0, domain.getSize());
   }

   public void testFoo()
   {
      TypeDomain domain = new TypeDomain();
      assertType(String.class, domain.addTypeModel(String.class));
      assertEquals(5, domain.getSize());
      assertType(String.class, domain.getTypeModel(String.class));
      assertType(Object.class, domain.getTypeModel(Object.class));
      assertType(Integer.class, domain.getTypeModel(Integer.class));
      assertType(char[].class, domain.getTypeModel(char[].class));
      assertType(Number.class, domain.getTypeModel(Number.class));
   }

   public void testJuu()
   {
      TypeDomain domain = new TypeDomain();
      ClassTypeModel<A> aTM = (ClassTypeModel<A>)domain.addTypeModel(A.class);
      assertEquals(A.class.getName(), aTM.getName());
/*
      assertEquals(SetBuilder.
         create(domain.getTypeModel(Object.class)).
         with(domain.getTypeModel(Integer.class)).
         with(domain.getTypeModel(Number.class)).
         with(domain.getTypeModel(char[].class)).
         with(aTM).
         with(domain.getTypeModel(Boolean.class)).
         build(domain.getTypeModel(String.class))
         , domain.getTypeModels());
*/
      Map<String, FieldModel<A, ?>> fieldMap = aTM.getFieldMap();
      assertEquals(3, fieldMap.size());
      FieldModel aFM = fieldMap.get("a");
      assertEquals("a", aFM.getName());
      assertEquals(domain.getTypeModel(String.class), aFM.getType());
      FieldModel bFM = fieldMap.get("b");
      assertEquals("b", bFM.getName());
      assertEquals(domain.getTypeModel(Integer.class), bFM.getType());
      FieldModel cFM = fieldMap.get("c");
      assertEquals("c", cFM.getName());
      assertEquals(domain.getTypeModel(Boolean.class), cFM.getType());
   }

   public void testDoubleAdd()
   {
      TypeDomain domain = new TypeDomain();
      ClassTypeModel<A> aTM1 = (ClassTypeModel<A>)domain.addTypeModel(A.class);
      ClassTypeModel<A> aTM2 = (ClassTypeModel<A>)domain.addTypeModel(A.class);
      assertSame(aTM2, aTM1);
   }

   private void assertType(Class<?> javaType, TypeModel typeModel)
   {
      assertTrue(typeModel instanceof ClassTypeModel);
      ClassTypeModel serializableTypeModel = (ClassTypeModel)typeModel;
      assertEquals(javaType, serializableTypeModel.getJavaType());
   }
}
