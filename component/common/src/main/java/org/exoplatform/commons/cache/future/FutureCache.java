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
 * A future cache that prevents the loading of the same resource twice. This should be used when the resource
 * to load is very expensive or cannot be concurrently retrieved (like a classloading). 
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class FutureCache<K, V, C>
{

   /** . */
   private final Loader<K, V, C> loader;

   /** . */
   private final ConcurrentMap<K, FutureTask<Entry<V>>> local;

   /** . */
   private final Logger log = LoggerFactory.getLogger(FutureCache.class);

   public FutureCache(Loader<K, V, C> loader)
   {
      this.loader = loader;
      this.local = new ConcurrentHashMap<K, FutureTask<Entry<V>>>();
   }

   protected abstract Entry<V> get(K key);

   protected abstract void put(K key, Entry<V> entry);

   public V get(final C context, final K key)
   {
      // First we try a simple cache get
      Entry<V> entry = get(key);

      // If it does not succeed then we go through a process that will avoid to load
      // the same resource concurrently
      if (entry == null)
      {
         // Create our future
         FutureTask<Entry<V>> future = new FutureTask<Entry<V>>(new Callable<Entry<V>>()
         {
            public Entry<V> call() throws Exception
            {
               // Retrieve the value from the loader
               V value = loader.retrieve(context, key);

               //
               if (value != null)
               {
                  // Create the entry
                  Entry<V> entry = Entry.create(value);

                  // Cache it, it is made available to other threads (unless someone removes it)
                  put(key, entry);

                  // Return entry
                  return entry;
               }
               else
               {
                  return null;
               }
            }
         });

         // Was our means that we inserted in the local
         boolean inserted = true;

         //
         try
         {
            FutureTask<Entry<V>> phantom = local.putIfAbsent(key, future);

            // Use the entry that could have been inserted by another thread
            if (phantom != null)
            {
               future = phantom;
               inserted = false;
            }
            else
            {
               future.run();
            }

            // Returns the entry
            entry = future.get();
         }
         catch (ExecutionException e)
         {
            log.error("Computing of resource " + key + " threw an exception", e.getCause());
         }
         catch (Exception e)
         {
            log.error("Retrieval of resource " + key + " threw an exception", e);
         }
         finally
         {
            // Clean up the per key map but only if our insertion succeeded and with our future
            if (inserted)
            {
               local.remove(key, future);
            }
         }
      }

      //
      return entry != null ? entry.getValue() : null;
   }
}
