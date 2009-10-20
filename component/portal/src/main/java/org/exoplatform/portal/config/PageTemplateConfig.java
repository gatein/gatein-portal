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

package org.exoplatform.portal.config;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 3, 2008  
 */
public class PageTemplateConfig
{

   String location;

   List<String> templates;

   /**
    * @return the location
    */
   public String getLocation()
   {
      return location;
   }

   /**
    * @param location the location to set
    */
   public void setLocation(String location)
   {
      this.location = location;
   }

   /**
    * @return the templates
    */
   public List<String> getTemplates()
   {
      return templates;
   }

   /**
    * @param templates the templates to set
    */
   public void setTemplates(List<String> templates)
   {
      this.templates = templates;
   }

}
