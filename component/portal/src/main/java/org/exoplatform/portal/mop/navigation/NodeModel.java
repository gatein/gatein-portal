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
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface NodeModel<N>
{

   /**
    * A model based on itself.
    */
   NodeModel<NodeContext<?>> SELF_MODEL = new NodeModel<NodeContext<?>>()
   {
      public NodeContext<NodeContext<?>> getContext(NodeContext<?> node)
      {
         throw new UnsupportedOperationException();
      }
      public NodeContext<?> create(NodeContext<NodeContext<?>> context)
      {
         return context;
      }
   };

   /**
    * Returns the context of a node.
    *
    * @param node the node
    * @return the node context
    */
   NodeContext<N> getContext(N node);

   /**
    * Create a node wrapping a context.
    *
    * @param context the node context
    * @return the node instance
    */
   N create(NodeContext<N> context);

}
