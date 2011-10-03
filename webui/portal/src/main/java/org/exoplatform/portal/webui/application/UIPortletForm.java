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

import org.exoplatform.commons.utils.ExceptionUtil;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.pc.ExoPortletState;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.portal.pom.spi.wsrp.WSRPPortletStateType;
import org.exoplatform.portal.portlet.PortletExceptionHandleService;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.portletcontainer.PortletContainerException;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.*;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.organization.UIListPermissionSelector;
import org.exoplatform.webui.organization.UIListPermissionSelector.EmptyIteratorValidator;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.info.PreferenceInfo;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.portlet.impl.spi.*;

import javax.portlet.PortletMode;
import javax.servlet.http.Cookie;
import java.util.*;

/** Author : Nhu Dinh Thuan nhudinhthuan@yahoo.com Jun 8, 2006 */
@ComponentConfigs({
   @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/portal/webui/portal/UIPortletForm.gtmpl", events = {
      @EventConfig(listeners = UIPortletForm.SaveActionListener.class),
      @EventConfig(listeners = UIPortletForm.CloseActionListener.class, phase = Phase.DECODE)}),
   @ComponentConfig(id = "PortletPermission", type = UIFormInputSet.class, lifecycle = UIContainerLifecycle.class)})
public class UIPortletForm extends UIFormTabPane
{
   private static Log log = ExoLogger.getLogger("portal:UIPortletForm");

   private UIPortlet uiPortlet_;

   private UIComponent backComponent_;

   private static final String FIELD_THEME = "Theme";

   private static final String FIELD_PORTLET_PREF = "PortletPref";

   @SuppressWarnings("unchecked")
   public UIPortletForm() throws Exception
   {
      super("UIPortletForm");
      UIFormInputSet uiPortletPrefSet = new UIFormInputSet(FIELD_PORTLET_PREF).setRendered(false);
      addUIFormInput(uiPortletPrefSet);
      UIFormInputSet uiSettingSet = new UIFormInputSet("PortletSetting");
      uiSettingSet.
         /*addUIFormInput(new UIFormStringInput("id", "id", null).
                     addValidator(MandatoryValidator.class).setEditable(false)).
      addUIFormInput(new UIFormStringInput("windowId", "windowId", null).setEditable(false)).*/
            addUIFormInput(new UIFormInputInfo("displayName", "displayName", null)).addUIFormInput(
         new UIFormStringInput("title", "title", null).addValidator(StringLengthValidator.class, 3, 60).addValidator(ExpressionValidator.class, "[^\\<\\>]*",
               "UIPortletForm.msg.InvalidPortletTitle"))
         .addUIFormInput(
            new UIFormStringInput("width", "width", null).addValidator(ExpressionValidator.class, "(^([1-9]\\d*)px$)?",
               "UIPortletForm.msg.InvalidWidthHeight")).addUIFormInput(
         new UIFormStringInput("height", "height", null).addValidator(ExpressionValidator.class,
            "(^([1-9]\\d*)px$)?", "UIPortletForm.msg.InvalidWidthHeight")).addUIFormInput(
         new UIFormCheckBoxInput("showInfoBar", "showInfoBar", false)).addUIFormInput(
         new UIFormCheckBoxInput("showPortletMode", "showPortletMode", false)).addUIFormInput(
         new UIFormCheckBoxInput("showWindowState", "showWindowState", false)).addUIFormInput(
         new UIFormTextAreaInput("description", "description", null).addValidator(StringLengthValidator.class, 0,
            255).addValidator(ExpressionValidator.class, "[^\\<\\>]*", "UIPortletForm.msg.InvalidPortletDescription"));
      addUIFormInput(uiSettingSet);
      UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector("Icon", "icon");
      addUIFormInput(uiIconSelector);

      UIFormInputThemeSelector uiThemeSelector = new UIFormInputThemeSelector(FIELD_THEME, null);
      SkinService skinService = getApplicationComponent(SkinService.class);
      uiThemeSelector.getChild(UIItemThemeSelector.class).setValues(skinService.getPortletThemes());
      addUIFormInput(uiThemeSelector);

      UIListPermissionSelector uiListPermissionSelector = createUIComponent(UIListPermissionSelector.class, null, null);
      uiListPermissionSelector.configure("UIAccessPermission", "accessPermissions");
      uiListPermissionSelector.addValidator(EmptyIteratorValidator.class);
      UIFormInputSet uiPermissionSet = createUIComponent(UIFormInputSet.class, "PortletPermission", null);
      uiPermissionSet.addChild(uiListPermissionSelector);
      addUIFormInput(uiPermissionSet);
   }

