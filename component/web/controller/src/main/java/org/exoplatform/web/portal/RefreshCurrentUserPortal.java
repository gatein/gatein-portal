/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.web.portal;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.web.application.RequestContext;

/**
 * This listener attempts to find the {@link UserPortal} associated with the current request
 * and invalidate it when the navigation service emits an event for a navigation modification.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class RefreshCurrentUserPortal extends Listener<NavigationService, SiteKey>
{

   @Override
   public void onEvent(Event<NavigationService, SiteKey> event) throws Exception
   {
      RequestContext ctx = RequestContext.getCurrentInstance();
      if (ctx != null)
      {
         UserPortal userPortal = ctx.getUserPortal();
         if (userPortal != null)
         {
            userPortal.refresh();
         }
      }
   }
}
