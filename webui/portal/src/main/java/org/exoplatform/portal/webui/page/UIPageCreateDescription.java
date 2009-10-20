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
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : lxchiati   
 *          lebienthuy@gmail.com
 * Jul 11, 2006  
 */
@ComponentConfig(template = "system:/groovy/portal/webui/page/UIPageCreateDescription.gtmpl")
public class UIPageCreateDescription extends UIContainer
{

   private String titleKey_;

   //  private String title_ ;

   public UIPageCreateDescription() throws Exception
   {
   }

   public String getTitleKey()
   {
      return titleKey_;
   }

   public void setTitleKey(String titleKey_)
   {
      this.titleKey_ = titleKey_;
   }

   //  public void setTitle(String key){
   //	WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
   //	ResourceBundle res = context.getApplicationResourceBundle();
   //	title_ = res.getString(key);
   //  }
   //  
   //  public String getTitle(){ return title_; }

}