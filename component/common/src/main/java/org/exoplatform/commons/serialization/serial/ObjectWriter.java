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
import org.exoplatform.commons.serialization.model.*;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.io.*;
import java.util.IdentityHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ObjectWriter extends ObjectOutputStream
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(ObjectWriter.class);

   /** . */
   private final SerializationContext context;

   /** . */
   private final IdentityHashMap<Object, Integer> objectToId;

   public ObjectWriter(SerializationContext context, OutputStream out) throws IOException
   {
      super(out);

      //
      enableReplaceObject(true);

      //
      this.context = context;
      this.objectToId = new IdentityHashMap<Object, Integer>();
   }

   private int register(Object o)
   {
      int nextId = objectToId.size();
      objectToId.put(o, nextId);
      return nextId;
   }

   private void write(Object obj, DataContainer output) throws IOException
   {
      Class objClass = obj.getClass();
      TypeModel typeModel = context.getTypeDomain().getTypeModel(objClass);

      //
      if (typeModel == null)
      {
         throw new NotSerializableException("Object " + obj + " does not have its type described");
      }

      //
      if (typeModel instanceof ClassTypeModel)
      {
         // This is obviously unchecked because of the fact that Object.getClass() returns Class<?>
         // need to find a work around for that
         write((ClassTypeModel)typeModel, obj, output);
      }
      else
      {
         write((ConvertedTypeModel)typeModel, obj, output);
      }
   }

   private <O, T> void write(ConvertedTypeModel<O, T> typeModel, O obj, DataContainer output) throws IOException
   {
      Class<? extends TypeConverter<O, T>> converterClass = typeModel.getConverterJavaType();

      //
      TypeConverter<O, T> converter;
      try
      {
         converter = converterClass.newInstance();
      }
      catch (Exception e)
      {
         throw new AssertionError(e);
      }

      //
      T target;
      try
      {
         target = converter.write(obj);
      }
      catch (Exception e)
      {
         InvalidObjectException ioe = new InvalidObjectException("The object " + obj + " conversion threw an exception ");
         ioe.initCause(e);
         throw ioe;
      }
      if (target == null)
      {
         throw new InvalidObjectException("The object " + obj + " was converted to null by converter " + converter);
      }

      //
      output.writeInt(DataKind.CONVERTED_OBJECT);
      output.writeObject(typeModel.getJavaType());
      write(target, output);
   }

   private <O> void write(ClassTypeModel<O> typeModel, O obj, DataContainer output) throws IOException
   {
      if (typeModel.getSerializationMode() == SerializationMode.SERIALIZED)
      {
         //
         output.writeInt(DataKind.OBJECT);
         output.writeInt(register(obj));
         output.writeObject(typeModel.getJavaType());

         //
         SerializationStatus status = SerializationStatus.NONE;
         for (ClassTypeModel<? super O> currentTypeModel = typeModel;currentTypeModel != null;currentTypeModel = currentTypeModel.getSuperType())
         {
            if (currentTypeModel instanceof ClassTypeModel<?>)
            {
               for (FieldModel<?, ?> fieldModel : currentTypeModel.getFields())
               {
                  if (!fieldModel.isTransient())
                  {
//                     System.out.println("About to serialize field " + fieldModel + " of type " + currentTypeModel);
                     Object fieldValue = fieldModel.get(obj);
                     if (fieldValue == null)
                     {
                        output.writeObject(DataKind.NULL_VALUE);
                     }
                     else
                     {
                        Integer fieldValueId = objectToId.get(fieldValue);
                        if (fieldValueId != null)
                        {
                           output.writeObject(DataKind.OBJECT_REF);
                           output.writeInt(fieldValueId);
                        }
                        else
                        {
                           output.writeObject(DataKind.OBJECT);
                           output.writeObject(fieldValue);
                        }
                     }
                  }
               }
               switch (status)
               {
                  case NONE:
                     status = SerializationStatus.FULL;
                     break;
               }
            }
            else
            {
               if (!currentTypeModel.getFields().isEmpty())
               {
                  switch (status)
                  {
                     case FULL:
                        status = SerializationStatus.PARTIAL;
                        break;
                  }
               }
            }
         }

         //
         switch (status)
         {
            case FULL:
               break;
            case PARTIAL:
               log.debug("Partial serialization of object " + obj);
               break;
            case NONE:
               throw new NotSerializableException("Type " + typeModel + " is not serializable");
         }
      }
      else if (typeModel.getSerializationMode() == SerializationMode.SERIALIZABLE)
      {
         output.writeInt(DataKind.SERIALIZED_OBJECT);
         output.writeObject(obj);
      }
      else
      {
         throw new NotSerializableException("Type " + typeModel + " is not serializable");
      }
   }

   @Override
   protected Object replaceObject(final Object obj) throws IOException
   {
      if (obj == null)
      {
         return null;
      }
      if (obj instanceof Serializable)
      {
         return obj;
      }

      //
      DataContainer output = new DataContainer();

      //
      Integer id = objectToId.get(obj);
      if (id != null)
      {
         output = new DataContainer();
         output.writeInt(DataKind.OBJECT_REF);
         output.writeObject(id);
      }
      else
      {
         write(obj, output);
      }

      //
      return output;
   }
}