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

package org.exoplatform.portal.resource.config.tasks;

import org.exoplatform.portal.skin.SkinService;

import javax.servlet.ServletContext;

/**
 * 
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 *      Sep 16, 2009
 */
public class PortletSkinTask extends AbstractSkinTask
{

   private String applicationName;

   private String portletName;

   private String skinName;

   private String cssPath;

   private boolean overwrite;

   public PortletSkinTask()
   {
      this.skinName = "Default";
      this.overwrite = true;
   }

   public void setApplicationName(String _applicationName)
   {
      this.applicationName = _applicationName;
   }

   public void setPortletName(String _portletName)
   {
      this.portletName = _portletName;
   }

   public void setSkinName(String _skinName)
   {
      this.skinName = _skinName;
   }

   public void setCSSPath(String _cssPath)
   {
      this.cssPath = _cssPath;
   }

   public void setOverwrite(boolean _overwrite)
   {
      this.overwrite = _overwrite;
   }

   @Override
   public void execute(SkinService skinService, ServletContext scontext)
   {
      if (portletName == null || skinName == null || cssPath == null)
      {
         return;
      }
      if (applicationName == null)
      {
         applicationName = scontext.getContextPath();
      }
      String moduleName = applicationName + "/" + portletName;
      String fullCSSPath = scontext.getContextPath() + cssPath;
      skinService.addSkin(moduleName, skinName, fullCSSPath, scontext, overwrite);
   }

}
