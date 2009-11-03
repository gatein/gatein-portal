/**
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
package org.exoplatform.portal.pom.config.cache;

import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.config.TaskExecutor;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DataCache implements TaskExecutor
{

   /** . */
   private TaskExecutor next;

   /** . */
   private ExoCache<Serializable, Object> cache;

   public DataCache(CacheService cacheService, TaskExecutor next)
   {
      this.next = next;
      this.cache = cacheService.getCacheInstance(DataCache.class.getSimpleName());
   }

   public <T extends POMTask> T execute(T task) throws Exception
   {
      if (task instanceof CacheableDataTask)
      {
         CacheableDataTask<?, ?> loadTask = (CacheableDataTask<?,?>)task;
         switch (loadTask.getAccessMode())
         {
            case READ:
               return (T)read(loadTask);
            case CREATE:
               return (T)create(loadTask);
            case WRITE:
               return (T)write(loadTask);
            case DESTROY:
               return (T)remove(loadTask);
            default:
               throw new UnsupportedOperationException();

         }
      }
      else
      {
         return next.execute(task);
      }
   }

   private <K extends Serializable, V, T extends CacheableDataTask<K, V>> T remove(T task) throws Exception
   {
      K key = task.getKey();
      cache.remove(key);
      return next.execute(task);
   }

   private <K extends Serializable, V, T extends CacheableDataTask<K, V>> T write(T task) throws Exception
   {
      K key = task.getKey();
      cache.remove(key);
      return next.execute(task);
   }

   private <K extends Serializable, V, T extends CacheableDataTask<K, V>> T create(T task) throws Exception
   {
      // Nothing to do for now
      return next.execute(task);
   }

   private <K extends Serializable, V, T extends CacheableDataTask<K, V>> T read(T task) throws Exception
   {
      K key = task.getKey();
      Object o = cache.get(key);
      V v = null;
      if (o != null)
      {
         Class<V> type = task.getValueType();
         if (type.isInstance(o))
         {
            v = type.cast(o);
         }
      }

      //
      if (v != null)
      {
         task.setValue(v);
      }
      else
      {
         //
         next.execute(task);

         //
         v = task.getValue();
         if (v != null)
         {
            cache.put(key, v);
         }
      }

      //
      return task;
   }
}
