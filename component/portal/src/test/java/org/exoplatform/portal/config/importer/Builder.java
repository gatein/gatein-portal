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

package org.exoplatform.portal.config.importer;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Builder
{

   /** . */
   private String value;

   /** . */
   private List<Builder> nodes;

   private Builder(String value)
   {
      this.value = value;
      this.nodes = new ArrayList<Builder>();
   }

   public static Builder navigation(String ownerId)
   {
      return new Builder(ownerId);
   }

   public static Builder node(String name)
   {
      return new Builder(name);
   }

   public Builder add(Builder... nodes)
   {
      for (Builder node : nodes)
      {
         this.nodes.add(node);
      }
      return this;
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

   private ArrayList<PageNode> buildNodes()
   {
      ArrayList<PageNode> nodes = new ArrayList<PageNode>();
      for (Builder node : this.nodes)
      {
         nodes.add(node.buildNode());
      }
      return nodes;
   }

   public PageNavigation build()
   {
      PageNavigation navigation = new PageNavigation();
      navigation.setOwnerType("portal");
      navigation.setOwnerId(value);
      navigation.setNodes(buildNodes());
      return navigation;
   }

   private PageNode buildNode()
   {
      PageNode node = new PageNode();
      node.setName(value);
      node.setLabel(value);
      node.setChildren(buildNodes());
      return node;
   }
}
