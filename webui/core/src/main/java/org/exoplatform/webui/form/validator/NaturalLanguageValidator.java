/**
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.form.validator;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.form.UIFormInput;

/**
 * @author <a href="mailto:haint@exoplatform.com">Nguyen Thanh Hai</a>
 * @datOct 11, 2011
 *
 * Validates whether this value is composed of letters or spaces
 */
@Serialized
public class NaturalLanguageValidator extends AbstractValidator
{

   @Override
   protected String getMessageLocalizationKey()
   {
      return "NaturalLanguageValidator.msg.Invalid-char";
   }

   @Override
   protected boolean isValid(String value, UIFormInput uiInput)
   {
      for (int i = 0; i < value.length(); i++)
      {
         char c = value.charAt(i);
         if (Character.isLetter(c) || Character.isSpaceChar(c))
         {
            continue;
         }
         return false;
      }
      return true;
   }
}
