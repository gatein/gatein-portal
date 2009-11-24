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

package org.exoplatform.portal.pom.config;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.gatein.mop.core.api.MOPService;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class POMSessionManager
{

   /** . */
   private static final ThreadLocal<POMSession> current = new ThreadLocal<POMSession>();

   /** . */
   final RepositoryService repositoryService;

   /** . */
   private MOPService pomService;

   /** . */
   final String repositoryName = "repository";

   /** . */
   final String workspaceName = "portal-system";

   /** . */
   final ExoCache<Serializable, Object> cache;

   public POMSessionManager(CacheService cacheService, RegistryService service) throws Exception
   {
      RepositoryService repositoryService = service.getRepositoryService();

      //
      this.cache = cacheService.getCacheInstance(POMSessionManager.class.getSimpleName());
      this.repositoryService = repositoryService;
      this.pomService = null;
   }
   
   public void clearCache()
   {
      cache.clearCache();
   }

   public Session login() throws RepositoryException
   {
      ManageableRepository repo = repositoryService.getCurrentRepository();
      return repo.login();
   }

   public Session login(String workspace) throws RepositoryException
   {
      Repository repo = repositoryService.getCurrentRepository();
      return repo.login(workspace);
   }

   public Session login(Credentials credentials, String workspace) throws RepositoryException
   {
      Repository repo = repositoryService.getCurrentRepository();
      return repo.login(credentials, workspace);
   }

   public Session login(Credentials credentials) throws RepositoryException
   {
      Repository repo = repositoryService.getCurrentRepository();
      return repo.login(credentials);
   }

   /*
    * todo : use better than the synchronized block  
    */
   public synchronized MOPService getPOMService()
   {
      if (pomService == null)
      {
         PortalMOPService mopService = new PortalMOPService();

         //
         try
         {
            mopService.start();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         //
         this.pomService = mopService;
      }
      return pomService;
   }

   /**
    * <p>Returns the session currently associated with the current thread of execution.</p>
    *
    * @return the current session
    */
   public static POMSession getSession()
   {
      return current.get();
   }

   /**
    * <p>Open and returns a session to the model. When the current thread is already associated with a previously opened
    * session the method will throw an <tt>IllegalStateException</tt>.</p>
    *
    * @return a session to the model.
    */
   public POMSession openSession()
   {
      POMSession session = current.get();
      if (session == null)
      {
         session = new POMSession(this);
         current.set(session);

         //
         // A bit ugly but we will improve that later
         ChromatticSession csession = session.getSession();
         csession.addEventListener(new Injector(session));
      }
      else
      {
         throw new IllegalStateException("A session is already opened.");
      }
      return session;
   }

   /**
    * <p>Closes the current session and discard the changes done during the session.</p>
    *
    * @return a boolean indicating if the session was closed
    * @see #closeSession(boolean)
    */
   public boolean closeSession()
   {
      return closeSession(false);
   }

   /**
    * <p>Closes the current session and optionally saves its content. If no session is associated then this method has
    * no effects and returns false.</p>
    *
    * @param save if the session must be saved
    * @return a boolean indicating if the session was closed
    */
   public boolean closeSession(boolean save)
   {
      POMSession session = current.get();
      if (session == null)
      {
         // Should warn
         return false;
      }
      else
      {
         current.set(null);
         try
         {
            if (save)
            {
               session.save();
            }
         }
         finally
         {
            session.close();
         }
         return true;
      }
   }

   /**
    * <p>Execute the task with a session. The method attempts first to get a current session and if no such session is
    * found then a session will be created for the scope of the method.</p>
    *
    * @param task the task to execute
    * @throws Exception any exception thrown by the task
    */
   public void execute(POMTask task) throws Exception
   {
      POMSession session = getSession();
      if (session == null)
      {
         session = openSession();
         try
         {
            session.execute(task);
         }
         finally
         {
            closeSession(true);
         }
      }
      else
      {
         session.execute(task);
      }
   }
}
