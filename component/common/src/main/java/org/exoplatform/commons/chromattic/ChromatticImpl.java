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

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticSession;

import javax.jcr.Credentials;

/**
 * <p>A specific implementation of the {@link org.chromattic.api.Chromattic} interface that delegates
 * the obtention of a session to the managed system.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class ChromatticImpl implements Chromattic
{

   /** . */
   private final ChromatticLifeCycle configurator;

   public ChromatticImpl(ChromatticLifeCycle configurator)
   {
      this.configurator = configurator;
   }

   public ChromatticSession openSession()
   {
      SessionContext sessionContext = configurator.getContext(false);

      //
      if (sessionContext == null)
      {
         sessionContext = configurator.openSynchronizedContext();
      }

      //
      return sessionContext.getSession();
   }

   public ChromatticSession openSession(String workspace)
   {
      throw new UnsupportedOperationException();
   }

   public ChromatticSession openSession(Credentials credentials, String workspace)
   {
      throw new UnsupportedOperationException();
   }

   public ChromatticSession openSession(Credentials credentials)
   {
      throw new UnsupportedOperationException();
   }
}
