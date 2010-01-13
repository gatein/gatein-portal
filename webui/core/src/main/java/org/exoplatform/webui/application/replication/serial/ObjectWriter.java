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

import org.exoplatform.webui.application.replication.model.ClassTypeModel;
import org.exoplatform.webui.application.replication.model.FieldModel;
import org.exoplatform.webui.application.replication.model.TypeDomain;
import org.exoplatform.webui.application.replication.model.TypeModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ObjectWriter
{

   /** . */
   private final TypeDomain domain;

   /** . */
   private final IdentityHashMap<Object, Integer> objectToId;

   /** . */
   private final Map<Integer, Object> idToObject;

   public ObjectWriter(TypeDomain domain)
   {
      this.domain = domain;
      this.objectToId = new IdentityHashMap<Object, Integer>();
      this.idToObject = new HashMap<Integer, Object>();
   }

   private int register(Object o)
   {
      int nextId = objectToId.size();
      objectToId.put(o, nextId);
      idToObject.put(nextId, o);
      return nextId;
   }

   public void write(Object o, DataOutput output)
   {
      if (o == null)
      {
         output.writeInt(DataKind.NULL_VALUE);
      }
      else
      {
         Integer id = objectToId.get(o);
         if (id != null)
         {
            output.writeInt(DataKind.OBJECT_REF);
            output.writeInt(id);
         }
         else
         {
            TypeModel typeModel = domain.getTypeModel(o.getClass());
            if (typeModel instanceof ClassTypeModel)
            {
               ClassTypeModel classTypeModel = (ClassTypeModel)typeModel;

               //
               output.writeInt(DataKind.OBJECT);
               output.writeInt(register(o));
               output.writeSerializable(o.getClass());

               //
               ClassTypeModel currentTypeModel = classTypeModel;
               while (true)
               {
                  for (FieldModel fieldModel : (currentTypeModel).getFields())
                  {
                     Object fieldValue = fieldModel.getValue(o);
                     write(fieldValue, output);
                  }
                  TypeModel currentSuperTypeModel = currentTypeModel.getSuperType();
                  if (currentSuperTypeModel == null)
                  {
                     break;
                  }
                  if (currentSuperTypeModel instanceof ClassTypeModel)
                  {
                     currentTypeModel = (ClassTypeModel)currentSuperTypeModel;
                  }
                  else
                  {
                     output.writeInt(DataKind.SERIALIZABLE_OBJECT);
                     output.writeSerializable((Serializable)o);
                     break;
                  }
               }
            }
            else  if (o instanceof Serializable)
            {
               output.writeInt(DataKind.SERIALIZABLE_OBJECT);
               output.writeSerializable((Serializable)o);
            }
            else
            {
               throw new IllegalArgumentException();
            }
         }
      }
   }
}
