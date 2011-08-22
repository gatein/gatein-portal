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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.mop.api.workspace.ObjectType;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNavigationServiceWrapper extends AbstractTestNavigationService
{

   /** . */
   private NavigationService navigationService;

   /** . */
   private ListenerService listenerService;

   /** . */
   private POMSessionManager mgr;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      //
      PortalContainer container = getContainer();

      //
      listenerService = (ListenerService)container.getComponentInstanceOfType(ListenerService.class);
      navigationService = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
   }

   public void testNotification() throws NavigationServiceException
   {
      class ListenerImpl extends Listener<NavigationService, SiteKey>
      {

         /** . */
         private final LinkedList<Event> events = new LinkedList<Event>();

         @Override
         public void onEvent(Event event) throws Exception
         {
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
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "notification");

      // Create
      NavigationContext navigation = new NavigationContext(SiteKey.portal("notification"), new NavigationState(3));
      navigationService.saveNavigation(navigation);
      assertEquals(1, createListener.events.size());
      Event event = createListener.events.removeFirst();
      assertEquals(SiteKey.portal("notification"), event.getData());
      assertEquals(EventType.NAVIGATION_CREATED, event.getEventName());
      assertSame(navigationService, event.getSource());
      assertEquals(0, updateListener.events.size());
      assertEquals(0, destroyListener.events.size());

      //

      // Update
      navigation.setState(new NavigationState(1));
      navigationService.saveNavigation(navigation);
      assertEquals(0, createListener.events.size());
      assertEquals(1, updateListener.events.size());
      event = updateListener.events.removeFirst();
      assertEquals(SiteKey.portal("notification"), event.getData());
      assertEquals(EventType.NAVIGATION_UPDATED, event.getEventName());
      assertSame(navigationService, event.getSource());
      assertEquals(0, destroyListener.events.size());

      // Update
      navigation = navigationService.loadNavigation(SiteKey.portal("notification"));
      Node root = navigationService.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
      root.setState(new NodeState.Builder(root.getState()).label("foo").build());
      navigationService.saveNode(root.context, null);
      assertEquals(0, createListener.events.size());
      assertEquals(1, updateListener.events.size());
      event = updateListener.events.removeFirst();
      assertEquals(SiteKey.portal("notification"), event.getData());
      assertEquals(EventType.NAVIGATION_UPDATED, event.getEventName());
      assertSame(navigationService, event.getSource());
      assertEquals(0, destroyListener.events.size());

      // Destroy
      navigationService.destroyNavigation(navigation);
      assertEquals(0, createListener.events.size());
      assertEquals(0, updateListener.events.size());
      assertEquals(1, destroyListener.events.size());
      event = destroyListener.events.removeFirst();
      assertEquals(SiteKey.portal("notification"), event.getData());
      assertEquals(EventType.NAVIGATION_DESTROYED, event.getEventName());
      assertSame(navigationService, event.getSource());

      //
      end();
   }

   public void testCacheInvalidation() throws Exception
   {
      SiteKey key = SiteKey.portal("wrapper_cache_invalidation");

      //
      begin();
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "wrapper_cache_invalidation").getRootNavigation().addChild("default");
      end(true);

      //
      begin();
      navigationService.saveNavigation(new NavigationContext(key, new NavigationState(0)));
      end(true);

      //
      begin();
      NavigationContext nav = navigationService.loadNavigation(key);
      assertNotNull(nav);
      NodeContext<Node> root = navigationService.loadNode(Node.MODEL, nav, Scope.ALL, null);
      assertNotNull(root);
      end(true);

      //
      begin();
      mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "wrapper_cache_invalidation").destroy();
      end(true);

      //
      begin();
      assertNull(navigationService.loadNavigation(key));
      try
      {
         navigationService.rebaseNode(root, null, null);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
      }
      end();
   }
}
