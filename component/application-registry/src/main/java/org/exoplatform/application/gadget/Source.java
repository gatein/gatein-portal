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

package org.exoplatform.application.gadget;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Oct 23, 2008  
 */
public class Source implements Serializable
{

   private String name;

   private String content;

   private String mimeType = "text/plain";

   private Calendar lastModified;

   public Source(String name)
   {
      this.name = name;
   }

   public Source(String name, String mimeType)
   {
      this.name = name;
      this.mimeType = mimeType;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getMimeType()
   {
      return mimeType;
   }

   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }

   public Calendar getLastModified()
   {
      return lastModified;
   }

   public void setLastModified(Calendar lastModified)
   {
      this.lastModified = lastModified;
   }

   public void setTextContent(String text) throws Exception
   {
      content = text;
   }

   public String getTextContent() throws Exception
   {
      return content;
   }
}