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

package org.exoplatform.webui.core;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.replication.api.annotations.Serialized;

import java.io.Writer;
import java.util.Set;

@Serialized
abstract public class UIPortletApplication extends UIApplication
{
   private int minWidth = 300;

   private int minHeight = 300;

   static public String VIEW_MODE = "ViewMode";

   static public String EDIT_MODE = "EditMode";

   static public String HELP_MODE = "HelpMode";

   static public String CONFIG_MODE = "ConfigMode";

   public UIPortletApplication() throws Exception
   {
   }

   public int getMinWidth()
   {
      return minWidth;
   }

   public void setMinWidth(int minWidth)
   {
      this.minWidth = minWidth;
   }

   public int getMinHeight()
   {
      return minHeight;
   }

   public void setMinHeight(int minHeight)
   {
      this.minHeight = minHeight;
   }

   /**
    * The default processRender for an UIPortletApplication handles two cases:
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
}