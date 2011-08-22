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

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.commons.cache.CacheManager;
import org.exoplatform.commons.cache.future.FutureCacheManager;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.io.Serializable;
import java.util.Collection;

/**
 * An implementation using the cache service.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ExoDataCache extends DataCache
{

   /** . */
   protected CacheManager cacheManager;

   /** . */
   protected FutureCacheManager<Serializable, Serializable, POMSession> objects;

   /** . */
   private Loader<Serializable, Serializable, POMSession> navigationLoader = new Loader<Serializable, Serializable, POMSession>()
   {
      public Serializable retrieve(POMSession session, Serializable key) throws Exception
      {
         if (key instanceof SiteKey)
         {
            return loadNavigation(session, (SiteKey)key);
         }
         else
         {
            return loadNode(session, (String)key);
         }
      }
   };

   public ExoDataCache(CacheManager cacheManager)
   {
      this.cacheManager = cacheManager;
      this.objects = new FutureCacheManager<Serializable, Serializable, POMSession>(NavigationService.class, navigationLoader, cacheManager);
   }

   @Override
   protected void removeNodes(Collection<String> keys)
   {
      for (String key : keys)
      {
         objects.remove(key);
      }
   }

   @Override
   protected NodeData getNode(POMSession session, String key)
   {
      return (NodeData)objects.get(session, key);
   }

   @Override
   protected void removeNavigation(SiteKey key)
   {
      objects.remove(key);
   }

   @Override
   protected NavigationData getNavigation(POMSession session, SiteKey key)
   {
      return (NavigationData)objects.get(session, key);
   }

   @Override
   protected void clear()
   {
      objects.clear();
   }
}