   public UIComponent getBackComponent()
   {
      return backComponent_;
   }

   public void setBackComponent(final UIComponent uiComp) throws Exception
   {
      backComponent_ = uiComp;
   }

   public UIPortlet getUIPortlet()
   {
      return uiPortlet_;
   }

   public boolean hasEditMode()
   {
      return uiPortlet_.getSupportModes().contains("edit");
   }

   public String getEditModeContent()
   {
      StringBuilder portletContent = new StringBuilder();
      try
      {
         PortalRequestContext prcontext = (PortalRequestContext)WebuiRequestContext.getCurrentInstance();
         prcontext.ignoreAJAXUpdateOnPortlets(true);
         StatefulPortletContext portletContext = uiPortlet_.getPortletContext();

         ExoPortletInvocationContext portletInvocationContext = new ExoPortletInvocationContext(prcontext, uiPortlet_);

         List<Cookie> requestCookies = new ArrayList<Cookie>(Arrays.asList(prcontext.getRequest().getCookies()));

         RenderInvocation renderInvocation = new RenderInvocation(portletInvocationContext);
         renderInvocation.setClientContext(new AbstractClientContext(prcontext.getRequest(), requestCookies));
         renderInvocation.setServerContext(new AbstractServerContext(prcontext.getRequest(), prcontext.getResponse()));


         // instance context
         InstanceContext instanceContext;
         if (portletContext.getType() instanceof WSRPPortletStateType)
         {
            WSRP wsrp = (WSRP)portletContext.getState();
            AccessMode accessMode = AccessMode.CLONE_BEFORE_WRITE;
            if (wsrp.isCloned())
            {
               accessMode = AccessMode.READ_WRITE;
            }
            instanceContext = new ExoPortletInstanceContext(wsrp.getPortletId(), accessMode);
         }
         else
         {
            ExoPortletState exo = (ExoPortletState)portletContext.getState();
            instanceContext = new ExoPortletInstanceContext(exo.getPortletId());
         }
         renderInvocation.setInstanceContext(instanceContext);

         renderInvocation.setUserContext(new AbstractUserContext(prcontext.getRequest()));
         renderInvocation.setWindowContext(new AbstractWindowContext(uiPortlet_.getWindowId()));
         renderInvocation.setPortalContext(new AbstractPortalContext(Collections.singletonMap(
            "javax.portlet.markup.head.element.support", "true")));
         renderInvocation.setSecurityContext(new AbstractSecurityContext(prcontext.getRequest()));
         renderInvocation.setTarget(portletContext);

         renderInvocation.setMode(Mode.create(PortletMode.EDIT.toString()));
         renderInvocation.setWindowState(org.gatein.pc.api.WindowState.create(uiPortlet_.getCurrentWindowState()
            .toString()));

         PortletInvocationResponse portletResponse = uiPortlet_.invoke(renderInvocation);

         String content;
         if (portletResponse instanceof FragmentResponse)
         {
            FragmentResponse fragmentResponse = (FragmentResponse)portletResponse;
            if (fragmentResponse.getType() == FragmentResponse.TYPE_BYTES)
            {
               content = new String(fragmentResponse.getBytes(), "UTF-8");
            }
            else
            {
               content = fragmentResponse.getContent();
            }

         }
         else
         {
            PortletContainerException pcException;

            //
            if (portletResponse instanceof ErrorResponse)
            {
               ErrorResponse errorResponse = (ErrorResponse)portletResponse;
               pcException = new PortletContainerException(errorResponse.getMessage(), errorResponse.getCause());
            }
            else
            {
               pcException =
                  new PortletContainerException("Unknown invocation response type [" + portletResponse.getClass()
                     + "]. Expected a FragmentResponse or an ErrorResponse");
            }

            //
            PortletExceptionHandleService portletExceptionService = uiPortlet_.getApplicationComponent(PortletExceptionHandleService.class);
            if (portletExceptionService != null)
            {
                portletExceptionService.handle(pcException);
            }
            else
            {
               log.warn("Could not find the PortletExceptionHandleService in the exo container");
            }

            //
            log.error("Portlet render in edit mode threw an exception", pcException);
            content = "An error has occured. Please see the logs for details.";
         }

         portletContent.setLength(0);

         portletContent.append(content);
      }
      catch (Throwable ex)
      {
         WebuiRequestContext webuiRequest = WebuiRequestContext.getCurrentInstance();
         portletContent.append(webuiRequest.getApplicationResourceBundle().getString("UIPortlet.message.RuntimeError"));
         log.error("The portlet " + uiPortlet_.getName() + " could not be loaded. Check if properly deployed.",
            ExceptionUtil.getRootCause(ex));
      }
      return portletContent.toString();
   }

