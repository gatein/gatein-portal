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

package org.exoplatform.webui.application.portlet;

import org.exoplatform.webui.application.StateManager;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;

public class ParentAppStateManager extends StateManager
{

   /**
    * This method simply delegate the call to the same method of the parent WebuiRequestContext
    */
   @SuppressWarnings("unchecked")
   public UIApplication restoreUIRootComponent(WebuiRequestContext context) throws Exception
   {
      WebuiRequestContext pcontext = (WebuiRequestContext)context.getParentAppRequestContext();
      return pcontext.getStateManager().restoreUIRootComponent(context);
   }

   @SuppressWarnings("unused")
   public void storeUIRootComponent(WebuiRequestContext context) throws Exception
   {
      WebuiRequestContext pcontext = (WebuiRequestContext)context.getParentAppRequestContext();
      pcontext.getStateManager().storeUIRootComponent(context);
   }

   @SuppressWarnings("unused")
   public void expire(String sessionId, WebuiApplication app)
   {
   }
}