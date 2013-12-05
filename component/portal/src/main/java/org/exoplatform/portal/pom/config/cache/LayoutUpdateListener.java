/**
 * Copyright (C) 2013 eXo Platform SAS.
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

package org.exoplatform.portal.pom.config.cache;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

public class LayoutUpdateListener extends Listener<DataStorage, Object> {

    private POMSessionManager manager;

    public LayoutUpdateListener(POMSessionManager manager) {
        this.manager = manager;
    }

    /**
     * @see org.exoplatform.services.listener.Listener#onEvent(org.exoplatform.services.listener.Event)
     */
    @Override
    public void onEvent(Event<DataStorage, Object> event) throws Exception {
        Object config = event.getData();

        if (config instanceof PortalConfig || config instanceof Container) {
            //Update a layout, it should clear cache
            manager.clearCache();
        }
    }
}
