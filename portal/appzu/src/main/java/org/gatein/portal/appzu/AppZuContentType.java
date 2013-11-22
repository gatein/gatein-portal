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

import org.exoplatform.portal.config.model.ApplicationType;
import org.gatein.portal.content.Content;
import org.gatein.portal.content.ContentType;
import org.staxnav.StaxNavigator;

/**
 * @author Julien Viet
 */
public class AppZuContentType extends ContentType<App> {

    /** . */
    public static final org.gatein.mop.api.content.ContentType<App> LEGACY_CONTENT_TYPE = new org.gatein.mop.api.content.ContentType<App>("application/juzu", App.class);

    /** . */
    private static final ApplicationType<App> APPLICATION_TYPE = new ApplicationType<App>(LEGACY_CONTENT_TYPE, "application/juzu") {};

    @Override
    public String getValue() {
        return "application/juzu";
    }

    @Override
    public ApplicationType<App> getApplicationType() {
        return APPLICATION_TYPE;
    }

    @Override
    public String getTagName() {
        // We don't support that for now
        return null;
    }

    @Override
    public Content<App> readState(StaxNavigator<String> xml) {
        throw new UnsupportedOperationException("Not supported");
    }
}
