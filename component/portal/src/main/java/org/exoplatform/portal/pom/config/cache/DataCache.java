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

import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.config.TaskExecutor;
import org.exoplatform.portal.pom.config.TaskExecutionDecorator;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DataCache extends TaskExecutionDecorator
{

   /** . */
   private final ExoCache<Serializable, Object> cache;

   /** . */
   private final AtomicLong readCount = new AtomicLong();

   public DataCache(CacheService cacheService, TaskExecutor next)
   {
      super(next);

      //
      this.cache = cacheService.getCacheInstance(DataCache.class.getSimpleName());
   }

   public void execute(POMSession session, POMTask task) throws Exception
   {
      if (task instanceof CacheableDataTask)
      {
         if (!session.isModified())
         {
            CacheableDataTask<?, ?> loadTask = (CacheableDataTask<?,?>)task;
            switch (loadTask.getAccessMode())
            {
               case READ:
                  read(session, loadTask);
                  break;
               case CREATE:
                  create(session, loadTask);
                  break;
               case WRITE:
                  write(session, loadTask);
                  break;
               case DESTROY:
                  remove(session, loadTask);
                  break;
               default:
                  throw new UnsupportedOperationException();
            }
         }
         else
         {
            super.execute(session, task);
         }
      }
      else
      {
         super.execute(session, task);
      }
   }

   public void clear()
   {
      cache.clearCache();
   }

   private <K extends Serializable, V> void remove(POMSession session, CacheableDataTask<K, V> task) throws Exception
   {
      K key = task.getKey();
      cache.remove(key);
      super.execute(session, task);
   }

   private <K extends Serializable, V> void write(POMSession session, CacheableDataTask<K, V> task) throws Exception
   {
      K key = task.getKey();
      cache.remove(key);
      super.execute(session, task);
   }

   private <K extends Serializable, V> void create(POMSession session, CacheableDataTask<K, V> task) throws Exception
   {
      // Nothing to do for now
      super.execute(session, task);
   }

   private <K extends Serializable, V> void read(POMSession session, CacheableDataTask<K, V> task) throws Exception
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
         readCount.incrementAndGet();

         //
         super.execute(session, task);

         //
         v = task.getValue();
         if (v != null)
         {
            cache.put(key, v);
         }
      }
   }

   public long getReadCount()
   {
      return readCount.longValue();
   }
}
