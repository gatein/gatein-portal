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

/**
 * <p>The scope describes a set of nodes, the scope implementation should be stateless and should be shared
 * between many threads.</p>
 *
 * <p>A scope is responsible for providing a {@link Visitor} object that is used to determine which nodes are
 * loaded when a loading operation occurs. Visitors are not thread safe, as a consequence the {@link #get()} operation
 * should create a new visitor instance on each call, unless the visitor itself is stateless by nature.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface Scope
{

   /**
    * The node without its children.
    */
   Scope SINGLE = GenericScope.treeShape(0);

   /**
    * A node and its chidren.
    */
   Scope CHILDREN = GenericScope.treeShape(1);

   /**
    * A node, its chidren and grandchildren.
    */
   Scope GRANDCHILDREN = GenericScope.treeShape(2);

   /**
    * The entire hierarchy, to use with care.
    */
   Scope ALL = GenericScope.treeShape(-1);

   Visitor get();

   /**
    * A scope visitor responsible for determining the loading of a node.
    */
   public interface Visitor
   {
      /**
       * Signals a node is ented and returns the visit mode for that node.
       *
       * @param depth the relative depth to the root of the loading
       * @param id the node persistent id
       * @param name the node name
       * @param state the node state
       * @return the visit mode
       */
      VisitMode enter(int depth, String id, String name, NodeState state);

      /**
       * Signals a node is left.
       *
       * @param depth the relative depth to the root of the loading
       * @param id the node persistent id
       * @param name the node name
       * @param state the node state
       */
      void leave(int depth, String id, String name, NodeState state);
   }
}
