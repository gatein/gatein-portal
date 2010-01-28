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

package org.exoplatform.webui.form;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.validator.Validator;

import java.util.List;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Nov 20, 2006
 */
@ComponentConfig(template = "system:/groovy/webui/core/UIPopupWindow.gtmpl", events = @EventConfig(listeners = UIFormPopupWindow.CloseActionListener.class, name = "CloseFormPopup", phase = Phase.DECODE))
@Serialized
public class UIFormPopupWindow extends UIPopupWindow implements UIFormInput<Object>
{

   public UIFormPopupWindow()
   {
      closeEvent_ = "CloseFormPopup";
   }

   public void processDecode(WebuiRequestContext context) throws Exception
   {
      UIForm uiForm = getAncestorOfType(UIForm.class);
      String action = uiForm.getSubmitAction();
      if (action == null)
         return;
      Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
      if (event != null)
         event.broadcast();
      getUIComponent().processDecode(context);
      if (getUIComponent() == null)
         return;
   }

   public String event(String name) throws Exception
   {
      UIForm uiForm = getAncestorOfType(UIForm.class);
      if (uiForm != null)
         return uiForm.event(name, getId(), (String)null);
      return super.event(name);
   }

   static public class CloseActionListener extends EventListener<UIPopupWindow>
   {
      public void execute(Event<UIPopupWindow> event) throws Exception
      {
         UIPopupWindow uiPopupWindow = event.getSource();
         UIForm uiForm = uiPopupWindow.getAncestorOfType(UIForm.class);
         uiPopupWindow.setShow(false);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
      }
   }

   public String getLabel()
   {
      return getName();
   }

   @SuppressWarnings("unused")
   public <E extends Validator> UIFormInput addValidator(Class<E> clazz, Object... params) throws Exception
   {
      return this;
   }

   public String getBindingField()
   {
      return null;
   }

   public List getValidators()
   {
      return null;
   }

   public Object getValue() throws Exception
   {
      return null;
   }

   public void reset()
   {
   }

   public Class getTypeValue()
   {
      return null;
   }

   @SuppressWarnings("unused")
   public UIFormInput setValue(Object value) throws Exception
   {
      return null;
   }

}
