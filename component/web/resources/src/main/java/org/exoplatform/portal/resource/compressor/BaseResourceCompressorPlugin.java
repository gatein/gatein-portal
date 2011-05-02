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
package org.exoplatform.portal.resource.compressor;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Aug 19, 2010
 */
@Managed
@ManagedDescription("A resource compressor plugin")
@NameTemplate({@Property(key = "service", value = "resource"), @Property(key = "compressor", value = "{Name}")})
public abstract class BaseResourceCompressorPlugin extends BaseComponentPlugin implements ResourceCompressorPlugin
{

   private int priority;
  
   protected final Logger log = LoggerFactory.getLogger(getClass());

   public BaseResourceCompressorPlugin(InitParams params)
   {
      ValueParam priorityParam = params.getValueParam("plugin.priority");
      try
      {
         this.priority = Integer.parseInt(priorityParam.getValue());
      }
      catch (NumberFormatException NBFEx)
      {
         this.priority = -1;
      }
   }

   @Managed
   @ManagedDescription("The plugin priority")
   public int getPriority()
   {
      return priority;
   }

   @Managed
   @ManagedDescription("The plugin type")
   public String getType()
   {
      return getResourceType().name();
   }
}
