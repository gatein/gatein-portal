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

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.user.UserPortalContext;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.mop.user.UserPortalImpl;

public class UserPortalConfig
{

   PortalConfig portal;

   final UserPortalConfigService service;

   final String portalName;

   final String accessUser;

   /** . */
   private UserPortalImpl userPortal;

   /** . */
   private UserPortalContext bundleResolver;

   public UserPortalConfig()
   {
      this.portal = null;
      this.service = null;
      this.portalName = null;
      this.accessUser = null;
      this.bundleResolver = null;
   }

   public UserPortalConfig(PortalConfig portal, UserPortalConfigService service, String portalName, String accessUser, UserPortalContext bundleResolver)
   {
      this.portal = portal;
      this.service = service;
      this.portalName = portalName;
      this.accessUser = accessUser;
      this.bundleResolver = bundleResolver;
   }

   public UserPortal getUserPortal()
   {
         userPortal = new UserPortalImpl(
            service,
            service.navService,
            service.orgService_,
            service.userACL_,
            portalName,
            portal,
            accessUser,
            bundleResolver
         );
      return userPortal;
   }

   public PortalConfig getPortalConfig()
   {
      return portal;
   }

   public void setPortal(PortalConfig portal)
   {
      this.portal = portal;
   }
}