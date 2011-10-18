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
 * Author : Dang Van Minh
 *          minhdv81@yahoo.com
 * Jun 7, 2006
 * 
 * Validates whether this number is in a correct format
 */
public class NumberFormatValidator implements Validator
{

   public void validate(UIFormInput uiInput) throws Exception
   {
      if (uiInput.getValue() == null || ((String)uiInput.getValue()).length() == 0)
      {
         return;
      }
         
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
         label = uiInput.getName().trim();
      }
      Object[] args = {label};
      
      String s = (String)uiInput.getValue();
      if(s.charAt(0) == '0' && s.length() > 1) 
      {
         throw new MessageException(new ApplicationMessage("NumberFormatValidator.msg.Invalid-number", args));
      }
      else if(s.charAt(0) == '-' && s.length() > 1 && s.charAt(1) == '0')
      {
         throw new MessageException(new ApplicationMessage("NumberFormatValidator.msg.Invalid-number", args));
      }
      try
      {
         Integer.parseInt(s);
      } 
      catch(NumberFormatException e)
      {
         throw new MessageException(new ApplicationMessage("NumberFormatValidator.msg.Invalid-number", args));
      }
   }

}
