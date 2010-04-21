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

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.data.NavigationNodeData;
import org.gatein.common.text.EntityEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class PageNode extends PageNodeContainer
{

   private ArrayList<PageNode> children;

   private String uri;

   private String label;

   private String icon;

   private String name;

   private String resolvedLabel;
   
   private String encodedResolvedLabel;

   private Date startPublicationDate;

   private Date endPublicationDate;

   private Visibility visibility = Visibility.DISPLAYED;

   private String pageReference;

   private transient boolean modifiable;

   public PageNode(NavigationNodeData nav)
   {
      super(nav.getStorageId());

      //
      ArrayList<PageNode> children = new ArrayList<PageNode>(nav.getNodes().size());
      for (NavigationNodeData child : nav.getNodes())
      {
         PageNode node = new PageNode(child);
         children.add(node);
      }

      //
      this.uri = nav.getURI();
      this.label = nav.getLabel();
      this.resolvedLabel = nav.getLabel();
      this.encodedResolvedLabel = null;
      this.icon = nav.getIcon();
      this.name = nav.getName();
      this.startPublicationDate = nav.getStartPublicationDate();
      this.endPublicationDate = nav.getEndPublicationDate();
      this.visibility = nav.getVisibility();
      this.pageReference = nav.getPageReference();
      this.children = children;
   }

   public PageNode(String storageId)
   {
      super(storageId);

      //
      this.children = new ArrayList<PageNode>();
   }

   public PageNode()
   {
      this((String)null);
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
      resolvedLabel = s;
      encodedResolvedLabel = null;
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

   public String getResolvedLabel()
   {
      return resolvedLabel;
   }
   
   public String getEncodedResolvedLabel()
   {
	   EntityEncoder encoder = EntityEncoder.FULL;
	   if (encodedResolvedLabel == null)
	   {
		   encodedResolvedLabel = encoder.encode(resolvedLabel);
	   }
	   return encodedResolvedLabel;
   }

   public void setResolvedLabel(String res)
   {
      resolvedLabel = res;
      encodedResolvedLabel = null;
   }

   public void setResolvedLabel(ResourceBundle res)
   {
      resolvedLabel = ExpressionUtil.getExpressionValue(res, label);
      if (resolvedLabel == null)
         resolvedLabel = getName();
      encodedResolvedLabel = null;
   }

   public List<PageNode> getChildren()
   {
      return children;
   }

   public void setChildren(ArrayList<PageNode> list)
   {
      children = list;
   }

   public boolean isModifiable()
   {
      return modifiable;
   }

   public void setModifiable(boolean b)
   {
      modifiable = b;
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

   public boolean isDisplay()
   {
      switch (visibility)
      {
         case DISPLAYED:
            return true;
         case HIDDEN:
            return false;
         case TEMPORAL:
            return isInPublicationDate();
         case SYSTEM:
            return false;
         default:
            throw new AssertionError();
      }
   }

   public boolean isVisible()
   {
      switch (visibility)
      {
         case DISPLAYED:
         case TEMPORAL:
            return true;
         case SYSTEM:
         case HIDDEN:
            return false;
         default:
            throw new AssertionError();
      }
   }

   public boolean isSystem() {
	  switch(visibility) {
	   	 case SYSTEM:
	   		 return true;
	   	 case TEMPORAL:
	   	 case HIDDEN:
	   	 case DISPLAYED:
	   		 return false;
	   	 default:
	   		 throw new AssertionError();
	  }
   }
   public void setVisible(Boolean b)
   {
      if (b != null)
      {
         switch (visibility)
         {
            case SYSTEM:
               break;
            case HIDDEN:
            case DISPLAYED:
            case TEMPORAL:
               visibility = b ? Visibility.DISPLAYED : Visibility.HIDDEN;
               break;
         }
      }
   }

   public void setVisibility(Visibility visibility)
   {
      this.visibility = visibility;
   }
   
   public Visibility getVisibility()
   {
      return this.visibility;
   }

   private boolean isInPublicationDate()
   {
      Date currentDate = new Date();
      
      // Case 1: start date, end date are not null and current date is between start and end date
      boolean case1 = (startPublicationDate != null) && (endPublicationDate != null) && (currentDate.compareTo(startPublicationDate) >= 0 && currentDate.compareTo(endPublicationDate) <= 0);
      
      // Case 2: start date is null, end date is not null and current date is before end date
      boolean case2 = (startPublicationDate == null) && (endPublicationDate != null) && (currentDate.compareTo(endPublicationDate) <= 0);
      
      // Case 3: start date is not null, end date is null and current date is after start date
      boolean case3 = (startPublicationDate != null) && (endPublicationDate == null) && (currentDate.compareTo(startPublicationDate) >= 0);
      
      // Case 4: start date and end date are null both
      boolean case4 = (startPublicationDate == null) && (endPublicationDate == null);
      
      return case1 || case2 || case3 || case4;
   }

   public void setShowPublicationDate(Boolean show)
   {
      if (show != null)
      {
         switch (visibility)
         {
            case SYSTEM:
            case HIDDEN:
               break;
            case TEMPORAL:
            case DISPLAYED:
               visibility = show ? Visibility.TEMPORAL : Visibility.DISPLAYED;
               break;
         }
      }
   }

   public boolean isShowPublicationDate()
   {
      return visibility == Visibility.TEMPORAL;
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

   public PageNode clone()
   {
      PageNode newNode = new PageNode();
      newNode.setUri(uri);
      newNode.setLabel(label);
      newNode.setIcon(icon);
      newNode.setName(name);
      newNode.setResolvedLabel(resolvedLabel);
      newNode.setPageReference(pageReference);
      newNode.setModifiable(modifiable);
      newNode.setStartPublicationDate(startPublicationDate);
      newNode.setEndPublicationDate(endPublicationDate);
      newNode.setVisibility(visibility);
      if (children == null || children.size() < 1)
         return newNode;
      for (PageNode ele : children)
      {
         newNode.getChildren().add(ele.clone());
      }
      return newNode;
   }

   @Override
   public NavigationNodeData build()
   {
      List<NavigationNodeData> children = buildNavigationChildren();
      return new NavigationNodeData(
         storageId,
         uri,
         label,
         icon,
         name,
         startPublicationDate,
         endPublicationDate,
         visibility,
         pageReference,
         children
      );
   }
}