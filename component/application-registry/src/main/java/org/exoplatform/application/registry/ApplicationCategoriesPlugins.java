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

package org.exoplatform.application.registry;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Le Bien Thuy
 *          lebienthuy@gmail.com
 * 9 Oct 2007  
 */

public class ApplicationCategoriesPlugins extends BaseComponentPlugin
{
   private ConfigurationManager cmanager_;

   private ApplicationRegistryService pdcService_;

   private List<?> configs;

   public ApplicationCategoriesPlugins(ApplicationRegistryService pdcService, ConfigurationManager cmanager,
      InitParams params) throws Exception
   {
      configs = params.getObjectParamValues(ApplicationCategory.class);
      cmanager_ = cmanager;
      pdcService_ = pdcService;
   }

   public void run() throws Exception
   {
      if (configs == null)
         return;
      for (Object ele : configs)
      {
         ApplicationCategory category = (ApplicationCategory)ele;
         pdcService_.save(category);
         List<Application> apps = category.getApplications();
         for (Application app : apps)
            pdcService_.save(category, app);
      }
   }
}
