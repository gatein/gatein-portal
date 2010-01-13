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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ObjectReader extends ObjectInputStream
{

   /** . */
   private final TypeDomain domain;

   /** . */
   private final Map<Integer, Object> idToObject;

   /** . */
   private final Map<Integer, List<Resolution>> idToResolutions;

   public ObjectReader(TypeDomain domain, InputStream in) throws IOException
   {
      super(in);

      //
      enableResolveObject(true);

      //
      this.domain = domain;
      this.idToObject = new HashMap<Integer, Object>();
      this.idToResolutions = new HashMap<Integer, List<Resolution>>();
   }

   @Override
   protected Object resolveObject(Object obj) throws IOException
   {
      if (obj instanceof DataContainer)
      {
         DataContainer container = (DataContainer) obj;

         int id;
         switch (container.readInt())
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
               Class clazz = (Class) container.readObject();
               Object instance;
               try
               {
                  instance = clazz.newInstance();
               }
               catch (Exception e)
               {
                  throw new AssertionError(e);
               }
               ClassTypeModel typeModel = (ClassTypeModel) domain.getTypeModel(clazz);
               idToObject.put(id, instance);

               //
               ClassTypeModel currentTypeModel = typeModel;
               while (true)
               {
                  for (FieldModel fieldModel : (currentTypeModel).getFields())
                  {
                     switch (container.readInt())
                     {
                        case DataKind.NULL_VALUE:
                           fieldModel.setValue(instance, null);
                           break;
                        case DataKind.OBJECT_REF:
                           int refId = container.readInt();
                           Object refO = idToObject.get(refId);
                           if (refO != null)
                           {
                              fieldModel.setValue(instance, refO);
                           }
                           else
                           {
                              List<Resolution> resolutions = idToResolutions.get(refId);
                              if (resolutions == null)
                              {
                                 resolutions = new ArrayList<Resolution>();
                                 idToResolutions.put(refId, resolutions);
                              }
                              resolutions.add(new Resolution(instance, fieldModel));
                           }
                           break;
                        case DataKind.OBJECT:
                           Object o = container.readObject();
                           fieldModel.setValue(instance, o);
                           break;

                     }
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
                     throw new UnsupportedOperationException();
                  }
               }

               //
               List<Resolution> resolutions = idToResolutions.remove(id);
               if (resolutions != null)
               {
                  for (Resolution resolution : resolutions)
                  {
                     resolution.fieldModel.setValue(resolution.target, instance);
                  }
               }

               //
               return instance;
            default:
               throw new AssertionError();
         }
      }
      else
      {
         return obj;
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
