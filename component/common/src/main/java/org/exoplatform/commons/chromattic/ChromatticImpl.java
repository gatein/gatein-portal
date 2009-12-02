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

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticSession;
import org.chromattic.api.SessionTask;

import javax.jcr.Credentials;

/**
 * <p>A specific implementation of the {@link org.chromattic.api.Chromattic} interface that delegates
 * the obtention of a session to the managed system.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ChromatticImpl implements Chromattic
{

   /** . */
   private final ChromatticLifeCycle configurator;

   public ChromatticImpl(ChromatticLifeCycle configurator)
   {
      this.configurator = configurator;
   }

   public ChromatticSession openSession()
   {
      SessionContext sessionContext = configurator.getSessionContext();

      //
      if (sessionContext == null)
      {
         sessionContext = configurator.openGlobalContext();
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

   public void execute(SessionTask task) throws Throwable
   {
      throw new UnsupportedOperationException();
   }

   public void stop()
   {
      throw new UnsupportedOperationException();
   }
}
