/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.commons.serialization;

import org.gatein.common.io.IOTools;
import org.gatein.common.io.UndeclaredIOException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;

/**
 * A simple marshalled object that retain the state of an object as a bytes.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MarshalledObject<S extends Serializable>
{

   public static <S extends Serializable> MarshalledObject<S> marshall(S serializable) throws NullPointerException
   {
      if (serializable == null)
      {
         throw new NullPointerException("Cannot marshall null");
      }
      try
      {
         byte[] bytes = IOTools.serialize(serializable);
         return new MarshalledObject<S>(serializable.getClass().getClassLoader(), bytes);
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }
   }

   /** . */
   private final ClassLoader loader;

   /** . */
   private final byte[] state;

   private MarshalledObject(ClassLoader loader, byte[] state)
   {
      this.loader = loader;
      this.state = state;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof MarshalledObject)
      {
         MarshalledObject<?> that = (MarshalledObject<?>)obj;
         return Arrays.equals(state, that.state);
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return Arrays.hashCode(state);
   }

   public S unmarshall() throws UndeclaredThrowableException
   {
      try
      {
         return (S)IOTools.unserialize(state, loader);
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }
      catch (ClassNotFoundException e)
      {
         throw new UndeclaredThrowableException(e);
      }
   }
}
