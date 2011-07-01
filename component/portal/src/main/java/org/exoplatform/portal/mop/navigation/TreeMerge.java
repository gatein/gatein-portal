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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeMerge<N> extends NodeChangeListener.Base<NodeContext<N>>
{

   /** . */
   private final TreeContext<N> merged;

   /** . */
   private final NodeChangeListener<NodeContext<N>> next;

   TreeMerge(TreeContext<N> merged, NodeChangeListener<NodeContext<N>> next)
   {
      this.merged = merged;
      this.next = next;
   }

   public void onCreate(NodeContext<N> target, NodeContext<N> _parent, NodeContext<N> _previous, String name) throws NavigationServiceException
   {
      String parentHandle = _parent.handle;
      NodeContext<N> parent = merged.getNode(parentHandle);
      if (parent == null)
      {
         throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE);
      }

      //
      NodeContext<N> previous;
      if (_previous != null)
      {
         previous = merged.getNode(_previous.handle);
         if (previous == null)
         {
            throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
         }
      }
      else
      {
         previous = null;
      }

      //
      NodeContext<N> added = parent.get(name);
      if (added != null)
      {
         throw new NavigationServiceException(NavigationError.ADD_CONCURRENTLY_ADDED_NODE);
      }

      //
      NodeContext<N> source = merged.create(target.handle, name, target.getState());

      //
      next.onCreate(source, parent, previous, name);
   }

   public void onDestroy(NodeContext<N> target, NodeContext<N> _parent)
   {
      NodeContext<N> removed = merged.getNode(target.handle);

      //
      if (removed != null)
      {
         NodeContext<N> parent = merged.getNode(_parent.handle);
         next.onDestroy(removed, parent);
      }
   }

   public void onRename(NodeContext<N> target, NodeContext<N> _parent, String _name) throws NavigationServiceException
   {
      //
      String renamedHandle = target.handle;
      NodeContext<N> renamed = merged.getNode(renamedHandle);
      if (renamed == null)
      {
         throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE);
      }

      //
      NodeContext<N> parent = renamed.getParent();
      if (parent.get(_name) != null)
      {
         throw new NavigationServiceException(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME);
      }

      //
      next.onRename(renamed, parent, _name);
   }

   public void onUpdate(NodeContext<N> target, NodeState state) throws NavigationServiceException
   {
      String updatedHandle = target.handle;
      NodeContext<N> navigation = merged.getNode(updatedHandle);
      if (navigation == null)
      {
         throw new NavigationServiceException(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE);
      }

      //
      next.onUpdate(navigation, state);
   }

   public void onMove(NodeContext<N> target, NodeContext<N> _from, NodeContext<N> _to, NodeContext<N> _previous) throws NavigationServiceException
   {
      String srcHandle = _from.handle;
      NodeContext<N> src = merged.getNode(srcHandle);
      if (src == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE);
      }

      //
      String dstHandle = _to.handle;
      NodeContext<N> dst = merged.getNode(dstHandle);
      if (dst == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE);
      }

      //
      String movedHandle = target.handle;
      NodeContext<N> moved = merged.getNode(movedHandle);
      if (moved == null)
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE);
      }

      //
      NodeContext<N> previous;
      if (_previous != null)
      {
         previous = merged.getNode(_previous.handle);
         if (previous == null)
         {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE);
         }
      }
      else
      {
         previous = null;
      }

      //
      if (src != moved.getParent())
      {
         throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE);
      }

      //
      if (src != dst)
      {
         String name = moved.getName();
         NodeContext<N> existing = dst.get(name);
         if (existing != null)
         {
            throw new NavigationServiceException(NavigationError.MOVE_CONCURRENTLY_DUPLICATE_NAME);
         }
      }

      //
      next.onMove(moved, src, dst, previous);
   }
}
