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

import org.chromattic.api.ChromatticSession;

/**
 * The contract for integration between client of a chromattic session and the session management system. Attachments are useful
 * to associated an arbitrary payload with the session context.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface SessionContext {

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

    /**
     * Registers a synchronization listener.
     *
     * @param listener the listener
     */
    void addSynchronizationListener(SynchronizationListener listener);
}
