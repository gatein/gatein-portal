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

import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 * 
 */
public class JavascriptRemoval implements WebAppListener, Startable
{

   private JavascriptConfigService javascriptService;

   public JavascriptRemoval(JavascriptConfigService _javascriptService)
   {
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

   /**
    * Removes javascript deployed in this web app.
    *
    * @param scontext the servlet context
    */
   private void removeJavascript(ServletContext scontext)
   {
      String webApp = scontext.getContextPath();
      javascriptService.remove(scontext);
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
