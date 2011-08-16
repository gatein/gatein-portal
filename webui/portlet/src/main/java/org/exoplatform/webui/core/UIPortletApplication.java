/*
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

package org.exoplatform.webui.core;

import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

@Serialized
abstract public class UIPortletApplication extends UIApplication
{
   private int minWidth = 300;

   private int minHeight = 300;
   
   private Map<String, UIPopupMessages> _uiPopupMessages;

   static public String VIEW_MODE = "ViewMode";

   static public String EDIT_MODE = "EditMode";

   static public String HELP_MODE = "HelpMode";

   static public String CONFIG_MODE = "ConfigMode";

   public UIPortletApplication() throws Exception
   {
      _uiPopupMessages = new HashedMap();
   }

   @Override
   public UIPopupMessages getUIPopupMessages()
   {
      PortletRequestContext pContext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      String currMode = pContext.getApplicationMode().toString();
      
      if (!_uiPopupMessages.containsKey(currMode)) {
         try
         {
            UIPopupMessages popMsg = createUIComponent(UIPopupMessages.class, null, null);
            popMsg.setId("_" + popMsg.hashCode());
            _uiPopupMessages.put(currMode, popMsg);
         }
         catch (Exception e)
         {
            return null;
         }         
      }
      
      return _uiPopupMessages.get(currMode);
   }

   @Deprecated
   public int getMinWidth()
   {
      return minWidth;
   }
   
   @Deprecated
   public void setMinWidth(int minWidth)
   {
      this.minWidth = minWidth;
   }
   
   @Deprecated
   public int getMinHeight()
   {
      return minHeight;
   }
   
   @Deprecated
   public void setMinHeight(int minHeight)
   {
      this.minHeight = minHeight;
   }

   /**
    * The default processRender for an UIPortletApplication does nothing if the current WindowState in the 
    * render request is MINIMIZED. Otherwise, it handles two cases:
    * 
    *   A. Ajax is used 
    *   ---------------
    *     If Ajax is used and that the entire portal should not be re rendered, then an AJAX fragment is 
    *     generated with information such as the portlet id, the portlet title, the portlet modes, the window 
    *     states as well as the HTML for the block to render
    *   
    *   B. A full render is made
    *   ------------------------
    *      a simple call to the method super.processRender(context) which will delegate the call to all the 
    *      Lifecycle components
    *   
    */
   public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception
   {
      //Do nothing if WindowState in the render request is MINIMIZED
      WindowState currentWindowState = ((PortletRequestContext)context).getRequest().getWindowState();
      if(currentWindowState == WindowState.MINIMIZED)
      {
         return;
      }
      
      WebuiRequestContext pContext = (WebuiRequestContext)context.getParentAppRequestContext();
      
      if (context.useAjax() && !pContext.getFullRender())
      {
         Writer w = context.getWriter();

         Set<UIComponent> list = context.getUIComponentToUpdateByAjax();
         //      if(list == null) list = app.getDefaultUIComponentToUpdateByAjax(context) ;
         if (list != null)
         {
            if (getUIPopupMessages().hasMessage())
               context.addUIComponentToUpdateByAjax(getUIPopupMessages());
            for (UIComponent uicomponent : list)
            {
               renderBlockToUpdate(uicomponent, context, w);
            }
            return;
         }
      }
      super.processRender(context);
   }

   /**
    * Root uicomponent of a portlet should override this method to leverage serveResource that JSR286 offers 
    * @param context - WebUI context
    */
   public void serveResource(WebuiRequestContext context) throws Exception
   {      
      if (!(context.getRequest() instanceof ResourceRequest))
      {
         throw new IllegalStateException("serveSource can only be called in portlet context");
      }
   }
}
