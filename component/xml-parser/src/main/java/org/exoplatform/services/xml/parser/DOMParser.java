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

package org.exoplatform.services.xml.parser;

import org.exoplatform.services.token.TypeToken;

import java.util.List;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Aug 3, 2006
 */
class DOMParser
{

   static void parse(XMLToken tokens, XMLNode root)
   {
      XMLNode temp = tokens.pop();
      XMLNode current = root;
      while (tokens.hasNext())
      {
         if (temp.getType() == TypeToken.CLOSE)
         {
            current = closeNode(temp.getName(), current);
         }
         else
         {
            current.addChild(temp);
            if (temp.getType() == TypeToken.TAG)
               current = temp;
         }
         temp = tokens.pop();
      }
      closeAll(root);
   }

   private static XMLNode closeNode(String name, XMLNode n)
   {
      XMLNode node = n;
      while (node.getParent() != null)
      {
         if (node.isOpen() && node.isNode(name))
         {
            closeAll(node);
            return node.getParent();
         }
         node = node.getParent();
      }
      return n;
   }

   static private void closeAll(XMLNode node)
   {
      if (!node.isOpen())
         return;
      XMLNode ele;
      List<XMLNode> children = node.getChildren();
      for (int i = 0; i < children.size(); i++)
      {
         ele = children.get(i);
         closeAll(ele);
      }
      node.setIsOpen(false);
   }

}
