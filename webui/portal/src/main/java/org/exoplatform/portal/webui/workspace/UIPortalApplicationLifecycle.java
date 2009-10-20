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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * May 8, 2006
 */
public class UIPortalApplicationLifecycle extends Lifecycle<UIPortalApplication>
{

   public void processDecode(UIPortalApplication uicomponent, WebuiRequestContext context) throws Exception
   {
      String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
      if (componentId == null)
         return;
      UIComponent uiTarget = uicomponent.findComponentById(componentId);
      if (uiTarget == null)
         return;
      if (uiTarget == uicomponent)
         super.processDecode(uicomponent, context);
      uiTarget.processDecode(context);
   }

   /**
    * The processAction() method of the UIPortalApplication is called, as there is no 
    * method in the object itself it will call the processAction() of the 
    * UIPortalApplicationLifecycle bound to the UI component
    * 
    * If no uicomponent object is targeted, which is the case the first time 
    * (unless a bookmarked link is used) then nothing is done. Otherwise, the 
    * targeted component is extracted and a call of its processAction() method is executed.
    * 
    */
   public void processAction(UIPortalApplication uicomponent, WebuiRequestContext context) throws Exception
   {
      String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
      if (componentId == null)
         return;
      UIComponent uiTarget = uicomponent.findComponentById(componentId);
      if (uiTarget == null)
         return;
      if (uiTarget == uicomponent)
         super.processAction(uicomponent, context);
      uiTarget.processAction(context);
   }

}