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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ClassTypeModel<O> extends TypeModel
{

   /** . */
   private final Class<O> objectType;

   /** . */
   private final TypeModel superType;

   /** . */
   private final Map<String, FieldModel> fields;

   /** . */
   private final Map<String, FieldModel> immutableFields;

   ClassTypeModel(
      Class<O> javaType,
      TypeModel superType,
      Map<String, FieldModel> fields)
   {
      super(javaType);

      //
      this.objectType = javaType;
      this.superType = superType;
      this.fields = fields;
      this.immutableFields = Collections.unmodifiableMap(fields);
   }

   public Class<O> getObjectType()
   {
      return objectType;
   }

   public TypeModel getSuperType()
   {
      return superType;
   }

   public Collection<FieldModel> getFields()
   {
      return immutableFields.values();
   }

   public Map<String, FieldModel> getFieldMap()
   {
      return immutableFields;
   }
}
