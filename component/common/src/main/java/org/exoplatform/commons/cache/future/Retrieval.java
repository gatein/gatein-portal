package org.exoplatform.commons.cache.future;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Retrieval<K, V, C> implements Callable<V>
{

   /** . */
   private final C context;

   /** . */
   private final K key;

   /** . */
   private final FutureCache<K, V, C> cache;

   /** . */
   final FutureTask<V> future;

   /** Avoid reentrancy. */
   transient Thread current;

   public Retrieval(C context, K key, FutureCache<K, V, C> cache)
   {
      this.key = key;
      this.context = context;
      this.future = new FutureTask<V>(this);
      this.cache = cache;
      this.current = null;
   }

   public V call() throws Exception
   {
      // Retrieve the value from the loader
      V value = cache.loader.retrieve(context, key);

      //
      if (value != null)
      {
         // Cache it, it is made available to other threads (unless someone removes it)
         cache.put(key, value);

         // Return value
         return value;
      }
      else
      {
         return null;
      }
   }
}
