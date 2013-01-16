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

package org.exoplatform.portal.mop.hierarchy;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Provider;

import org.exoplatform.portal.mop.navigation.NavigationError;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NodeManager<S extends Serializable> {

    /** . */
    private final Provider<? extends NodePersistence<S>> persistenceProvider;

    public NodeManager(Provider<? extends NodePersistence<S>> persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    public <N> NodeContext<N, S> loadNode(
            NodeModel<N, S> model,
            String nodeId,
            Scope<S> scope,
            NodeChangeListener<NodeContext<N, S>, S> listener) {
        NodePersistence<S> persistence = persistenceProvider.get();
        try {
            NodeData<S> data = persistence.getNode(nodeId);
            if (data != null) {
                NodeContext<N, S> context = new NodeContext<N, S>(model, data);
                updateNode(context, scope, listener);
                return context;
            } else {
                return null;
            }
        } finally {
            persistence.close();
        }
    }

    public <L, N> void diff(NodeAdapter<L, N, S> adapter, N node, NodeContext<N, S> context) {
        TreeDiff<L, N, S> diff = new TreeDiff<L, N, S>(node, context, adapter);
        diff.perform();
    }

    public <N> void updateNode(
            NodeContext<N, S> root,
            Scope<S> scope,
            NodeChangeListener<NodeContext<N, S>, S> listener)
            throws NullPointerException, IllegalArgumentException, NavigationServiceException {
        Scope.Visitor<S> visitor;
        if (scope != null) {
            visitor = new FederatingVisitor<N, S>(root.tree, root, scope);
        } else {
            visitor = root.tree;
        }
        updateTree(root.tree, visitor, listener);
    }

    public <N> void saveNode(NodeContext<N, S> context, NodeChangeListener<NodeContext<N, S>, S> listener) throws NullPointerException,
            NavigationServiceException {
        saveTree(context.tree, listener);
    }

    public <N> void rebaseNode(
            NodeContext<N, S> context,
            Scope<S> scope,
            NodeChangeListener<NodeContext<N, S>, S> listener)
            throws NavigationServiceException {
        Scope.Visitor<S> visitor;
        if (scope != null) {
            visitor = new FederatingVisitor<N, S>(context.tree.origin(), context, scope);
        } else {
            visitor = context.tree.origin();
        }
        rebaseTree(context.tree, visitor, listener);
    }

    private <N> void updateTree(
            TreeContext<N, S> tree,
            Scope.Visitor<S> visitor,
            NodeChangeListener<NodeContext<N, S>, S> listener)
            throws NullPointerException, IllegalArgumentException, NavigationServiceException {
        if (tree.hasChanges()) {
            throw new IllegalArgumentException("For now we don't accept to update a context that has pending changes");
        }
        NodePersistence<S> persistence = persistenceProvider.get();
        try {
            NodeData<S> data = persistence.getNode(tree.root.data.id);
            if (data == null) {
                throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
            }

            // Switch to edit mode
            tree.editMode = true;

            // Apply diff changes to the model
            try {

                TreeUpdate.perform(tree, NodeContextUpdateAdapter.<N, S> create(), data,
                        NodeDataUpdateAdapter.create(persistence), listener, visitor);
            } finally {
                // Disable edit mode
                tree.editMode = false;
            }
        } finally {
            persistence.close();
        }
    }

    private <N> void rebaseTree(
            TreeContext<N, S> tree,
            Scope.Visitor<S> visitor,
            NodeChangeListener<NodeContext<N, S>, S> listener)
            throws NavigationServiceException {
        if (!tree.hasChanges()) {
            updateTree(tree, visitor, listener);
        } else {
            TreeContext<N, S> rebased = rebase(tree, visitor);
            TreeUpdate.perform(tree, NodeContextUpdateAdapter.<N, S> create(), rebased.root,
                    NodeContextUpdateAdapter.<N, S> create(), listener, rebased);
        }
    }

    private <N> TreeContext<N, S> rebase(
            TreeContext<N, S> tree,
            Scope.Visitor<S> visitor) throws NavigationServiceException {
        NodePersistence<S> persistence = persistenceProvider.get();
        try {
            NodeData<S> data = persistence.getNode(tree.root.getId());
            if (data == null) {
                throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
            }

            //
            TreeContext<N, S> rebased = new NodeContext<N, S>(tree.model, data).tree;

            //
            TreeUpdate.perform(rebased, NodeContextUpdateAdapter.<N, S> create(), data,
                    NodeDataUpdateAdapter.create(persistence), null, visitor);

            //
            NodeChangeQueue<NodeContext<N, S>, S> changes = tree.getChanges();

            //
            NodeChangeListener<NodeContext<N, S>, S> merger = new TreeMerge<N, S>(rebased, rebased);

            //
            if (changes != null) {
                changes.broadcast(merger);
            }

            //
            return rebased;
        } finally {
            persistence.close();
        }
    }

    private <N> void saveTree(TreeContext<N, S> tree, NodeChangeListener<NodeContext<N, S>, S> listener) throws NullPointerException,
            NavigationServiceException {

        NodePersistence<S> persistence = persistenceProvider.get();

        try {
            NodeData<S> data = persistence.getNode(tree.root.data.id);
            if (data == null) {
                throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
            }

            // Attempt to rebase
            TreeContext<N, S> rebased = rebase(tree, tree.origin());

            //
            NodePersister<N, S> persister = new NodePersister<N, S>(persistence);

            //
            NodeChangeQueue<NodeContext<N, S>, S> changes = rebased.getChanges();
            if (changes != null) {
                changes.broadcast(persister);

                // Update the tree handles to the persistent values
                for (Map.Entry<String, String> entry : persister.toPersist.entrySet()) {
                    NodeContext<N, S> a = tree.getNode(entry.getKey());
                    a.handle = entry.getValue();
                }

                // Update data
                for (String ddd : persister.toUpdate) {
                    NodeContext<N, S> a = tree.getNode(ddd);
                    a.data = new NodeData<S>(a);
                    a.name = null;
                    a.state = null;
                }

                // Clear changes
                changes.clear();
                tree.getChanges().clear();
            }

            // Update
            TreeUpdate.perform(tree, NodeContextUpdateAdapter.<N, S> create(), rebased.root, NodeContextUpdateAdapter.<N, S> create(),
                    listener, rebased);
        } finally {
            persistence.close();
        }
    }
}
