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

import org.exoplatform.portal.tree.diff.HierarchyAdapter;

/**
 * Adapter for the update operation.
 *
 * @param <N> the node generic type
 */
interface TreeUpdateAdapter<N> extends HierarchyAdapter<String[], N, String>
{

   /**
    * Returns the persistent data associated with the node or null if such data does not exist.
    *
    * @param node the node
    * @return the node data
    */
   NodeData getData(N node);

   /**
    * Returns the transient state associated with the node.
    *
    * @param node the node
    * @return the transient state
    */
   NodeState getState(N node);

   /**
    * Returns the transient name associated with the node.
    *
    * @param node the node
    * @return the transient name
    */
   String getName(N node);

}
