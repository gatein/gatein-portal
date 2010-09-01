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

import java.io.Serializable;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv81@yahoo.com
 * Jun 7, 2006
 */
public class ApplicationMessage implements Serializable
{
   final public static int ERROR = 0, WARNING = 1, INFO = 2;

   private int type_ = INFO;

   private String messageKey_;

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

}
