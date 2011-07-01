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

import org.exoplatform.portal.mop.Visibility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PageNode extends PageNodeContainer
{

   /** . */
   private ArrayList<PageNode> children;

   /** . */
   private String uri;

   /** . */
   private String label;

   /** . */
   private String icon;

   /** . */
   private String name;

   /** . */
   private Date startPublicationDate;

   /** . */
   private Date endPublicationDate;

   /** . */
   private Visibility visibility = Visibility.DISPLAYED;

   /** . */
   private String pageReference;

   public PageNode()
   {
      this.children = new ArrayList<PageNode>();
   }

   public String getUri()
   {
      return uri;
   }

   public void setUri(String s)
   {
      uri = s;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String s)
   {
      label = s;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String s)
   {
      icon = s;
   }

   public String getPageReference()
   {
      return pageReference;
   }

   public void setPageReference(String s)
   {
      pageReference = s;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public List<PageNode> getChildren()
   {
      return children;
   }

   public void setChildren(ArrayList<PageNode> list)
   {
      children = list;
   }

   public Date getStartPublicationDate()
   {
      return startPublicationDate;
   }

   public void setStartPublicationDate(Date startDate)
   {
      startPublicationDate = startDate;
   }

   public Date getEndPublicationDate()
   {
      return endPublicationDate;
   }

   public void setEndPublicationDate(Date endDate)
   {
      endPublicationDate = endDate;
   }

   public void setVisibility(Visibility visibility)
   {
      this.visibility = visibility;
   }
   
   public Visibility getVisibility()
   {
      return this.visibility;
   }

   public PageNode getChild(String name)
   {
      if (children == null)
         return null;
      for (PageNode node : children)
      {
         if (node.getName().equals(name))
            return node;
      }
      return null;
   }

   public List<PageNode> getNodes()
   {
      return children;
   }

   @Override
   public String toString()
   {
      return "PageNode[" + name + "]";
   }
}