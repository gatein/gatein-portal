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

import java.io.Serializable;

public class UserPortalConfig implements Serializable
{

   PortalConfig portal;

   final UserPortalConfigService service;

   final String portalName;

   final String accessUser;

   /** . */
   private UserPortalContext userPortalContext;

   public UserPortalConfig()
   {
      this.portal = null;
      this.service = null;
      this.portalName = null;
      this.accessUser = null;
      this.userPortalContext = null;
   }

   public UserPortalConfig(PortalConfig portal, UserPortalConfigService service, String portalName, String accessUser, UserPortalContext userPortalContext)
   {
      this.portal = portal;
      this.service = service;
      this.portalName = portalName;
      this.accessUser = accessUser;
      this.userPortalContext = userPortalContext;
   }

   public UserPortal getUserPortal()
   {
      return new UserPortalImpl(service, portalName, portal, accessUser, userPortalContext);
   }

   public PortalConfig getPortalConfig()
   {
      return portal;
   }

   public void setPortalConfig(PortalConfig portal)
   {
      this.portal = portal;
   }
   
   public String getPortalName()
   {
      return portalName;
   }
}