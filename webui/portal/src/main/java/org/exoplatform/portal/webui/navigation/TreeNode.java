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

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Tam Nguyen 
 *          tamndrok@gmail.com
 * Aug 28, 2009
 * 5:37:31 PM 
 */
public class TreeNode
{
   //TODO Need use this class for BC TreeNode
   private boolean isExpanded_;

   private boolean hasChild_;

   private String path_;

   private PageNode node_;

   private PageNavigation navigation_;

   private List<TreeNode> children_ = new ArrayList<TreeNode>();

   public TreeNode(PageNode node, PageNavigation nav, boolean hasChild)
   {
      this(node, node.getUri(), nav, hasChild);
   }

   private TreeNode(PageNode node, String path, PageNavigation nav, boolean hasChild)
   {
      node_ = node;
      navigation_ = nav;
      isExpanded_ = false;
      path_ = path;
      hasChild_ = hasChild;
   }

   public boolean isExpanded()
   {
      return isExpanded_;
   }

   public void setExpanded(boolean isExpanded)
   {
      isExpanded_ = isExpanded;
   }

   public String getName() throws RepositoryException
   {
      return node_.getName();
   }

   public String getPath()
   {
      return path_;
   }

   public String getNodePath() throws RepositoryException
   {
      return node_.getUri();
   }

   public PageNode getNode()
   {
      return node_;
   }

   public void setNode(PageNode node)
   {
      node_ = node;
   }

   public List<TreeNode> getChildren()
   {
      return children_;
   }

   public int getChildrenSize()
   {
      return children_.size();
   }

   public TreeNode getChildByPath(String path, TreeNode treeNode)
   {
      TreeNode returnVal = null;

      for (TreeNode child : treeNode.getChildren())
      {

         if (returnVal != null)
            continue;

         if (child.getPath().equals(path))
         {
            returnVal = child;
         }
         else if (child.getChildren() != null)
         {
            returnVal = child.getChildByPath(path, child);
         }
      }

      return returnVal;
   }

   public void setChildren(List<PageNode> children, PageNavigation nav) throws Exception
   {
      setExpanded(true);
      for (PageNode child : children)
      {
         boolean isHasChild = (child.getChildren().size() > 0);
         children_.add(new TreeNode(child, nav.getId() + "::" + child.getUri(), nav, isHasChild));
      }
   }

   public void addChildren(TreeNode treeNode)
   {
      children_.add(treeNode);
   }

   public void setNavigation(PageNavigation navigation_)
   {
      this.navigation_ = navigation_;
   }

   public PageNavigation getNavigation()
   {
      return navigation_;
   }

   public void setHasChild(boolean hasChild)
   {
      this.hasChild_ = hasChild;
   }

   public boolean isHasChild()
   {
      return hasChild_;
   }
}