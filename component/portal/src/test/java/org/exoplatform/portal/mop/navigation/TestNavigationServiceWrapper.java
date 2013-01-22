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

import java.util.LinkedList;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.PersistenceContext;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.navigation.Node;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.Scope;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationError;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.navigation.NavigationServiceException;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNavigationServiceWrapper extends AbstractMopServiceTest {

    /** . */
    private NavigationService wrapper;

    /** . */
    private ListenerService listenerService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //
        PortalContainer container = getContainer();
        POMSessionManager mgr = ((PersistenceContext.JCR) context).getManager();
        listenerService = new ListenerService(container.getContext());
        wrapper = new NavigationServiceWrapper(
                (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class),
                mgr,
                listenerService,
                (CacheService) container.getComponentInstanceOfType(CacheService.class));
    }

    public void testNotification() throws NavigationServiceException {
        class ListenerImpl extends Listener<NavigationService, SiteKey> {

            /** . */
            private final LinkedList<Event> events = new LinkedList<Event>();

            @Override
            public void onEvent(Event event) throws Exception {
                events.addLast(event);
            }
        }

        //
        ListenerImpl createListener = new ListenerImpl();
        ListenerImpl updateListener = new ListenerImpl();
        ListenerImpl destroyListener = new ListenerImpl();

        //
        listenerService.addListener(EventType.NAVIGATION_CREATED, createListener);
        listenerService.addListener(EventType.NAVIGATION_UPDATED, updateListener);
        listenerService.addListener(EventType.NAVIGATION_DESTROYED, destroyListener);

        //
        begin();
        createSite(SiteType.PORTAL, "notification");

        // Create
        NavigationContext navigation = new NavigationContext(SiteKey.portal("notification"), new NavigationState(3));
        wrapper.saveNavigation(navigation);
        assertEquals(1, createListener.events.size());
        Event event = createListener.events.removeFirst();
        assertEquals(SiteKey.portal("notification"), event.getData());
        assertEquals(EventType.NAVIGATION_CREATED, event.getEventName());
        assertSame(wrapper, event.getSource());
        assertEquals(0, updateListener.events.size());
        assertEquals(0, destroyListener.events.size());

        //

        // Update
        navigation.setState(new NavigationState(1));
        wrapper.saveNavigation(navigation);
        assertEquals(0, createListener.events.size());
        assertEquals(1, updateListener.events.size());
        event = updateListener.events.removeFirst();
        assertEquals(SiteKey.portal("notification"), event.getData());
        assertEquals(EventType.NAVIGATION_UPDATED, event.getEventName());
        assertSame(wrapper, event.getSource());
        assertEquals(0, destroyListener.events.size());

        // Update
        navigation = wrapper.loadNavigation(SiteKey.portal("notification"));
        Node root = wrapper.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
        root.setState(new NodeState.Builder(root.getState()).label("foo").build());
        wrapper.saveNode(root.getContext(), null);
        assertEquals(0, createListener.events.size());
        assertEquals(1, updateListener.events.size());
        event = updateListener.events.removeFirst();
        assertEquals(SiteKey.portal("notification"), event.getData());
        assertEquals(EventType.NAVIGATION_UPDATED, event.getEventName());
        assertSame(wrapper, event.getSource());
        assertEquals(0, destroyListener.events.size());

        // Destroy
        wrapper.destroyNavigation(navigation);
        assertEquals(0, createListener.events.size());
        assertEquals(0, updateListener.events.size());
        assertEquals(1, destroyListener.events.size());
        event = destroyListener.events.removeFirst();
        assertEquals(SiteKey.portal("notification"), event.getData());
        assertEquals(EventType.NAVIGATION_DESTROYED, event.getEventName());
        assertSame(wrapper, event.getSource());

        //
        end();
    }

    public void testCacheInvalidation() throws Exception {
        SiteKey key = SiteKey.portal("wrapper_cache_invalidation");

        //
        begin();
        createNavigatation(createSite(SiteType.PORTAL, "wrapper_cache_invalidation"));
        end(true);

        //
        begin();
        wrapper.saveNavigation(new NavigationContext(key, new NavigationState(0)));
        end(true);

        //
        begin();
        NavigationContext nav = wrapper.loadNavigation(key);
        assertNotNull(nav);
        NodeContext<Node, NodeState> root = wrapper.loadNode(Node.MODEL, nav, Scope.ALL, null);
        assertNotNull(root);
        end(true);

        //
        begin();
        assertTrue(destroySite(SiteType.PORTAL, "wrapper_cache_invalidation"));
        end(true);

        //
        begin();
        assertNull(wrapper.loadNavigation(key));
        try {
            wrapper.rebaseNode(root, null, null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
        end();
    }

    public void testCachingInMultiThreading() throws InterruptedException {
        final SiteKey foo = SiteKey.portal("test_caching_in_multi_threading");
        assertNull(wrapper.loadNavigation(foo));

        //
        createSite(SiteType.PORTAL, "test_caching_in_multi_threading");
        assertTrue(isSessionModified());
        sync(true);

        //
        wrapper.saveNavigation(new NavigationContext(foo, new NavigationState(0)));

        // Start a new thread to work with navigations in parallels
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                begin();
                // Loading the foo navigation and update into the cache if any
                assertFalse(isSessionModified());
                assertNull(wrapper.loadNavigation(foo));
                end(true);
            }
        });
        t.start();
        t.join();

        // It loads directly from DB
        assertTrue(isSessionModified());
        assertNotNull(wrapper.loadNavigation(foo));

        sync(true);

        // It will load from Cache first if any
        assertFalse(isSessionModified());
        assertNotNull(wrapper.loadNavigation(foo));
    }
}
