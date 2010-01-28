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

package org.exoplatform.commons.serialization.model;

import org.exoplatform.commons.serialization.api.TypeConverter;
import org.exoplatform.commons.serialization.api.annotations.Converted;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.serialization.model.metadata.ClassTypeMetaData;
import org.exoplatform.commons.serialization.model.metadata.ConvertedTypeMetaData;
import org.exoplatform.commons.serialization.model.metadata.DomainMetaData;
import org.exoplatform.commons.serialization.model.metadata.TypeMetaData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class TypeModelBuilder
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(TypeModelBuilder.class);

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

   // Build the missing types required to have knowledge about the
   // provided java type
   private final Map<String, TypeModel<?>> addedTypeModels = new HashMap<String, TypeModel<?>>();

   /** . */
   private final DomainMetaData metaData;

   /** . */
   private final Map<String, TypeModel<?>> existingTypeModels;

   public TypeModelBuilder(DomainMetaData metaData,  Map<String, TypeModel<?>> existingTypeModels)
   {
      this.metaData = metaData;
      this.existingTypeModels = existingTypeModels;
   }

   Map<String, TypeModel<?>> getAddedTypeModels()
   {
      return addedTypeModels;
   }

   <O> TypeModel<O> build(Class<O> javaType)
   {
      if (javaType.isPrimitive())
      {
         throw new IllegalArgumentException("No primitive type accepted");
      }

      //
      TypeModel<O> typeModel = get(javaType);

      //
      if (typeModel != null)
      {
         log.debug("Found type model " + typeModel + "  for java type " + javaType);
         return typeModel;
      }


      //
      log.debug("About to build type model for java type " + javaType);
      TypeMetaData typeMetaData = metaData.getTypeMetaData(javaType);

      //
      if (typeMetaData == null)
      {
         log.debug("No meta data found for java type " + javaType + " about to build it");
         boolean serialized = javaType.getAnnotation(Serialized.class) != null;
         Converted converted = javaType.getAnnotation(Converted.class);
         if (serialized)
         {
            if (converted != null)
            {
               throw new TypeException();
            }
            typeMetaData = new ClassTypeMetaData(javaType.getName(), true);
         }
         else if (converted != null)
         {
            typeMetaData = new ConvertedTypeMetaData(javaType.getName(), converted.value());
         }
         else
         {
            typeMetaData = new ClassTypeMetaData(javaType.getName(), false);
         }
      }
      log.debug("Built type meta data " + typeMetaData + " for java type " + javaType);

      //
      return build(javaType, typeMetaData);
   }

   private <O> TypeModel<O> build(Class<O> javaType, TypeMetaData typeMetaData)
   {
      if (typeMetaData instanceof ClassTypeMetaData)
      {
         return buildClassType(javaType, (ClassTypeMetaData)typeMetaData);
      }
      else
      {
         return buildConvertedType(javaType, (ConvertedTypeMetaData)typeMetaData);
      }
   }

   private <O> ConvertedTypeModel<O, ?> buildConvertedType(Class<O> javaType, ConvertedTypeMetaData typeMetaData)
   {
      log.debug("About to build type model from type type metadata " + typeMetaData);

      //
      Class<? extends TypeConverter<?, ?>> converterClass = typeMetaData.getConverterClass();
      ParameterizedType converterParameterizedType = (ParameterizedType)converterClass.getGenericSuperclass();
      if (!converterParameterizedType.getActualTypeArguments()[0].equals(javaType))
      {
         throw new TypeException("The declared type parameter in the converter " + converterClass.getName() +
            " does not match the type it is related to " + javaType.getName());
      }

      //
      Class<? extends TypeConverter<O, ?>> converterJavaType = (Class<TypeConverter<O, ?>>)converterClass;

      //
      return buildConvertedType(javaType, converterJavaType);
   }

   private <O, T> ConvertedTypeModel<O, T> buildConvertedType(
      Class<O> javaType,
      Class<? extends TypeConverter<O, ? /* This is a bit funky and nasty, need to investigate*/ >> converterJavaType)
   {
      Class<T> outputClass = (Class<T>)((ParameterizedType)converterJavaType.getGenericSuperclass()).getActualTypeArguments()[1];

      //
      ClassTypeModel<T> targetType = (ClassTypeModel<T>)build(outputClass);

      //
      TypeModel<? super O> superType = null;
      Class<? super O> superJavaType = javaType.getSuperclass();
      if (superJavaType != null)
      {
         superType = build(superJavaType);
      }

      //
      ConvertedTypeModel<O, T> typeModel = new ConvertedTypeModel<O, T>(
         javaType,
         superType,
         targetType,
         (Class<TypeConverter<O, T>>) converterJavaType);

      //
      addedTypeModels.put(typeModel.getName(), typeModel);

      //
      return typeModel;
   }

   private <O> ClassTypeModel<O> buildClassType(
      Class<O> javaType,
      ClassTypeMetaData typeMetaData)
   {
      ClassTypeModel<? super O> superTypeModel = null;
      if (javaType.getSuperclass() != null)
      {
         TypeModel<? super O> builtType = build(javaType.getSuperclass());
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
      SerializationMode serializationMode;
      if (typeMetaData.isSerialized())
      {
         serializationMode = SerializationMode.SERIALIZED;
      }
      else if (Serializable.class.isAssignableFrom(javaType))
      {
         serializationMode = SerializationMode.SERIALIZABLE;
      }
      else
      {
         serializationMode = SerializationMode.NONE;
      }

      //
      ClassTypeModel<O> typeModel = new ClassTypeModel<O>(javaType, superTypeModel, fieldModels, serializationMode);

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

            TypeModel<?> fieldTypeModel = build(fieldJavaType);
            if (fieldTypeModel != null)
            {
               fieldModels.put(field.getName(), createField(typeModel, field, fieldTypeModel));
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

   private <O> TypeModel<O> get(Class<O> javaType)
   {
      TypeModel<?> typeModel = existingTypeModels.get(javaType.getName());
      if (typeModel == null)
      {
         typeModel = addedTypeModels.get(javaType.getName());
      }

      // Cast OK
      return (TypeModel<O>)typeModel;
   }
}
