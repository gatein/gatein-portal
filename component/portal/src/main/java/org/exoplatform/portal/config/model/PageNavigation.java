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

import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationNodeData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PageNavigation extends PageNodeContainer
{

   private String ownerType;

   private String ownerId;

   private transient boolean modifiable;

   private ArrayList<PageNode> pageNodes;

   private int priority = 1;

   PageNavigation(String storageId)
   {
      super(storageId);

      //
      this.pageNodes = new ArrayList<PageNode>();
   }

   public PageNavigation()
   {
      this((String)null);
   }

   public PageNavigation(NavigationData nav)
   {
      super(nav.getStorageId());

      ArrayList<PageNode> children = new ArrayList<PageNode>(nav.getNodes().size());
      for (NavigationNodeData child : nav.getNodes())
      {
         PageNode node = new PageNode(child);
         children.add(node);
      }

      //
      this.ownerType = nav.getOwnerType();
      this.ownerId = nav.getOwnerId();
      this.priority = nav.getPriority();
      this.pageNodes = children;
   }

   // Make gtmpl happy with that for now
   public String getDescription()
   {
      return null;
   }

   public int getId()
   {
      return getOwner().hashCode();
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

   public boolean isModifiable()
   {
      return modifiable;
   }

   public void setModifiable(boolean b)
   {
      modifiable = b;
   }

   public int getPriority()
   {
      return priority;
   }

   public void setPriority(int i)
   {
      priority = i;
   }

   public String getOwner()
   {
      return ownerType + "::" + ownerId;
   }

   public void addNode(PageNode node)
   {
      if (pageNodes == null)
         pageNodes = new ArrayList<PageNode>();
      pageNodes.add(node);
   }

   public ArrayList<PageNode> getNodes()
   {
      return pageNodes;
   }

   public void setNodes(ArrayList<PageNode> nodes)
   {
      pageNodes = nodes;
   }

   public PageNode getNode(String name)
   {
      for (PageNode node : pageNodes)
      {
         if (node.getName().equals(name))
            return node;
      }
      return null;
   }

   public PageNavigation clone()
   {
      PageNavigation newNav = new PageNavigation();
      newNav.setOwnerId(ownerId);
      newNav.setOwnerType(ownerType);
      newNav.setPriority(priority);
      newNav.setModifiable(modifiable);

      if (pageNodes == null || pageNodes.isEmpty())
         return newNav;
      for (PageNode ele : pageNodes)
      {
         newNav.getNodes().add(ele.clone());
      }
      return newNav;
   }

   public void merge(PageNavigation nav)
   {
      if (ownerId == null)
         setOwnerId(nav.ownerId);
      if (ownerType == null)
         setOwnerType(nav.ownerType);
      if (priority == 1)
         setPriority(nav.priority);
      if (!modifiable)
         setModifiable(nav.modifiable);

      if (nav.pageNodes == null || nav.pageNodes.isEmpty())
      {
         return;
      }
      if (pageNodes == null || pageNodes.isEmpty())
      {
         this.pageNodes = nav.pageNodes;
         return;
      }
      Map<String, PageNode> mPageNodes = new LinkedHashMap<String, PageNode>();
      for (PageNode node : nav.pageNodes)
      {
         mPageNodes.put(node.getName(), node);
      }
      if (pageNodes != null)
      {
         for (PageNode node : pageNodes)
         {
            mPageNodes.put(node.getName(), node);
         }
      }
      this.pageNodes = new ArrayList<PageNode>(mPageNodes.values());
   }

   @Override
   public String toString()
   {
      return "PageNavigation[ownerType=" + ownerType + ",ownerId=" + ownerId + "]";
   }

   @Override
   public NavigationData build()
   {
      List<NavigationNodeData> children = buildNavigationChildren();
      return new NavigationData(storageId, ownerType, ownerId, priority, children);
   }
}