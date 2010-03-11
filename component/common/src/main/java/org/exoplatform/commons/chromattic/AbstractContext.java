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

import org.chromattic.api.ChromatticSession;
import org.exoplatform.services.jcr.core.ManageableRepository;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * An abstract implementation of the {@link org.exoplatform.commons.chromattic.SessionContext} interface. The context
 * owns the chromattic session.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class AbstractContext implements SessionContext
{

   /** . */
   ChromatticSession session;

   /** . */
   private Map<String, Object> attributes;

   /** The related life cycle. */
   final ChromatticLifeCycle lifeCycle;

   /** . */
   private HashSet<SynchronizationListener> listeners;

   AbstractContext(ChromatticLifeCycle lifeCycle)
   {
      this.lifeCycle = lifeCycle;
      this.session = null;
      this.listeners = null;
   }

   public final ChromatticSession getSession()
   {
      if (session == null)
      {
         session = lifeCycle.realChromattic.openSession();
      }
      return session;
   }

   public final Object getAttachment(String name)
   {
      if (attributes != null)
      {
         return attributes.get(name);
      }
      return null;
   }

   public final void setAttachment(String name, Object attribute)
   {
      if (attribute != null)
      {
         if (attributes == null)
         {
            attributes = new HashMap<String, Object>();
         }
         attributes.put(name, attribute);
      }
      else if (attributes != null)
      {
         attributes.remove(name);
      }
   }

   public abstract Session doLogin() throws RepositoryException;

   /**
    * Open and returns a session. Should be used by subclasses.
    *
    * @return a session
    * @throws RepositoryException any repository exception
    */
   protected final Session openSession() throws RepositoryException
   {
      ManageableRepository repo = lifeCycle.manager.repositoryService.getCurrentRepository();
      return repo.getSystemSession(lifeCycle.getWorkspaceName());
   }

   public void close(boolean save)
   {
      if (listeners != null)
      {
         for (SynchronizationListener listener : listeners)
         {
            try
            {
               listener.beforeSynchronization();
            }
            catch (Exception e)
            {
               // to log
            }
         }
      }

      //
      if (session != null)
      {
         if (save)
         {
            session.save();
         }

         //
         session.close();
      }

      //
      if (listeners != null)
      {
         SynchronizationStatus status = save ? SynchronizationStatus.SAVED : SynchronizationStatus.DISCARDED;
         for (SynchronizationListener listener : listeners)
         {
            try
            {
               listener.afterSynchronization(status);
            }
            catch (Exception e)
            {
               // to log
            }
         }
      }

      //
      lifeCycle.currentContext.set(null);

      //
      lifeCycle.onCloseSession(this);
   }

   public final void addSynchronizationListener(SynchronizationListener listener)
   {
      if (listener == null)
      {
         throw new NullPointerException();
      }
      if (listeners == null)
      {
         listeners = new HashSet<SynchronizationListener>();
      }
      listeners.add(listener);
   }
}
