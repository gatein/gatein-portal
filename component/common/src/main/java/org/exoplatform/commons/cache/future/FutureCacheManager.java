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
package org.exoplatform.commons.cache.future;

import org.exoplatform.commons.cache.CacheManager;
import org.exoplatform.services.cache.ExoCache;

import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FutureCacheManager<K extends Serializable, V, C> extends FutureCache<K, V, C>
{

   /** . */
   private final CacheManager manager;

   /** . */
   private final Class<?> cacheType;

   public FutureCacheManager(Class<?> cacheType, Loader<K, V, C> loader, CacheManager manager)
   {
      super(loader);
      
      //
      this.cacheType = cacheType;
      this.manager = manager;
   }

   public void clear()
   {
      ExoCache<?, ?> cache = manager.getCurrentProvider().getCache(cacheType);

      //
      if (cache != null)
      {
         cache.clearCache();
      }
   }

   public void remove(K key)
   {
      manager.getCurrentProvider().put(cacheType, key, null);
   }

   @Override
   public V get(K key)
   {
      return manager.getCurrentProvider().get(cacheType, key);
   }

   @Override
   public void put(K key, V entry)
   {
      manager.getCurrentProvider().put(cacheType, key, entry);
   }
}
