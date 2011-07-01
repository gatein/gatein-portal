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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.mop.user.UserNode;

/**
 * Created by The eXo Platform SARL
 * Author : Tam Nguyen 
 *          tamndrok@gmail.com
 * Aug 28, 2009
 * 5:37:31 PM 
 */
public class TreeNode
{
   private boolean isExpanded_;

   private UserNode node_;

   private List<TreeNode> children_ = Collections.emptyList();

   private Map<String, TreeNode> cachedTreeNodes_;

   private TreeNode rootNode;

   public TreeNode()
   {
      cachedTreeNodes_ = new HashMap<String, TreeNode>();
      rootNode = this;
   }

   private TreeNode(UserNode node, TreeNode rootNode) throws Exception
   {
      node_ = node;
      isExpanded_ = false;
      this.rootNode = rootNode; 
      setChildren(node.getChildren());
   }

   public boolean isExpanded()
   {
      return isExpanded_;
   }

   public void setExpanded(boolean isExpanded)
   {
      isExpanded_ = isExpanded;
   }

   public UserNode getNode()
   {               
      return node_;
   }

   public List<TreeNode> getChildren()
   {
      return children_;
   }

   public int getChildrenSize()
   {
      return children_.size();
   }

   public void setChildren(Collection<UserNode> children) throws Exception
   {
      if (children == null)
         return;
                                   
      children_ = new LinkedList<TreeNode>();      
      for (UserNode child : children)
      {
         TreeNode node = new TreeNode(child, rootNode);
         children_.add(node);
         rootNode.cachedTreeNodes_.put(child.getId(), node);         
      }
   }

   public boolean hasChild()
   {
      return node_.getChildrenCount() > 0;
   }

   /**
    * Help to find nodes on the whole tree, not only in the childrens of this node
     * @param nodeId - id of the UserNode
    * @return null if not found
    */
   public TreeNode findNodes(String nodeId)
   {
      return rootNode.cachedTreeNodes_.get(nodeId);
   }
}