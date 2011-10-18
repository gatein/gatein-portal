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
import org.exoplatform.commons.serialization.api.annotations.Serialized;
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
 * Validates whether an email is in the correct format
 * Valid characters that can be used in a domain name are:
 *     a-z
 *     0-9
 *     - (dash) or . (dot) but not as a starting or ending character
 *     . (dot) as a separator for the textual portions of a domain name
 *     
 * Valid characters that can be used in a domain name are:
 *     a-z
 *     0-9
 *     _ (underscore) or .  (dot) but not as a starting or ending character
 */
@Serialized
public class EmailAddressValidator implements Validator
{

   public void validate(UIFormInput uiInput) throws Exception
   {
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
      Object[] args = {label};
      
      if (uiInput.getValue() == null || ((String)uiInput.getValue()).trim().length() == 0)
      {
         return;
      }
      
      String s = (String)uiInput.getValue();
      int atIndex = s.indexOf('@');
      if (atIndex == -1)
      {
         throw new MessageException(new ApplicationMessage("EmailAddressValidator.msg.Invalid-input", args,
            ApplicationMessage.WARNING));
      }
      
      String localPart = s.substring(0, atIndex);
      String domainName = s.substring(atIndex + 1);

      if (!validateLocalPart(localPart.toCharArray()) || !validateDomainName(domainName.toCharArray()))
      {
         throw new MessageException(new ApplicationMessage("EmailAddressValidator.msg.Invalid-input", args,
            ApplicationMessage.WARNING));
      }
   }

   private boolean validateLocalPart(char[] localPart)
   {
      if(!isAlphabet(localPart[0]) || !isAlphabetOrDigit(localPart[localPart.length -1]))
      {
         return false;
      }

      for(int i = 1; i < localPart.length -1; i++)
      {
         char c = localPart[i];
         char next = localPart[i+1];

         if(isAlphabetOrDigit(c) || (isLocalPartSymbol(c) && isAlphabetOrDigit(next)))
         {
            continue;
         }
         else
         {
            return false;
         }
      }
      return true;
   }

   private boolean validateDomainName(char[] domainName)
   {
      if(!isAlphabet(domainName[0]) || !isAlphabetOrDigit(domainName[domainName.length -1]))
      {
         return false;
      }

      //Check if there is no non-alphabet following the last dot
      boolean foundValidLastDot = false;
      for(int i = 1; i < domainName.length -1; i++)
      {
         char c = domainName[i];
         char next = domainName[i+1];

         if(c == '.')
         {
            foundValidLastDot = true;
         }
         else if(!isAlphabet(c))
         {
            foundValidLastDot = false;
         }

         if(isAlphabetOrDigit(c) || (isDomainNameSymbol(c) && isAlphabetOrDigit(next)))
         {
            continue;
         }
         else
         {
            return false;
         }
      }
      return foundValidLastDot;
   }
   
   private boolean isAlphabet(char c)
   {
      return c >= 'a' && c <= 'z';
   }
   
   private boolean isDigit(char c)
   {
      return c >= '0' && c <= '9';
   }

   private boolean isAlphabetOrDigit(char c)
   {
      return isAlphabet(c) || isDigit(c);
   }
   
   private boolean isLocalPartSymbol(char c)
   {
      return c == '_' || c == '.';
   }
   
   private boolean isDomainNameSymbol(char c)
   {
      return c == '-' || c == '.';
   }
}