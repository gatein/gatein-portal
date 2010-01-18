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

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Jul 28, 2008  
 */
public class Gadget implements Serializable
{

   private String name;

   private String url;

   private String title;

   private String description;

   private String referenceUrl;

   private String thumbnail;

   private boolean isLocal = true;

   public String getName()
   {
      return name;
   }

   public void setName(String n)
   {
      name = n;
   }

   public String getUrl()
   {
      return url;
   }

   public void setUrl(String u)
   {
      url = u;
   }

   public boolean isLocal()
   {
      return isLocal;
   }

   public void setLocal(Boolean b)
   {
      isLocal = b.booleanValue();
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getReferenceUrl()
   {
      return referenceUrl;
   }

   public void setReferenceUrl(String referenceUrl)
   {
      this.referenceUrl = referenceUrl;
   }

   public String getThumbnail()
   {
      return thumbnail;
   }

   public void setThumbnail(String thumbnail)
   {
      this.thumbnail = thumbnail;
   }

}
