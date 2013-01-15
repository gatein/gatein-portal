/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.commons.cache;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class InvalidationBridge implements EventListener {

    /** . */
    private ObservationManager mgr;

    public InvalidationBridge() {
    }

    public void start(Session session) throws RepositoryException {
        mgr = session.getWorkspace().getObservationManager();

        //
        mgr.addEventListener(this, Event.NODE_REMOVED, "/", true, null, new String[]{"mop:portalsites", "mop:groupsites", "mop:usersites"}, false);
    }

    public void stop() {
        if (mgr != null) {
            try {
                mgr.removeEventListener(this);
            } catch (RepositoryException ignore) {
            }
        }
    }
}
