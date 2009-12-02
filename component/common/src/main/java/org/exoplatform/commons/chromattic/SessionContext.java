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

/**
 * The contract for integration between client of a chromattic session and the session management system.
 * Attachments are useful to associated an arbitrary payload with the session context.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface SessionContext
{

   /**
    * Returns the managed chromattic session.
    *
    * @return the session
    */
   ChromatticSession getSession();

   /**
    * Returns an attachment of this context.
    *
    * @param name the attachment name
    * @return the attached object
    */
   Object getAttachment(String name);

   /**
    * Sets an attachment on this context.
    *
    * @param name the attachment name
    * @param payload the attachment payload
    */
   void setAttachment(String name, Object payload);
}
