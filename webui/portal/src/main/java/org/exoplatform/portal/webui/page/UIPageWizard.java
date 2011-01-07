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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/** Created by The eXo Platform SAS Mar 21, 2007 */
public abstract class UIPageWizard extends UIWizard
{

   protected UIPopupWindow uiHelpWindow;

   private int numberStep_;

   private boolean showWelcome = true;

   private boolean showActions = true;

   public UIPageWizard() throws Exception
   {
      uiHelpWindow = createUIComponent(UIPopupWindow.class, null, null);
      uiHelpWindow.setWindowSize(300, 200);
      uiHelpWindow.setShow(false);
      uiHelpWindow.setId("UIPageWizardHelp");
   }

   public void setNumberSteps(int s)
   {
      numberStep_ = s;
   }

   public int getNumberSteps()
   {
      return numberStep_;
   }

   public boolean isShowActions()
   {
      return showActions;
   }

   public void setShowActions(boolean showActions)
   {
      this.showActions = showActions;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      super.processRender(context);
      uiHelpWindow.processRender(context);
   }

   public boolean isShowWelcomeComponent()
   {
      return showWelcome;
   }

   public void setShowWelcomeComponent(boolean value)
   {
      showWelcome = value;
   }

   public UIPopupWindow getHelpWindow()
   {
      return uiHelpWindow;
   }

   protected void updateUIPortal(Event<? extends UIPageWizard> event) throws Exception
   {
      PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();

      UIPortal uiPortal = Util.getUIPortal();
      uiPortal.setRenderSibling(UIPortal.class);
      pcontext.ignoreAJAXUpdateOnPortlets(true);
   }

   void updateWizardComponent()
   {
      UIPortalApplication uiPortalApp = getAncestorOfType(UIPortalApplication.class);
      PortalRequestContext pcontext = Util.getPortalRequestContext();

      UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);

      pcontext.ignoreAJAXUpdateOnPortlets(true);
   }

   static public class AbortActionListener extends EventListener<UIPageWizard>
   {
      public void execute(Event<UIPageWizard> event) throws Exception
      {
         UIPortalApplication uiPortalApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();

         UIPortal uiPortal = Util.getUIPortal();
         uiPortal.setRenderSibling(UIPortal.class);
         pcontext.ignoreAJAXUpdateOnPortlets(true);

         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         UIPortalToolPanel toolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         toolPanel.setUIComponent(null);
         pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
         JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }
   }
}
