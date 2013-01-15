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

import javax.inject.Provider;

import org.exoplatform.portal.mop.hierarchy.GenericScope;
import org.exoplatform.portal.mop.hierarchy.NodeChangeListener;
import org.exoplatform.portal.mop.hierarchy.NodeContext;
import org.exoplatform.portal.mop.hierarchy.NodeManager;
import org.exoplatform.portal.mop.hierarchy.NodeModel;
import org.exoplatform.portal.mop.hierarchy.Scope;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.ui.UIContainer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LayoutServiceImpl implements LayoutService {

    /** . */
    private final Scope<ElementState> ALL = GenericScope.treeShape(-1);

    /** . */
    private final POMSessionManager manager;

    /** . */
    private final NodeManager<ElementState> nodeManager;

    public LayoutServiceImpl(final POMSessionManager manager) {

        //
        Provider<LayoutPersistence> persistenceProvider = new Provider<LayoutPersistence>() {
            @Override
            public LayoutPersistence get() {
                return new LayoutPersistence(manager.getSession());
            }
        };

        //
        this.manager = manager;
        this.nodeManager = new NodeManager<ElementState>(persistenceProvider);
    }

    @Override
    public <N> NodeContext<N, ElementState> loadLayout(NodeModel<N, ElementState> model, String layoutId, NodeChangeListener<NodeContext<N, ElementState>, ElementState> listener) {
        if (model == null) {
            throw new NullPointerException("No nullmodel accepted");
        }
        if (layoutId == null) {
            throw new NullPointerException("No null layout id accepted");
        }

        //
        POMSession session = manager.getSession();
        UIContainer root = session.findObjectById(ObjectType.CONTAINER, layoutId);

        //
        if (root == null) {
            return null;
        } else {
            return nodeManager.loadNode(model, root.getObjectId(), ALL, listener);
        }
    }

    @Override
    public <N> void saveLayout(NodeContext<N, ElementState> context, NodeChangeListener<NodeContext<N, ElementState>, ElementState> listener) throws NullPointerException {
        nodeManager.saveNode(context, listener);
    }
}
