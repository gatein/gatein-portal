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
* A flexible scope implementation.
*/
public class GenericScope implements Scope
{

   public static Scope branchShape(String[] path)
   {
      return branchShape(Arrays.asList(path), Scope.CHILDREN);
   }

   public static Scope branchShape(List<String> path)
   {
      return branchShape(path, Scope.CHILDREN);
   }

   public static Scope branchShape(String[] path, Scope federated)
   {
      return branchShape(Arrays.asList(path), federated);
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
   public static Scope branchShape(final List<String> path, final Scope federated) throws NullPointerException
   {
      if (path == null)
      {
         throw new NullPointerException("no null path accepted");
      }
      if (federated == null)
      {
         throw new NullPointerException("no null federated scope accepted");
      }
      return new Scope()
      {
         public Visitor get()
         {
            return new Visitor()
            {
               public VisitMode enter(int depth, String id, String name, NodeState state)
               {
                  if (depth == 0)
                  {
                     return VisitMode.ALL_CHILDREN;
                  }
                  else if (depth > 0)
                  {
                     if (depth < path.size())
                     {
                        if ((name.equals(path.get(depth - 1))))
                        {
                           return VisitMode.ALL_CHILDREN;
                        }
                        else
                        {
                           return VisitMode.NO_CHILDREN;
                        }
                     }
                     else if (depth == path.size())
                     {
                        if ((name.equals(path.get(path.size() - 1))))
                        {
                           return federated.get().enter(0, id, name, state);
                        }
                        else
                        {
                           return VisitMode.NO_CHILDREN;
                        }
                     }
                     else
                     {
                        return federated.get().enter(depth - path.size(), id, name, state);
                     }
                  }
                  throw new AssertionError();
               }

               public void leave(int depth, String id, String name, NodeState state)
               {
               }
            };
         }
      };
   }


   /** . */
   private final Visitor visitor;

   /**
    * Creates a new navigation scope. When the height is positive or null, the tree will be pruned to the specified
    * height, when the height is negative  no pruning will occur.
    *
    * @param height the max height of the pruned tree
    */
   public GenericScope(final int height)
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
