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
   public static final String ALLOWED_SYMBOLS = "'_', '.'";

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
         messages.addMessage("FirstCharacterUsernameValidator.msg", new Object[]{label});
      }

      char c = buff[buff.length - 1];
      if (!isLowerCaseLetterOrDigit(c))
      {
         messages.addMessage("LastCharacterUsernameValidator.msg", new Object[]{label, c});
      }

      boolean hasConsecutive = false;
      boolean hasInvalid = false;
      for (int i = 1; i < buff.length - 1; i++)
      {
         c = buff[i];

         if (isLowerCaseLetterOrDigit(c))
         {
            continue;
         }

         if (isSymbol(c))
         {
            char next = buff[i + 1];
            if (isSymbol(next))
            {
               if (!hasConsecutive)
               {
                  messages.addMessage("ConsecutiveSymbolValidator.msg", new Object[]{label, ALLOWED_SYMBOLS});
                  hasConsecutive = true;
               }
            }
            else if (!Character.isLetterOrDigit(next))
            {
               if (!hasInvalid)
               {
                  messages.addMessage("UsernameValidator.msg.Invalid-char", new Object[]{label});
                  hasInvalid = true;
               }
            }
         }
         else
         {
            if (!hasInvalid)
            {
               messages.addMessage("UsernameValidator.msg.Invalid-char", new Object[]{label});
               hasInvalid = true;
            }
         }

         // if we have both error conditions, fail "fast" instead of going on
         if (hasConsecutive && hasInvalid)
         {
            break;
         }
      }
   }

   private static boolean isLowerCaseLetterOrDigit(char character)
   {
      return Character.isDigit(character) || (character >= 'a' && character <= 'z');
   }

   private static boolean isSymbol(char c)
   {
      return c == '_' || c == '.';
   }
}