   public void setValues(final UIPortlet uiPortlet) throws Exception
   {
      uiPortlet_ = uiPortlet;
      invokeGetBindingBean(uiPortlet_);
      String icon = uiPortlet.getIcon();

      if (icon == null || icon.length() < 0)
      {
         icon = "PortletIcon";
      }
      getChild(UIFormInputIconSelector.class).setSelectedIcon(icon);
      getChild(UIFormInputThemeSelector.class).getChild(UIItemThemeSelector.class).setSelectedTheme(
         uiPortlet.getSuitedTheme(null));
      if (hasEditMode())
      {
         uiPortlet.setCurrentPortletMode(PortletMode.EDIT);
      }
      else
      {
         Map<String, String> portletPreferenceMaps = new HashMap<String, String>();
         org.gatein.pc.api.Portlet portlet = uiPortlet.getProducedOfferedPortlet();
         Set<String> keySet = portlet.getInfo().getPreferences().getKeys();

         for (String key : keySet)
         {
            PreferenceInfo preferenceInfo = portlet.getInfo().getPreferences().getPreference(key);
            if (!preferenceInfo.isReadOnly())
            {
               String ppValue = (preferenceInfo.getDefaultValue().size() > 0) ? preferenceInfo.getDefaultValue().get
                  (0) : "";
               portletPreferenceMaps.put(key, ppValue);
            }
         }

         Portlet pp = uiPortlet.getPreferences();
         if (pp != null)
         {
            for (Preference pref : pp)
            {
               if (!pref.isReadOnly())
               {
                  portletPreferenceMaps.put(pref.getName(), (pref.getValues().size() > 0) ? pref.getValues().get(0) :
                  "");
               }
            }
         }

         if (portletPreferenceMaps.size() > 0)
         {
            Set<String> ppKeySet = portletPreferenceMaps.keySet();
            UIFormInputSet uiPortletPrefSet = getChildById(FIELD_PORTLET_PREF);
            uiPortletPrefSet.getChildren().clear();
            for (String ppKey : ppKeySet)
            {
               String ppValue = portletPreferenceMaps.get(ppKey);
               UIFormStringInput preferenceStringInput = new UIFormStringInput(ppKey, null, ppValue);
               preferenceStringInput.setLabel(ppKey);
               preferenceStringInput.addValidator(MandatoryValidator.class);
               uiPortletPrefSet.addUIFormInput(preferenceStringInput);
            }

            uiPortletPrefSet.setRendered(true);
            setSelectedTab(FIELD_PORTLET_PREF);
            return;
         }

         setSelectedTab("PortletSetting");
      }
   }

