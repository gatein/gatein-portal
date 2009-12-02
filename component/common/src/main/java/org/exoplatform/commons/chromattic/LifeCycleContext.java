/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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
