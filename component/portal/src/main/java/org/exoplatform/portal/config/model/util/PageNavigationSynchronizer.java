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
public class PageNavigationSynchronizer
{

   private PageNavigation wrappedPageNavigation;
   
   private List<PageNavigation> tobeSynchronizedNavigations;
   
   public PageNavigationSynchronizer(PageNavigation _wrappedPageNavigation)
   {
      this.wrappedPageNavigation = _wrappedPageNavigation;
      this.tobeSynchronizedNavigations = new ArrayList<PageNavigation>();
   }
   
   public void start(List<PageNavigation> _synchronizedNavigations)
   {
      this.tobeSynchronizedNavigations = _synchronizedNavigations;
   }
   
   public void stopSynchronizing(List<PageNavigation> notSynchronizedAnymoreNavs)
   {
      
   }
   
   public void stop()
   {
      
   }
   
   /**
    *  Add a page node to the <code>wrappedPageNavigation</code> as well as synchronize this action to all navigations in list of 
    * tobeSynchronizedNavigations
    *
    */
   public void addPageNode(PageNode pageNode, String parentUri)
   {
      PageNavigationUtil.addPageNode(wrappedPageNavigation, pageNode, parentUri);
      for(PageNavigation synchronizedNav : tobeSynchronizedNavigations)
      {
         PageNavigationUtil.addPageNode(synchronizedNav, pageNode, parentUri);
      }
   }
   
   
   public void removePageNode(PageNode pageNode, String parentUri)
   {
      PageNavigationUtil.removePageNode(wrappedPageNavigation, pageNode, parentUri);
      for(PageNavigation synchronizedNav : tobeSynchronizedNavigations)
      {
         PageNavigationUtil.removePageNode(synchronizedNav, pageNode, parentUri);
      }
   }
   
   public void movePageNode(PageNode pageNode, String parentUri, boolean moveUp)
   {
    
   }
   
   
}
