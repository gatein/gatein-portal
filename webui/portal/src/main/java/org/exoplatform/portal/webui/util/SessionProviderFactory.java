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

package org.exoplatform.portal.webui.util;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoa.pham@exoplatform.com
 * Jan 28, 2008
 */
public class SessionProviderFactory
{

   public static boolean isAnonim()
   {
      String userId = Util.getPortalRequestContext().getRemoteUser();
      if (userId == null)
         return true;
      return false;
   }

   public static SessionProvider createSystemProvider()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      SessionProviderService service =
         (SessionProviderService)container.getComponentInstanceOfType(SessionProviderService.class);
      return service.getSystemSessionProvider(null);
   }

   public static SessionProvider createSessionProvider()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      SessionProviderService service =
         (SessionProviderService)container.getComponentInstanceOfType(SessionProviderService.class);
      return service.getSessionProvider(null);
   }

   public static SessionProvider createAnonimProvider()
   {
      return SessionProvider.createAnonimProvider();
   }

}
