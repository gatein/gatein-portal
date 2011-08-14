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

/**
 * Extends a skin with additional information.
 *
 * Created by The eXo Platform SAS
 * Jan 19, 2007  
 */
public interface SkinConfig extends Skin
{

   /**
    * Returns the skin name
    *
    * @return the skin name
    */
   String getName();

   /**
    * Returns the skin module.
    *
    * @return the module
    */
   String getModule();

   /**
    * Returns the css path.
    *
    * @return the css path
    */
   String getCSSPath();
   
   /**
    * Returns the priority number
    *
    * @return the priority number
    */
   int getCSSPriority();

}