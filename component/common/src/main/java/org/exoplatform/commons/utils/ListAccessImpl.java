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

package org.exoplatform.commons.utils;

import org.gatein.common.util.ParameterValidation;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ListAccessImpl<E> implements ListAccess<E>, Serializable
{

   /** . */
   private final List<E> list;

   /** . */
   private final Class<E> elementType;

   public ListAccessImpl(Class<E> elementType, List<E> list)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(elementType, "element type");
      ParameterValidation.throwIllegalArgExceptionIfNull(list, "elements");
      this.elementType = elementType;
      this.list = list;
   }

   public E[] load(int index, int length) throws Exception, IllegalArgumentException
   {
      E[] array = (E[])Array.newInstance(elementType, length);
      list.subList(index, index + length).toArray(array);
      return array;
   }

   public int getSize() throws Exception
   {
      return list.size();
   }
}
