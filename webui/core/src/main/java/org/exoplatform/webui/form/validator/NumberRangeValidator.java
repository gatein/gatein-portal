/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.webui.form.validator;

import java.io.Serializable;

import org.exoplatform.web.application.CompoundApplicationMessage;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class NumberRangeValidator extends NumberFormatValidator
{
   private int min;
   private int max;
   
   public NumberRangeValidator(int min, int max)
   {
      this.min = min;
      this.max = max;
   }
   
   @Override
   protected String getMessageLocalizationKey()
   {
      return "NumberRangeValidator.msg.Invalid-number";
   }

   @Override
   protected Integer validateInteger(String value, String label, CompoundApplicationMessage messages)
   {
      Integer integer = super.validateInteger(value, label, messages);
      
      if (integer == null)
      {
         return null;
      }
      else if (integer < min || integer > max)
      {
         messages.addMessage(getMessageLocalizationKey(), new Object[]{label, min, max});
         return null;
      }
      else
      {
         return integer;
      }
   }
}

