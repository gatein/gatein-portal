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

package org.exoplatform.commons.cache.future;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FutureMap<K extends Serializable, V, C> extends FutureCache<K, V, C>
{

   /** . */
   final Map<K, V> data;

   public FutureMap(Loader<K, V, C> loader)
   {
      super(loader);

      //
      this.data = Collections.synchronizedMap(new HashMap<K, V>());
   }

   public void clear()
   {
      data.clear();
   }

   public void remove(K key)
   {
      data.remove(key);
   }

   @Override
   protected V get(K key)
   {
      return data.get(key);
   }

   @Override
   protected void put(K key, V value)
   {
      data.put(key, value);
   }
}