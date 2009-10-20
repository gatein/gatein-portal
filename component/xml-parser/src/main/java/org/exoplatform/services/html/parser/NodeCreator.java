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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Aug 13, 2006
 */
final class NodeCreator
{

   private List<NodeImpl> opens = new ArrayList<NodeImpl>();

   List<NodeImpl> getOpens()
   {
      return opens;
   }

   NodeImpl getLast()
   {
      return opens.get(opens.size() - 1);
   }

   NodeImpl getOpenParent(NodeConfig config, boolean create)
   {
      List<Name[]> list = new ArrayList<Name[]>();
      while (config.parent().length > 0)
      {
         list.add(0, config.parent());
         config = HTML.getConfig(config.parent()[0]);
      }
      if (opens.size() < 1)
         return null;
      if (list.size() < 1)
         return opens.get(opens.size() - 1);
      NodeImpl parent = opens.get(opens.size() - 1);
      NodeImpl impl = null;
      Iterator<Name[]> iter = list.iterator();
      boolean start = false;
      while (iter.hasNext())
      {
         Name[] names = iter.next();
         if (start)
         {
            List<HTMLNode> children = parent.getChildrenNode();
            for (int i = children.size() - 1; i > -1; i--)
            {
               NodeImpl child = (NodeImpl)children.get(i);
               if (!child.isOpen())
                  break;
               for (Name name : names)
               {
                  if (child.getName() != name)
                     continue;
                  impl = child;
                  break;
               }
            }
         }
         else
         {
            impl = getOpenNode(names);
         }
         if (impl == null)
         {
            if (create)
               return createNode(list, parent);
            return null;
         }
         parent = impl;
         impl = null;
         iter.remove();
         start = true;
      }
      return parent;
   }

   private NodeImpl createNode(List<Name[]> list, NodeImpl parent)
   {
      NodeImpl child = null;
      for (Name[] names : list)
      {
         Name name = names[0];
         child = new NodeImpl(name.toString().toCharArray(), name, TypeToken.TAG);
         if (child.getConfig().only())
         {
            parent = ParserService.getNodeSetter().set(child);
         }
         else
         {
            parent.addChild(child);
            child.setParent(parent);
            opens.add(child);
            parent = child;
         }
      }
      return parent;
   }

   private NodeImpl getOpenNode(Name[] names)
   {
      for (int i = opens.size() - 1; i > -1; i--)
      {
         for (Name name : names)
         {
            if (opens.get(i).getConfig().name() == name)
            {
               return opens.get(i);
            }
         }
         if (opens.get(i).getConfig().block())
            break;
      }
      return null;
   }
}
