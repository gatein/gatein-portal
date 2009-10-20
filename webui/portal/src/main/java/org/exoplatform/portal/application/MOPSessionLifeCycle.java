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
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MOPSessionLifeCycle implements ApplicationLifecycle<WebuiRequestContext>
{

   /** . */
   private final ThreadLocal<POMSessionManager> currentMgr = new ThreadLocal<POMSessionManager>();

   public void onInit(Application app) throws Exception
   {
   }

   public void onStartRequest(Application app, WebuiRequestContext context) throws Exception
   {
      ExoContainer container = context.getApplication().getApplicationServiceContainer();
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      mgr.openSession();
      currentMgr.set(mgr);
   }

   public void onEndRequest(Application app, WebuiRequestContext context) throws Exception
   {
      POMSessionManager mgr = currentMgr.get();
      currentMgr.remove();

      // Need to see if saving untouched session has an impact or not on performances
      mgr.closeSession(true);
   }

   public void onDestroy(Application app) throws Exception
   {
   }
}
