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

package org.exoplatform.portal.webui.navigation;

import java.util.Iterator;

import org.exoplatform.portal.mop.navigation.NodeChange;
import org.exoplatform.portal.mop.navigation.NodeChangeQueue;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;

@ComponentConfig(
   template = "system:/groovy/portal/webui/navigation/UIPageNodeSelector.gtmpl"
)
public class UIPageNodeSelector extends UIContainer
{
   private UserNode rootNode;
   
   private UserNode selectedNode;
   
   private UserPortal userPortal;

   public UIPageNodeSelector() throws Exception
   {
      UITree uiTree = addChild(UITree.class, null, "TreePageSelector");
      uiTree.setIcon("DefaultPageIcon");
      uiTree.setSelectedIcon("DefaultPageIcon");
      uiTree.setBeanIdField("URI");
      uiTree.setBeanLabelField("encodedResolvedLabel");
      uiTree.setBeanIconField("icon");
      uiTree.setBeanChildCountField("childrenCount");

      userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();      
   }  
   
   public void configure(UserNode node) throws Exception
   {
      if (node == null)
      {
         throw new IllegalArgumentException("node can't be null");
      }
      
      this.rootNode = node;
      while (rootNode.getParent() != null)
      {
         this.rootNode = rootNode.getParent();
      }
      setSelectedNode(node);
   }
   
   private void setSelectedNode(UserNode node) throws Exception
   {
      //If node is root node, and it's been deleted --> throw NavigationServiceException
      node = updateNode(node);
      
      //If node has been deleted --> select root node
      if(node == null)
      {
         node = getRootNode();
      }
      
      UITree tree = getChild(UITree.class);
      tree.setSelected(node);
      UserNode parent = node.getParent();
      if (parent != null)
      {        
         tree.setChildren(node.getChildren());
         tree.setSibbling(parent.getChildren());
         tree.setParentSelected(parent);
      }
      else
      {
         tree.setChildren(null);
         tree.setSibbling(node.getChildren());
         tree.setParentSelected(node);
      }
      selectedNode = node;
   }
   
   private UserNode updateNode(UserNode node) throws Exception
   {
      if (node == null) 
      {
         return null;
      }
      
      NodeChangeQueue<UserNode> queue = new NodeChangeQueue<UserNode>();
      userPortal.updateNode(node, Scope.GRANDCHILDREN, queue);
      for (NodeChange<UserNode> change : queue)
      {
         if (change instanceof NodeChange.Removed)
         {
            UserNode deletedNode = ((NodeChange.Removed<UserNode>)change).getTarget();
            if (findUserNodeByURI(deletedNode, node.getURI()) != null)
            {
               return null;
            }
         }
      }
      return node;
   }
   
   public void setSelectedURI(String uri) throws Exception
   {
      if (selectedNode == null)
      {
         throw new IllegalStateException("selectedNode is null, configure method must be called first");
      }
      
      UserNode node;
      if (selectedNode.getParent() != null)
      {
         node = findUserNodeByURI(selectedNode.getParent(), uri);
      }
      else
      {
         node = findUserNodeByURI(selectedNode, uri);
      }
      setSelectedNode(node);
   }
   
   private UserNode findUserNodeByURI(UserNode rootNode, String uri)
   {
      if (rootNode.getURI().equals(uri))
      {
         return rootNode;
      }
      Iterator<UserNode> iterator = rootNode.getChildren().iterator();
      while (iterator.hasNext())
      {
         UserNode next = iterator.next();
         UserNode node = findUserNodeByURI(next, uri);
         if (node == null)
         {
            continue;
         }
         return node;
      }
      return null;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIRightClickPopupMenu uiPopupMenu = getChild(UIRightClickPopupMenu.class);
      if (uiPopupMenu != null)
      {
         uiPopupMenu.setRendered(true);
      }
      super.processRender(context);
   }
   
   private UserNode getRootNode()
   {
      return this.rootNode;
   }
   
   public UserNode getSelectedNode()
   {
      return selectedNode;
   }

   public UserNavigation getNavigation()
   {
      return selectedNode.getNavigation();
   }   
}
