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

package org.exoplatform.services.common;

import java.io.File;
import java.io.FileFilter;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 11, 2006
 */
public class FileFilterByExt implements FileFilter
{

   private String[] exts = new String[0];

   private boolean isFile = false;

   public FileFilterByExt()
   {
   }

   public FileFilterByExt(boolean isFile_)
   {
      isFile = isFile_;
   }

   public FileFilterByExt(String[] exts_)
   {
      exts = new String[exts_.length];
      for (int i = 0; i < exts_.length; i++)
      {
         if (exts_[i] != null)
            exts[i] = exts_[i].trim().toLowerCase();
      }
   }

   public FileFilterByExt(String ext)
   {
      exts = new String[]{ext.trim().toLowerCase()};
   }

   public boolean accept(File f)
   {
      if (exts.length == 0 && !isFile)
         return f.isDirectory();
      if (exts.length == 0 && isFile && f.isFile())
         return f.getName().indexOf(".") < 0;
      if (f.isDirectory())
         return false;
      return isEndWith(f.getName());
   }

   private boolean isEndWith(String name)
   {
      name = name.trim().toLowerCase();
      for (String ele : exts)
         if (ele != null && name.endsWith(ele))
            return true;
      return false;
   }
}
