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
import org.exoplatform.web.application.CompoundApplicationMessage;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * 14 March, 2006
 *
 * Validates whether the value is composed of letters, numbers or '_'
 */
@Serialized
public class ResourceValidator extends MultipleConditionsValidator
{
   @Override
   protected void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput)
   {
      char firstChar = value.charAt(0);
      if (!Character.isLetter(firstChar))
      {
         Object[] args = {label, uiInput.getBindingField()};
         messages.addMessage("FirstCharacterNameValidator.msg", args);
      }
      for (int i = 0; i < value.length(); i++)
      {
         char c = value.charAt(i);
         if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_' && c != '-' && c != '.')
         {
            Object[] args = {label};
            messages.addMessage("ResourceValidator.msg.Invalid-char", args);
            break;
         }
      }
   }
}
