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

import org.exoplatform.portal.resource.SkinDependentManager;
import org.exoplatform.portal.resource.SkinService;

import javax.servlet.ServletContext;

/**
 * 
 * Created by eXoPlatform SAS
 * 
 * Author: Minh Hoang TO - hoang281283@gmail.com
 * 
 * Sep 16, 2009
 */
public class PortalSkinTask extends AbstractSkinTask
{

   private static final String DEFAULT_MODULE_NAME = "CoreSkin";

   private static final String DEFAULT_SKIN_NAME = "Default";

   private String moduleName;

   private String skinName;

   private String cssPath;

   private boolean overwrite;

   public PortalSkinTask()
   {
      this.overwrite = true;
      this.moduleName = DEFAULT_MODULE_NAME;
      this.skinName = DEFAULT_SKIN_NAME;
   }

   public void setModuleName(String _moduleName)
   {
      this.moduleName = _moduleName;
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
      if (moduleName == null || skinName == null || cssPath == null)
      {
         return;
      }
      String contextPath = scontext.getContextPath();
      String fullCSSPath = contextPath + cssPath;
      skinService.addPortalSkin(moduleName, skinName, fullCSSPath, scontext, overwrite);
      updateSkinDependentManager(contextPath, moduleName, skinName);
   }

   /** Update skinDependentManager as it is needed to undeploy skin at runtime */
   private void updateSkinDependentManager(String webApp, String moduleName, String skinName)
   {
      SkinDependentManager.addPortalSkin(webApp, moduleName, skinName);
      SkinDependentManager.addSkinDeployedInApp(webApp, skinName);

      // Remark: Invoked only in PortalSkinTask
      SkinDependentManager.addDependentAppToSkinName(skinName, webApp);
   }
}
