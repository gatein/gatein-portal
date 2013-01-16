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

package org.gatein.portal.mop.description;

import java.io.Serializable;

import org.exoplatform.commons.utils.Safe;

/**
 * The composite state of the {@code Described} mixin.
 */
public class DescriptionState implements Serializable {

    /** . */
    private final String name;

    /** . */
    private final String description;

    public DescriptionState(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof DescriptionState) {
            DescriptionState that = (DescriptionState) obj;
            return Safe.equals(name, that.name) && Safe.equals(description, that.description);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Description[name=" + name + ",description=" + description + "]";
    }
}
