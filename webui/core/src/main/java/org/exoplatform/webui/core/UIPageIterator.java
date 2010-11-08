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

package org.exoplatform.webui.core;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.EmptySerializablePageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A component that allows pagination, with an iterator to change pages
 *
 */
@ComponentConfig(template = "system:/groovy/webui/core/UIPageIterator.gtmpl", events = @EventConfig(listeners = UIPageIterator.ShowPageActionListener.class))
@Serialized
public class UIPageIterator extends UIComponent
{
   /**
    * The list of pages
    */
   private PageList pageList_ = EmptySerializablePageList.get();

   private Set<String> selectedItems = new HashSet<String>();
   
   public UIPageIterator()
   {
   }

   public void setPageList(PageList pageList)
   {
      pageList_ = pageList;
   }

   public PageList getPageList()
   {
      return pageList_;
   }

   public int getAvailablePage()
   {
      return pageList_.getAvailablePage();
   }

   public int getCurrentPage()
   {
      return pageList_.getCurrentPage();
   }

   public List getCurrentPageData() throws Exception
   {
      return pageList_.currentPage();
   }

   public int getAvailable()
   {
      return pageList_.getAvailable();
   }

   public int getFrom()
   {
      return pageList_.getFrom();
   }

   public int getTo()
   {
      return pageList_.getTo();
   }

   public Object getObjectInPage(int index) throws Exception
   {
      return pageList_.currentPage().get(index);
   }

   public void setCurrentPage(int page) throws Exception
   {
      pageList_.getPage(page);
   }
   
   public void setSelectedItem(String key, boolean value)
   {
      if (value == false && this.selectedItems.contains(key))
      {
         selectedItems.remove(key);
      }
      else if (value)
      {
         selectedItems.add(key);
      }
   }
   
   public Set<String> getSelectedItems()
   {
      return selectedItems;
   }
   
   public boolean isSelectedItem(String key)
   {
      return selectedItems.contains(key);
   }

   @SuppressWarnings("unused")
   static public class ShowPageActionListener extends EventListener<UIPageIterator>
   {
      public void execute(Event<UIPageIterator> event) throws Exception
      {
         UIPageIterator uiPageIterator = event.getSource();
         int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
         uiPageIterator.setCurrentPage(page);
         UIComponent parent = uiPageIterator.getParent();
         if (parent == null)
            return;
         event.getRequestContext().addUIComponentToUpdateByAjax(parent);
         parent.broadcast(event, event.getExecutionPhase());
      }
   }

}
