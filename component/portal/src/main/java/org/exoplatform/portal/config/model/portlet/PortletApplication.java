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

package org.exoplatform.portal.config.model.portlet;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.spi.portlet.Preferences;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletApplication extends Application<Preferences, PortletId>
{

   public PortletApplication(ApplicationData<Preferences, PortletId> data)
   {
      super(data);
   }

   public PortletApplication(String storageId, String applicationName, String portletName)
   {
      super(storageId, new PortletId(applicationName, portletName));
   }

   public PortletApplication(String applicationName, String portletName)
   {
      super(new PortletId(applicationName, portletName));
   }

   public PortletApplication(String storageId, PortletId instanceId)
   {
      super(storageId, instanceId);
   }

   public PortletApplication(PortletId instanceId)
   {
      super(instanceId);
   }

   public ApplicationType<Preferences, PortletId> getType()
   {
      return ApplicationType.PORTLET;
   }
}
