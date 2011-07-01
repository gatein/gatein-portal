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
 * A visitor implementation that federates a scope along with a federation root.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class FederatingVisitor<N> implements Scope.Visitor
{

   /** . */
   private final Scope.Visitor visitor;

   /** . */
   private final NodeContext<N> federationRoot;

   /** . */
   private final int federationDepth;

   /** . */
   private final Scope federatedScope;

   /** . */
   private Scope.Visitor federated;

   FederatingVisitor(Scope.Visitor visitor, NodeContext<N> federationRoot, Scope federatedScope)
   {
      this.visitor = visitor;
      this.federationRoot = federationRoot;
      this.federatedScope = federatedScope;
      this.federated = null;
      this.federationDepth = federationRoot.getDepth(federationRoot.tree.root);
   }

   public VisitMode enter(int depth, String id, String name, NodeState state)
   {
      if (federationRoot.handle.equals(id))
      {
         federated = federatedScope.get();
      }

      //
      VisitMode visit;
      if (federated != null)
      {
         visit = federated.enter(depth - federationDepth, id, name, state);
      }
      else
      {
         visit = VisitMode.NO_CHILDREN;
      }

      // Override
      VisitMode override = visitor.enter(depth, id, name, state);
      if (override == VisitMode.ALL_CHILDREN)
      {
         visit = VisitMode.ALL_CHILDREN;
      }

      //
      return visit;
   }

   public void leave(int depth, String id, String name, NodeState state)
   {
      if (federationRoot.handle.equals(id))
      {
         federated = null;
      }

      //
      if (federated != null)
      {
         federated.leave(depth - federationDepth, id, name, state);
      }
   }
}
