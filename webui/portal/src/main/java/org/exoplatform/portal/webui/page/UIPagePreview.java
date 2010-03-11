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

import org.exoplatform.portal.webui.application.UIApplication;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.core.UIComponentDecorator;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv@exoplatform.com
 * Aug 23, 2006  
 */

@ComponentConfigs({@ComponentConfig(lifecycle = UIComponentDecorator.UIComponentDecoratorLifecycle.class),
   @ComponentConfig(id = "UIPagePreviewWithMessage", template = "system:/groovy/portal/webui/page/UIPagePreview.gtmpl"
   //events = @EventConfig(listeners = UIPagePreview.BackActionListener.class)
   )})
public class UIPagePreview extends UIComponentDecorator
{
   public boolean isPageHasApplication()
   {
      if (uicomponent_ == null)
         return false;
      UIApplication existingApp = uicomponent_.findFirstComponentOfType(UIApplication.class);
      if (existingApp != null)
         return true;

      return false;
   }
}
