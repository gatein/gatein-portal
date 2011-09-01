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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * May 15, 2007
 * 
 * Validates whether a date is in a correct format
 */

public class DateTimeValidator implements Validator
{
   static private final String SPLIT_REGEX = "/|\\s+|:";

   public void validate(UIFormInput uiInput) throws Exception
   {
      if (uiInput.getValue() == null || ((String)uiInput.getValue()).trim().length() == 0)
         return;
      String s = (String)uiInput.getValue();
      DateFormat stFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      UIFormDateTimeInput uiDateInput = (UIFormDateTimeInput)uiInput;
      SimpleDateFormat sdf = new SimpleDateFormat(uiDateInput.getDatePattern_().trim());

      UIForm uiForm = ((UIComponent)uiInput).getAncestorOfType(UIForm.class);
      String label;
      try
      {
    	  label = uiForm.getId() + ".label." + uiInput.getName();
      }
      catch (Exception e)
      {
         label = uiInput.getName();
      }
      Object[] args = {label, s};

      try
      {
         // Specify whether or not date/time parsing is to be lenient. 
         sdf.setLenient(false);
         sdf.parse(s);
      }
      catch (Exception e)
      {
         throw new MessageException(new ApplicationMessage("DateTimeValidator.msg.Invalid-input", args, ApplicationMessage.WARNING));
      }
   }
}
