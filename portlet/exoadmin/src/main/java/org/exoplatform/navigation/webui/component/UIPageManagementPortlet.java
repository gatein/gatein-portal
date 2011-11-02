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

package org.exoplatform.navigation.webui.component;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.webui.page.UIPageBrowser;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Tan Pham Dinh
 *          pdtanit@gmail.com
 * Jul 29, 2009  
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
@Serialized
public class UIPageManagementPortlet extends UIPortletApplication
{
   public static String PAGE_LIST_HEIGHT = "pageListHeight";
   
   public UIPageManagementPortlet() throws Exception
   {
      UIPageBrowser pageBrowser = addChild(UIPageBrowser.class, null, null);
      pageBrowser.setShowAddNewPage(true);
      UIVirtualList virtualList = pageBrowser.getChild(UIVirtualList.class);
      virtualList.setAutoAdjustHeight(true);
   }
}
