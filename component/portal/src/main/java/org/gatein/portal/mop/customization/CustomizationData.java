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

package org.gatein.portal.mop.customization;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CustomizationData<S extends Serializable> implements Serializable {

    /** Useful (generic is safe since state == null). */
    public static final CustomizationData EMPTY = new CustomizationData();

    /** . */
    public final String id;

    /** . */
    public final ContentType<S> contentType;

    /** . */
    public final String contentId;

    /** . */
    public final S state;

    private CustomizationData() {
        this.id = null;
        this.contentType = null;
        this.contentId = null;
        this.state = null;
    }

    public CustomizationData(String id, ContentType<S> contentType, String contentId, S state) {
        this.id = id;
        this.contentType = contentType;
        this.contentId = contentId;
        this.state = state;
    }

    protected Object readResolve() {
        if (state == null && id == null) {
            return EMPTY;
        } else {
            return this;
        }
    }
}
