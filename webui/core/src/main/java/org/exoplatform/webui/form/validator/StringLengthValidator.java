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

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 * minhdv81@yahoo.com
 * Jun 7, 2006
 * <p/>
 * Validates whether this value has a length between min and max
 */
@Serialized
public class StringLengthValidator extends AbstractValidator
{
   /** The minimum number of characters in this String */
   private Integer min_ = 0;

   /** The maximum number of characters in this String */
   private Integer max_ = 0;

   public StringLengthValidator()
   {
   }

   public StringLengthValidator(Integer max)
   {
      max_ = max;
   }

   public StringLengthValidator(Integer min, Integer max)
   {
      min_ = min;
      max_ = max;
   }

   @Override
   protected String getMessageLocalizationKey()
   {
      return "StringLengthValidator.msg.length-invalid";
   }

   @Override
   protected boolean isValid(String value, UIFormInput uiInput)
   {
      int length = getValue(value).length();
      return min_ <= length && max_ >= length;
   }

   protected String getValue(String value)
   {
      return value.trim();
   }

   @Override
   protected Object[] getMessageArgs(String value, UIFormInput uiInput) throws Exception
   {
      return new Object[]{getLabelFor(uiInput), min_.toString(), max_.toString()};
   }
}
