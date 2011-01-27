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

package org.exoplatform.dashboard.webui.component;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.webui.application.GadgetUtil;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.URLValidator;

import java.net.URI;

/**
 * Created by The eXo Platform SAS
 * Oct 15, 2008  
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/dashboard/webui/component/UIAddGadgetForm.gtmpl", events = @EventConfig(listeners = UIAddGadgetForm.AddGadgetByUrlActionListener.class))
public class UIAddGadgetForm extends UIForm
{

   public static String FIELD_URL = "url";

   public UIAddGadgetForm() throws Exception
   {
      addUIFormInput(new UIFormStringInput(FIELD_URL, FIELD_URL, null));
   }

   static public class AddGadgetByUrlActionListener extends EventListener<UIAddGadgetForm>
   {
      public void execute(final Event<UIAddGadgetForm> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIAddGadgetForm uiForm = event.getSource();

         UIDashboard uiDashboard = uiForm.getAncestorOfType(UIDashboard.class);
         UIDashboardContainer uiContainer = uiDashboard.getChild(UIDashboardContainer.class);

         GadgetRegistryService service = uiForm.getApplicationComponent(GadgetRegistryService.class);
         String url = uiForm.getUIStringInput(FIELD_URL).getValue();
         UIApplication uiApplication = context.getUIApplication();
         if (url == null || url.trim().length() == 0)
         {
            uiApplication.addMessage(new ApplicationMessage("UIDashboard.msg.required", null));
            return;
         }
         url = url.trim();
         if (!url.matches(URLValidator.URL_REGEX))
         {
            uiApplication.addMessage(new ApplicationMessage("UIDashboard.msg.notUrl", null));
            return;
         }
         Gadget gadget;
         UIGadget uiGadget;

         //TODO check the way we create the unique ID, is it really unique?
         try
         {
            String name = "gadget" + url.hashCode();
            gadget = GadgetUtil.toGadget(name, url, false);
            service.saveGadget(gadget);
            uiGadget = uiForm.createUIComponent(context, UIGadget.class, null, null);
            uiGadget.setState(new TransientApplicationState<org.exoplatform.portal.pom.spi.gadget.Gadget>(gadget.getName()));
         }
         catch (Exception e)
         {
            String aggregatorId = uiDashboard.getAggregatorId();
            gadget = service.getGadget(aggregatorId);
            //TODO make sure it's an rss feed
            // TODO make sure that we did not add it already
            uiGadget = uiForm.createUIComponent(context, UIGadget.class, null, null);

            org.exoplatform.portal.pom.spi.gadget.Gadget contentState = new org.exoplatform.portal.pom.spi.gadget.Gadget();
            contentState.addUserPref("{'rssurl':'" + url + "'}");
            TransientApplicationState<org.exoplatform.portal.pom.spi.gadget.Gadget> applicationState = new TransientApplicationState<org.exoplatform.portal.pom.spi.gadget.Gadget>(gadget.getName(), contentState);

            uiGadget.setState(applicationState);
         }

         uiContainer.addUIGadget(uiGadget, 0, 0);
         uiContainer.save();
         uiForm.reset();
         context.addUIComponentToUpdateByAjax(uiForm);
         context.addUIComponentToUpdateByAjax(uiContainer);
      }

   }

}
