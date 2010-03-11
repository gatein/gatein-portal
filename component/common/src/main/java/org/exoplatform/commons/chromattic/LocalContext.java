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

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A local context that is managed by a thread local owned by a chromattic life cycle instance.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class LocalContext extends AbstractContext 
{

   /** The related JCR session. */
   private Session jcrSession;

   public LocalContext(ChromatticLifeCycle configurator)
   {
      super(configurator);
   }

   public Session doLogin() throws RepositoryException
   {
      if (jcrSession != null)
      {
         throw new IllegalStateException("Already logged in");
      }
      jcrSession = openSession();
      return jcrSession;
   }

   @Override
   public void close(boolean save)
   {
      try
      {
         super.close(save);
      }
      finally
      {
         if (jcrSession != null)
         {
            jcrSession.logout();
         }
      }
   }
}
