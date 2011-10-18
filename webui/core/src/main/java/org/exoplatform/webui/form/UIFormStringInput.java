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

package org.exoplatform.webui.form;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.webui.application.WebuiRequestContext;

import java.io.Writer;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Jun 6, 2006
 * 
 * Represents a input string field
 * The value is stored in UIFormInputBase
 */
@Serialized
public class UIFormStringInput extends UIFormInputBase<String>
{
   /**
    * type : text
    */
   final static public short TEXT_TYPE = 0;

   /**
    * type : password
    */
   final static public short PASSWORD_TYPE = 1;

   /**
    * type of the text field
    */
   private short type_ = TEXT_TYPE;

   /**
    * max size of text field
    */
   private int maxLength = 0;

   public UIFormStringInput()
   {
   }

   public UIFormStringInput(String name, String bindingExpression, String value)
   {
      super(name, bindingExpression, String.class);
      this.value_ = value;
   }

   public UIFormStringInput(String name, String value)
   {
      this(name, null, value);
   }

   public UIFormStringInput setType(short type)
   {
      type_ = type;
      return this;
   }

   public UIFormStringInput setMaxLength(int maxLength)
   {
      this.maxLength = maxLength;
      return this;
   }

   public int getMaxLength()
   {
      return maxLength;
   }

   public void decode(Object input, WebuiRequestContext context) throws Exception
   {
      String val = (String)input;
      if ((val == null || val.length() == 0) && type_ == PASSWORD_TYPE)
         return;
      value_ = val;
      if (value_ != null && value_.length() == 0)
         value_ = null;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      String value = getValue();
      Writer w = context.getWriter();
      w.write("<input name='");
      w.write(getName());
      w.write('\'');
      if (type_ == PASSWORD_TYPE)
         w.write(" type='password'");
      else
         w.write(" type='text'");
      w.write(" id='");
      w.write(getId());
      w.write('\'');
      if (value != null && value.length() > 0)
      {
         value = HTMLEntityEncoder.getInstance().encodeHTMLAttribute(value);
         w.write(" value='");
         w.write(value);
         w.write('\'');
      }
      if (maxLength > 0)
         w.write(" maxlength='" + maxLength + "'");
      if (readonly_)
         w.write(" readonly ");
      if (!enable_)
         w.write(" disabled ");
      
      renderHTMLAttribute(w);
      
      w.write("/>");
      if (this.isMandatory())
         w.write(" *");
   }
}