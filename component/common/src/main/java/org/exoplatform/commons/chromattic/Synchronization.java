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
package org.exoplatform.commons.chromattic;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * A global notion of synchronization for all chromattic sessions and all JCR sessions.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Synchronization implements LoginContext
{

   /** The sessions mapped by workspace name. */
   private final Map<String, Session> repositorySessions = new HashMap<String, Session>();
   
   /** . */
   private final Map<String, GlobalContext> contexts = new HashMap<String, GlobalContext>();

   /** . */
   private boolean save = true;

   /**
    * Returns a specified global context by its name.
    *
    * @param name the global context name
    * @return the global context or null if no such context exists
    */
   public GlobalContext getContext(String name)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      return contexts.get(name);
   }

   /**
    * Opens a global context related to this synchronization object.
    *
    * @param lifeCycle the life cycle for the session
    * @return the global context related to life cycle
    * @throws IllegalStateException if a context is already created for the specified life cycle
    */
   public GlobalContext openContext(ChromatticLifeCycle lifeCycle) throws IllegalStateException
   {
      if (lifeCycle == null)
      {
         throw new NullPointerException();
      }
      String name = lifeCycle.getName();
      GlobalContext context = contexts.get(name);
      if (context != null)
      {
         throw new IllegalStateException();
      }
      context = new GlobalContext(lifeCycle, this);
      contexts.put(name, context);
      lifeCycle.onOpenSession(context);
      return context;
   }

   public void loggedIn(Session session)
   {
      repositorySessions.put(session.getWorkspace().getName(), session);
   }

   public void close(boolean save)
   {
      // First save all global contexts (sessions)
      for (GlobalContext context : contexts.values())
      {
         context.close(save);
      }

      // Now close all JCR sessions
      for (Session session : repositorySessions.values())
      {
         session.logout();
      }
   }

   public boolean isSave()
   {
      return save;
   }

   public void setSave(boolean save)
   {
      this.save = save;
   }
}
