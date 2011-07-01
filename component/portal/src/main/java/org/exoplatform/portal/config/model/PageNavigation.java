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

package org.exoplatform.portal.config.model;

import java.util.ArrayList;

public class PageNavigation extends PageNodeContainer
{

   /** . */
   private String ownerType;

   /** . */
   private String ownerId;

   /** . */
   private ArrayList<PageNode> pageNodes;

   /** . */
   private int priority = 1;

   public PageNavigation()
   {
      this.pageNodes = new ArrayList<PageNode>();
   }

   public String getOwnerId()
   {
      return ownerId;
   }

   public void setOwnerId(String ownerId)
   {
      this.ownerId = ownerId;
   }

   public String getOwnerType()
   {
      return ownerType;
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public int getPriority()
   {
      return priority;
   }

   public void setPriority(int i)
   {
      priority = i;
   }

   public ArrayList<PageNode> getNodes()
   {
      return pageNodes;
   }

   public void setNodes(ArrayList<PageNode> nodes)
   {
      pageNodes = nodes;
   }

   @Override
   public String toString()
   {
      return "PageNavigation[ownerType=" + ownerType + ",ownerId=" + ownerId + "]";
   }
}