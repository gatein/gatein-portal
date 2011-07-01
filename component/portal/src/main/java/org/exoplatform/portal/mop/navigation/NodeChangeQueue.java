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

import java.util.Collection;
import java.util.LinkedList;

/**
 * A queuing implementation of the {@link NodeChangeListener} interface.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NodeChangeQueue<N> extends LinkedList<NodeChange<N>> implements NodeChangeListener<N>
{

   public NodeChangeQueue()
   {
   }

   public NodeChangeQueue(Collection<? extends NodeChange<N>> c)
   {
      super(c);
   }

   public void broadcast(NodeChangeListener<N> listener)
   {
      for (NodeChange<N> change : this)
      {
         change.dispatch(listener);
      }
   }

   public void onAdd(N target, N parent, N previous)
   {
      add(new NodeChange.Added<N>(parent, previous, target));
   }

   public void onCreate(N target, N parent, N previous, String name)
   {
      add(new NodeChange.Created<N>(parent, previous, target, name));
   }

   public void onRemove(N target, N parent)
   {
      add(new NodeChange.Removed<N>(parent, target));
   }

   public void onDestroy(N target, N parent)
   {
      add(new NodeChange.Destroyed<N>(parent, target));
   }

   public void onRename(N target, N parent, String name)
   {
      add(new NodeChange.Renamed<N>(parent, target, name));
   }

   public void onUpdate(N target, NodeState state)
   {
      add(new NodeChange.Updated<N>(target, state));
   }

   public void onMove(N target, N from, N to, N previous)
   {
      add(new NodeChange.Moved<N>(from, to, previous, target));
   }
}
