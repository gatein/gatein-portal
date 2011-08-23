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

package org.exoplatform.portal.mop.description;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExoDataCache extends DataCache
{

   /** . */
   protected ExoCache<CacheKey, CacheValue> cache;

   /** . */
   protected FutureExoCache<CacheKey, CacheValue, POMSession> values;

   /** . */
   private Loader<CacheKey, CacheValue, POMSession> valueLoader = new Loader<CacheKey, CacheValue, POMSession>()
   {
      public CacheValue retrieve(POMSession session, CacheKey key) throws Exception
      {
         return getValue(session, key);
      }
   };

   public ExoDataCache(CacheService cacheService)
   {
      this.cache = cacheService.getCacheInstance(DescriptionService.class.getSimpleName());
      this.values = new FutureExoCache<CacheKey, CacheValue, POMSession>(valueLoader, cache)
      {
         @Override
         protected void put(CacheKey key, CacheValue entry)
         {
            // Do nothing on purpose
            // as data in inserted with the putValue method
            // during the getValue method
         }
      };
   }

   @Override
   protected void removeState(CacheKey key)
   {
      cache.remove(key);
   }

   @Override
   protected Described.State getState(POMSession session, CacheKey key)
   {
      CacheValue value = values.get(session, key);
      return value != null ? value.state : null;
   }

   @Override
   protected void putValue(CacheKey key, CacheValue value)
   {
      cache.put(key, value);
   }
}
