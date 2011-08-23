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

package org.exoplatform.commons.scope;

import java.io.Serializable;

/**
 * A scoped key wrapping a key including the a scope. The scoped key implements the {@link #hashCode()} and
 * {@link #equals(Object)} method based on the scope and the current value.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ScopedKey<S extends Serializable> extends AbstractScopedKey
{

   /**
    * Create a scoped key from a key, the scope value is obtained from the {@link ScopeManager#getCurrentScope()} method.
    *
    * @param key the local key
    * @param <S> the key generic serializable type
    * @return the scoped key
    * @throws NullPointerException if the key argument is null
    */
   public static <S extends Serializable> ScopedKey<S> create(S key) throws NullPointerException
   {
      return new ScopedKey<S>(key);
   }

   /**
    * Creates a new scoped key with an explicit scope value.
    *
    * @param scope the scope value
    * @param key the key
    * @param <S> the key generic serializable type
    * @return the scoped key
    * @throws NullPointerException if any argument is null
    */
   public static <S extends Serializable> ScopedKey<S> create(String scope, S key) throws NullPointerException
   {
      return new ScopedKey<S>(scope, key);
   }

   /** . */
   private final S key;

   public ScopedKey(S key) throws NullPointerException
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      this.key = key;
   }

   public ScopedKey(String scope, S key) throws NullPointerException
   {
      super(scope);

      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      this.key = key;
   }

   public S getKey()
   {
      return key;
   }

   @Override
   public int hashCode()
   {
      return super.hashCode() ^ key.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      return super.equals(obj) && ((ScopedKey)obj).key.equals(key);
   }

   @Override
   public String toString()
   {
      return "ScopedKey[scope=" + getScope() + ",key=" + key + "]";
   }
}
