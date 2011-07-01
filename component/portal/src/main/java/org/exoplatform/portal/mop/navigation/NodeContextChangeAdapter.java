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
public class NodeContextChangeAdapter<N> implements NodeChangeListener<NodeContext<N>>
{

   public static <N> NodeChangeListener<NodeContext<N>> safeWrap(NodeChangeListener<N> wrapped)
   {
      if (wrapped == null)
      {
         return null;
      }
      else
      {
         return new NodeContextChangeAdapter<N>(wrapped);
      }
   }

   /** . */
   private final NodeChangeListener<N> wrapped;

   public NodeContextChangeAdapter(NodeChangeListener<N> wrapped)
   {
      if (wrapped == null)
      {
         throw new NullPointerException();
      }
      this.wrapped = wrapped;
   }

   public void onAdd(NodeContext<N> target, NodeContext<N> parent, NodeContext<N> previous)
   {
      wrapped.onAdd(unwrap(target), unwrap(parent), unwrap(previous));
   }

   public void onCreate(NodeContext<N> target, NodeContext<N> parent, NodeContext<N> previous, String name)
   {
      wrapped.onCreate(unwrap(target), unwrap(parent), unwrap(previous), name);
   }

   public void onRemove(NodeContext<N> target, NodeContext<N> parent)
   {
      wrapped.onRemove(unwrap(target), unwrap(parent));
   }

   public void onDestroy(NodeContext<N> target, NodeContext<N> parent)
   {
      wrapped.onDestroy(unwrap(target), unwrap(parent));
   }

   public void onRename(NodeContext<N> target, NodeContext<N> parent, String name)
   {
      wrapped.onRename(unwrap(target), unwrap(parent), name);
   }

   public void onUpdate(NodeContext<N> target, NodeState state)
   {
      wrapped.onUpdate(unwrap(target), state);
   }

   public void onMove(NodeContext<N> target, NodeContext<N> from, NodeContext<N> to, NodeContext<N> previous)
   {
      wrapped.onMove(unwrap(target), unwrap(from), unwrap(to), unwrap(previous));
   }

   private N unwrap(NodeContext<N> context)
   {
      return context != null ? context.getNode() : null;
   }
}
