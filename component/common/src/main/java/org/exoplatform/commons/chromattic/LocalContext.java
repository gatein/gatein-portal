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

import javax.jcr.Session;

/**
 * A local context that is managed by a thread local owned by a chromattic life cycle instance.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LocalContext extends AbstractContext implements LoginContext
{

   /** The related JCR session. */
   Session jcrSession;

   public LocalContext(ChromatticLifeCycle configurator)
   {
      super(configurator);
   }

   public void loggedIn(Session session)
   {
      this.jcrSession = session;
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
         jcrSession.logout();
      }
   }
}
