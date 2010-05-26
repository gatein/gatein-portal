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

package org.exoplatform.portal.webui.page;

import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.MoveChildActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

/**
 * May 19, 2006
 */
@ComponentConfig(lifecycle = UIPageLifecycle.class, template = "system:/groovy/portal/webui/page/UIPage.gtmpl", events = {@EventConfig(listeners = MoveChildActionListener.class)})
public class UIPage extends UIContainer
{

   /** . */
   private String pageId;

   private String ownerId;

   private String ownerType;

   private String editPermission;

   private boolean showMaxWindow = false;

   private UIPortlet maximizedUIPortlet;

   public String getOwnerId()
   {
      return ownerId;
   }

   public void setOwnerId(String s)
   {
      ownerId = s;
   }

   public boolean isShowMaxWindow()
   {
      return showMaxWindow;
   }

   public void setShowMaxWindow(Boolean showMaxWindow)
   {
      this.showMaxWindow = showMaxWindow;
   }

   public String getEditPermission()
   {
      return editPermission;
   }

   public void setEditPermission(String editPermission)
   {
      this.editPermission = editPermission;
   }

   public String getPageId()
   {
      return pageId;
   }

   public void setPageId(String id)
   {
      pageId = id;
   }

   public UIPortlet getMaximizedUIPortlet()
   {
      return maximizedUIPortlet;
   }

   public String getOwnerType()
   {
      return ownerType;
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public void setMaximizedUIPortlet(UIPortlet maximizedUIPortlet)
   {
      this.maximizedUIPortlet = maximizedUIPortlet;
   }
}