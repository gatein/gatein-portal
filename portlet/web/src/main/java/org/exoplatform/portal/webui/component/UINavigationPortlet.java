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

package org.exoplatform.portal.webui.component;

import org.exoplatform.portal.webui.navigation.UIPortalNavigation;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

@ComponentConfigs({
   @ComponentConfig(lifecycle = UIApplicationLifecycle.class),
   @ComponentConfig(type = UIPortalNavigation.class, id = "UIHorizontalNavigation", events = @EventConfig(listeners = UIPortalNavigation.SelectNodeActionListener.class))})
public class UINavigationPortlet extends UIPortletApplication
{
   public UINavigationPortlet() throws Exception
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      PortletPreferences prefers = prequest.getPreferences();
      String template = prefers.getValue("template", "app:/groovy/portal/webui/component/UIPortalNavigation.gtmpl");

      UIPortalNavigation portalNavigation = addChild(UIPortalNavigation.class, "UIHorizontalNavigation", null);
      portalNavigation.setUseAjax(Boolean.valueOf(prefers.getValue("useAJAX", "true")));
      portalNavigation.setShowUserNavigation(Boolean.valueOf(prefers.getValue("showUserNavigation", "true")));
      portalNavigation.setTemplate(template);

      portalNavigation.setCssClassName(prefers.getValue("CSSClassName", ""));
   }
}