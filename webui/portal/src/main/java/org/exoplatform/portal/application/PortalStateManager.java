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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.webui.application.StateManager;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;

public class PortalStateManager extends StateManager
{

   /** . */
   private final StateManager impl;

   public PortalStateManager()
   {
      boolean clustered = ExoContainer.getProfiles().contains("cluster");

      //
      if (clustered)
      {
         impl = new ReplicatingStateManager();
      }
      else
      {
         impl = new LegacyPortalStateManager();
      }
   }

   @Override
   public UIApplication restoreUIRootComponent(WebuiRequestContext context) throws Exception
   {
      return impl.restoreUIRootComponent(context);
   }

   @Override
   public void storeUIRootComponent(WebuiRequestContext context) throws Exception
   {
      impl.storeUIRootComponent(context);
   }

   @Override
   public void expire(String sessionId, WebuiApplication app) throws Exception
   {
      impl.expire(sessionId, app);
   }
}