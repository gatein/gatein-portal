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

package org.gatein.portal.mop.site;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum SiteType {

    PORTAL, GROUP, USER;

    /**
     * Returns the site type for the specified site type name. If no type matches the
     * site name then null is returned.
     *
     * @param name the site name
     * @return the site type
     */
    public static SiteType forName(String name) {
        if (PORTAL.name.equals(name)) {
            return PORTAL;
        } else if (GROUP.name.equals(name)) {
            return GROUP;
        } else if (USER.name.equals(name)) {
            return USER;
        } else {
            return null;
        }
    }

    /** . */
    final String name;

    SiteType() {
        this.name = name().toLowerCase();
    }

    /**
     * Returns the name of the site type, which is the enum string in lower case.
     *
     * @return the type name
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a site key with the current type and the provided <code>name</code> argument.
     *
     * @param name the site name
     * @return the site key
     * @throws NullPointerException when the <code>name</code> argument is null
     */
    public SiteKey key(String name) throws NullPointerException {
        return new SiteKey(this, name);
    }
}
