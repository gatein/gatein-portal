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

package org.exoplatform.portal.resource;

import org.exoplatform.services.resources.Orientation;

/**
 * An implementation of the skin config.
 *
 * Created by The eXo Platform SAS
 * Jan 19, 2007
 */
class SimpleSkin implements SkinConfig
{

   private final SkinService service_;

   private final String module_;

   private final String name_;

   private final String cssPath_;

   private final String id_;
   
   private final int priority;

   public SimpleSkin(SkinService service, String module, String name, String cssPath)
   {
      this(service, module, name, cssPath, Integer.MAX_VALUE);
   }

   public SimpleSkin(SkinService service, String module, String name, String cssPath, int cssPriority)
   {
      service_ = service;
      module_ = module;
      name_ = name;
      cssPath_ = cssPath;
      id_ = module.replace('/', '_');
      priority = cssPriority;
   }
   
   public int getCSSPriority()
   {
      return priority;
   }
   
   public String getId()
   {
      return id_;
   }

   public String getModule()
   {
      return module_;
   }

   public String getCSSPath()
   {
      return cssPath_;
   }

   public String getName()
   {
      return name_;
   }

   public String toString()
   {
      return "SimpleSkin[id=" + id_ + ",module=" + module_ + ",name=" + name_ + ",cssPath=" + cssPath_ + ", priority=" + priority +"]";
   }
   
   public SkinURL createURL()
   {
      return new SkinURL()
      {

         Orientation orientation = null;

         public void setOrientation(Orientation orientation)
         {
            this.orientation = orientation;
         }

         @Override
         public String toString()
         {
            return cssPath_.replaceAll("\\.css$", service_.getSuffix(orientation));
         }
      };
   }
}