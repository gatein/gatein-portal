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

package org.gatein.portal.mop.customization.portlet;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Preference implements Serializable {

    /** . */
    final String name;

    /** . */
    final List<String> values;

    /** . */
    final boolean readOnly;

    public Preference(String name, String value, boolean readOnly) {
        this.name = name;
        this.values = Collections.singletonList(value);
        this.readOnly = readOnly;
    }

    public Preference(String name, List<String> value, boolean readOnly) {
        this.name = name;
        this.values = value;
        this.readOnly = readOnly;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return values.size() > 0 ? values.get(0) : null;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
