package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * <p>A wrapper for the {@link PageServiceImpl}, the wrappers takes care of integrating the implementation
 * with the GateIn runtime.</p>
 *
 * <p>The wrapper emits events when page modifications are performed:</p>
 * <ul>
 *    <li>{@link EventType#PAGE_CREATED}: when a page is created</li>
 *    <li>{@link EventType#PAGE_UPDATED}: when a page is updated</li>
 *    <li>{@link EventType#PAGE_DESTROYED}: when a page is destroyed</li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageServiceWrapper implements PageService
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(PageServiceWrapper.class);

   /** . */
   private final PageServiceImpl service;

   /** . */
   private final POMSessionManager manager;

   /** . */
   private final ListenerService listenerService;

   public PageServiceWrapper(POMSessionManager manager, ListenerService listenerService)
   {
      this.service = new PageServiceImpl(manager);
      this.manager = manager;
      this.listenerService = listenerService;
   }

   public PageServiceWrapper(POMSessionManager manager, ListenerService listenerService, CacheService cacheService)
   {
      this.service = new PageServiceImpl(manager, new ExoDataCache(cacheService));
      this.manager = manager;
      this.listenerService = listenerService;
   }

   @Override
   public PageContext loadPage(PageKey key)
   {
      return service.loadPage(key);
   }

   @Override
   public boolean savePage(PageContext page)
   {
      boolean created = service.savePage(page);

      //
      if (created)
      {
         notify(EventType.PAGE_CREATED, page.key);
      }
      else
      {
         notify(EventType.PAGE_UPDATED, page.key);
      }

      //
      return created;
   }

   @Override
   public boolean destroyPage(PageKey key)
   {
      boolean destroyed = service.destroyPage(key);

      //
      if (destroyed)
      {
         notify(EventType.PAGE_DESTROYED, key);
      }

      //
      return destroyed;
   }

   @Override
   public PageContext clone(PageKey src, PageKey dst)
   {
      return service.clone(src, dst);
   }

   @Override
   public QueryResult<PageContext> findPages(int offset, int limit, SiteType siteType, String siteName, String pageName, String title)
   {
      return service.findPages(offset, limit, siteType, siteName, pageName, title);
   }

   private void notify(String name, PageKey key)
   {
      try
      {
         listenerService.broadcast(name, this, key);
      }
      catch (Exception e)
      {
         log.error("Error when delivering notification " + name + " for page " + key, e);
      }
   }
}
