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

package org.exoplatform.webui.application.replication.serial;

import org.exoplatform.webui.application.replication.SerializationContext;
import org.exoplatform.webui.application.replication.factory.ObjectFactory;
import org.exoplatform.webui.application.replication.model.ReplicatableTypeModel;
import org.exoplatform.webui.application.replication.model.FieldModel;
import org.exoplatform.webui.application.replication.model.TypeModel;

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
   private final Map<Integer, List<Resolution>> idToResolutions;

   public ObjectReader(SerializationContext context, InputStream in) throws IOException
   {
      super(in);

      //
      enableResolveObject(true);

      //
      this.context = context;
      this.idToObject = new HashMap<Integer, Object>();
      this.idToResolutions = new HashMap<Integer, List<Resolution>>();
   }

   private <O> O instantiate(ReplicatableTypeModel<O> typeModel, Map<FieldModel<?, ?>, ?> state) throws InvalidClassException
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

   @Override
   protected Object resolveObject(Object obj) throws IOException
   {
      if (obj instanceof DataContainer)
      {
         DataContainer container = (DataContainer) obj;

         int id;
         int sw = container.readInt();
         switch (sw)
         {
            case DataKind.OBJECT_REF:
               id = container.readInt();
               Object o1 = idToObject.get(id);
               if (o1 == null)
               {
                  throw new AssertionError();
               }
               return o1;
            case DataKind.OBJECT:
               id = container.readInt();
               Class<?> clazz = (Class) container.readObject();

               ReplicatableTypeModel<?> typeModel = (ReplicatableTypeModel)context.getTypeDomain().getTypeModel(clazz);

               //
               Map<FieldModel<?, ?>, Object> state = new HashMap<FieldModel<?, ?>, Object>();
               TypeModel<?> currentTypeModel = typeModel;
               List<Bilto> biltos = new ArrayList<Bilto>();
               while (currentTypeModel != null)
               {
                  if (currentTypeModel instanceof ReplicatableTypeModel)
                  {
                     for (FieldModel<?, ?> fieldModel : currentTypeModel.getFields())
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
                                    biltos.add(new Bilto(refId, fieldModel));
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
               Object instance = instantiate(typeModel, state);

               //
               for (Bilto bilto : biltos)
               {
                  List<Resolution> resolutions = idToResolutions.get(bilto.ref);
                  if (resolutions == null)
                  {
                     resolutions = new ArrayList<Resolution>();
                     idToResolutions.put(bilto.ref, resolutions);
                  }
                  resolutions.add(new Resolution(instance, bilto.fieldModel));
               }

               //
               idToObject.put(id, instance);

               //
               List<Resolution> resolutions = idToResolutions.remove(id);
               if (resolutions != null)
               {
                  for (Resolution resolution : resolutions)
                  {
                     resolution.fieldModel.set(resolution.target, instance);
                  }
               }

               //
               return instance;
            default:
               throw new StreamCorruptedException("Unrecognized data " + sw);
         }
      }
      else
      {
         return obj;
      }
   }

   private static class Bilto
   {
      /** . */
      private final int ref;

      /** . */
      private final FieldModel fieldModel;

      private Bilto(int ref, FieldModel fieldModel)
      {
         this.ref = ref;
         this.fieldModel = fieldModel;
      }
   }

   private static class Resolution
   {
      /** . */
      private final Object target;

      /** . */
      private final FieldModel fieldModel;

      private Resolution(Object target, FieldModel fieldModel)
      {
         this.target = target;
         this.fieldModel = fieldModel;
      }
   }

}
