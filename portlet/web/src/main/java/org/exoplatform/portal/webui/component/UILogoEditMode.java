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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/** Created by The eXo Platform SAS Author : eXoPlatform October 2, 2009 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {@EventConfig(listeners = UILogoEditMode.SaveActionListener.class)})
public class UILogoEditMode extends UIForm
{

   final static private String FIELD_URL = "logoUrl";

   public UILogoEditMode() throws Exception
   {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      addUIFormInput(new UIFormStringInput(FIELD_URL, FIELD_URL, pref.getValue("url", "")));
   }

   static public class SaveActionListener extends EventListener<UILogoEditMode>
   {
      public void execute(Event<UILogoEditMode> event) throws Exception
      {
         UILogoEditMode uiForm = event.getSource();
         String url = uiForm.getUIStringInput(FIELD_URL).getValue();
         if(url == null)
         {
            throwMessageException(uiForm);
         }
         else
         {
            url = url.trim();
            
            if(url.length() == 0)
            {
               throwMessageException(uiForm);
            }
            else
            {

               String tmp = url;

               // check if we have an absolute URL
               if(url.startsWith("/"))
               {
                  // build a fake file: URI for validation purposes
                  tmp = "file://" + url;
               }

               try
               {
                  URI uri = new URI(tmp);
                  uri.toURL();
               }
               catch (URISyntaxException e)
               {
                  throwMessageException(uiForm);
               }
               catch (MalformedURLException e)
               {
                  throwMessageException(uiForm);
               }
            }
         }

         PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
         PortletPreferences pref = pcontext.getRequest().getPreferences();
         pref.setValue("url", uiForm.getUIStringInput(FIELD_URL).getValue());
         pref.store();

         UIPortalApplication portalApp = Util.getUIPortalApplication();
         if (portalApp.getModeState() == UIPortalApplication.NORMAL_MODE)
         {
            pcontext.setApplicationMode(PortletMode.VIEW);
         }
      }

      private void throwMessageException(UILogoEditMode uiForm) throws MessageException
      {
         UILogoPortlet uiPortlet = uiForm.getParent();
         uiForm.getUIStringInput(FIELD_URL).setValue(uiPortlet.getURL());
         Object[] args = {FIELD_URL, "URL"};
         throw new MessageException(new ApplicationMessage("ExpressionValidator.msg.value-invalid", args));
      }
   }
}
