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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Thi Hoa
 *          hoa.nguyen@exoplatform.com
 * Nov 1, 2006  
 */
@ComponentConfigs({
   @ComponentConfig(lifecycle = UIFormLifecycle.class
   //template =  "system:/groovy/webui/form/UIFormTabPane.gtmpl"
   ),
   @ComponentConfig(id = "PageTemplateOption", type = UIPageTemplateOptions.class, template = "system:/groovy/portal/webui/page/UIWizardPageSelectLayoutForm.gtmpl", initParams = @ParamConfig(name = "PageLayout", value = "system:/WEB-INF/conf/uiconf/portal/webui/page/PageConfigOptions.groovy"))})
public class UIWizardPageSelectLayoutForm extends UIForm
{

   @SuppressWarnings("unchecked")
   public UIWizardPageSelectLayoutForm() throws Exception
   {
      //super("UIWizardPageSelectLayoutForm");
      //super.setWithRenderTab(false);

      UIPageTemplateOptions uiTemplateConfig =
         createUIComponent(UIPageTemplateOptions.class, "PageTemplateOption", null);
      addUIComponentInput(uiTemplateConfig);
      //setSelectedTab(uiTemplateConfig.getId()) ;
   }

}
