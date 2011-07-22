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

package org.exoplatform.portal.mop.importer;

import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PageNodeContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Builder<B extends Builder<B>>
{

   /** . */
   protected final String value;

   /** . */
   protected final List<NodeBuilder> nodes;

   protected Builder(String value)
   {
      this.value = value;
      this.nodes = new ArrayList<NodeBuilder>();
   }

   public static NodeBuilder node(String name)
   {
      return new NodeBuilder(name);
   }

   public static FragmentBuilder fragment(String... path)
   {
      StringBuilder sb = new StringBuilder();
      for (String name : path)
      {
         if (sb.length() > 0)
         {
            sb.append('/');
         }
         sb.append(name);
      }
      return new FragmentBuilder(sb.toString());
   }

   public B add(NodeBuilder... nodes)
   {
      for (NodeBuilder node : nodes)
      {
         this.nodes.add(node);
      }
      return (B)this;
   }

   public Builder get(String value)
   {
      for (Builder node : nodes)
      {
         if (node.value.equals(value))
         {
            return node;
         }
      }
      return null;
   }

   protected final ArrayList<PageNode> buildNodes()
   {
      ArrayList<PageNode> nodes = new ArrayList<PageNode>();
      for (NodeBuilder node : this.nodes)
      {
         nodes.add(node.build());
      }
      return nodes;
   }

   public abstract PageNodeContainer build();

}
