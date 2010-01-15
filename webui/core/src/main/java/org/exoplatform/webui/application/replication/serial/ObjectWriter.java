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
import org.exoplatform.webui.application.replication.model.ReplicatableTypeModel;
import org.exoplatform.webui.application.replication.model.FieldModel;
import org.exoplatform.webui.application.replication.model.TypeModel;

import java.io.*;
import java.util.IdentityHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ObjectWriter extends ObjectOutputStream
{

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

   @Override
   protected Object replaceObject(Object obj) throws IOException
   {
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
         output.writeInt(DataKind.OBJECT_REF);
         output.writeObject(id);
      }
      else
      {
         ReplicatableTypeModel<?> typeModel = (ReplicatableTypeModel<?>)context.getTypeDomain().getTypeModel(obj.getClass());

         //
         if (typeModel == null)
         {
            throw new NotSerializableException("Object " + obj + " is not serializable");
         }

         //
         output.writeInt(DataKind.OBJECT);
         output.writeInt(register(obj));
         output.writeObject(obj.getClass());

         //
         ReplicatableTypeModel<?> currentTypeModel = typeModel;
         while (true)
         {
            for (FieldModel fieldModel : (currentTypeModel).getFields())
            {
               Object fieldValue = fieldModel.getValue(obj);
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

            //
            TypeModel currentSuperTypeModel = currentTypeModel.getSuperType();
            if (currentSuperTypeModel == null)
            {
               break;
            }

            //
            if (currentSuperTypeModel instanceof ReplicatableTypeModel)
            {
               currentTypeModel = (ReplicatableTypeModel)currentSuperTypeModel;
            }
            else
            {
               throw new UnsupportedOperationException();
            }
         }
      }

      //
      return output;
   }
}