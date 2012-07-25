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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.bean.UIDataFeed;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(template = "system:/groovy/webui/core/UIVirtualList.gtmpl", events = {@EventConfig(listeners = UIVirtualList.LoadNextActionListener.class)})
@Serialized
public class UIVirtualList extends UIComponentDecorator
{

   private int pageSize = 1;
   
   private int height;
   
   private boolean autoAdjustHeight;

   public int getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }
   
   public int getHeight()
   {
      return height;
   }
   
   public void setHeight(int height)
   {
      this.height = height;
   }
   
   public boolean isAutoAdjustHeight()
   {
      return autoAdjustHeight;
   }
   
   public void setAutoAdjustHeight(boolean auto)
   {
      this.autoAdjustHeight = auto;
   }

   public String event(String name, String beanId) throws Exception
   {
      UIComponent parent = this.getParent();
      return parent.event(name, beanId);
   }

   public void dataBind(PageList datasource) throws Exception
   {
      UIDataFeed dataFeed = this.getDataFeed();
      datasource.setPageSize(this.getPageSize());
      dataFeed.setDataSource(datasource);
   }

   public UIDataFeed getDataFeed()
   {
      try
      {
         return (UIDataFeed)this.uicomponent_;
      }
      catch (Exception e)
      {
         throw new NullPointerException("dataFeed doesn't attached");
      }
   }

   static public class LoadNextActionListener extends EventListener<UIVirtualList>
   {
      public void execute(Event<UIVirtualList> event) throws Exception
      {
         UIVirtualList virtualList = event.getSource();
         UIDataFeed dataFeed = virtualList.getDataFeed();
         WebuiRequestContext rContext = event.getRequestContext();
         try
         {
            dataFeed.feedNext();
         }
         catch (NoSuchDataException e)
         {
            // Update parent of virtual list to refresh
            event.getRequestContext().addUIComponentToUpdateByAjax(virtualList.getParent());
            return;
         }
         
         RequireJS require = rContext.getJavascriptManager().require("SHARED/webui-ext", "webuiExt");
         if (dataFeed.hasNext())
         {
            require.addScripts("webuiExt.UIVirtualList.updateList('" + virtualList.getId() + "', true);");
         }
         else
         {
            require.addScripts("webuiExt.UIVirtualList.updateList('" + virtualList.getId() + "', false);");
         }
         
         rContext.addUIComponentToUpdateByAjax((UIComponent)dataFeed);
      }
   }
}
