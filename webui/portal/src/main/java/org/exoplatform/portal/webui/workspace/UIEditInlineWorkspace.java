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

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Tan Pham Dinh
 *          pdtanit@gmail.com
 * Aug 27, 2009  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIEditInlineWorkspace extends UIContainer
{

   public UIEditInlineWorkspace() throws Exception
   {
      addChild(UIPortalComposer.class, null, null);
      addChild(UIPortalToolPanel.class, null, null);
   }

   public void setUIComponent(UIComponent uiComp)
   {
      getChild(UIPortalToolPanel.class).setUIComponent(uiComp);
   }

   public UIComponent getUIComponent()
   {
      return getChild(UIPortalToolPanel.class).getUIComponent();
   }

   public UIPortalComposer getComposer()
   {
      return getChild(UIPortalComposer.class);
   }
}