   private void savePreferences() throws Exception
   {
      UIFormInputSet uiPortletPrefSet = getChildById(FIELD_PORTLET_PREF);
      List<UIFormStringInput> uiFormInputs = new ArrayList<UIFormStringInput>(3);
      uiPortletPrefSet.findComponentOfType(uiFormInputs, UIFormStringInput.class);
      if (uiFormInputs.size() < 1)
      {
         return;
      }

      PropertyChange[] propertyChanges = new PropertyChange[uiFormInputs.size()];

      for (int i = 0; i < uiFormInputs.size(); i++)
      {
         String name = uiFormInputs.get(i).getName();
         String value = uiFormInputs.get(i).getValue();
         propertyChanges[i] = PropertyChange.newUpdate(name, value);
      }

      // Now save it
      uiPortlet_.update(propertyChanges);
   }

   static public class SaveActionListener extends EventListener<UIPortletForm>
   {
      public void execute(final Event<UIPortletForm> event) throws Exception
      {
         UIPortletForm uiPortletForm = event.getSource();
         UIPortlet uiPortlet = uiPortletForm.getUIPortlet();
         UIFormInputIconSelector uiIconSelector = uiPortletForm.getChild(UIFormInputIconSelector.class);
         uiPortletForm.invokeSetBindingBean(uiPortlet);
         if (uiIconSelector.getSelectedIcon().equals("Default"))
         {
            uiPortlet.setIcon("PortletIcon");
         }
         else
         {
            uiPortlet.setIcon(uiIconSelector.getSelectedIcon());
         }
         UIFormInputThemeSelector uiThemeSelector = uiPortletForm.getChild(UIFormInputThemeSelector.class);
         uiPortlet.putSuitedTheme(null, uiThemeSelector.getChild(UIItemThemeSelector.class).getSelectedTheme());
         uiPortletForm.savePreferences();
         UIMaskWorkspace uiMaskWorkspace = uiPortletForm.getParent();
         uiMaskWorkspace.setUIComponent(null);
         if (uiPortletForm.hasEditMode())
         {
            uiPortlet.setCurrentPortletMode(PortletMode.VIEW);
         }

         String width = uiPortletForm.getUIStringInput("width").getValue();
         if (width == null || width.length() == 0)
         {
            uiPortlet.setWidth(null);
         }
         else
         {
            uiPortlet.setWidth(width);
         }

         String height = uiPortletForm.getUIStringInput("height").getValue();
         if (height == null || height.length() == 0)
         {
            uiPortlet.setHeight(null);
         }
         else
         {
            uiPortlet.setHeight(height);
         }

         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         pcontext.getJavascriptManager().addJavascript("eXo.portal.UIPortal.changeComposerSaveButton();");
         pcontext.addUIComponentToUpdateByAjax(uiMaskWorkspace);
         UIPortalApplication uiPortalApp = uiPortlet.getAncestorOfType(UIPortalApplication.class);
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
         pcontext.ignoreAJAXUpdateOnPortlets(true);
      }
   }

   public static class CloseActionListener extends EventListener<UIPortletForm>
   {
      public void execute(final Event<UIPortletForm> event) throws Exception
      {
         UIPortletForm uiPortletForm = event.getSource();
         UIPortlet uiPortlet = uiPortletForm.getUIPortlet();
         if (uiPortletForm.hasEditMode())
         {
            uiPortlet.setCurrentPortletMode(PortletMode.VIEW);
         }
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         //add by Pham Dinh Tan
         UIMaskWorkspace uiMaskWorkspace = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         uiMaskWorkspace.setUIComponent(null);
         uiMaskWorkspace.setWindowSize(-1, -1);
         pcontext.addUIComponentToUpdateByAjax(uiMaskWorkspace);
         pcontext.ignoreAJAXUpdateOnPortlets(true);
      }
   }

}
