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
package org.exoplatform.web.application.javascript;

import java.util.List;

import javax.servlet.ServletContext;

import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 * 
 */
public class JavascriptRemoval implements WebAppListener, Startable
{

   private String portalContainerName;

   private JavascriptConfigService javascriptService;

   public JavascriptRemoval(String _portalContainerName, JavascriptConfigService _javascriptService)
   {
      this.portalContainerName = _portalContainerName;
      this.javascriptService = _javascriptService;
   }

   /**
    * @see org.gatein.wci.WebAppListener#onEvent(org.gatein.wci.WebAppEvent)
    */
   public void onEvent(WebAppEvent arg0)
   {
      if (arg0 instanceof WebAppLifeCycleEvent)
      {
         WebAppLifeCycleEvent wevent = (WebAppLifeCycleEvent)arg0;
         if (wevent.getType() == WebAppLifeCycleEvent.REMOVED)
         {
            removeJavascript(wevent.getWebApp().getServletContext());
            refreshJavascript();
         }
      }
   }

   /** Remove javascript deployed in this web app * */
   private void removeJavascript(ServletContext scontext)
   {
      String webApp = scontext.getContextPath();
      List<JavascriptKey> jsKeys = JavascriptDependentManager.getDeployedJScripts(webApp);
      if (jsKeys == null)
      {
         return;
      }
      for (JavascriptKey key : jsKeys)
      {
         javascriptService.removeJavascript(key, scontext);
      }
      JavascriptDependentManager.clearAssociatedJScripts(webApp);
   }

   private void refreshJavascript()
   {
      javascriptService.refreshMergedJavascript();
   }

   public void start()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(this);
   }

   public void stop()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(this);
   }

}
