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

package org.exoplatform.portal.application;

import javax.servlet.ServletConfig;

public class StandaloneApplication extends PortalApplication
{
   private String webuiConfigPath;

   final static public String STANDALONE_APPLICATION_ID = "StandaloneApplication";

   public StandaloneApplication(ServletConfig config) throws Exception
   {
      super(config);
   }

   public void setWebUIConfigPath(String path)
   {
      webuiConfigPath = path;
   }

   public String getApplicationId()
   {
      return STANDALONE_APPLICATION_ID;
   }

   public String getApplicationInitParam(String name)
   {
      if ("webui.configuration".equals(name))
      {
         return webuiConfigPath;
      }
      return getServletConfig().getInitParameter("standalone." + name);
   }
}
