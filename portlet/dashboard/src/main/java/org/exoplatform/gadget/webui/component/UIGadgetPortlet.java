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

import org.exoplatform.portal.webui.application.GadgetUtil;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.json.JSONException;
import org.json.JSONObject;

import javax.portlet.PortletPreferences;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * June 27, 2008
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/gadget/webui/component/UIGadgetPortlet.gtmpl")
public class UIGadgetPortlet extends UIPortletApplication
{
   public UIGadgetPortlet() throws Exception
   {
      addChild(UIGadgetViewMode.class, null, null);      
   }

   public String getUrl()
   {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      return pref.getValue("url", "http://www.google.com/ig/modules/horoscope.xml");
   }

   public String getMetadata()
   {
      String metadata_ = GadgetUtil.fetchGagdetMetadata(getUrl());
      try
      {
         JSONObject jsonObj = new JSONObject(metadata_);
         JSONObject obj = jsonObj.getJSONArray("gadgets").getJSONObject(0);
         String token = GadgetUtil.createToken(getUrl(), new Long(hashCode()));
         obj.put("secureToken", token);
         metadata_ = jsonObj.toString();
      }
      catch (JSONException e)
      {
         e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
      }
      return metadata_;
   }
}
