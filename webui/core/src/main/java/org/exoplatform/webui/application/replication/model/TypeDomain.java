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

package org.exoplatform.webui.application.replication.model;

import org.exoplatform.webui.application.replication.annotations.ReplicatedType;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TypeDomain
{

   /** . */
   private final Map<String, TypeModel> typeModelMap;

   /** . */
   private final Map<String, TypeModel> immutableTypeModelMap;

   /** . */
   private final Set<TypeModel> typeModelSet;

   /** . */
   private final Set<TypeModel> immutableTypeModelSet;

   public TypeDomain()
   {
      HashMap<String, TypeModel> typeModelMap = new HashMap<String, TypeModel>();
      Map<String, TypeModel> immutableTypeModelMap = Collections.unmodifiableMap(typeModelMap);
      HashSet<TypeModel> typeModelSet = new HashSet<TypeModel>();
      Set<TypeModel> immutableTypeModelSet = Collections.unmodifiableSet(typeModelSet);

      //
      this.typeModelMap = typeModelMap;
      this.immutableTypeModelMap = immutableTypeModelMap;
      this.typeModelSet = typeModelSet;
      this.immutableTypeModelSet = immutableTypeModelSet;
   }

   public Map<String, TypeModel> getTypeModelMap()
   {
      return immutableTypeModelMap;
   }

   public Set<TypeModel> getTypeModels()
   {
      return immutableTypeModelSet;
   }

   public TypeModel getTypeModel(String typeName)
   {
      if (typeName == null)
      {
         throw new NullPointerException();
      }
      return typeModelMap.get(typeName);
   }

   public TypeModel getTypeModel(Class javaType)
   {
      if (javaType == null)
      {
         throw new NullPointerException();
      }
      return typeModelMap.get(javaType.getName());
   }

   public TypeModel add(Class<?> javaType)
   {
      if (javaType == null)
      {
         throw new NullPointerException();
      }
      Map<String, TypeModel> addedTypeModels = new HashMap<String, TypeModel>();
      TypeModel model = build(javaType, addedTypeModels);
      typeModelMap.putAll(addedTypeModels);
      typeModelSet.addAll(addedTypeModels.values());
      return model;
   }

   public int getSize()
   {
      return typeModelMap.size();
   }

   private TypeModel build(Class<?> javaType, Map<String, TypeModel> addedTypeModels)
   {
      ReplicatedType replicatedType = javaType.getAnnotation(ReplicatedType.class);
      if (replicatedType != null)
      {
         return buildClassTypeModel(javaType, addedTypeModels);
      }
      else if (javaType.isPrimitive())
      {
         return buildSerializable(javaType, addedTypeModels);
      }
      else if (Serializable.class.isAssignableFrom(javaType))
      {
         return buildSerializable(javaType, addedTypeModels);
      }
      return null;
   }

   private <O> ClassTypeModel<O> buildClassTypeModel(Class<O> javaType, Map<String, TypeModel> addedTypeModels)
   {
      ClassTypeModel typeModel = (ClassTypeModel) get(javaType, addedTypeModels);
      if (typeModel == null)
      {
         TypeModel superTypeModel = null;
         for (Class<?> ancestor = javaType.getSuperclass();ancestor != null;ancestor = ancestor.getSuperclass())
         {
            superTypeModel = build(ancestor, addedTypeModels);
            if (superTypeModel != null)
            {
               break;
            }
         }

         //
         TreeMap<String, FieldModel> fieldModels = new TreeMap<String, FieldModel>();

         //
         typeModel = new ClassTypeModel<O>(javaType, superTypeModel, fieldModels);

         //
         addedTypeModels.put(javaType.getName(), typeModel);

         // Now build fields
         for (Field field : javaType.getDeclaredFields())
         {
            field.setAccessible(true);
            Class<?> fieldJavaType = field.getType();
            TypeModel fieldTypeModel = build(fieldJavaType, addedTypeModels);
            if (fieldTypeModel != null)
            {
               fieldModels.put(field.getName(), new FieldModel(field, fieldTypeModel));
            }
         }
      }

      // It must be good
      return (ClassTypeModel<O>)typeModel;
   }

   private SerializableTypeModel buildSerializable(Class<?> javaType, Map<String, TypeModel> addedTypeModels)
   {
      SerializableTypeModel typeModel = (SerializableTypeModel) get(javaType, addedTypeModels);
      if (typeModel == null)
      {
         typeModel = new SerializableTypeModel((Class<Serializable>)javaType);
         addedTypeModels.put(javaType.getName(), typeModel);
      }
      return typeModel;
   }

   private TypeModel get(Class<?> javaType, Map<String, TypeModel> addedTypeModels)
   {
      TypeModel typeModel = typeModelMap.get(javaType.getName());
      if (typeModel == null)
      {
         typeModel = addedTypeModels.get(javaType.getName());
      }
      return typeModel;
   }
}
