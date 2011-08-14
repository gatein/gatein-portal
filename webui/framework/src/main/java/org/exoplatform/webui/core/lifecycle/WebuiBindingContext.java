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

package org.exoplatform.webui.core.lifecycle;

import groovy.lang.Closure;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.groovyscript.text.BindingContext;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIContainer;

import java.io.Writer;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class WebuiBindingContext extends BindingContext
{

   protected static Log log = ExoLogger.getLogger("portal:WebuiBindingContext");

   private UIComponent uicomponent_;

   private WebuiRequestContext rcontext_;

   public WebuiBindingContext(ResourceResolver resolver, Writer w, UIComponent uicomponent, final WebuiRequestContext context)
   {
      super(resolver, w);
      uicomponent_ = uicomponent;
      rcontext_ = context;

      // Closure nodeurl()
      put("nodeurl", new Closure(this)
      {
         @Override
         public Object call(Object[] args)
         {
            return context.createURL(NodeURL.TYPE);
         }
      });

      // Add Orientation specific information
      Orientation orientation = context.getOrientation();
      this.put("orientation", orientation);
      this.put("isLT", orientation.isLT());
      this.put("isRT", orientation.isRT());
      this.put("dir", orientation.isLT() ? "ltr" : "rtl");
   }

   public UIComponent getUIComponent()
   {
      return uicomponent_;
   }

   public WebuiRequestContext getRequestContext()
   {
      return rcontext_;
   }

   public String getContextPath()
   {
      return rcontext_.getRequestContextPath();
   }

   public String getPortalContextPath()
   {
      return rcontext_.getPortalContextPath();
   }

   public BindingContext clone()
   {
      BindingContext newContext = new WebuiBindingContext(resolver_, writer_, uicomponent_, rcontext_);
      newContext.putAll(this);
      newContext.setGroovyTemplateService(service_);
      return newContext;
   }

   public String appRes(String mesgKey) throws Exception
   {
      String value = "";
      try
      {
         ResourceBundle res = rcontext_.getApplicationResourceBundle();
         value = res.getString(mesgKey);
      }
      catch (MissingResourceException ex)
      {
         if (PropertyManager.isDevelopping())
            log.warn("Can not find resource bundle for key : " + mesgKey);
         if(mesgKey != null)
            value = mesgKey.substring(mesgKey.lastIndexOf('.') + 1);
      }
      return value;
   }

   public void renderChildren() throws Exception
   {
      if (uicomponent_ instanceof UIComponentDecorator)
      {
         UIComponentDecorator uiComponentDecorator = (UIComponentDecorator)uicomponent_;
         if (uiComponentDecorator.getUIComponent() == null)
            return;
         uiComponentDecorator.getUIComponent().processRender(rcontext_);
         return;
      }
      UIContainer uicontainer = (UIContainer)uicomponent_;
      List<UIComponent> children = uicontainer.getChildren();
      for (UIComponent child : children)
      {
         if (child.isRendered())
         {
            child.processRender(rcontext_);
         }
      }
   }

   public void renderChild(String id) throws Exception
   {
      if (!(uicomponent_ instanceof UIContainer))
         return;
      UIContainer uicontainer = (UIContainer)uicomponent_;
      UIComponent uiChild = uicontainer.getChildById(id);
      uiChild.processRender(rcontext_);
   }

   public void renderUIComponent(UIComponent uicomponent) throws Exception
   {
      uicomponent.processRender(rcontext_);
   }

   public void renderChild(int index) throws Exception
   {
      if (!(uicomponent_ instanceof UIContainer))
         return;
      UIContainer uicontainer = (UIContainer)uicomponent_;
      UIComponent uiChild = uicontainer.getChild(index);
      uiChild.processRender(rcontext_);
   }

   @SuppressWarnings("unused")
   public void userRes(String mesgKey)
   {

   }
}