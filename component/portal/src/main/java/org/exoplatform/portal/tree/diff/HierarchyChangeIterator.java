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

package org.exoplatform.portal.tree.diff;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class HierarchyChangeIterator<L1, N1, L2, N2, H> implements Iterator<HierarchyChangeType> {

   /** . */
   private final HierarchyDiff<L1, N1, L2, N2, H> diff;

   /** . */
   private Frame frame;

   /** . */
   private final HierarchyContext<L1, N1, H> context1;

   /** . */
   private final HierarchyContext<L2, N2, H> context2;

   /** . */
   private final ListDiff<L1, L2, H> listDiff;

   HierarchyChangeIterator(HierarchyDiff<L1, N1, L2, N2, H> diff, HierarchyContext<L1, N1, H> context1, HierarchyContext<L2, N2, H> context2) {
      this.diff = diff;
      this.context1 = context1;
      this.context2 = context2;
      this.frame = new Frame(null, context1.getRoot(), context2.getRoot());
      this.listDiff = new ListDiff<L1, L2, H>(
            diff.listAdapter1,
            diff.listAdapter2,
            diff.comparator);
   }

   /**
    * The internal status.
    */
   private enum Status {

      INIT(null),

      ENTER(HierarchyChangeType.ENTER),

      KEEP(HierarchyChangeType.KEEP),

      ADDED(HierarchyChangeType.ADDED),

      REMOVED(HierarchyChangeType.REMOVED),

      MOVED_IN(HierarchyChangeType.MOVED_IN),

      MOVED_OUT(HierarchyChangeType.MOVED_OUT),

      LEAVE(HierarchyChangeType.LEAVE),

      ERROR(HierarchyChangeType.ERROR),

      RESUME(null);

      /** The associated change type. */
      final HierarchyChangeType changeType;

      private Status(HierarchyChangeType changeType) {
         this.changeType = changeType;
      }
   }

   private class Frame {

      /** . */
      private final Frame parent;

      /** . */
      private final N1 srcRoot;

      /** . */
      private final N2 dstRoot;

      /** . */
      private ListChangeIterator<L1, L2, H> it;

      /** . */
      private Status previous;

      /** . */
      private Status next;

      /** . */
      private Iterator<H> srcIt;

      /** . */
      private Iterator<H> dstIt;

      /** . */
      private N1 src;

      /** . */
      private N2 dst;

      private Frame(Frame parent, N1 srcRoot, N2 dstRoot) {
         this.parent = parent;
         this.srcRoot = srcRoot;
         this.dstRoot = dstRoot;
         this.previous = Status.INIT;
      }
   }

   public boolean hasNext() {
      if (frame != null && frame.next == null) {
         while (true) {

            if (frame.previous == Status.INIT) {
               H id2 = context2.getHierarchyAdapter().getHandle(frame.dstRoot);
               if (frame.srcRoot == null)
               {
                  frame.next = Status.ENTER;
                  frame.src = null;
                  frame.dst = frame.dstRoot;
               }
               else
               {
                  H id1 = context1.getHierarchyAdapter().getHandle(frame.srcRoot);
                  if (diff.comparator.compare(id1, id2) != 0) {
                     frame.next = Status.ERROR;
                     frame.src = frame.srcRoot;
                     frame.dst = frame.dstRoot;
                  } else {
                     frame.next = Status.ENTER;
                     frame.src = frame.srcRoot;
                     frame.dst = frame.dstRoot;
                  }
               }
               break;
            } else if (frame.previous == Status.ERROR) {
               break;
            } else if (frame.previous == Status.LEAVE) {
               frame = frame.parent;
               if (frame != null) {
                  frame.previous = Status.RESUME;
                  continue;
               } else {
                  break;
               }
            } else if (frame.previous == Status.KEEP) {
               frame = new Frame(frame, frame.src, frame.dst);
               continue;
            } else if (frame.previous == Status.MOVED_IN) {
               frame = new Frame(frame, frame.src, frame.dst);
               continue;
            } else if (frame.previous == Status.ADDED) {
               frame = new Frame(frame, frame.src, frame.dst);
               continue;
            } else if (frame.previous == Status.ENTER) {
               L1 children1;
               if (frame.src != null) {
                  children1 = context1.getHierarchyAdapter().getChildren(frame.srcRoot);
                  frame.srcIt = diff.listAdapter1.iterator(children1, false);
               }
               else {
                  children1 = null;
                  frame.srcIt = null;
               }
               L2 children2 = context2.getHierarchyAdapter().getChildren(frame.dstRoot);
               frame.dstIt = diff.listAdapter2.iterator(children2, false);
               frame.it = listDiff.iterator(children1, children2);
            } else {
               // Nothing
            }

            //
            if (frame.it.hasNext()) {
               switch (frame.it.next()) {
                  case SAME:
                     N1 next1 = context1.findByHandle(frame.srcIt.next());
                     N2 next2 = context2.findByHandle(frame.dstIt.next());
                     frame.next = Status.KEEP;
                     frame.src = next1;
                     frame.dst = next2;
                     break;
                  case ADD:
                     frame.dstIt.next();
                     H addedHandle = frame.it.getElement();
                     N2 added = context2.findByHandle(addedHandle);
                     H addedId = context2.getHierarchyAdapter().getHandle(added);
                     N1 a = context1.findByHandle(addedId);
                     if (a != null) {
                        frame.next = Status.MOVED_IN;
                        frame.src = a;
                        frame.dst = added;
                     } else {
                        frame.next = Status.ADDED;
                        frame.src = null;
                        frame.dst = added;
                     }
                     break;
                  case REMOVE:
                     frame.srcIt.next();
                     H removedHandle = frame.it.getElement();
                     N1 removed = context1.findByHandle(removedHandle);
                     H removedId = context1.getHierarchyAdapter().getHandle(removed);
                     N2 b = context2.findByHandle(removedId);
                     if (b != null) {
                        frame.next = Status.MOVED_OUT;
                        frame.src = removed;
                        frame.dst = b;
                     } else {
                        frame.next = Status.REMOVED;
                        frame.src = removed;
                        frame.dst = null;
                     }
                     break;
                  default:
                     throw new AssertionError();
               }
            } else {
               frame.next = Status.LEAVE;
               frame.src = frame.srcRoot;
               frame.dst = frame.dstRoot;
            }

            //
            break;
         }
      }
      return frame != null && frame.next != null;
   }

   public HierarchyChangeType next() {
      if (!hasNext()) {
         throw new NoSuchElementException();
      } else {
         frame.previous = frame.next;
         frame.next = null;
         return frame.previous.changeType;
      }
   }

   public void skip() {
      if (frame.previous == HierarchyChangeIterator.Status.ENTER) {

         // A bit hackish as it bypass the main loop
         // the proper way to do it would be to introduce a SKIP status
         // and properly react to it to update the state machine
         // but for now it will do

         frame.next = Status.LEAVE;
         frame.src = frame.srcRoot;
         frame.dst = frame.dstRoot;
      } else {
         throw new IllegalStateException("Cannot skip when in state " + frame.previous);
      }
   }

   public N1 getSource() {
      return frame.src;
   }

   public N2 getDestination() {
      return frame.dst;
   }

   public N1 peekSourceRoot()
   {
      return frame.srcRoot;
   }

   public N2 peekDestinationRoot()
   {
      return frame.dstRoot;
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }
}
