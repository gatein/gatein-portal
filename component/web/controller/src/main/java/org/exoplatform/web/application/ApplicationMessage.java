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

package org.exoplatform.web.application;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv81@yahoo.com
 * Jun 7, 2006
 */
public class ApplicationMessage implements Serializable
{
   private static Log log = ExoLogger.getLogger(ApplicationMessage.class);
   
   final public static int ERROR = 0, WARNING = 1, INFO = 2;

   private int type_ = INFO;
   
   private String messageKey_;
   
   private ResourceBundle resourceBundle;

   private Object[] messageArgs_;
   
   private boolean argsLocalized = true;

   public ApplicationMessage(String key, Object[] args)
   {
      messageKey_ = key;
      messageArgs_ = args;
   }

   public ApplicationMessage(String key, Object[] args, int type)
   {
      this(key, args);
      type_ = type;
   }

   public String getMessage()
   {
      String msg = resolveMessage(messageKey_);
      if (msg != null && messageArgs_ != null)
      {
         for(int i = 0; i < messageArgs_.length; i++) 
         {
            String arg = messageArgs_ [i].toString();
            if (isArgsLocalized()) 
            {
               arg = resolveMessage(arg);
            }
            msg = msg.replace("{" + i + "}", arg);            
         }           
      }      
      
      return msg;
   }     
   
   public void setResourceBundle(ResourceBundle resourceBundle)
   {
      this.resourceBundle = resourceBundle;
   }

   public String getMessageKey()
   {
      return messageKey_;
   }

   public Object[] getMessageAruments()
   {
      return messageArgs_;
   }

   public int getType()
   {
      return type_;
   }

   public void setType(int type)
   {
      this.type_ = type;
   }

   public void setArgsLocalized(boolean argsLocalized)
   {
      this.argsLocalized = argsLocalized;
   }

   public boolean isArgsLocalized()
   {
      return argsLocalized;
   }

   private String resolveMessage(String key)
   {
      if (key == null && resourceBundle == null)
      {
         return key;
      }
      
      String value;
      try
      {         
         value = resourceBundle.getString(key);         
      }
      catch (MissingResourceException ex)
      {
         if (PropertyManager.isDevelopping())
         {
            log.warn("Can not find resource bundle for key : " + key);            
         }
          value = key.substring(key.lastIndexOf('.') + 1);
      }
      return value;
   }
}
