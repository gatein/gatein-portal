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

package org.exoplatform.portal.mop.site;

import org.gatein.portal.mop.site.SiteKey;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SiteContext {

    /** . */
    final SiteKey key;

    /** The new state if any. */
    SiteState state;

    /** A data snapshot. */
    SiteData data;

    SiteContext(SiteData data) {
        this.key = data.key;
        this.state = null;
        this.data = data;
    }

    public SiteContext(SiteKey key, SiteState state) {
        this.key = key;
        this.state = state;
        this.data = null;
    }

    /**
     * Returns the site id or null.
     *
     * @return the site id
     */
    public String getId() {
        return data != null ? data.id : null;
    }

    /**
     * Returns the navigation key.
     *
     * @return the navigation key
     */
    public SiteKey getKey() {
        return key;
    }

    /**
     * Returns the layout id or null if the Site is not persistent.
     *
     * @return the layout id
     */
    public String getLayoutId() {
        return data != null ? data.layoutId : null;
    }

    /**
     * Returns the navigation state.
     *
     * @return the navigation state
     */
    public SiteState getState() {
        if (state != null) {
            return state;
        } else if (data != null) {
            return data.state;
        } else {
            return null;
        }
    }

    /**
     * Updates the site state the behavior is not the same wether or not the Site is persistent:
     * <ul>
     * <li>When the site is persistent, any state is allowed:
     * <li>A non null state overrides the current persistent state.</li>
     * <li>The null state means to reset the state to the persistent state.</li>
     * </li>
     * <li>When the site is transient, only a non null state is allowed as it will be used for creation purpose.</li>
     * </ul>
     *
     * @param state the new state
     * @throws IllegalStateException when the state is cleared and the navigation is not persistent
     */
    public void setState(SiteState state) throws IllegalStateException {
        if (data == null && state == null) {
            throw new IllegalStateException("Cannot clear state on a transient Site");
        }
        this.state = state;
    }
}
