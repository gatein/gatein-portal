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

import org.exoplatform.portal.tree.diff.Adapters;
import org.exoplatform.portal.tree.diff.HierarchyAdapter;
import org.exoplatform.portal.tree.diff.HierarchyChangeIterator;
import org.exoplatform.portal.tree.diff.HierarchyChangeType;
import org.exoplatform.portal.tree.diff.HierarchyDiff;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class TreeUpdate<N1, N2>
{

   static <N1, N2> void perform(
      TreeContext<N1> src,
      HierarchyAdapter<String[], NodeContext<N1>, String> srcAdatper,
      N2 dst,
      TreeUpdateAdapter<N2> updateAdapter,
      NodeChangeListener<NodeContext<N1>> listener,
      Scope.Visitor visitor)
   {

      TreeUpdate<N1, N2> update = new TreeUpdate<N1, N2>(
         src,
         srcAdatper,
         dst,
         updateAdapter,
         listener,
         visitor
      );

      //
      update.perform();
   }

   private TreeUpdate(
      TreeContext<N1> src,
      HierarchyAdapter<String[], NodeContext<N1>, String> srcAdatper,
      N2 dst,
      TreeUpdateAdapter<N2> updateAdapter,
      NodeChangeListener<NodeContext<N1>> listener,
      Scope.Visitor visitor)
   {

      // We create the diff object
      HierarchyDiff<String[], NodeContext<N1>, String[], N2, String> diff = HierarchyDiff.create(
         Adapters.<String>list(),
         srcAdatper,
         Adapters.<String>list(),
         updateAdapter,
         Utils.<String>comparator());

      // We obtain the iterator
      HierarchyChangeIterator<String[], NodeContext<N1>, String[], N2, String> it = diff.iterator(src.root, dst);

      //
      this.it = it;
      this.updateAdapter = updateAdapter;
      this.visitor = visitor;
      this.depth = 0;
      this.last = null;
      this.listener = listener;
   }

   /** . */
   private final HierarchyChangeIterator<String[], NodeContext<N1>, String[], N2, String> it;

   /** . */
   private final TreeUpdateAdapter<N2> updateAdapter;

   /** . */
   private final Scope.Visitor visitor;

   /** . */
   private int depth;

   /** . */
   private NodeContext<N1> last;

   /** . */
   private NodeChangeListener<NodeContext<N1>> listener;

   private void perform()
   {

      // Consume the first ENTER that we won't skip
      HierarchyChangeType change = it.next();

      // Start recursion
      perform(it.getSource());
   }

   private void perform(NodeContext<N1> parent)
   {
      // Compute visit
      final N2 d = it.getDestination();
      final NodeData data = updateAdapter.getData(d);

      //
      final VisitMode visit;
      if (data != null)
      {
         visit = visitor.enter(depth, data.id, data.name, data.state);
      }
      else
      {
         visit = null;
      }

      // Cut the recursion if necessary
      if (visit != VisitMode.ALL_CHILDREN)
      {
         it.skip();

         // Consume leave
         it.next();
      }
      else
      {
         depth++;

         // Expand if needed
         if (!parent.isExpanded())
         {
            parent.expand();
         }

         //
         while (true)
         {
            HierarchyChangeType change = it.next();
            if (change == HierarchyChangeType.LEAVE)
            {
               // End recursion here
               break;
            }
            else
            {
               if (change == HierarchyChangeType.KEEP)
               {
                  // Consume ENTER
                  it.next();

                  // Recurse
                  perform(it.getSource());
               }
               else if (change == HierarchyChangeType.ADDED)
               {
                  // Consume ENTER
                  it.next();

                  //
                  NodeContext<N1> previous;
                  NodeContext<N1> added;
                  NodeData addedData = updateAdapter.getData(it.getDestination());
                  if (last == null || last.getParent() != parent)
                  {
                     previous = null;
                     added = parent.insertAt(0, addedData);
                  }
                  else
                  {
                     previous = last;
                     added = last.insertAfter(addedData);
                  }

                  //
                  if (listener != null)
                  {
                     listener.onAdd(
                        added,
                        parent,
                        previous);
                  }

                  // Recurse
                  perform(added);
               }
               else if (change == HierarchyChangeType.MOVED_IN)
               {
                  // Consume ENTER
                  it.next();


                  //
                  NodeContext<N1> to = parent;
                  NodeContext<N1> moved = it.getSource();
                  NodeContext<N1> from = moved.getParent();
                  NodeContext<N1> previous;
                  if (last == null || last.getParent() != parent)
                  {
                     previous = null;
                     to.insertAt(0, moved);
                  }
                  else
                  {
                     previous = last;
                     last.insertAfter(moved);
                  }

                  //
                  if (listener != null)
                  {
                     listener.onMove(
                        moved,
                        from,
                        to,
                        previous != null ? previous : null);
                  }

                  // Recurse
                  perform(it.getSource());
               }
               else if (change == HierarchyChangeType.MOVED_OUT)
               {
                  // Do nothing
               }
               else if (change == HierarchyChangeType.REMOVED)
               {
                  NodeContext<N1> removed = it.getSource();
                  NodeContext<N1> removedParent = removed.getParent();

                  //
                  removed.remove();

                  //
                  if (listener != null)
                  {
                     listener.onRemove(
                        removed,
                        removedParent);
                  }
               }
               else
               {
                  throw new UnsupportedOperationException("Not supported " + change);
               }
            }
         }

         //
         depth--;
      }

      //
      if (data != null)
      {
         if (!parent.data.state.equals(data.state))
         {
            if (listener != null)
            {
               listener.onUpdate(parent,  data.state);
            }
         }

         //
         if (!parent.data.name.equals(data.name))
         {
            parent.name = data.name;
            if (listener != null)
            {
               listener.onRename(parent, parent.getParent(), data.name);
            }
         }

         //
         parent.state = updateAdapter.getState(d);
         parent.name = updateAdapter.getName(d);
         parent.data = data;

         //
         visitor.leave(depth, data.id, data.name, data.state);
      }

      //
      last = parent;
   }
}
