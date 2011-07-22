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

import java.util.Arrays;
import java.util.List;

/**
* Flexible scope implementations.
*/
public class GenericScope
{

   public static abstract class Branch implements Scope
   {

      /** . */
      private final Scope federated;

      /**
       * Create a new branch scope.
       *
       * @param federated the federated scope
       * @throws NullPointerException if the federated scope is null
       */
      public Branch(Scope federated) throws NullPointerException
      {
         if (federated == null)
         {
            throw new NullPointerException("no null federated scope accepted");
         }

         //
         this.federated = federated;
      }

      protected abstract int getSize();

      protected abstract String getName(int index);

      public Visitor get()
      {
         return new Visitor()
         {
            @Override
            protected int getSize()
            {
               return GenericScope.Branch.this.getSize();
            }

            @Override
            protected String getName(int index)
            {
               return GenericScope.Branch.this.getName(index);
            }

            @Override
            protected Scope.Visitor getFederated()
            {
               return federated.get();
            }
         };
      }

      public static abstract class Visitor implements Scope.Visitor
      {

         /** . */
         private Scope.Visitor visitor;

         protected Visitor()
         {
            this.visitor = null;
         }

         protected abstract int getSize();

         protected abstract String getName(int index);

         protected abstract Scope.Visitor getFederated();

         public VisitMode enter(int depth, String id, String name, NodeState state)
         {
            int size = getSize();

            //
            if (depth < size)
            {
               if (depth == 0 || name.equals(getName(depth - 1)))
               {
                  return VisitMode.ALL_CHILDREN;
               }
               else
               {
                  return VisitMode.NO_CHILDREN;
               }
            }
            else if (depth == size)
            {
               if (depth == 0 || name.equals(getName(depth - 1)))
               {
                  Scope.Visitor visitor = getFederated();
                  VisitMode mode = visitor.enter(0, id, name, state);
                  if (mode == VisitMode.ALL_CHILDREN)
                  {
                     this.visitor = visitor;
                  }
                  return mode;
               }
               else
               {
                  return VisitMode.NO_CHILDREN;
               }
            }
            else
            {
               return visitor.enter(depth - size, id, name, state);
            }
         }

         public void leave(int depth, String id, String name, NodeState state)
         {
            int size = getSize();

            //
            if (depth < size)
            {
               // Do nothing
            }
            else if (depth == size)
            {
               if (depth == 0 || name.equals(getName(depth - 1)))
               {
                  visitor.leave(0, id, name, state);
                  visitor = null;
               }
               else
               {
                  // Do nothing
               }
            }
            else
            {
               visitor.leave(depth - size, id, name, state);
            }
         }
      }
   }

   /**
    * <p>A scope with the shape of a tree branch following the rules:
    * <ul>
    *    <li>the first node with depth 0 will have all of its children visited</li>
    *    <li>any node above the root node that fits in the <code>path</code> array will be matched
    *    only if the node name matches the corresponding value in the <code>path</code> array. The last
    *    node whose depth is equals to the <code>path</code> list size will have its visit mode value delegated
    *    to the <code>federated</code> scope argument with a depth of 0 and the same other arguments, any other node
    *    will have all of its children visited.</li>
    *    <li>any other node will have its visit mode delegated to the <code>federated</code> scope argument
    *    with the same arguments except the depth that will be subtracted the <code>path</code> list argument size.</li>
    * </ul></p>
    *
    * @param path the names that describing the tree path
    * @param federated the federated scope
    * @return the branch shape scope
    * @throws NullPointerException if any argument is null
    */
   public static Scope branchShape(final List<String> path, Scope federated) throws NullPointerException
   {
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }
      return new Branch(federated)
      {
         @Override
         protected int getSize()
         {
            return path.size();
         }

         @Override
         protected String getName(int index)
         {
            return path.get(index);
         }
      };
   }

   public static Scope branchShape(final String[] path, Scope federated)
   {
      return new Branch(federated)
      {
         @Override
         protected int getSize()
         {
            return path.length;
         }

         @Override
         protected String getName(int index)
         {
            return path[index];
         }
      };
   }

   public static Scope branchShape(List<String> path)
   {
      return branchShape(path, Scope.CHILDREN);
   }

   public static Scope branchShape(String[] path)
   {
      return branchShape(Arrays.asList(path), Scope.CHILDREN);
   }

   /** . */
   private static final GenericScope.Tree ALL = new Tree(-1);

   /** . */
   private static GenericScope.Tree[] PREDEFINED = {
      new Tree(0),
      new Tree(1),
      new Tree(2),
      new Tree(3),
      new Tree(4),
      new Tree(5),
      new Tree(6),
      new Tree(7),
      new Tree(8),
      new Tree(9) };

   public static Scope treeShape(int height)
   {
      if (height < 0)
      {
         return ALL;
      }
      else if (height < PREDEFINED.length)
      {
         return PREDEFINED[height];
      }
      else
      {
         return new Tree(height);
      }
   }

   public static class Tree implements Scope
   {

      /** . */
      private final Visitor visitor;

      /**
       * Creates a new navigation scope. When the height is positive or zero, the tree will be pruned to the specified
       * height, when the height is negative no pruning will occur.
       *
       * @param height the max height of the pruned tree
       */
      public Tree(final int height)
      {
         this.visitor = new Visitor()
         {
            public VisitMode enter(int depth, String id, String name, NodeState state)
            {
               if (height < 0 || depth < height)
               {
                  return VisitMode.ALL_CHILDREN;
               }
               else
               {
                  return VisitMode.NO_CHILDREN;
               }
            }

            public void leave(int depth, String id, String name, NodeState state)
            {
            }
         };
      }

      public Visitor get()
      {
         return visitor;
      }
   }
}
