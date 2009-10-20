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

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv@exoplatform.com
 * Aug 31, 2006  
 * 
 * A component that represents a toolbar
 */

public abstract class UIToolbar extends UIComponent
{
   /**
    * The css style
    */
   private String toolbarStyle_ = "LightToolbar";

   /**
    * A javascript expression
    */
   private String strJavascript_ = "";

   /**
    * A javascript event name
    */
   private String eventName_ = "";

   public UIToolbar() throws Exception
   {
   }

   public String getToolbarStyle()
   {
      return toolbarStyle_;
   }

   public void setToolbarStyle(String toolbarStyle)
   {
      toolbarStyle_ = toolbarStyle;
   }

   public List getEvents()
   {
      return getComponentConfig().getEvents();
   }

   public String getJavascript()
   {
      return strJavascript_;
   }

   public String getEventName()
   {
      return eventName_;
   }

   public void setJavascript(String eventName, String strJavascript)
   {
      strJavascript_ = strJavascript;
      eventName_ = eventName;
   }

}
