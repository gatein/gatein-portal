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

import org.exoplatform.commons.utils.Safe;

/**
 * The immutable id of a portlet application.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletId
{

   /** . */
   private final String applicationName;

   /** . */
   private final String portletName;

   public PortletId(String applicationName, String portletName)
   {
      if (applicationName == null)
      {
         throw new NullPointerException();
      }
      if (portletName == null)
      {
         throw new NullPointerException();
      }

      //
      this.applicationName = applicationName;
      this.portletName = portletName;
   }

   public String getApplicationName()
   {
      return applicationName;
   }

   public String getPortletName()
   {
      return portletName;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof PortletId)
      {
         PortletId that = (PortletId)obj;
         return Safe.equals(applicationName, that.applicationName) && Safe.equals(portletName, that.portletName);
      }
      return false;
   }
}
