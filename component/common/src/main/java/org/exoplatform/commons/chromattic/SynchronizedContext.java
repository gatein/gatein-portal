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
 * The synchronized context is associated with a {@link org.exoplatform.commons.chromattic.Synchronization} object.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class SynchronizedContext extends AbstractContext
{

   /** The related synchronization when it is not null. */
   final Synchronization synchronization;

   public SynchronizedContext(ChromatticLifeCycle configurator, Synchronization synchronization)
   {
      super(configurator);

      //
      this.synchronization = synchronization;
   }

   public Session doLogin() throws RepositoryException
   {
      return synchronization.doLogin(this);
   }
}
