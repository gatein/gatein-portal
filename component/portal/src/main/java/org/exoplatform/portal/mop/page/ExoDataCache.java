package org.exoplatform.portal.mop.page;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.scope.ScopedKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ExoDataCache extends DataCache
{

   /** . */
   protected ExoCache<ScopedKey<PageKey>, PageData> cache;

   /** . */
   protected FutureExoCache<ScopedKey<PageKey>, PageData, POMSession> objects;

   /** . */
   private Loader<ScopedKey<PageKey>, PageData, POMSession> pageLoader = new Loader<ScopedKey<PageKey>, PageData, POMSession>()
   {
      public PageData retrieve(POMSession session, ScopedKey<PageKey> scopedKey) throws Exception
      {
         PageData data = loadPage(session, scopedKey.getKey());
         return data == PageData.EMPTY ? null : data;
      }
   };

   public ExoDataCache(CacheService cacheService)
   {
      this.cache = cacheService.getCacheInstance(PageService.class.getSimpleName());
      this.objects = new FutureExoCache<ScopedKey<PageKey>, PageData, POMSession>(pageLoader, cache);
   }

   @Override
   protected PageData getPage(POMSession session, PageKey key)
   {
      return objects.get(session, ScopedKey.create(key));
   }

   @Override
   protected void removePage(POMSession session, PageKey key)
   {
      cache.remove(ScopedKey.create(key));
   }

   @Override
   protected void putPage(PageData data)
   {
      cache.put(ScopedKey.create(data.key), data);
   }

   @Override
   protected void clear()
   {
      cache.clearCache();
   }
}
