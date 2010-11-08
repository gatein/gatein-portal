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

package org.exoplatform.portal.webui.login;

import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Jul 11, 2006  
 */
@ComponentConfig(template = "system:/groovy/portal/webui/UILoginForm.gtmpl", events = {
   @EventConfig(phase = Phase.DECODE, listeners = UIMaskWorkspace.CloseActionListener.class),
   @EventConfig(phase = Phase.DECODE, listeners = UILoginForm.ForgetPasswordActionListener.class)})
public class UILoginForm extends UIComponent
{

   public UILoginForm() throws Exception
   {
   }

   static public class ForgetPasswordActionListener extends EventListener<UILoginForm>
   {
      public void execute(Event<UILoginForm> event) throws Exception
      {
         UILogin uiLogin = event.getSource().getParent();
         uiLogin.getChild(UILoginForm.class).setRendered(false);
         uiLogin.getChild(UIForgetPasswordWizard.class).setRendered(true);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiLogin);
      }
   }

   @Override
   public void processDecode(WebuiRequestContext context) throws Exception
   {
      super.processDecode(context);
      String action = context.getRequestParameter(context.getActionParameterName());
      Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
      if (event != null)
      {
         event.broadcast();
      }
   }
   
   
}
