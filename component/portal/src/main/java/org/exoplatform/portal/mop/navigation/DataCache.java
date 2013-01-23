/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.navigation;

import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.navigation.NavigationData;
import org.gatein.portal.mop.navigation.NodeState;

/**
 * todo : see if it makes sense to use a bloom filter for not found site black list
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class DataCache {

    /** . */
    MopStore persistence;

    protected abstract void removeNodes(Iterable<String> keys);

    protected abstract NodeData<NodeState> getNode(String key);

    protected abstract NavigationData getNavigation(SiteKey key);

    protected abstract void removeNavigation(SiteKey key);

    protected abstract void clear();

}
