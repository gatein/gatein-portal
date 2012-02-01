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
import org.exoplatform.web.application.CompoundApplicationMessage;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Validate username whether the value is only alpha lower, digit, dot and underscore with first, last character is
 * alpha lower or digit and cannot contain consecutive underscore, dot or both.
 *
 * @author <a href="mailto:haint@exoplatform.com">Nguyen Thanh Hai</a>
 * @date Sep 28, 2011
 */

@Serialized
public class UsernameValidator extends MultipleConditionsValidator
{
   protected static final int DEFAULT_MIN_LENGTH = 3;
   protected static final int DEFAULT_MAX_LENGTH = 30;
   protected Integer min = DEFAULT_MIN_LENGTH;
   protected Integer max = DEFAULT_MAX_LENGTH;

   // required by @Serialized
   public UsernameValidator()
   {
   }

   public UsernameValidator(Integer min, Integer max)
   {
      this.min = min;
      this.max = max;
   }

   protected void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput)
   {
      validate(value, label, messages, min, max);
   }

   static void validate(String value, String label, CompoundApplicationMessage messages, Integer min, Integer max)
   {
      char[] buff = value.toCharArray();
      if (buff.length < min || buff.length > max)
      {
         messages.addMessage("StringLengthValidator.msg.length-invalid", new Object[]{label, min.toString(), max.toString()});
      }

      if (!Character.isLowerCase(buff[0]))
      {
         messages.addMessage("FirstCharacterNameValidator.msg", new Object[]{label});
      }

      if (!Character.isLetterOrDigit(buff[buff.length - 1]))
      {
         messages.addMessage("LastCharacterUsernameValidator.msg", new Object[]{label, buff[buff.length - 1]});
      }

      for (int i = 1; i < buff.length - 1; i++)
      {
         char c = buff[i];

         if (Character.isLetterOrDigit(c))
         {
            continue;
         }

         if (isSymbol(c))
         {
            char next = buff[i + 1];
            if (isSymbol(next))
            {
               messages.addMessage("ConsecutiveSymbolValidator.msg", new Object[]{label, buff[i], buff[i + 1]});
            }
            else if (!Character.isLetterOrDigit(next))
            {
               messages.addMessage("UsernameValidator.msg.Invalid-char", new Object[]{label});
            }
         }
         else
         {
            messages.addMessage("UsernameValidator.msg.Invalid-char", new Object[]{label});
         }
      }
   }

   private static boolean isSymbol(char c)
   {
      return c == '_' || c == '.';
   }
}
