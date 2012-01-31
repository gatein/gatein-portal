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

import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * May 15, 2007
 *
 * Validates whether a date is in a correct format
 */

public class DateTimeValidator extends AbstractValidator implements Serializable
{
   @Override
   protected String getMessageLocalizationKey()
   {
      return "DateTimeValidator.msg.Invalid-input";
   }

   @Override
   protected boolean isValid(String value, UIFormInput uiInput)
   {
      UIFormDateTimeInput uiDateInput = (UIFormDateTimeInput)uiInput;
      SimpleDateFormat sdf = new SimpleDateFormat(uiDateInput.getDatePattern_().trim());
      // Specify whether or not date/time parsing is to be lenient.
      sdf.setLenient(false);
      try
      {
         sdf.parse(value);
         return true;
      }
      catch (ParseException e)
      {
         return false;
      }
   }

   @Override
   protected String trimmedValueOrNullIfBypassed(String value, UIFormInput uiInput, boolean exceptionOnMissingMandatory, boolean trimValue) throws Exception
   {
      if(!(uiInput instanceof UIFormDateTimeInput))
      {
         return null;
      }
      else
      {
         return super.trimmedValueOrNullIfBypassed(value, uiInput, exceptionOnMissingMandatory, trimValue);
      }
   }

   @Override
   protected Object[] getMessageArgs(String value, UIFormInput uiInput) throws Exception
   {
      return new Object[]{getLabelFor(uiInput), value};
   }
}
