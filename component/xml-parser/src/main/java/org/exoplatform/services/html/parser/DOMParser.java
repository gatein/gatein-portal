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
import org.exoplatform.services.html.NodeConfig;
import org.exoplatform.services.token.TypeToken;

import java.util.Iterator;
import java.util.List;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Aug 3, 2006
 */
final class DOMParser
{

   final void parse(CharsToken tokens)
   {
      if (!tokens.hasNext())
         return;
      NodeImpl temp = tokens.pop();

      NodeCreator creator = ParserService.getNodeCreator();
      NodeSetter setter = ParserService.getNodeSetter();
      NodeCloser closer = ParserService.getNodeCloser();

      while (tokens.hasNext())
      {
         NodeConfig config = temp.getConfig();

         if (config.hidden())
            setter.add(creator.getLast(), temp);

         else if (temp.getType() == TypeToken.CLOSE)
            closer.close(config);

         else if (temp.getType() == TypeToken.TAG)
            setter.add(temp);

         else
            setter.add(creator.getLast(), temp);

         temp = tokens.pop();
      }

      move(ParserService.getRootNode());
      closer.close(ParserService.getRootNode());
   }

   private void move(HTMLNode root)
   {
      List<HTMLNode> children = root.getChildren();
      if (children == null || children.size() < 1)
         return;
      HTMLNode head = null;
      HTMLNode body = null;
      for (HTMLNode child : children)
      {
         if (child.isNode(Name.HEAD))
            head = child;
         if (child.isNode(Name.BODY))
            body = child;
      }
      if (head == null)
         head = ParserService.createHeader();
      if (body == null)
         body = ParserService.createBody();

      Iterator<HTMLNode> iter = children.iterator();
      while (iter.hasNext())
      {
         HTMLNode ele = iter.next();
         if (ele.isNode(Name.HEAD) || ele.isNode(Name.BODY))
            continue;
         if (ele.isNode(Name.SCRIPT))
         {
            head.addChild(ele);
            ele.setParent(head);
         }
         else
         {
            body.addChild(ele);
            ele.setParent(body);
         }
         iter.remove();
      }
   }

}
