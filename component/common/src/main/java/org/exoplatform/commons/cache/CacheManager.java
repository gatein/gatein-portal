/*
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
package org.exoplatform.commons.cache;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Manages the life cycle of {@link CacheProvider} with the current thread. The class implements the
 * {@link ComponentRequestLifecycle} interface to associate the thread with a provider.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CacheManager implements ComponentRequestLifecycle
{

   /** . */
   final RepositoryService repositoryService;

   /** . */
   final CacheService cacheService;

   /** . */
   final ThreadLocal<CacheProvider> current;

   public CacheManager(RepositoryService repositoryService, CacheService cacheService) throws NullPointerException
   {
      if (repositoryService == null)
      {
         throw new NullPointerException("No null repository service accepted");
      }
      if (cacheService == null)
      {
         throw new NullPointerException("No null cache service accepted");
      }

      //
      this.repositoryService = repositoryService;
      this.cacheService = cacheService;
      this.current = new ThreadLocal<CacheProvider>();
   }

   /**
    * Returns the current provider or return null if no such provider exist.
    *
    * @return the current provider
    */
   public CacheProvider getCurrentProvider()
   {
      return current.get();
   }

   public void startRequest(ExoContainer exoContainer)
   {
      begin();
   }

   public void endRequest(ExoContainer exoContainer)
   {
      end();
   }

   /**
    * Attempt to begin a request.
    *
    * @return true if the request was begun, false if a provider was already associated
    */
   public boolean begin()
   {
      if (current.get() != null)
      {
         return false;
      }
      CacheProvider provider = new CacheProvider(this);
      current.set(provider);
      return true;
   }

   /**
    * Attempt to end a request.
    *
    * @return true if the request was stopped, false if no request was previously associated
    */
   public boolean end()
   {
      if (current.get() == null)
      {
         return false;
      }
      current.set(null);
      return true;
   }
}
