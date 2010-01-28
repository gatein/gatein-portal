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

package org.exoplatform.portal.webui.component;

import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.core.UIBreadcumbs.SelectPathActionListener;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 30, 2006
 * @version:: $Id$
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, events = @EventConfig(listeners = UIBreadcumbsPortlet.SelectPathActionListener.class))
public class UIBreadcumbsPortlet extends UIPortletApplication
{

   public UIBreadcumbsPortlet() throws Exception
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      PortletPreferences prefers = prequest.getPreferences();
      String template = prefers.getValue("template", "system:/groovy/webui/core/UIBreadcumbs.gtmpl");

      UIBreadcumbs uiBreadCumbs = addChild(UIBreadcumbs.class, null, null);
      uiBreadCumbs.setTemplate(template);
   }

   public void loadSelectedPath()
   {
      List<PageNode> nodes = Util.getUIPortal().getSelectedPath();
      List<LocalPath> paths = new ArrayList<LocalPath>();
      for (PageNode node : nodes)
      {
         if (node == null)
            continue;
         if (node.getPageReference() == null)
         {
            paths.add(new LocalPath(null, node.getResolvedLabel()));
         }
         else
         {
            paths.add(new LocalPath(node.getUri(), node.getResolvedLabel()));
         }
      }
      UIBreadcumbs uiBreadCumbs = getChild(UIBreadcumbs.class);
      uiBreadCumbs.setPath(paths);
   }   

   @Override
   public void renderChildren() throws Exception
   {
      loadSelectedPath();
      super.renderChildren();
   }

   static public class SelectPathActionListener extends EventListener<UIBreadcumbs>
   {
      @Override
      public void execute(Event<UIBreadcumbs> event) throws Exception
      {
         String uri = event.getRequestContext().getRequestParameter(OBJECTID);
         UIPortal uiPortal = Util.getUIPortal();
         PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
      }
   }

}
