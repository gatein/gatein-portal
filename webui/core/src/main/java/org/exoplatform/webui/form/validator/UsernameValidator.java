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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;

/**
 * @author <a href="mailto:haint@exoplatform.com">Nguyen Thanh Hai</a>
 *
 * @datSep 28, 2011
 * 
 *  Validate username whether the value is only alpha lower, digit, dot and underscore with first, last character is alpha lower or digit 
 *  and cannot contain consecutive underscore, dot or both. 
 */

@Serialized
public class UsernameValidator implements Validator
{
   private Integer min = 3;
   private Integer max = 30;
   
   public UsernameValidator(Integer min, Integer max)
   {
      this.min = min;
      this.max = max;
   }
   
   @Override
   public void validate(UIFormInput uiInput) throws Exception
   {
      if (uiInput.getValue() == null || ((String)uiInput.getValue()).trim().length() == 0) 
      {
         return;
      }
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
      
      char[] buff = ((String)uiInput.getValue()).toCharArray();
      if(buff.length < min || buff.length > max) 
      {
         Object[] args = {label, min.toString(), max.toString()};
         throw new MessageException(new ApplicationMessage("StringLengthValidator.msg.length-invalid", args,
            ApplicationMessage.WARNING));
      }
      
      if(!isAlphabet(buff[0])) 
      {
         Object[] args = {label};
         throw new MessageException(new ApplicationMessage("FirstCharacterNameValidator.msg", args,
            ApplicationMessage.WARNING));
      }
      
      if(!isAlphabetOrDigit(buff[buff.length - 1]))
      {
         Object[] args = {label, buff[buff.length - 1]};
         throw new MessageException(new ApplicationMessage("LastCharacterUsernameValidator.msg", args,
            ApplicationMessage.WARNING));
      }
      
      for(int i = 1; i < buff.length -1; i++)
      {
         char c = buff[i];

         if (isAlphabetOrDigit(c))
         {
            continue;
         }

         if (isSymbol(c))
         {
            char next = buff[i + 1];
            if (isSymbol(next))
            {
               Object[] args = {label, buff[i], buff[i + 1]};
               throw new MessageException(new ApplicationMessage("ConsecutiveSymbolValidator.msg", args,
                  ApplicationMessage.WARNING));
            }
            else if (!isAlphabetOrDigit(next))
            {
               Object[] args = {label};
               throw new MessageException(new ApplicationMessage("UsernameValidator.msg.Invalid-char", args, ApplicationMessage.WARNING));
            }
         }
         else
         {
            Object[] args = {label};
            throw new MessageException(new ApplicationMessage("UsernameValidator.msg.Invalid-char", args, ApplicationMessage.WARNING));
         }
      }
   }
   
   private boolean isAlphabet(char c)
   {
      return c >= 'a' && c <= 'z';
   }
   
   private boolean isDigit(char c)
   {
      return c >= '0' && c <= '9';
   }
   
   private boolean isSymbol(char c)
   {
      return c == '_' || c == '.';
   }

   private boolean isAlphabetOrDigit(char c)
   {
      return isAlphabet(c) || isDigit(c);
   }

}
