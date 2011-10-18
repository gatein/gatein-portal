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
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS
 * Author : Tan Pham Dinh
 *          pdtanit@gmail.com
 * Oct 30, 2008  
 */
public class URLValidator implements Validator, Serializable
{
   
   static private final String IP_REGEX =
      "(((((25[0-5])|(2[0-4][0-9])|([01]?[0-9]?[0-9]))\\.){3}((25[0-4])|(2[0-4][0-9])|((1?[1-9]?[1-9])|([1-9]0))))|(0\\.){3}0)";
      
   static public final String URL_REGEX = "^((ht|f)tp(s?)://)" //protocol 
      + "(\\w+(:\\w+)?@)?" //username:password@
      + "(" + IP_REGEX //ip 
      + "|([0-9a-z_!~*'()-]+\\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\.[a-z]{2,6}" //domain like www.exoplatform.org 
      + "|([a-zA-Z][-a-zA-Z0-9]+))" // domain like localhost
      + "(:[0-9]{1,5})?" // port number :8080
      + "((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$"; //uri

   private String key_;

   public URLValidator()
   {
      key_ = "URLValidator.msg.invalid-url";
   }

   public URLValidator(String key)
   {
      if (key != null)
         key_ = key;
   }

   public void validate(UIFormInput uiInput) throws Exception
   {
      if ((uiInput.getValue() == null) || (uiInput.getValue().toString().trim().length() == 0))
         return;
      String s = uiInput.getValue().toString().trim();
      if (s.matches(URL_REGEX))
         return;

      UIForm uiForm = ((UIComponent)uiInput).getAncestorOfType(UIForm.class);
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
      throw new MessageException(new ApplicationMessage(key_, args, ApplicationMessage.WARNING));
   }
}
