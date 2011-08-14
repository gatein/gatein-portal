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

package org.exoplatform.toolbar.webui.component;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import javax.portlet.EventRequest;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * May 26, 2009  
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl",
   events = {@EventConfig(listeners = UIUserToolBarDashboardPortlet.NavigationChangeActionListener.class)})
public class UIUserToolBarDashboardPortlet extends BasePartialUpdateToolbar
{

   public static String DEFAULT_TAB_NAME = "Tab_Default";

   public UIUserToolBarDashboardPortlet() throws Exception
   {
      UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
      builder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      builder.withTemporalCheck();
      toolbarFilterConfig = builder.build();      
   }

   public UserNavigation getCurrentUserNavigation() throws Exception
   {
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      return getNavigation(SiteKey.user(rcontext.getRemoteUser()));
   }  
   
   @Override
   protected String getResourceIdFromNode(UserNode node, String navId) throws Exception
   {
      if (node == null) 
      {
         throw new IllegalArgumentException("node can't be null");
      }
      return node.getURI(); 
   }

   @Override
   protected UserNode getNodeFromResourceID(String resourceId) throws Exception
   {
      UserNavigation currNav = getCurrentUserNavigation();
      if (currNav == null) return null;
    
      UserPortal userPortal = getUserPortal(); 
      UserNode node = userPortal.resolvePath(currNav, toolbarFilterConfig, resourceId);
      if (node != null && node.getURI().equals(resourceId))
      {
         return node;
      }
      return null;
   }
   
   /**
    * An empty action listener for trigger update portlet by ajax via Portlet Event
    * 
    * @author <a href="trongtt@gmail.com">Trong Tran</a>
    * @version $Revision$
    */
   static public class NavigationChangeActionListener extends EventListener<UIUserToolBarDashboardPortlet>
   {
      private Log log = ExoLogger.getExoLogger(NavigationChangeActionListener.class);

      @Override
      public void execute(Event<UIUserToolBarDashboardPortlet> event) throws Exception
      {
         log.debug("PageNode : " + ((EventRequest)event.getRequestContext().getRequest()).getEvent().getValue() + " is deleted");
      }
   }
}
