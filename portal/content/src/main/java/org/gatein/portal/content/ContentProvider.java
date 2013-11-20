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
package org.gatein.portal.content;

import java.io.Serializable;

/**
 * @author Julien Viet
 * @param <S> the content state
 */
public interface ContentProvider<S extends Serializable> {

    /**
     * Return the provider supported content type.
     *
     * @return the content type
     */
    ContentType<S> getContentType();

    /**
     * Retrieve the content of a window, null is returned when the content does not exists.
     *
     * @param id the content id
     * @return the window content
     */
    WindowContent<S> getContent(String id);

    /**
     * Returns the available contents.
     *
     * @param filter the query filter
     * @param offset the offset
     * @param limit the limit
     * @return the available contents
     */
    Iterable<ContentDescription> findContents(String filter, int offset, int limit);

}
