/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.mop;

/**
 * Group various event types.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class EventType {

    private EventType() {
    }

    /** . */
    public static final String NAVIGATION_CREATED = "org.exoplatform.portal.mop.navigation.navigation_created";

    /** . */
    public static final String NAVIGATION_DESTROYED = "org.exoplatform.portal.mop.navigation.navigation_destroyed";

    /** . */
    public static final String NAVIGATION_UPDATED = "org.exoplatform.portal.mop.navigation.navigation_updated";

    /** . */
    public static final String PAGE_CREATED = "org.exoplatform.portal.mop.page.page_created";

    /** . */
    public static final String PAGE_DESTROYED = "org.exoplatform.portal.mop.page.page_destroyed";

    /** . */
    public static final String PAGE_UPDATED = "org.exoplatform.portal.mop.page.page_updated";

}
