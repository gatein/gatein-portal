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

package org.exoplatform.download;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Dec 26, 2005
 */
abstract public class DownloadResource
{

   private String downloadType_;

   private String downloadName_;

   private String resourceMimeType_;

   private long liveTime_;

   private int limit_;

   private int accessCounter_;

   public DownloadResource(String resourceMimeType)
   {
      this(null, resourceMimeType);
   }

   public DownloadResource(String downloadType, String resourceMimeType)
   {
      downloadType_ = downloadType;
      resourceMimeType_ = resourceMimeType;
   }

   public String getDownloadType()
   {
      return downloadType_;
   }

   public String getDownloadName()
   {
      return downloadName_;
   }

   public void setDownloadName(String name)
   {
      downloadName_ = name;
   }

   public String getResourceMimeType()
   {
      return resourceMimeType_;
   }

   public long getLiveTime()
   {
      return liveTime_;
   }

   public void setLiveTime(long t)
   {
      liveTime_ = t;
   }

   public int getLimit()
   {
      return limit_;
   }

   public void setLimit(int n)
   {
      limit_ = n;
   }

   public int getAccessCounter()
   {
      return accessCounter_;
   }

   public void setAccessCounter(int c)
   {
      accessCounter_ = c;
   }

   abstract public InputStream getInputStream() throws IOException;

}