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

package org.exoplatform.commons.serialization.converter;

import junit.framework.AssertionFailedError;
import org.exoplatform.commons.serialization.api.TypeConverter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class A3 extends TypeConverter<A1, A2>
{

   /** . */
   public static TypeConverter<A1, A2> delegate;

   @Override
   public A2 write(A1 input) throws Exception
   {
      if (delegate == null)
      {
         throw new AssertionFailedError();
      }
      return delegate.write(input);
   }

   @Override
   public A1 read(A2 output) throws Exception
   {
      if (delegate == null)
      {
         throw new AssertionFailedError();
      }
      return delegate.read(output);
   }
}
