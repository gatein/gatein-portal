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
package org.gatein.portal.web.content.simple;

import org.exoplatform.portal.config.model.ApplicationType;
import org.gatein.portal.content.Content;
import org.gatein.portal.content.ContentType;
import org.staxnav.StaxNavigator;

/**
 * @author Julien Viet
 */
public class SimpleContentType extends ContentType<SimpleState> {

    /** . */
    public static final org.gatein.mop.api.content.ContentType<SimpleState> LEGACY_CONTENT_TYPE = new org.gatein.mop.api.content.ContentType<SimpleState>("simple/content", SimpleState.class);

    /** . */
    private static final ApplicationType<SimpleState> APPLICATION_TYPE = new ApplicationType<SimpleState>(LEGACY_CONTENT_TYPE, "simple/content") {};

    @Override
    public String getValue() {
        return "simple/content";
    }

    @Override
    public ApplicationType<SimpleState> getApplicationType() {
        return APPLICATION_TYPE;
    }

    @Override
    public String getTagName() {
        return "simple";
    }

    @Override
    public Content<SimpleState> readState(StaxNavigator<String> xml) {
        validate(xml.sibling("id"));
        String path = xml.getContent();
        return new Content<SimpleState>(path, null);
    }
}
