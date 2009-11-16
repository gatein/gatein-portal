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

package org.exoplatform.services.resources.impl;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import java.util.List;

/**
 * This class is used to define new resource bundles
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 24 sept. 2009
 */
public class BaseResourceBundlePlugin extends BaseComponentPlugin
{

   private final InitParams params;

   public BaseResourceBundlePlugin(InitParams params)
   {
      this.params = params;
   }

   /**
    * @return the list of enclosed "classpath" resource bundles
    */
   @SuppressWarnings("unchecked")
   public List<String> getClasspathResources()
   {
      ValuesParam vParam = params.getValuesParam("classpath.resources");
      return vParam == null ? null : vParam.getValues();
   }

   /**
    * @return the list of enclosed "portal" resource bundles
    */
   @SuppressWarnings("unchecked")
   public List<String> getPortalResources()
   {
      ValuesParam vParam = params.getValuesParam("portal.resource.names");
      return vParam == null ? null : vParam.getValues();
   }

   /**
    * @return the list of enclosed "init" resource bundles
    */
   @SuppressWarnings("unchecked")
   public List<String> getInitResources()
   {
      ValuesParam vParam = params.getValuesParam("init.resources");
      return vParam == null ? null : vParam.getValues();
   }
}
