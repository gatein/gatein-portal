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

import static org.exoplatform.portal.mop.Utils.siteType;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;

import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.commons.cache.InvalidationBridge;
import org.exoplatform.portal.mop.EventType;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;
import org.gatein.portal.mop.hierarchy.NodeChangeListener;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.hierarchy.Scope;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.navigation.NavigationServiceException;
import org.gatein.portal.mop.navigation.NavigationServiceImpl;
import org.gatein.portal.mop.navigation.NodeState;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NavigationServiceWrapper implements NavigationService, Startable {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(NavigationServiceWrapper.class);

    /** . */
    private final NavigationServiceImpl service;

    /** . */
    private final ListenerService listenerService;

    /** . */
    private final RepositoryService repositoryService;

    /** . */
    private final InvalidationBridge bridge;

    /** . */
    private final MopStore persistence;

    public NavigationServiceWrapper(RepositoryService repositoryService, POMSessionManager manager,
            ListenerService listenerService) {
        this(repositoryService, manager, listenerService, new SimpleDataCache());
    }

    public NavigationServiceWrapper(RepositoryService repositoryService, POMSessionManager manager,
            ListenerService listenerService, CacheService cacheService) {
        this(repositoryService, manager, listenerService, new ExoDataCache(cacheService));
    }

    public NavigationServiceWrapper(
            RepositoryService repositoryService,
            final POMSessionManager manager,
            ListenerService listenerService,
            DataCache cache) {
        this.repositoryService = repositoryService;
        this.persistence = new MopStore(manager, cache);
        this.service = new NavigationServiceImpl(persistence);
        this.listenerService = listenerService;
        this.bridge = new InvalidationBridge() {
            @Override
            public void onEvent(EventIterator events) {
                persistence.clear();
            }
        };
    }

    public NavigationContext loadNavigation(SiteKey key) {
        return service.loadNavigation(key);
    }

    @Override
    public List<NavigationContext> loadNavigations(SiteType type) throws NullPointerException, NavigationServiceException {
        return service.loadNavigations(type);
    }

    public void saveNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException {
        boolean created = !navigation.isPersistent();

        //
        service.saveNavigation(navigation);

        //
        if (created) {
            notify(EventType.NAVIGATION_CREATED, navigation.getKey());
        } else {
            notify(EventType.NAVIGATION_UPDATED, navigation.getKey());
        }
    }

    public boolean destroyNavigation(NavigationContext navigation) throws NullPointerException, NavigationServiceException {
        boolean destroyed = service.destroyNavigation(navigation);

        //
        if (destroyed) {
            notify(EventType.NAVIGATION_DESTROYED, navigation.getKey());
        }

        //
        return destroyed;
    }

    public <N> NodeContext<N, NodeState> loadNode(NodeModel<N, NodeState> model, NavigationContext navigation, Scope<NodeState> scope,
            NodeChangeListener<NodeContext<N, NodeState>, NodeState> listener) {
        return service.loadNode(model, navigation, scope, listener);
    }

    public <N> void saveNode(NodeContext<N, NodeState> context, NodeChangeListener<NodeContext<N, NodeState>, NodeState> listener)
            throws NavigationServiceException {
        service.saveNode(context, listener);
        org.gatein.mop.api.workspace.Navigation nav = persistence.mgr.getSession().findObjectById(ObjectType.NAVIGATION,
                context.getId());
        Site site = nav.getSite();
        SiteKey key = new SiteKey(siteType(site.getObjectType()), site.getName());
        notify(EventType.NAVIGATION_UPDATED, key);
    }

    public <N> void updateNode(NodeContext<N, NodeState> context, Scope<NodeState> scope, NodeChangeListener<NodeContext<N, NodeState>, NodeState> listener)
            throws NullPointerException, NavigationServiceException {
        service.updateNode(context, scope, listener);
    }

    public <N> void rebaseNode(NodeContext<N, NodeState> context, Scope<NodeState> scope, NodeChangeListener<NodeContext<N, NodeState>, NodeState> listener)
            throws NullPointerException, NavigationServiceException {
        service.rebaseNode(context, scope, listener);
    }

    private void notify(String name, SiteKey key) {
        try {
            listenerService.broadcast(name, this, key);
        } catch (Exception e) {
            log.error("Error when delivering notification " + name + " for navigation " + key, e);
        }
    }

    public void start() {
        Session session = null;
        try {
            String workspaceName = persistence.mgr.getLifeCycle().getWorkspaceName();
            ManageableRepository repo = repositoryService.getCurrentRepository();
            session = repo.getSystemSession(workspaceName);
            bridge.start(session);
        } catch (RepositoryException e) {
            throw new UndeclaredRepositoryException(e);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    public void stop() {
        bridge.stop();
    }
}
