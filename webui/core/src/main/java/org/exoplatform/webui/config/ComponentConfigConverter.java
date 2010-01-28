/*
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

package org.exoplatform.webui.config;

import org.exoplatform.webui.application.ConfigurationManager;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.commons.serialization.api.TypeConverter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ComponentConfigConverter extends TypeConverter<Component, ComponentHandle>
{

   @Override
   public ComponentHandle write(Component external) throws Exception
   {
      return external.handle;
   }

   @Override
   public Component read(ComponentHandle internal) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      WebuiApplication webuiApp = (WebuiApplication)context.getApplication();
      ConfigurationManager configMgr = webuiApp.getConfigurationManager();
      return configMgr.getComponentConfig(internal);
   }
}
