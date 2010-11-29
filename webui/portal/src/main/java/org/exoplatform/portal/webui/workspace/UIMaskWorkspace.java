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

package org.exoplatform.portal.webui.workspace;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/** Created by The eXo Platform SAS Mar 13, 2007 */
@ComponentConfig(id = "UIMaskWorkspace", template = "system:/groovy/portal/webui/workspace/UIMaskWorkspace.gtmpl",
   events = @EventConfig(phase = Phase.DECODE, listeners = UIMaskWorkspace.CloseActionListener.class))
public class UIMaskWorkspace extends UIComponentDecorator
{

   private int width_ = -1;

   private int height_ = -1;

   private boolean isShow = false;

   private boolean isUpdated = false;

   public int getWindowWidth()
   {
      return width_;
   }

   public int getWindowHeight()
   {
      return height_;
   }

   public void setWindowSize(int w, int h)
   {
      width_ = w;
      height_ = h;
   }

   public boolean isShow()
   {
      return isShow;
   }

   public void setShow(boolean bln)
   {
      this.isShow = bln;
      if (bln == false)
      {
         isUpdated = false;
      }
   }

   public boolean isUpdated()
   {
      return isUpdated;
   }

   public void setUpdated(boolean bln)
   {
//      this.isUpdated = bln;
   }

   public <T extends UIComponent> T createUIComponent(Class<T> clazz, String configId, String id) throws Exception
   {
      T uicomponent = super.createUIComponent(clazz, configId, id);
      setUIComponent(uicomponent);
      return uicomponent;
   }

   public <T extends UIComponent> T createUIComponent(Class<T> clazz) throws Exception
   {
      return createUIComponent(clazz, null, null);
   }

   public UIComponent setUIComponent(UIComponent uicomponent)
   {
      UIComponent oldOne = super.setUIComponent(uicomponent);
      setShow(uicomponent != null);
      return oldOne;
   }

   static public class CloseActionListener extends EventListener<UIComponent>
   {
      public void execute(Event<UIComponent> event) throws Exception
      {
         UIMaskWorkspace uiMaskWorkspace = null;
         UIComponent uiSource = event.getSource();
         if (uiSource instanceof UIMaskWorkspace)
         {
            uiMaskWorkspace = (UIMaskWorkspace)uiSource;
         }
         else
         {
            uiMaskWorkspace = uiSource.getAncestorOfType(UIMaskWorkspace.class);
         }
         if (uiMaskWorkspace == null || !uiMaskWorkspace.isShow())
         {
            return;
         }
         uiMaskWorkspace.setUIComponent(null);
         uiMaskWorkspace.setWindowSize(-1, -1);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWorkspace);
      }
   }
}
