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

package org.exoplatform.commons.serialization.metadata;

import org.exoplatform.commons.serialization.model.ClassTypeModel;
import org.exoplatform.commons.serialization.model.ConvertedTypeModel;
import org.exoplatform.commons.serialization.model.SerializationMode;
import org.exoplatform.commons.serialization.model.TypeDomain;
import org.exoplatform.commons.serialization.model.metadata.DomainMetaData;
import org.exoplatform.component.test.AbstractGateInTest;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestMetaData extends AbstractGateInTest
{

   public void testSerializedObjectClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(Object.class, true);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<Object> objectTM = (ClassTypeModel<Object>) typeDomain.addTypeModel(Object.class);
      assertEquals(SerializationMode.SERIALIZED, objectTM.getSerializationMode());
   }

   public void testObjectClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(Object.class, false);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<Object> objectTM = (ClassTypeModel<Object>)typeDomain.addTypeModel(Object.class);
      assertEquals(SerializationMode.NONE, objectTM.getSerializationMode());
   }

   public void testStringSerializedClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(String.class, true);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<String> stringTM = (ClassTypeModel<String>)typeDomain.addTypeModel(String.class);
      assertEquals(SerializationMode.SERIALIZED, stringTM.getSerializationMode());
   }

   public void testStringClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(String.class, false);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<String> stringTM = (ClassTypeModel<String>)typeDomain.addTypeModel(String.class);
      assertEquals(SerializationMode.SERIALIZABLE, stringTM.getSerializationMode());
   }

   public void testThreadConvertedType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addConvertedType(Thread.class, ThreadTypeConverter.class);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ConvertedTypeModel<Thread, String> objectTM = (ConvertedTypeModel<Thread, String>)typeDomain.addTypeModel(Thread.class);
      assertEquals(ThreadTypeConverter.class, objectTM.getConverterJavaType());
   }

   public void testArrayListConvertedType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addConvertedType(ArrayList.class, ArrayListTypeConverter.class);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ConvertedTypeModel<ArrayList, LinkedList> arrayListTM = (ConvertedTypeModel<ArrayList, LinkedList>)typeDomain.addTypeModel(ArrayList.class);
      assertEquals(ArrayListTypeConverter.class, arrayListTM.getConverterJavaType());
   }
}
