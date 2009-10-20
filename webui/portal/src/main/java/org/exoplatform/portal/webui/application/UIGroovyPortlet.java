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

package org.exoplatform.portal.webui.application;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;

import javax.portlet.PortletRequest;

@ComponentConfig()
public class UIGroovyPortlet extends UIPortletApplication
{

   private String DEFAULT_TEMPLATE = "system:/groovy/portal/webui/application/UIGroovyPortlet.gtmpl";

   private String template_;

   private String windowId;

   public UIGroovyPortlet() throws Exception
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      template_ = prequest.getPreferences().getValue("template", DEFAULT_TEMPLATE);
      windowId = prequest.getWindowID();
   }

   public String getId()
   {
      return windowId + "-portlet";
   }

   public String getTemplate()
   {
      return template_;
   }

   public UIComponent getViewModeUIComponent()
   {
      return null;
   }

}
