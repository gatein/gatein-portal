/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.services.html.parser;

import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.html.Name;
import org.exoplatform.services.html.Tag;

import java.util.List;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Aug 13, 2006
 */
final class NodeSetter
{

   void add(NodeImpl node)
   {
      if (node.getConfig().only())
      {
         set(node);
         return;
      }

      HTMLNode parent = ParserService.getNodeCreator().getOpenParent(node.getConfig(), true);
      if (parent != null && parent.getConfig().end() == Tag.OPTIONAL && HTML.isEndType(node, parent.getConfig()))
      {
         ParserService.getNodeCloser().close((NodeImpl)parent);
         parent = ParserService.getNodeCreator().getOpenParent(node.getConfig(), true);
      }

      //close all older children in parent #Bug 28/11 
      List<HTMLNode> children = parent.getChildren();
      if (children.size() > 0)
      {
         ParserService.getNodeCloser().close((NodeImpl)children.get(children.size() - 1));
      }

      add(parent, node);
      if (node.getConfig().end() != Tag.FORBIDDEN)
      {
         if (node.isOpen())
            ParserService.getNodeCreator().getOpens().add(node);
      }

   }

   HTMLNode add(HTMLNode node, HTMLNode ele)
   {
      ele.setParent(node);
      node.addChild(ele);
      if (ele.getConfig().end() != Tag.FORBIDDEN)
         return ele;
      return node;
   }

   NodeImpl set(NodeImpl node)
   {
      if (node.getName() == Name.HTML)
         return ParserService.getRootNode();
      List<HTMLNode> children = ParserService.getRootNode().getChildren();

      for (HTMLNode ele : children)
      {
         if (ele.getConfig().name() != node.getConfig().name())
            continue;
         ele.setValue(node.getValue());
         return (NodeImpl)ele;
      }

      if (node.getName() == Name.BODY)
      {
         add(ParserService.getRootNode(), node);
         ParserService.getNodeCreator().getOpens().add(1, node);
         return node;
      }

      children.add(0, node);
      node.setParent(ParserService.getRootNode());
      node.setIsOpen(false);
      return node;
   }
}
