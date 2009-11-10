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

package org.exoplatform.gadget.webui.component;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.URLValidator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

/**
 * Created by The eXo Platform SARL Author : dang.tung tungcnw@gmail.com June
 * 27, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIGadgetEditMode.SaveActionListener.class),
   @EventConfig(listeners = UIGadgetEditMode.SelectTypeActionListener.class, phase = Phase.DECODE)})
public class UIGadgetEditMode extends UIForm
{

   public static final String FIELD_URL = "gadgetUrl";

   public static final String TYPE_SELECTOR = "typeSelector";

   public static final String GADGET_SELECTOR = "gadgetSelector";

   public static final String REMOTE_TYPE = "remote";

   public static final String LOCAL_TYPE = "local";

   public UIGadgetEditMode() throws Exception
   {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();

      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
      options.add(new SelectItemOption<String>(REMOTE_TYPE, REMOTE_TYPE));
      options.add(new SelectItemOption<String>(LOCAL_TYPE, LOCAL_TYPE));

      UIFormSelectBox typeSelector = new UIFormSelectBox(TYPE_SELECTOR, null, options);
      typeSelector.setOnChange("SelectType");
      addUIFormInput(typeSelector);
      addUIFormInput(new UIFormStringInput(FIELD_URL, FIELD_URL, pref.getValue("url",
         "http://www.google.com/ig/modules/horoscope.xml")));
      UIFormSelectBox gadgetSelector =
         new UIFormSelectBox(GADGET_SELECTOR, null, new ArrayList<SelectItemOption<String>>());
      gadgetSelector.setRendered(false);
      gadgetSelector.addValidator(MandatoryValidator.class);
      addUIFormInput(gadgetSelector);
      setActions(new String[]{"Save"});
   }

   public static class SaveActionListener extends EventListener<UIGadgetEditMode>
   {
      public void execute(final Event<UIGadgetEditMode> event) throws Exception
      {
         UIGadgetEditMode uiGadgetEditMode = event.getSource();
         PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
         String url = uiGadgetEditMode.getUIStringInput(FIELD_URL).getValue();
         UIGadgetPortlet uiPortlet = uiGadgetEditMode.getParent();
         UIFormSelectBox typeSelector = uiGadgetEditMode.getUIFormSelectBox(TYPE_SELECTOR);
         org.exoplatform.webui.core.UIApplication uiApplication = pcontext.getUIApplication();
         UIPortalApplication portalApp = Util.getUIPortalApplication();

         if (typeSelector.getValue().equals(REMOTE_TYPE))
         {
            String label = uiGadgetEditMode.getLabel(FIELD_URL);
            if (label.charAt(label.length() - 1) == ':')
               label = label.substring(0, label.length() - 1);
            Object[] args = {label};

            if (url == null || url.length() == 0)
            {
               uiGadgetEditMode.getUIStringInput(FIELD_URL).setValue(uiPortlet.getUrl());
               uiApplication.addMessage(new ApplicationMessage("EmptyFieldValidator.msg.empty-input", args,
                  ApplicationMessage.WARNING));
               return;
            }
            url = url.trim();
            if (!url.matches(URLValidator.URL_REGEX))
            {
               uiGadgetEditMode.getUIStringInput(FIELD_URL).setValue(uiPortlet.getUrl());
               uiApplication.addMessage(new ApplicationMessage("URLValidator.msg.invalid-url", args,
                  ApplicationMessage.WARNING));
               return;
            }
            try
            {
               PortletPreferences pref = pcontext.getRequest().getPreferences();
               new URL(url);
               pref.setValue("url", url);
               pref.store();
               uiGadgetEditMode.getUIStringInput(FIELD_URL).setValue(url);
               if (portalApp.getModeState() == UIPortalApplication.NORMAL_MODE)
                  pcontext.setApplicationMode(PortletMode.VIEW);
            }
            catch (Exception e)
            {
               uiGadgetEditMode.getUIStringInput(FIELD_URL).setValue(uiPortlet.getUrl());
            }
         }
         else
         {
            UIFormSelectBox gadgetSelector = uiGadgetEditMode.getUIFormSelectBox(GADGET_SELECTOR);
            String gadgetId = gadgetSelector.getValue();

            ApplicationRegistryService service = uiPortlet.getApplicationComponent(ApplicationRegistryService.class);
            Application application = service.getApplication(gadgetId);
            if (application == null)
            {
               return;
            }
            UIGadget uiGadget = event.getSource().createUIComponent(pcontext, UIGadget.class, null, null);
            uiGadget.setState(new TransientApplicationState<Gadget>(application.getApplicationName()));
            PortletPreferences pref = pcontext.getRequest().getPreferences();
            pref.setValue("url", uiGadget.getUrl());
            pref.store();
            uiGadgetEditMode.getUIStringInput(FIELD_URL).setValue(uiGadget.getUrl());

            if (portalApp.getModeState() == UIPortalApplication.NORMAL_MODE)
               pcontext.setApplicationMode(PortletMode.VIEW);
            pcontext.addUIComponentToUpdateByAjax(uiPortlet);
         }
      }
   }

   static public class SelectTypeActionListener extends EventListener<UIGadgetEditMode>
   {
      public void execute(final Event<UIGadgetEditMode> event) throws Exception
      {
         UIGadgetEditMode uiGadgetEdit = event.getSource();
         UIFormSelectBox typeSelector = uiGadgetEdit.getUIFormSelectBox(TYPE_SELECTOR);
         String selectedValue = typeSelector.getValue();
         UIFormStringInput urlInput = uiGadgetEdit.getUIStringInput(FIELD_URL);
         UIFormSelectBox gadgetSelector = uiGadgetEdit.getUIFormSelectBox(GADGET_SELECTOR);
         if (selectedValue.equals(REMOTE_TYPE))
         {
            urlInput.setRendered(true);
            gadgetSelector.setRendered(false);
         }
         else
         {
            urlInput.setRendered(false);
            gadgetSelector.setRendered(true);
            List<SelectItemOption<String>> gadgetItems = gadgetSelector.getOptions();
            gadgetItems.clear();

            ApplicationRegistryService service = uiGadgetEdit.getApplicationComponent(ApplicationRegistryService.class);
            UserACL acl = uiGadgetEdit.getApplicationComponent(UserACL.class);
            List<Application> appList = service.getAllApplications();
            for (Application app : appList)
            {
               if (app.getType().equals(ApplicationType.GADGET))
               {
                  for (String per : app.getAccessPermissions())
                  {
                     if (acl.hasPermission(per))
                     {
                        gadgetItems.add(new SelectItemOption<String>(app.getDisplayName(), app.getId()));
                        break;
                     }
                  }
               }
            }
         }
         event.getRequestContext().addUIComponentToUpdateByAjax(uiGadgetEdit);
      }
   }
}
