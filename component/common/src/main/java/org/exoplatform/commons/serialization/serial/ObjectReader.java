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

package org.exoplatform.commons.serialization.serial;

import org.exoplatform.commons.serialization.SerializationContext;
import org.exoplatform.commons.serialization.api.TypeConverter;
import org.exoplatform.commons.serialization.api.factory.ObjectFactory;
import org.exoplatform.commons.serialization.model.ClassTypeModel;
import org.exoplatform.commons.serialization.model.ConvertedTypeModel;
import org.exoplatform.commons.serialization.model.FieldModel;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ObjectReader extends ObjectInputStream
{

   /** . */
   private final SerializationContext context;

   /** . */
   private final Map<Integer, Object> idToObject;

   /** . */
   private final Map<Integer, List<FutureFieldUpdate<?>>> idToResolutions;

   public ObjectReader(SerializationContext context, InputStream in) throws IOException
   {
      super(in);

      //
      enableResolveObject(true);

      //
      this.context = context;
      this.idToObject = new HashMap<Integer, Object>();
      this.idToResolutions = new HashMap<Integer, List<FutureFieldUpdate<?>>>();
   }

   private <O> O instantiate(ClassTypeModel<O> typeModel, Map<FieldModel<? super O, ?>, ?> state) throws InvalidClassException
   {
      try
      {
         ObjectFactory<? super O> factory = context.getFactory(typeModel.getJavaType());

         //
         return factory.create(typeModel.getJavaType(), state);
      }
      catch (Exception e)
      {
         InvalidClassException ice = new InvalidClassException("Cannot instantiate object from class " + typeModel.getJavaType().getName());
         ice.initCause(e);
         throw ice;
      }
   }

   protected <O> O instantiate(int id, DataContainer container, ClassTypeModel<O> typeModel) throws IOException
   {
      Map<FieldModel<? super O, ?>, Object> state = new HashMap<FieldModel<? super O, ?>, Object>();
      ClassTypeModel<? super O> currentTypeModel = typeModel;
      List<FieldUpdate<O>> sets = new ArrayList<FieldUpdate<O>>();
      while (currentTypeModel != null)
      {
         if (currentTypeModel instanceof ClassTypeModel)
         {
            for (FieldModel<? super O, ?> fieldModel : currentTypeModel.getFields())
            {
               if (!fieldModel.isTransient())
               {
                  switch (container.readInt())
                  {
                     case DataKind.NULL_VALUE:
                        state.put(fieldModel, null);
                        break;
                     case DataKind.OBJECT_REF:
                        int refId = container.readInt();
                        Object refO = idToObject.get(refId);
                        if (refO != null)
                        {
                           state.put(fieldModel, refO);
                        }
                        else
                        {
                           sets.add(new FieldUpdate<O>(refId, fieldModel));
                        }
                        break;
                     case DataKind.OBJECT:
                        Object o = container.readObject();
                        state.put(fieldModel, o);
                        break;
                  }
               }
            }
         }
         currentTypeModel = currentTypeModel.getSuperType();
      }

      //
      O instance = instantiate(typeModel, state);

      // Create future field updates
      for (FieldUpdate<O> set : sets)
      {
         List<FutureFieldUpdate<?>> resolutions = idToResolutions.get(set.ref);
         if (resolutions == null)
         {
            resolutions = new ArrayList<FutureFieldUpdate<?>>();
            idToResolutions.put(set.ref, resolutions);
         }
         resolutions.add(new FutureFieldUpdate<O>(instance, set.fieldModel));
      }

      //
      idToObject.put(id, instance);

      // Resolve future field updates
      List<FutureFieldUpdate<?>> resolutions = idToResolutions.remove(id);
      if (resolutions != null)
      {
         for (FutureFieldUpdate<?> resolution : resolutions)
         {
            resolution.fieldModel.castAndSet(resolution.target, instance);
         }
      }

      //
      return instance;
   }

   private static class FieldUpdate<O>
   {
      /** . */
      private final int ref;

      /** . */
      private final FieldModel<? super O, ?> fieldModel;

      private FieldUpdate(int ref, FieldModel<? super O, ?> fieldModel)
      {
         this.ref = ref;
         this.fieldModel = fieldModel;
      }
   }

   private static class FutureFieldUpdate<O>
   {
      /** . */
      private final O target;

      /** . */
      private final FieldModel<? super O, ?> fieldModel;

      private FutureFieldUpdate(O target, FieldModel<? super O, ?> fieldModel)
      {
         this.target = target;
         this.fieldModel = fieldModel;
      }
   }

   private Object read(DataContainer container) throws IOException
   {
      int sw = container.readInt();
      switch (sw)
      {
         case DataKind.OBJECT_REF:
         {
            int id = container.readInt();
            Object o1 = idToObject.get(id);
            if (o1 == null)
            {
               throw new AssertionError();
            }
            return o1;
         }
         case DataKind.OBJECT:
         {
            int id = container.readInt();
            Class<?> clazz = (Class)container.readObject();
            ClassTypeModel<?> typeModel = (ClassTypeModel<?>)context.getTypeDomain().getTypeModel(clazz);
            return instantiate(id, container, typeModel);
         }
         case DataKind.CONVERTED_OBJECT:
         {
            Class<?> tclazz = (Class<?>)container.readObject();
            ConvertedTypeModel<?, ?> ctm = (ConvertedTypeModel<?,?>) context.getTypeDomain().getTypeModel(tclazz);
            return convertObject(container, ctm);
         }
         case DataKind.SERIALIZED_OBJECT:
            return container.readObject();
         default:
            throw new StreamCorruptedException("Unrecognized data " + sw);
      }
   }

   private <O, T> O convertObject(DataContainer container, ConvertedTypeModel<O, T> convertedType) throws IOException
   {
      Object inner = resolveObject(container);
      T t = convertedType.getTargetType().getJavaType().cast(inner);

      TypeConverter<O, T> converter;
      try
      {
         converter = convertedType.getConverterJavaType().newInstance();
      }
      catch (Exception e)
      {
         throw new AssertionError(e);
      }

      //
      O o = null;
      try
      {
         o = converter.read(t);
      }
      catch (Exception e)
      {
         InvalidObjectException ioe = new InvalidObjectException("The object " + t + " conversion throw an exception " + converter);
         ioe.initCause(e);
         throw ioe;
      }
      if (o == null)
      {
         throw new InvalidObjectException("The object " + t + " was converted to null by converter " + converter);
      }

      //
      return o;
   }

   @Override
   protected Object resolveObject(Object obj) throws IOException
   {
      if (obj instanceof DataContainer)
      {
         return read((DataContainer)obj);
      }
      else
      {
         return obj;
      }
   }

   @Override
   protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
   {
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         String name = desc.getName();
         return Class.forName(name, false, cl);
      }
      catch (ClassNotFoundException ex)
      {
         return super.resolveClass(desc);
      }
   }
}
