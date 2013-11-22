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
package org.gatein.portal.appzu;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import juzu.impl.common.Name;

/**
 * @author Julien Viet
 */
public class ApplicationRepository {

    /** . */
    static final ApplicationRepository instance = new ApplicationRepository();

    /** . */
    private final ConcurrentHashMap<Name, AppContent> applications = new ConcurrentHashMap<Name, AppContent>();

    public AppContent getApplication(Name name) {
        return applications.get(name);
    }

    public Iterable<AppContent> getApplications() {
        return applications.values();
    }

    /**
     * Add a new application
     *
     * @param name the application name
     * @return the app
     */
    public AppContent addApplication(Name name) throws IOException {
        AppContent app = new AppContent(name);
        AppContent phantom = applications.putIfAbsent(name, app);
        if (phantom != null) {
            app = phantom;
        }
        return app;
    }
}
