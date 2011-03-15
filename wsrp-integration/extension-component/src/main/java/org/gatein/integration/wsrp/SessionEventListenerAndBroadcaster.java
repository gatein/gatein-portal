/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.integration.wsrp;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.gatein.wsrp.api.session.SessionEvent;
import org.gatein.wsrp.api.session.SessionEventBroadcaster;
import org.gatein.wsrp.api.session.SessionEventListener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class SessionEventListenerAndBroadcaster extends Listener<PortalContainer, HttpSessionEvent> implements SessionEventBroadcaster
{
   private Map<String, SessionEventListener> listeners = new ConcurrentHashMap<String, SessionEventListener>();
   private static final String SESSION_CREATED = "org.exoplatform.web.GenericHttpListener.sessionCreated";
   private static final String SESSION_DESTROYED = "org.exoplatform.web.GenericHttpListener.sessionDestroyed";

   public void registerListener(String listenerId, SessionEventListener listener)
   {
      listeners.put(listenerId, listener);
   }

   public void unregisterListener(String listenerId)
   {
      listeners.remove(listenerId);
   }

   public void notifyListenersOf(SessionEvent event)
   {
      for (SessionEventListener listener : listeners.values())
      {
         listener.onSessionEvent(event);
      }
   }

   @Override
   public void onEvent(Event<PortalContainer, HttpSessionEvent> event) throws Exception
   {
      String eventName = event.getEventName();
      SessionEvent.SessionEventType eventType;
      if (SESSION_CREATED.equals(eventName))
      {
         eventType = SessionEvent.SessionEventType.SESSION_CREATED;
      }
      else if (SESSION_DESTROYED.equals(eventName))
      {
         eventType = SessionEvent.SessionEventType.SESSION_DESTROYED;
      }
      else
      {
         // do nothing
         return;
      }

      notifyListenersOf(new SimpleSessionEvent(eventType, event.getData().getSession()));
   }

   private static class SimpleSessionEvent implements SessionEvent
   {
      private SessionEventType eventType;
      private HttpSession session;

      private SimpleSessionEvent(SessionEventType eventType, HttpSession session)
      {
         this.eventType = eventType;
         this.session = session;
      }

      public SessionEventType getType()
      {
         return eventType;
      }

      public HttpSession getSession()
      {
         return session;
      }
   }
}
