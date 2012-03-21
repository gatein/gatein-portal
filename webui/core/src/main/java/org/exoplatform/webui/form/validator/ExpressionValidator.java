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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by The eXo Platform SARL
 * Author : Le Bien Thuy
 *          lebienthuy@gmail.com
 * Oct 10, 2007
 *
 * Validates whether this value matches one regular expression.
 */
@Serialized
public class ExpressionValidator extends AbstractValidator
{
   private String regexp;
   private transient Matcher matcher;

   private String key_;

   // For @Serialized needs
   public ExpressionValidator()
   {
   }

   public ExpressionValidator(final String regex)
   {
      this(regex, "ExpressionValidator.msg.value-invalid");
   }

   public ExpressionValidator(final String regex, final String key)
   {
      this.regexp = regex;
      key_ = key;
   }

   private Matcher getMatcherFor(String input)
   {
      if(matcher == null)
      {
         matcher = Pattern.compile(regexp).matcher(input);
      }
      else
      {
         matcher.reset(input);
      }

      return matcher;
   }

   @Override
   protected String getMessageLocalizationKey()
   {
      return key_;
   }

   @Override
   protected boolean isValid(String value, UIFormInput uiInput)
   {
      return getMatcherFor(value).matches();
   }

   @Override
   protected Object[] getMessageArgs(String value, UIFormInput uiInput) throws Exception
   {
      return new Object[]{getLabelFor(uiInput), matcher.pattern().toString()};
   }
}
