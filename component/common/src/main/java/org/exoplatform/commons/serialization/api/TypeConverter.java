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

package org.exoplatform.commons.serialization.api;

/**
 * A type converter that performs a bidirectional conversion between an external
 * type and an internal type. The converter only assure conversion of non null values.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <E> the type parameter of the external type
 * @param <I> the type parameter of the internal type
 */
public abstract class TypeConverter<E, I>
{

   /**
    * Converts an external value to an internal value.
    *
    * @param external the external value
    * @return the the internal value
    * @throws Exception any conversion exception
    */
   public abstract I write(E external) throws Exception;

   /**
    * Converts an internal value to an external value.
    *
    * @param internal the internal value
    * @return the external value
    * @throws Exception any conversion exception
    */
   public abstract E read(I internal) throws Exception;

}
