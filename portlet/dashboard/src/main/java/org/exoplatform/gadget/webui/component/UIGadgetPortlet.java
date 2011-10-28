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

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetImporter;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.application.GadgetUtil;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * June 27, 2008
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/gadget/webui/component/UIGadgetPortlet.gtmpl", events = {
   @EventConfig(listeners = UIGadgetPortlet.SaveUserPrefActionListener.class)})
public class UIGadgetPortlet extends UIPortletApplication
{
   final static public String LOCAL_STRING = "local://";

   private static final Logger log = LoggerFactory.getLogger(GadgetImporter.class);

   /** User pref. */
   private String userPref;
   
   public UIGadgetPortlet() throws Exception
   {
      addChild(UIGadgetViewMode.class, null, null);
   }
   
   public String getUserPref()
   {
      return userPref;
   }
   
   public void setUserPref(String pref)
   {
      this.userPref = pref;
   }

   @Override
   public void processAction(WebuiRequestContext context) throws Exception
   {
      super.processAction(context);

      PortletRequest req = context.getRequest();
      userPref = req.getParameter("userPref");

      if (userPref != null && !userPref.isEmpty())
      {
         PortletPreferences prefs = req.getPreferences();
         prefs.setValue("userPref", userPref);
         prefs.store();
      }
   }
   
   @Override
   public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception
   {
      PortletRequest req = context.getRequest();
      PortletPreferences prefs = req.getPreferences();
      userPref = prefs.getValue("userPref", null);
      
      super.processRender(app, context);
   }

   public String getUrl()
   {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      String urlPref = pref.getValue("url", "http://www.google.com/ig/modules/horoscope.xml");
      if (urlPref.startsWith(LOCAL_STRING))
      {
         try
         {
            String gadgetName = urlPref.replaceFirst(LOCAL_STRING, "");
            ExoContainer container = ExoContainerContext.getCurrentContainer();
            GadgetRegistryService gadgetService =
               (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
            Gadget gadget = gadgetService.getGadget(gadgetName);
            if (gadget != null)
            {
               return GadgetUtil.reproduceUrl(gadget.getUrl(), gadget.isLocal());
            }
            else 
            {
            	if (log.isWarnEnabled())
            	{
            	   log.warn("The local gadget '" + gadgetName + "' was not found, nothing rendered");
            	}
            	return null;
            }
         }
         catch (Exception e)
         {
            log.warn("Failure retrieving gadget from url!");
         }
      }
      return urlPref;
   }

   public String getMetadata()
   {
      String url = getUrl();
      if (url == null)
      {
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApplication = context.getUIApplication();
         uiApplication.addMessage(new ApplicationMessage("UIGadgetPortlet.msg.url-invalid", null));
      }

      return getMetadata(url);
   }
   
   public String getMetadata(String url)
   {
      JSONObject metadata_ = null;
      try
      {
         String strMetadata = GadgetUtil.fetchGagdetRpcMetadata(url);
         metadata_ = new JSONArray(strMetadata).getJSONObject(0).getJSONObject(UIGadget.RPC_RESULT).getJSONObject(url);
         String token = GadgetUtil.createToken(url, new Long(hashCode()));
         metadata_.put("secureToken", token);
      }
      catch (JSONException e)
      {
         e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
      }
      return metadata_.toString();
   }
   
   static public class SaveUserPrefActionListener extends EventListener<UIGadgetPortlet>
   {
      public void execute(Event<UIGadgetPortlet> event) throws Exception
      {
         PortletRequest req = event.getRequestContext().getRequest();
         String userPref = req.getParameter("userPref");
         if (userPref != null && !userPref.isEmpty())
         {
            PortletPreferences prefs = req.getPreferences();
            prefs.setValue("userPref", userPref);
            prefs.store();
            
            UIGadgetPortlet gadgetPortlet = (UIGadgetPortlet) event.getSource();
            gadgetPortlet.setUserPref(userPref);
         }
         Util.getPortalRequestContext().setResponseComplete(true);
      }
   }
}
