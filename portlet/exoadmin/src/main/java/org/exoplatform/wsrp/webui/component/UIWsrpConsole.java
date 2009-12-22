/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.wsrp.webui.component;

import org.exoplatform.portal.webui.application.UIApplicationList;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainerList;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;

/** @author Wesley Hales */
@ComponentConfigs({
@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/groovy/wsrp/webui/component/UIWsrpConsole.gtmpl"),
@ComponentConfig(
   id = "UIWsrpConsoleTab",
   type = UITabPane.class,
   //template = "system:/groovy/webui/core/UITabPane.gtmpl",
   template = "app:/groovy/wsrp/webui/component/UIWsrpConsoleContent.gtmpl",
   events = {@EventConfig(listeners = UIWsrpConsole.SelectTabActionListener.class)})})
public class UIWsrpConsole extends UIContainer
{
   public UIWsrpConsole() throws Exception
   {
      UITabPane uiTabPane = addChild(UITabPane.class, "UIWsrpConsoleTab", null);
      uiTabPane.addChild(UIWsrpConsumerOverview.class, null, "Manage Consumers").setRendered(true);
      uiTabPane.addChild(UIWsrpProducerOverview.class, null, "Producer Configuration");
      uiTabPane.setSelectedTab(1);
   
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
//      UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
//      int portalMode = uiPortalApp.getModeState();
//      if (portalMode != UIPortalApplication.NORMAL_MODE)
//      {
//         UITabPane uiTabPane = this.getChild(UITabPane.class);
//         UIComponent uiComponent = uiTabPane.getChildById(uiTabPane.getSelectedTabId());
//         if (uiComponent instanceof UIWsrpConsumerOverview)
//         {
//            if (portalMode == UIPortalApplication.APP_VIEW_EDIT_MODE)
//            {
//               Util.showComponentEditInViewMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//            else
//            {
//               uiPortalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
//               Util.showComponentLayoutMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//         }
//         else if (uiComponent instanceof UIWsrpProducerOverview)
//         {
//            if (portalMode == UIPortalApplication.CONTAINER_VIEW_EDIT_MODE)
//            {
//               Util.showComponentEditInViewMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//            else
//            {
//               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE);
//               Util.showComponentLayoutMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//         }
//      }
     super.processRender(context);
   }

   static public class SelectTabActionListener extends UITabPane.SelectTabActionListener
   {
      public void execute(Event<UITabPane> event) throws Exception
      {
//         super.execute(event);
//         UITabPane uiTabPane = event.getSource();
//         UIComponent uiComponent = uiTabPane.getChildById(uiTabPane.getSelectedTabId());
//         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
//         int portalMode = uiPortalApp.getModeState();
//
//         if (uiComponent instanceof UIWsrpConsumerOverview)
//         { // Swicth to Porlets Tab
//            if (portalMode % 2 == 0)
//            {
//               uiPortalApp.setModeState(UIPortalApplication.APP_VIEW_EDIT_MODE);
//               Util.showComponentEditInViewMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//            else
//            {
//               uiPortalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
//               Util.showComponentLayoutMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//         }
//         else if (uiComponent instanceof UIWsrpProducerOverview)
//         { // Swicth to
//            // Containers Tab
//            if (portalMode % 2 == 0)
//            {
//               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_VIEW_EDIT_MODE);
//               Util.showComponentEditInViewMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//            else
//            {
//               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE);
//               Util.showComponentLayoutMode(org.exoplatform.portal.webui.container.UIContainer.class);
//            }
//         }
//         event.getRequestContext().addUIComponentToUpdateByAjax(
//            Util.getUIPortalApplication().getChildById(UIPortalApplication.UI_WORKING_WS_ID));
      }
   }

}
