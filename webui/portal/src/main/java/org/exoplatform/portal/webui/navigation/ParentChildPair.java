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
package org.exoplatform.portal.webui.navigation;

import org.exoplatform.portal.config.model.PageNode;

/**
 * This class wrappes a pair of PageNode and its parent.
 * 
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class ParentChildPair
{
   private PageNode parentNode;
   
   private PageNode childNode;
   
   public ParentChildPair(PageNode _parentNode, PageNode _childNode)
   {
      this.parentNode = _parentNode;
      this.childNode = _childNode;
   }
   
   public PageNode getParentNode()
   {
      return parentNode;
   }
   
   public PageNode getChildNode()
   {
      return childNode;
   }
   
   public void setParentNode(PageNode _parentNode)
   {
      this.parentNode = _parentNode;
   }
   
   public void setChildNode(PageNode _childNode)
   {
      this.childNode = _childNode;
   }
}
