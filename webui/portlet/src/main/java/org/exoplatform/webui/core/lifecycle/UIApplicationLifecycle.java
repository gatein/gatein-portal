/*
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

package org.exoplatform.webui.core.lifecycle;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * Jun 1, 2006
 */
public class UIApplicationLifecycle extends Lifecycle<UIPortletApplication>
{

   public void processDecode(UIPortletApplication uicomponent, WebuiRequestContext context) throws Exception
   {
      String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
      if (componentId == null || componentId.length() == 0)
         return;
      UIComponent uiTarget = uicomponent.findComponentById(componentId);
      //TODO to avoid exception
      if (uiTarget == null)
         return;
      else if (uiTarget == uicomponent)
         super.processDecode(uicomponent, context);
      else
         uiTarget.processDecode(context);
   }

   public void processAction(UIPortletApplication uicomponent, WebuiRequestContext context) throws Exception
   {
      String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
      if (componentId != null)
      {
         UIComponent uiTarget = uicomponent.findComponentById(componentId);
         if (uiTarget == uicomponent)
            super.processAction(uicomponent, context);
         else if (uiTarget != null)
            uiTarget.processAction(context);
      }
   }

   public void processRender(UIPortletApplication uicomponent, WebuiRequestContext context) throws Exception
   {
      if (uicomponent.getTemplate() != null)
      {
         super.processRender(uicomponent, context);
         return;
      }
      UIPortletApplication uiApp = uicomponent;

      context.getWriter().append("<div id=\"").append(uicomponent.getId()).append("\"").append("class=\"").append(uicomponent.getId()).append("\">");
      
      uiApp.renderChildren();
      context.getWriter().append("</div>");
   }
}