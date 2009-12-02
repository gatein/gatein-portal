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

import org.exoplatform.services.jcr.core.ManageableRepository;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LifeCycleContext
{

   /** . */
   static final ThreadLocal<LifeCycleContext> bootContext = new ThreadLocal<LifeCycleContext>();

   /** . */
   private final String workspaceName;

   /** . */
   private final ChromatticManager manager;

   /** . */
   private final ChromatticLifeCycle configurator;

   public LifeCycleContext(ChromatticLifeCycle configurator, ChromatticManager manager, String repositoryName)
   {
      this.configurator = configurator;
      this.manager = manager;
      this.workspaceName = repositoryName;
   }

   public Session doLogin() throws RepositoryException
   {
      LoginContext loginContext = configurator.getLoginContext();

      //
      if (loginContext == null)
      {
         throw new IllegalStateException();
      }

      //
      ManageableRepository repo = manager.repositoryService.getCurrentRepository();

      //
      Session session = repo.getSystemSession(workspaceName);

      //
      loginContext.loggedIn(session);

      //
      return session;
   }
}
