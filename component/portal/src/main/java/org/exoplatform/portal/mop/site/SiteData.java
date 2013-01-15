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

import java.io.Serializable;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Utils;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Templatized;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class SiteData implements Serializable {

    /** Useful. */
    static final SiteData EMPTY = new SiteData();

    /** . */
    final SiteKey key;

    /** . */
    final String id;

    /** . */
    final String layoutId;

    /** . */
    final SiteState state;

    private SiteData() {
        this.key = null;
        this.id = null;
        this.state = null;
        this.layoutId = null;
    }

    SiteData(Site site) {

        //
        SiteType type = Utils.siteType(site.getObjectType());
        Templatized templatized = site.getRootNavigation().getTemplatized();
        org.gatein.mop.api.workspace.Page layout = templatized.getTemplate();

        //
        this.key = new SiteKey(type, site.getName());
        this.id = site.getObjectId();
        this.state = new SiteState(site);
        this.layoutId = layout.getRootComponent().getObjectId();
    }

    protected Object readResolve() {
        if (key == null && state == null && id == null) {
            return EMPTY;
        } else {
            return this;
        }
    }
}
