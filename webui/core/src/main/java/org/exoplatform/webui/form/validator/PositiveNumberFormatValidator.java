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

package org.exoplatform.webui.form.validator;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          duytucntt@gmail.com
 * Jun 22, 2007
 * 
 * Validates whether this number is positive or 0
 */

public class PositiveNumberFormatValidator implements Validator
{

   public void validate(UIFormInput uiInput) throws Exception
   {
      if (uiInput.getValue() == null || ((String)uiInput.getValue()).length() == 0)
         return;
      //  modified by Pham Dinh Tan
      UIComponent uiComponent = (UIComponent)uiInput;
      UIForm uiForm = uiComponent.getAncestorOfType(UIForm.class);
      String label;
      try
      {
    	  label = uiForm.getId() + ".label." + uiInput.getName();
      }
      catch (Exception e)
      {
         label = uiInput.getName();
      }
      String s = (String)uiInput.getValue();
      boolean error = false;
      for (int i = 0; i < s.length(); i++)
      {
         char c = s.charAt(i);
         if (Character.isDigit(c) || (s.charAt(0) == '-' && i == 0))
         {
            error = true;
            continue;
         }
         error = false;
         Object[] args = {label, uiInput.getBindingField()};
         throw new MessageException(new ApplicationMessage("NumberFormatValidator.msg.Invalid-number", args));
      }
      if (error == true && s.charAt(0) == '-')
      {
         Object[] args = {label};
         throw new MessageException(new ApplicationMessage("PositiveNumberFormatValidator.msg.Invalid-number", args));
      }
   }
}