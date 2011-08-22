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
package org.exoplatform.commons.cache;

import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.services.cache.ExoCache;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides caches according to types (classes). The provider is scope to the current request and is managed
 * by a component request life cycle, it is made available through the {@link CacheManager#getCurrentProvider()} method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CacheProvider
{

   /** . */
   private final CacheManager manager;

   /** . */
   private Map<Class<?>, ExoCache> caches;

   CacheProvider(CacheManager manager)
   {
      this.manager = manager;
      this.caches = new HashMap<Class<?>, ExoCache>();
   }

   /**
    * Returns a cache for a specified cache type or null if such cache does not exist.
    *
    * @param cacheType the cache type
    * @return the corresponding cache
    * @throws NullPointerException if the cache type is null
    */
   public ExoCache<?, ?> getCache(Class<?> cacheType) throws NullPointerException
   {
      if (cacheType == null)
      {
         throw new NullPointerException("No null cache type accepted");
      }

      //
      return caches.get(cacheType);
   }

   /**
    * Returns a value for a key for a given cache type. When the value does not exist, null is returned.
    *
    * @param cacheType the cache type
    * @param key the key
    * @param <K> the key generic type
    * @param <S> the value generic type
    * @return the value
    */
   public <K extends Serializable, S> S get(Class<?> cacheType, K key) throws NullPointerException
   {
      if (key == null)
      {
         throw new NullPointerException("No null key accepted");
      }

      //
      ExoCache<K, S> cache = (ExoCache<K, S>)getCache(cacheType);

      //
      if (cache != null)
      {
         return cache.get(key);
      }

      //
      return null;
   }

   /**
    * Associates a value and a key for a given cache type. When the provided value is null, the value is instead
    * removed from the cache.
    *
    * @param cacheType the cache type
    * @param key the key
    * @param value the value
    * @param <K> the key generic type
    * @param <S> the value generic type
    */
   public <K extends Serializable, S> void put(Class<?> cacheType, K key, S value) throws NullPointerException
   {
      if (key == null)
      {
         throw new NullPointerException("No null key accepted");
      }

      //
      ExoCache<K, S> cache = (ExoCache<K, S>)getCache(cacheType);

      //
      if (value == null)
      {
         if (cache != null)
         {
            cache.remove(key);
         }
      }
      else
      {
         if (cache == null)
         {
            String ckey = cacheType.getSimpleName() + "." + getRepositoryName();
            cache = manager.cacheService.getCacheInstance(ckey);
            caches.put(cacheType, cache);
         }

         //
         cache.put(key, value);
      }
   }

   /**
    * Clear a cache for a given type.
    *
    * @param cacheType the cache type
    * @throws NullPointerException when the cache type is null
    */
   public void clear(Class<?> cacheType)  throws NullPointerException
   {
      ExoCache<?, ?> cache = getCache(cacheType);

      //
      if (cache != null)
      {
         cache.clearCache();
      }
   }

   private String getRepositoryName()
   {
      try
      {
         return manager.repositoryService.getCurrentRepository().getConfiguration().getName();
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException("JCR exceptions are really bad", e);
      }
   }
}
