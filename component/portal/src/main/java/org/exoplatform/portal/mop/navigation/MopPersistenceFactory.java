/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.portal.mop.navigation;

import javax.inject.Provider;

import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.portal.mop.navigation.NavigationPersistence;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MopPersistenceFactory implements Provider<MopPersistence> {

    /** . */
    final POMSessionManager manager;

    /** . */
    final DataCache cache;

    public MopPersistenceFactory(POMSessionManager manager, DataCache cache) {
        if (manager == null) {
            throw new NullPointerException("No null manager accepted");
        }

        //
        this.manager = manager;
        this.cache = cache;
    }

    public MopPersistenceFactory(POMSessionManager manager) {
        this(manager, new SimpleDataCache());
    }

    @Override
    public MopPersistence get() {
        return new MopPersistence(manager.getSession(), cache);
    }
}
