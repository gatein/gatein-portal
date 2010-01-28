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

package org.exoplatform.commons.serialization.model.metadata;

import org.exoplatform.commons.serialization.api.TypeConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DomainMetaData
{

   /** . */
   private final Map<String, TypeMetaData> state;

   public DomainMetaData(DomainMetaData domainMetaData)
   {
      if (domainMetaData == null)
      {
         throw new NullPointerException();
      }

      //
      this.state = new HashMap<String, TypeMetaData>(domainMetaData.state);
   }

   public DomainMetaData()
   {
      this.state = new HashMap<String, TypeMetaData>();
   }

   public TypeMetaData getTypeMetaData(Class clazz)
   {
      if (clazz == null)
      {
         throw new NullPointerException();
      }
      return state.get(clazz.getName());
   }

   public TypeMetaData getTypeMetaData(String name)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      return state.get(name);
   }

   public void addConvertedType(Class<?> clazz, Class<? extends TypeConverter<?, ?>> converterClass)
   {
      if (clazz == null)
      {
         throw new NullPointerException();
      }
      state.put(clazz.getName(), new ConvertedTypeMetaData(clazz.getName(), converterClass));
   }

   public void addClassType(Class<?>  clazz, boolean serialized)
   {
      if (clazz == null)
      {
         throw new NullPointerException();
      }
      state.put(clazz.getName(), new ClassTypeMetaData(clazz.getName(), serialized));
   }

   public void addConvertedType(String name, Class<? extends TypeConverter<?, ?>> converterClass)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      state.put(name, new ConvertedTypeMetaData(name, converterClass));
   }

   public void addClassType(String name, boolean serialized)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      state.put(name, new ClassTypeMetaData(name, serialized));
   }
}
