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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConvertedTypeModel<O, T> extends TypeModel<O>
{

   /** . */
   private final TypeModel<T> targetType;

   /** . */
   private final Class<? extends TypeConverter<O, T>> converterJavaType;

   ConvertedTypeModel(
      Class<O> javaType,
      TypeModel<? super O> superType,
      TypeModel<T> targetType,
      Class<? extends TypeConverter<O, T>> converterJavaType)
   {
      super(javaType, superType);

      //
      this.targetType = targetType;
      this.converterJavaType = converterJavaType;
   }

   public TypeModel<T> getTargetType()
   {
      return targetType;
   }

   public Class<? extends TypeConverter<O, T>> getConverterJavaType()
   {
      return converterJavaType;
   }
}
