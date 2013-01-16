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

package org.gatein.portal.mop.site;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SiteData implements Serializable {

    /** Useful. */
    public static final SiteData EMPTY = new SiteData();

    /** . */
    public final SiteKey key;

    /** . */
    public final String id;

    /** . */
    public final String layoutId;

    /** . */
    public final SiteState state;

    private SiteData() {
        this.key = null;
        this.id = null;
        this.state = null;
        this.layoutId = null;
    }

    public SiteData(SiteKey key, String id, String layoutId, SiteState state) {
        this.key = key;
        this.id = id;
        this.layoutId = layoutId;
        this.state = state;
    }

    protected Object readResolve() {
        if (key == null && state == null && id == null) {
            return EMPTY;
        } else {
            return this;
        }
    }
}
