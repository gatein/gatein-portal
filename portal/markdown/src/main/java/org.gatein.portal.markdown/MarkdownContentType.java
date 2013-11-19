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
package org.gatein.portal.markdown;

import org.exoplatform.portal.config.model.ApplicationType;
import org.gatein.portal.content.Content;
import org.gatein.portal.content.ContentType;
import org.staxnav.StaxNavigator;

/**
 * @author Julien Viet
 */
public class MarkdownContentType extends ContentType<Markdown> {

    /** . */
    public static MarkdownContentType INSTANCE = new MarkdownContentType();

    /** . */
    public static final org.gatein.mop.api.content.ContentType<Markdown> LEGACY_CONTENT_TYPE = new org.gatein.mop.api.content.ContentType<Markdown>("text/markdown", Markdown.class);

    /** . */
    private static final ApplicationType<Markdown> APPLICATION_TYPE = new ApplicationType<Markdown>(LEGACY_CONTENT_TYPE, "text/markdown") {};

    @Override
    public String getValue() {
        return "text/markdown";
    }

    @Override
    public ApplicationType<Markdown> getApplicationType() {
        return APPLICATION_TYPE;
    }

    @Override
    public String getTagName() {
        return "markdown";
    }

    @Override
    public Content<Markdown> readState(StaxNavigator<String> xml) {
        String text;
        if (xml.sibling("text")) {
            text = xml.getContent();
        } else {
            text = "";
        }
        return new Content<Markdown>("", new Markdown(text));
    }
}
