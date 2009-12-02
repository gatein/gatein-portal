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

import org.chromattic.api.ChromatticSession;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract implementation of the {@link org.exoplatform.commons.chromattic.SessionContext} interface.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class AbstractContext implements SessionContext
{

   /** . */
   ChromatticSession session;

   /** . */
   private Map<String, Object> attributes;

   /** The related configurator. */
   private final ChromatticLifeCycle configurator;

   public AbstractContext(ChromatticLifeCycle configurator)
   {
      this.configurator = configurator;
      this.session = null;
   }

   public final ChromatticSession getSession()
   {
      if (session == null)
      {
         session = configurator.realChromattic.openSession();
      }
      return session;
   }

   public final Object getAttachment(String name)
   {
      if (attributes != null)
      {
         return attributes.get(name);
      }
      return null;
   }

   public final void setAttachment(String name, Object attribute)
   {
      if (attribute != null)
      {
         if (attributes == null)
         {
            attributes = new HashMap<String, Object>();
         }
         attributes.put(name, attribute);
      }
      else if (attributes != null)
      {
         attributes.remove(name);
      }
   }

   public void close(boolean save)
   {
      if (session != null)
      {
         if (save)
         {
            session.save();
         }

         //
         session.close();
      }

      //
      configurator.currentContext.set(null);

      //
      configurator.onCloseSession(this);
   }
}
