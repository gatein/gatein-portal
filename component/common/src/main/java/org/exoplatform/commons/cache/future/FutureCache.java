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

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.concurrent.*;

/**
 * <p>A future cache that prevents the loading of the same resource twice. This should be used when the resource
 * to load is very expensive or cannot be concurrently retrieved (like a classloading).</p>
 *
 * <p>The future cache should be used with the {@link #get(Object, Object)} method, that retrieves an object
 * from the cache. When the object is not found, then the {@link Loader#retrieve(Object, Object)} method is
 * used to retrieve the data and then this data is inserted in the cache.</p>
 *
 * <p>The class is abstract and does not implement a cache technology by itself, the cache implementation is delegated
 * to the contractual methods {@link #get(Object)} and {@link #put(Object, Object)}. Those methods are intended
 * to be used by the future cache only.</p>
 *
 * <p>The {@link Loader} interface provides a source to retrieve objects to put in the cache. The goal to maintain
 * this interface is to decouple the cache from the object source.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 * @param <C> the context type parameter
 */
public abstract class FutureCache<K, V, C>
{

   /** . */
   final Loader<K, V, C> loader;

   /** . */
   private final ConcurrentMap<K, Retrieval<K, V, C>> futureEntries;

   /** . */
   private final Logger log = LoggerFactory.getLogger(FutureCache.class);

   public FutureCache(Loader<K, V, C> loader)
   {
      this.loader = loader;
      this.futureEntries = new ConcurrentHashMap<K, Retrieval<K, V, C>>();
   }

   /**
    * Retrieves the cached value corresponding to the specified key from the cache, it must returns null when the
    * key does not exist. This method is intended for internal use by the future cache only.
    *
    * @param key the key
    * @return the cache value
    */
   protected abstract V get(K key);

   /**
    * Updates the cache with a new key/value pair. This method is intended for internal use by the future cache only.
    *
    * @param key the key
    * @param value the cache value
    */
   protected abstract void put(K key, V value);

   /**
    * Perform a cache lookup for the specified key within the specified context.
    * When the value cannot be loaded (because it does not exist or it failed or anything else that
    * does not come to my mind), the value null is returned.
    *
    * @param context the context in which the resource is accessed
    * @param key the key identifying the resource
    * @return the value
    */
   public final V get(final C context, final K key)
   {
      // First we try a simple cache get
      V value = get(key);

      // If it does not succeed then we go through a process that will avoid to load
      // the same resource concurrently
      if (value == null)
      {
         // Create our future
         Retrieval<K, V, C> retrieval = new Retrieval<K, V, C>(context, key, this);

         // This boolean means we inserted in the local
         boolean inserted = true;

         //
         try
         {
            Retrieval<K, V, C> phantom = futureEntries.putIfAbsent(key, retrieval);

            // Use the value that could have been inserted by another thread
            if (phantom != null)
            {
               retrieval = phantom;
               inserted = false;
            }
            else
            {
               try
               {
                  retrieval.current = Thread.currentThread();
                  retrieval.future.run();
               }
               catch (Exception e)
               {
                  log.error("Retrieval of resource " + key + " threw an exception", e);
               }
               finally
               {
                  retrieval.current = null;
               }
            }

            // Returns the value
            if (retrieval.current == Thread.currentThread())
            {
               throw new IllegalStateException("Reentrancy detected when obtaining key " + key + " with context " + context + " detected");
            }
            else
            {
               try
               {
                  value = retrieval.future.get();
               }
               catch (ExecutionException e)
               {
                  log.error("Computing of resource " + key + " threw an exception", e.getCause());
               }
               catch (InterruptedException e)
               {
                  // We should handle interruped exception in some manner
                  log.error("Retrieval of resource " + key + " threw an exception", e);
               }
            }
         }
         finally
         {
            // Clean up the per key map but only if our insertion succeeded and with our future
            if (inserted)
            {
               futureEntries.remove(key, retrieval);
            }
         }
      }

      //
      return value;
   }
}
