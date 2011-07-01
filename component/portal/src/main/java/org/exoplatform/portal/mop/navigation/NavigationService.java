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

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.portal.mop.SiteKey;

/**
 * <p>The navigation service takes care of managing the various portal navigations and their nodes. In order to manage
 * an efficient loading of the nodes, a {@link Scope} is used to describe the set of nodes that should be retrieved
 * when a loading operation is performed.</p>
 *
 * <p>The node operations does not provide a model per se, but instead use the {@link NodeModel} interface to plug
 * an API model. Various node operations are quite complex and any API in front of this service would need to perform
 * a manual, error prone and tedious synchronization. Instead the model interface allows the navigation service to
 * operate directly on an existing model.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface NavigationService
{

   /**
    * Find and returns a navigation, if no such site exist, null is returned instead.
    *
    * @param key the navigation key
    * @return the matching navigation
    * @throws NullPointerException if the key is null
    * @throws NavigationServiceException anything that would prevent the operation to succeed
    */
   NavigationContext loadNavigation(SiteKey key) throws NullPointerException, NavigationServiceException;

   /**
    * Create, update a navigation. When the navigation state is not null, the navigation
    * will be created or updated depending on whether or not the navigation already exists.
    *
    * @param navigation the navigation
    * @throws NullPointerException if the key is null
    * @throws IllegalArgumentException if the navigation is already destroyed
    * @throws NavigationServiceException anything that would prevent the operation to succeed
    */
   void saveNavigation(NavigationContext navigation) throws NullPointerException, IllegalArgumentException, NavigationServiceException;

   /**
    * Destroy a navigation.
    *
    * @param navigation the navigation
    * @return true if the navigation was destroyed
    * @throws NullPointerException if the navigation is null
    * @throws IllegalArgumentException if the navigation is destroyed
    * @throws NavigationServiceException anything that would prevent the operation to succeed
    */
   boolean destroyNavigation(NavigationContext navigation) throws NullPointerException, IllegalArgumentException, NavigationServiceException;

   /**
    * Load a navigation node from a specified navigation. The returned context will be the root node of the navigation.
    *
    * @param model the node model
    * @param navigation the navigation
    * @param scope the scope
    * @param listener the optional listener
    * @param <N> the node generic type
    * @return the loaded node
    * @throws NullPointerException if any argument is null
    * @throws NavigationServiceException anything that would prevent the operation to succeed
    */
   <N> NodeContext<N> loadNode(NodeModel<N> model, NavigationContext navigation, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, NavigationServiceException;

   /**
    * <p>Save the specified context state to the persistent storage. The operation takes the pending changes done to
    * the tree and attempt to save them to the persistent storage. When conflicts happens, a merge will be attempted
    * however it can lead to a failure.</p>
    *
    * @param context the context to save
    * @param listener the optional listener
    * @throws NullPointerException if the context argument is null
    * @throws NavigationServiceException anything that would prevent the operation to succeed
    */
   <N> void saveNode(NodeContext<N> context, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, NavigationServiceException;

   /**
    * <p>Update the specified <code>context</code> argument with the most recent state. The update operation will
    * affect the entire tree even if the <code>context</code> argument is not the root of the tree. The <code>context</code>
    * argument determines the root from which the <code>scope</code> argument applies to.</p>
    *
    * <p>The update operation compares the actual tree and the most recent version of the same tree. When the
    * <code>scope</scope> argument is not null, it will be used to augment the tree with new nodes. During the
    * operation, any modification done to the tree wil be reported as a change to the optional <code>listener</code>
    * argument.</p>
    *
    * <p>The update operates recursively by doing a comparison of the node intrisic state (name or state) and its
    * structural state (the children). The comparison between the children of two nodes is done thanks to the
    * Longest Common Subsequence algorithm to minimize the number of changes to perform. The operation assumes
    * that no changes have been performed on the actual tree.</p>
    *
    * @param context the context to update
    * @param scope the optional scope
    * @param listener the optional node change listener
    * @param <N> the node generic type
    * @throws NullPointerException if the context argument is null
    * @throws NavigationServiceException anything that would prevent the operation to succeed
    * @throws IllegalArgumentException if the context argument has pending changes
    */
   <N> void updateNode(NodeContext<N> context, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, IllegalArgumentException, NavigationServiceException;

   /**
    * <p>Rebase the specified <code>context</code> argument with the most recent state. The rebase operation will
    * affect the entire tree even if the <code>context</code> argument is not the root of the tree. The <code>context</code>
    * argument determines the root from which the <code>scope</code> argument applies to.</p>
    *
    * <p>The rebase operation compares the actual tree and the most recent version of the same tree. When the
    * <code>scope</scope> argument is not null, it will be used to augment the tree with new nodes. During the
    * operation, any modification done to the tree wil be reported as a change to the optional <code>listener</code>
    * argument.</p>
    *
    * <p>The rebase operates in a similar way of the update operation, however it assumes that it can have pending changes
    * done to the tree (i.e changes that have not been saved). Actually a rebase operation with no changes will do the
    * same than an update operation. The rebase operation attempts to bring the most recent changes to the tree, by
    * doing a rebase of the pending operations on the actual tree. When conflicting changes exist, a merge will be
    * attempted, however it could fail and lead to a non resolvable situation.</p>
    *
    * @param context the context to rebase
    * @param scope the optional scope
    * @param listener the option node change listener  @throws NullPointerException if the context argument is null
    * @param <N> the node generic type
    * @throws NullPointerException if the context argument is null
    * @throws NavigationServiceException anything that would prevent the operation to succeed
    */
   <N> void rebaseNode(NodeContext<N> context, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException, NavigationServiceException;
}
