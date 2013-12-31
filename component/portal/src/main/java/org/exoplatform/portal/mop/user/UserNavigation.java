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

package org.exoplatform.portal.mop.user;

import java.util.ResourceBundle;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.gatein.common.util.EmptyResourceBundle;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserNavigation {

    /** . */
    final UserPortalImpl portal;

    /** . */
    final NavigationContext navigation;

    /** . */
    private final boolean modifiable;

    UserNavigation(UserPortalImpl portal, NavigationContext navigation, boolean modifiable) {
        if (navigation == null) {
            throw new NullPointerException();
        }
        if (navigation.getState() == null) {
            throw new IllegalArgumentException("No state for navigation " + navigation.getKey());
        }

        //
        this.portal = portal;
        this.navigation = navigation;
        this.modifiable = modifiable;
    }

    public ResourceBundle getBundle() {
        ResourceBundle bundle = portal.context.getBundle(this);
        if (bundle == null) {
            bundle = EmptyResourceBundle.INSTANCE;
        }
        return bundle;
    }

    public SiteKey getKey() {
        return navigation.getKey();
    }

    public int getPriority() {
        Integer priority = navigation.getState().getPriority();
        return priority != null ? priority : 1;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    @Override
    public String toString() {
        return "UserNavigation[key=" + navigation.getKey() + "]";
    }
}
