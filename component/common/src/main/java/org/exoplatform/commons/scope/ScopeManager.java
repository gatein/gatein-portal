/**
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.commons.scope;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * <p></p>The scope manager manages the life cycle of a scope value (which means a mere string) associated with the current
 * thread aimed to scope state. The scope manager takes it values from the current repository service name when
 * it is associated with a request on a container containing the repository service.</p>
 *
 * <p>The manager implements the {@link ComponentRequestLifecycle}</p> interface to be aware of the request life cycle.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScopeManager implements ComponentRequestLifecycle
{

   /** The scope thread local. */
   private static final ThreadLocal<String> currentScope = new ThreadLocal<String>();

   /** . */
   private static final Logger log = LoggerFactory.getLogger(ScopeManager.class);

   /**
    * Returns the current scope value or null if no value is associated with the current thread.
    *
    * @return the current scope
    */
   public static String getCurrentScope()
   {
      return currentScope.get();
   }

   public void startRequest(ExoContainer container)
   {
      if (currentScope.get() != null)
      {
         throw new IllegalStateException("Detected scope reentrancy " + currentScope.get());
      }

      //
      RepositoryService repositoryService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);

      //
      String scope = null;
      if (repositoryService != null)
      {
         try
         {
            ManageableRepository currentRepository = repositoryService.getCurrentRepository();
            if (currentRepository != null)
            {
               scope = currentRepository.getConfiguration().getName();
            }
         }
         catch (RepositoryException e)
         {
            log.error("Could not obtain scope value from repository", e);
         }
      }

      //
      if (scope == null)
      {
         scope = "";
      }

      //
      currentScope.set(scope);
      log.debug("Starting scope request \"" + scope + "\"");
   }

   public void endRequest(ExoContainer container)
   {
      if (currentScope.get() == null)
      {
         throw new IllegalStateException("Detected unscoped unscoping ");
      }

      //
      currentScope.set(null);
   }
}
