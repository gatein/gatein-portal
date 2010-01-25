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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.config.TaskExecutor;
import org.exoplatform.portal.pom.config.TaskExecutionDecorator;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DataCache extends TaskExecutionDecorator
{

   /** . */
   private final AtomicLong readCount = new AtomicLong();

   /** . */
   private boolean cluster = ExoContainer.getProfiles().contains("cluster");

   public DataCache(TaskExecutor next)
   {
      super(next);
   }

   public <V> V execute(POMSession session, POMTask<V> task) throws Exception
   {
      if (task instanceof CacheableDataTask && !cluster)
      {
         CacheableDataTask<?, V> loadTask = (CacheableDataTask<?, V>)task;
         switch (loadTask.getAccessMode())
         {
            case READ:
               return read(session, loadTask);
            case CREATE:
               return create(session, loadTask);
            case WRITE:
               return write(session, loadTask);
            case DESTROY:
               return remove(session, loadTask);
            default:
               throw new UnsupportedOperationException();
         }
      }
      else
      {
         return super.execute(session, task);
      }
   }

   private <K extends Serializable, V> V remove(POMSession session, CacheableDataTask<K, V> task) throws Exception
   {
      K key = task.getKey();
      session.scheduleForEviction(key);
      return super.execute(session, task);
   }

   private <K extends Serializable, V> V write(POMSession session, CacheableDataTask<K, V> task) throws Exception
   {
      K key = task.getKey();
      session.scheduleForEviction(key);
      return super.execute(session, task);
   }

   private <K extends Serializable, V> V create(POMSession session, CacheableDataTask<K, V> task) throws Exception
   {
      // Nothing to do for now
      return super.execute(session, task);
   }

   private <K extends Serializable, V> V read(POMSession session, CacheableDataTask<K, V> task) throws Exception
   {
      if (!session.isModified())
      {
         K key = task.getKey();
         Object o = session.getFromCache(key);
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
            return v;
         }
         else
         {
            readCount.incrementAndGet();

            //
            v = super.execute(session, task);

            //
            if (v != null)
            {
               session.putInCache(key, v);
            }

            //
            return v;
         }
      }
      else
      {
         return super.execute(session, task);
      }
   }

   public long getReadCount()
   {
      return readCount.longValue();
   }
}
