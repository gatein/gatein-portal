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

import org.exoplatform.webui.application.replication.api.annotations.Serialized;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class TypeDomain
{

   /** . */
   private static final Map<Class<?>, Class<?>> primitiveToWrapperMap = new HashMap<Class<?>, Class<?>>();

   static
   {
      primitiveToWrapperMap.put(byte.class, Byte.class);
      primitiveToWrapperMap.put(short.class, Short.class);
      primitiveToWrapperMap.put(int.class, Integer.class);
      primitiveToWrapperMap.put(long.class, Long.class);
      primitiveToWrapperMap.put(float.class, Float.class);
      primitiveToWrapperMap.put(double.class, Double.class);
      primitiveToWrapperMap.put(boolean.class, Boolean.class);
      primitiveToWrapperMap.put(char.class, Character.class);
   }

   /** . */
   private final Map<String, TypeModel> typeModelMap;

   /** . */
   private final Map<String, TypeModel> immutableTypeModelMap;

   /** . */
   private final Collection<TypeModel> immutableTypeModelSet;

   /** . */
   private final boolean buildIfAbsent;

   public TypeDomain()
   {
      this(false);
   }

   public TypeDomain(boolean buildIfAbsent)
   {
      ConcurrentHashMap<String, TypeModel> typeModelMap = new ConcurrentHashMap<String, TypeModel>();
      Map<String, TypeModel> immutableTypeModelMap = Collections.unmodifiableMap(typeModelMap);
      Collection<TypeModel> immutableTypeModelSet = Collections.unmodifiableCollection(typeModelMap.values());

      //
      this.typeModelMap = typeModelMap;
      this.immutableTypeModelMap = immutableTypeModelMap;
      this.immutableTypeModelSet = immutableTypeModelSet;
      this.buildIfAbsent = buildIfAbsent;
   }

   public Map<String, TypeModel> getTypeModelMap()
   {
      return immutableTypeModelMap;
   }

   public boolean getBuildIfAbsent()
   {
      return buildIfAbsent;
   }

   public Collection<TypeModel> getTypeModels()
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

   public TypeModel getTypeModel(Class<?> javaType)
   {
      if (javaType == null)
      {
         throw new NullPointerException();
      }

      //
      TypeModel typeModel = typeModelMap.get(javaType.getName());

      //
      if (typeModel == null && buildIfAbsent)
      {
         typeModel = add(javaType);
      }

      //
      return typeModel;
   }

   // For now that operation is synchronized
   public synchronized <O> TypeModel<O> add(Class<O> javaType)
   {
      if (javaType == null)
      {
         throw new NullPointerException();
      }

      // Build the missing types required to have knowledge about the
      // provided java type
      Map<String, TypeModel<?>> addedTypeModels = new HashMap<String, TypeModel<?>>();
      TypeModel<O> model = build(javaType, addedTypeModels);

      // Perform merge
      typeModelMap.putAll(addedTypeModels);

      //
      System.out.println("Added types " + addedTypeModels.values() + " to replication domain");

      //
      return model;
   }

   public int getSize()
   {
      return typeModelMap.size();
   }

   private <O> TypeModel<O> build(Class<O> javaType, Map<String, TypeModel<?>> addedTypeModels)
   {
      if (javaType.isPrimitive())
      {
         throw new IllegalArgumentException("No primitive type accepted");
      }

      // Cast OK
      TypeModel<O> typeModel = (TypeModel<O>)get(javaType, addedTypeModels);

      //
      if (typeModel == null)
      {
         boolean replicated = javaType.getAnnotation(Serialized.class) != null;

         //
         ClassTypeModel<? super O> superTypeModel = null;
         if (javaType.getSuperclass() != null)
         {
            TypeModel<? super O> builtType = build(javaType.getSuperclass(), addedTypeModels);
            if (builtType instanceof ClassTypeModel)
            {
               superTypeModel = (ClassTypeModel<? super O>)builtType;
            }
            else
            {
               throw new TypeException();
            }
         }

         //
         TreeMap<String, FieldModel<O, ?>> fieldModels = new TreeMap<String, FieldModel<O, ?>>();

         //
         typeModel = new ClassTypeModel<O>(javaType, superTypeModel, fieldModels, replicated);

         //
         addedTypeModels.put(javaType.getName(), typeModel);

         // Now build fields
         for (Field field : javaType.getDeclaredFields())
         {
            if (!Modifier.isStatic(field.getModifiers()))
            {
               field.setAccessible(true);
               Class<?> fieldJavaType = field.getType();

               // Replace if a primitive
               if (fieldJavaType.isPrimitive())
               {
                  fieldJavaType = primitiveToWrapperMap.get(fieldJavaType);
               }

               TypeModel<?> fieldTypeModel = build(fieldJavaType, addedTypeModels);
               if (fieldTypeModel != null)
               {
                  fieldModels.put(field.getName(), createField(typeModel, field, fieldTypeModel));
               }
            }
         }
      }

      //
      return typeModel;
   }

   private <O, V> FieldModel<O, V> createField(TypeModel<O> owner, Field field, TypeModel<V> fieldTypeModel)
   {
      return new FieldModel<O, V>(owner, field, fieldTypeModel);
   }

   private <O> TypeModel<O> get(Class<O> javaType, Map<String, TypeModel<?>> addedTypeModels)
   {
      TypeModel<?> typeModel = typeModelMap.get(javaType.getName());
      if (typeModel == null)
      {
         typeModel = addedTypeModels.get(javaType.getName());
      }
      // Cast OK
      return (TypeModel<O>)typeModel;
   }
}
