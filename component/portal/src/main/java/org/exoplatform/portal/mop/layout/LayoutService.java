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

package org.exoplatform.portal.mop.layout;

import org.gatein.portal.mop.hierarchy.NodeAdapter;
import org.gatein.portal.mop.hierarchy.NodeChangeListener;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface LayoutService {

    /**
     * Return the layout for the specified layout id or null if the layout does not exist
     *
     * @param model the model
     * @param layoutId the layout id
     * @param listener an optional listener
     * @param <N> the generic type
     * @return the layout
     * @throws NullPointerException if the model or layoutId argument are null
     * @throws LayoutServiceException anything that would prevent to load the layout
     */
    <N> NodeContext<N, ElementState> loadLayout(
            NodeModel<N, ElementState> model,
            String layoutId,
            NodeChangeListener<NodeContext<N, ElementState>, ElementState> listener) throws NullPointerException, LayoutServiceException;

    <N> void saveLayout(
            NodeContext<N, ElementState> context,
            NodeChangeListener<NodeContext<N, ElementState>, ElementState> listener) throws NullPointerException, LayoutServiceException;

    <L, N> void saveLayout(NodeAdapter<L, N, ElementState> adapter, N node, NodeContext<N, ElementState> context, NodeChangeListener<NodeContext<N, ElementState>, ElementState> listener);
}
