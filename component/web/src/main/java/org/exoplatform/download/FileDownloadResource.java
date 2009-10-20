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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Dec 26, 2005
 */
public class FileDownloadResource extends DownloadResource
{

   private String path_;

   public FileDownloadResource(String path, String resourceMimeType)
   {
      this(null, path, resourceMimeType);
   }

   public FileDownloadResource(String downloadType, String path, String resourceMimeType)
   {
      super(downloadType, resourceMimeType);
      path_ = path;
   }

   public InputStream getInputStream() throws IOException
   {
      FileInputStream is = new FileInputStream(path_);
      return is;
   }
}