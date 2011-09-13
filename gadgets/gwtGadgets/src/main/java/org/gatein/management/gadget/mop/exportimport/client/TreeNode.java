/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.management.gadget.mop.exportimport.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code TreeNode}
 * <p/>
 * Created on Dec 29, 2010, 1:25:55 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class TreeNode implements Serializable
{

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private TreeNode parent;
   private List<TreeNode> children;
   private String text;
   private String type;
   private String siteName;
   private String nodeInfo;
   private boolean updated = false;
   private boolean exportable;

   /**
    * Create a new instance of {@code TreeNode}
    */
   public TreeNode()
   {
      this("");
   }

   /**
    * Create a new instance of {@code TreeNode}
    *
    * @param text The tree node text
    */
   public TreeNode(String text)
   {
      this.text = text;
      this.children = new ArrayList<TreeNode>();
   }

   /**
    * Create a new instance of {@code TreeNode}
    *
    * @param text     The tree node text
    * @param children The tree node children
    */
   public TreeNode(String text, List<TreeNode> children)
   {
      this.text = text;
      this.children = children;
   }

   /**
    * Return the node informations
    *
    * @return The node informations
    */
   public String getNodeInfo()
   {
      return this.nodeInfo;
   }

   /**
    * Setter for the node info
    *
    * @param info the info to set
    */
   public void setNodeInfo(String info)
   {
      this.nodeInfo = info;
   }

   /**
    * Add a {@code TreeNode} child to the list of children
    *
    * @param child The child to add
    * @return {@code true} if the child is added successfully else {@code false}
    */
   public boolean addChild(TreeNode child)
   {
      child.setParent(this);
      return this.children.add(child);
   }

   /**
    * Remove the child, if exists, from the list of children
    *
    * @param tn The child to be removed
    * @return {@code true} if the child is removed successfully else {@code false}
    */
   public boolean removeChild(TreeNode tn)
   {
      return this.children.remove(tn);
   }

   /**
    * @return the children
    */
   public List<TreeNode> getChildren()
   {
      return this.children;
   }

   /**
    * Setter for the children
    *
    * @param children the children to set
    */
   public void setChildren(List<TreeNode> children)
   {
      this.children = children;
   }

   /**
    * @return the text
    */
   public String getText()
   {
      return text;
   }

   /**
    * Setter for the text
    *
    * @param text the text to set
    */
   public void setText(String text)
   {
      this.text = text;
   }

   /**
    * @return the type
    */
   public String getType()
   {
      return type;
   }

   /**
    * @param type the type to set
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * @return the path
    */
   public String getPath()
   {
      return (this.parent != null ? this.parent.getPath() : "") + " &raquo; " + this.text;
   }

   /**
    * @return the updated
    */
   public boolean isUpdated()
   {
      return updated;
   }

   /**
    * @param updated the updated to set
    */
   public void setUpdated(boolean updated)
   {
      this.updated = updated;
   }

   /**
    * @return the siteName
    */
   public String getSiteName()
   {
      return siteName;
   }

   /**
    * @param siteName the siteName to set
    */
   public void setSiteName(String siteName)
   {
      this.siteName = siteName;
   }

   /**
    * @return the exportable
    */
   public boolean isExportable()
   {
      return exportable;
   }

   /**
    * @param exportable the exportable to set
    */
   public void setExportable(boolean exportable)
   {
      this.exportable = exportable;
   }

   /**
    * @return the parent
    */
   public TreeNode getParent()
   {
      return parent;
   }

   /**
    * @param parent the parent to set
    */
   public void setParent(TreeNode parent)
   {
      this.parent = parent;
   }
}
