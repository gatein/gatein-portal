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
package org.exoplatform.portal.config.model.util;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class PageNavigationUtil
{

   public static void addPageNode(PageNavigation pageNav, PageNode pageNode, String parentUri)
   {
      PageNode parentNode = searchPageNodeByUri(pageNav, parentUri);
      if(parentNode != null)
      {
         parentNode.getChildren().add(pageNode);
      }
   }
   
   public static void removePageNode(PageNavigation pageNav, PageNode pageNode, String parentUri)
   {
      PageNode parentNode = searchPageNodeByUri(pageNav, parentUri);
      if(parentNode != null)
      {
         String nodeUri = pageNode.getUri();
         ArrayList<PageNode> nodes = new ArrayList<PageNode>();
         for(PageNode node : parentNode.getNodes())
         {
            if(nodeUri.equals(node.getUri()))
            {
               continue;
            }
            nodes.add(node);
         }
         parentNode.setChildren(nodes);
      }
   }
   
   public static PageNode searchPageNodeByUri(PageNavigation pageNav, String uri)
   {
      List<PageNode> children = pageNav.getNodes();
      for(PageNode child : children)
      {
         PageNode tempNode = searchDescendantNodeByUri(child, uri);
         if(tempNode != null)
         {
            return tempNode;
         }
      }
      return null;
   }
   
   private static PageNode searchDescendantNodeByUri(PageNode rootNode, String uri)
   {
      if(uri.equals(rootNode.getUri()))
      {
         return rootNode;
      }
      List<PageNode> children = rootNode.getChildren();
      if(children == null)
      {
         return null;
      }
      for(PageNode intermediateNode : children)
      {
         PageNode tempNode = searchDescendantNodeByUri(intermediateNode, uri);
         if(tempNode != null)
         {
            return tempNode;
         }
      }
      return null;
   }
}
