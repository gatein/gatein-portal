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

package org.gatein.portal.mop;

import java.io.Serializable;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
public abstract class Property implements Serializable {

    public abstract String getName();

    public static class Qualified<T> extends Property {

        /** . */
        final PropertyType<T> type;

        /** . */
        final T value;

        Qualified(PropertyType<T> type, T value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String getName() {
            return type.getName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Qualified<?>) {
                Qualified<?> that = (Qualified<?>) obj;
                return type.equals(that.type) && value.equals(that.value);
            } else {
                return false;
            }
        }
    }

    public static class Raw extends Property {

        /** . */
        final String name;

        /** . */
        final String value;

        Raw(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Raw) {
                Raw that = (Raw) obj;
                return name.equals(that.name) && value.equals(that.value);
            } else {
                return false;
            }
        }
    }
}
