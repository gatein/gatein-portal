/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.SiteKey;
import static org.exoplatform.portal.mop.navigation.Utils.*;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.picocontainer.Startable;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceWrapper implements NavigationService, Startable
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(NavigationServiceWrapper.class);

   /** . */
   private final NavigationServiceImpl service;

   /** . */
   private ListenerService listenerService;

   /** . */
   private final POMSessionManager manager;

   /** . */
   private Session session;

   /** . */
   private final RepositoryService repositoryService;

   /** . */
   private final InvalidationBridge bridge;

   public NavigationServiceWrapper(
      RepositoryService repositoryService,
      POMSessionManager manager,
      ListenerService listenerService)
   {
      SimpleDataCache cache = new SimpleDataCache();

      //
      this.repositoryService = repositoryService;
      this.manager = manager;
      this.service = new NavigationServiceImpl(manager, cache);
      this.listenerService = listenerService;
      this.bridge = new InvalidationBridge(cache);
   }

   public NavigationServiceWrapper(
      RepositoryService repositoryService,
      POMSessionManager manager,
      ListenerService listenerService,
      CacheService cacheService)
   {
      ExoDataCache cache = new ExoDataCache(cacheService);

      //
      this.repositoryService = repositoryService;
      this.manager = manager;
      this.service = new NavigationServiceImpl(manager, cache);
      this.listenerService = listenerService;
      this.bridge = new InvalidationBridge(cache);
   }

   public NavigationContext loadNavigation(SiteKey key)
   {
      return service.loadNavigation(key);
   }

   public void saveNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException
   {
      boolean created = navigation.data == null;

      //
      service.saveNavigation(navigation);

      //
      if (created)
      {
         notify(EventType.NAVIGATION_CREATED, navigation.key);
      }
      else
      {
         notify(EventType.NAVIGATION_UPDATED, navigation.key);
      }
   }

   public boolean destroyNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException
   {
      boolean destroyed = service.destroyNavigation(navigation);

      //
      if (destroyed)
      {
         notify(EventType.NAVIGATION_DESTROYED, navigation.key);
      }

      //
      return destroyed;
   }

   public <N> NodeContext<N> loadNode(NodeModel<N> model, NavigationContext navigation, Scope scope, NodeChangeListener<NodeContext<N>> listener)
   {
      return service.loadNode(model, navigation, scope, listener);
   }

   public <N> void saveNode(NodeContext<N> context, NodeChangeListener<NodeContext<N>> listener) throws NavigationServiceException
   {
      service.saveNode(context, listener);
      org.gatein.mop.api.workspace.Navigation nav = service.manager.getSession().findObjectById(ObjectType.NAVIGATION, context.data.id);
      Site site = nav.getSite();
      SiteKey key = new SiteKey(siteType(site.getObjectType()), site.getName());
      notify(EventType.NAVIGATION_UPDATED, key);
   }

   public <N> void updateNode(NodeContext<N> context, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, NavigationServiceException
   {
      service.updateNode(context, scope, listener);
   }

   public <N> void rebaseNode(NodeContext<N> context, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, NavigationServiceException
   {
      service.rebaseNode(context, scope, listener);
   }

   private void notify(String name, SiteKey key)
   {
      try
      {
         listenerService.broadcast(name, this, key);
      }
      catch (Exception e)
      {
         log.error("Error when delivering notification " + name + " for navigation " + key, e);
      }
   }

   public void start()
   {
      try
      {
         String workspaceName = manager.getLifeCycle().getWorkspaceName();
         ManageableRepository repo = repositoryService.getCurrentRepository();
         session = repo.getSystemSession(workspaceName);
         bridge.start(session);
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException(e);
      }
   }

   public void stop()
   {
      bridge.stop();

      //
      if (session != null)
      {
         session.logout();
      }
   }
}
