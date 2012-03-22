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
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.GadgetImporter;
import org.exoplatform.commons.utils.PropertyManager;
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
   
   /** Indicate height should be filled full in browser height */
   private boolean fillUpFreeSpace;
   
   private JSONObject metadata;

   private String url;
   
   public UIGadgetPortlet() throws Exception
   {
      setId(Integer.toString(hashCode()));
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      fillUpFreeSpace = Boolean.parseBoolean(context.getRequest().getPreferences().getValue("fillUpFreeSpace", "false"));
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
   
   public boolean isFillUpFreeSpace()
   {
      return fillUpFreeSpace;
   }
   
   public void setFillUpFreeSpace(boolean isFillUpFreeSpace)
   {
      this.fillUpFreeSpace = isFillUpFreeSpace;
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
      
      url = getUrl(prefs);
      metadata = fetchMetadata(url);
      
      super.processRender(app, context);
   }

   public String getUrl()
   {
      return url;
   }
   
   public boolean isLossData()
   {
      return (url == null || metadata.has("error"));
   }
   
   private String getUrl(PortletPreferences pref)
   {
      String url = null;
      String urlPref = pref.getValue("url", "local://Calendar");
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
               url = GadgetUtil.reproduceUrl(gadget.getUrl(), gadget.isLocal());
            }
            else 
            {
            	if (log.isWarnEnabled())
            	{
            	   log.warn("The local gadget '" + gadgetName + "' was not found, nothing rendered");
            	}
            }
         }
         catch (Exception e)
         {
            log.warn("Failure retrieving gadget from url!");
         }
      }
      else
      {
         url = urlPref;
      }
      
      if (url == null)
      {
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApplication = context.getUIApplication();
         uiApplication.addMessage(new ApplicationMessage("UIGadgetPortlet.msg.url-invalid", null));
      }
      
      return url;
      
   }

   public String getMetadata()
   {
      if (metadata == null)
      {
         PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
         PortletPreferences pref = pcontext.getRequest().getPreferences();
         url = getUrl(pref);
         metadata = fetchMetadata(url);
      }
      
      return metadata.toString();
   }
   
   private JSONObject fetchMetadata(String url)
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
         log.warn("Unable to retrieve metadata of url: " + url, e);
      }
      return metadata_;
   }
   
   public boolean isNoCache()
   {
      return PropertyManager.isDevelopping();
   }
   
   public boolean isDebug()
   {
      return PropertyManager.isDevelopping();
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
