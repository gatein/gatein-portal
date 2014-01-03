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
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.Safe;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SiteState implements Serializable {

    /** . */
    final String locale;

    /** . */
    final String label;

    /** . */
    final String description;

    /** . */
    final List<String> accessPermissions;

    /** . */
    final List<String> editPermissions;

    /** . */
    final Map<String, String> properties;

    /** . */
    final String skin;

    public SiteState(
            String locale,
            String label,
            String description,
            List<String> accessPermissions,
            List<String> editPermissions,
            Map<String, String> properties,
            String skin) {

        //
        this.locale = locale;
        this.label = label;
        this.description = description;
        this.accessPermissions = accessPermissions;
        this.editPermissions = editPermissions;
        this.properties = properties;
        this.skin = skin;
    }

    public String getLocale() {
        return locale;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAccessPermissions() {
        return accessPermissions;
    }

    public List<String> getEditPermissions() {
        return editPermissions;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getSkin() {
        return skin;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SiteState) {
            SiteState that = (SiteState) obj;
            return Safe.equals(locale, that.locale) &&
                    Safe.equals(label, that.label) &&
                    Safe.equals(description, that.description) &&
                    Safe.equals(accessPermissions, that.accessPermissions) &&
                    Safe.equals(editPermissions, that.editPermissions) &&
                    Safe.equals(properties, that.properties) &&
                    Safe.equals(skin, that.skin);
        }
        return false;
    }

    @Override
    public String toString() {
        return "SiteState[locale=" + locale + ",label=" + label + ",description=" + description +
                ",accessPermissions=" + accessPermissions + ",editPermission=" + editPermissions +
                ",properties=" + properties + ",skin=" + skin + "]";
    }
}
