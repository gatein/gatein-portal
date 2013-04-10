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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gatein.portal.mop.customization.ContentType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PortletState implements Serializable, Iterable<Preference> {

    /** . */
//    public static final ContentType<PortletState> CONTENT_TYPE = new ContentType<PortletState>() {
//        @Override
//        public String getValue() {
//            return "application/portlet";
//        }
//    };

    /** . */
    public static final PortletState EMPTY = new PortletState();

    /** . */
    private final Map<String, Preference> entries;

    public PortletState() {
        this.entries = Collections.emptyMap();
    }

    private PortletState(Map<String, Preference> entries) {
        this.entries = entries;
    }

    public Preference getPreference(String name) {
        return entries.get(name);
    }

    @Override
    public Iterator<Preference> iterator() {
        return entries.values().iterator();
    }

    public Builder builder() {
        return new Builder(entries);
    }

    public static class Builder {

        /** . */
        private Map<String, Preference> entries;

        /** . */
        private HashMap<String, Preference> newEntries;

        public Builder(Map<String, Preference> entries) {
            this.entries = entries;
            this.newEntries = null;
        }

        public Builder put(String name, String value) {
            return put(name, value, false);
        }

        public Builder put(String name, String value, boolean readOnly) {
            return put(name, Collections.singletonList(value), readOnly);
        }

        public Builder put(String name, List<String> value) {
            return put(name, value, false);
        }

        public Builder put(String name, List<String> value, boolean readOnly) {
            if (newEntries == null) {
                newEntries = new HashMap<String, Preference>(entries);
            }
            newEntries.put(name, new Preference(name, value, readOnly));
            return this;
        }

        public PortletState build() {
            entries = newEntries;
            newEntries = null;
            return new PortletState(entries);
        }
    }
}
