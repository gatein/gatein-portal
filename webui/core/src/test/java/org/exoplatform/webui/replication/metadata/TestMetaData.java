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

package org.exoplatform.webui.replication.metadata;

import junit.framework.TestCase;
import org.exoplatform.webui.application.replication.model.ClassTypeModel;
import org.exoplatform.webui.application.replication.model.ConvertedTypeModel;
import org.exoplatform.webui.application.replication.model.SerializationMode;
import org.exoplatform.webui.application.replication.model.TypeDomain;
import org.exoplatform.webui.application.replication.model.metadata.DomainMetaData;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestMetaData extends TestCase
{

   public void testSerializedObjectClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(Object.class, true);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<Object> objectTM = (ClassTypeModel<Object>) typeDomain.add(Object.class);
      assertEquals(SerializationMode.SERIALIZED, objectTM.getSerializationMode());
   }

   public void testObjectClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(Object.class, false);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<Object> objectTM = (ClassTypeModel<Object>)typeDomain.add(Object.class);
      assertEquals(SerializationMode.NONE, objectTM.getSerializationMode());
   }

   public void testStringSerializedClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(String.class, true);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<String> stringTM = (ClassTypeModel<String>)typeDomain.add(String.class);
      assertEquals(SerializationMode.SERIALIZED, stringTM.getSerializationMode());
   }

   public void testStringClassType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addClassType(String.class, false);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ClassTypeModel<String> stringTM = (ClassTypeModel<String>)typeDomain.add(String.class);
      assertEquals(SerializationMode.SERIALIZABLE, stringTM.getSerializationMode());
   }

   public void testThreadConvertedType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addConvertedType(Thread.class, ThreadTypeConverter.class);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ConvertedTypeModel<Thread, String> objectTM = (ConvertedTypeModel<Thread, String>)typeDomain.add(Thread.class);
      assertEquals(ThreadTypeConverter.class, objectTM.getConverterJavaType());
   }

   public void testArrayListConvertedType() throws Exception
   {
      DomainMetaData domainMD = new DomainMetaData();
      domainMD.addConvertedType(ArrayList.class, ArrayListTypeConverter.class);
      TypeDomain typeDomain = new TypeDomain(domainMD);
      ConvertedTypeModel<ArrayList, LinkedList> arrayListTM = (ConvertedTypeModel<ArrayList, LinkedList>)typeDomain.add(ArrayList.class);
      assertEquals(ArrayListTypeConverter.class, arrayListTM.getConverterJavaType());
   }
}
