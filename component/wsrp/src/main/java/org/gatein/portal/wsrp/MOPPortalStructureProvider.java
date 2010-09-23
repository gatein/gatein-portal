/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.gatein.common.util.ParameterValidation;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.wsrp.api.PortalStructureProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MOPPortalStructureProvider implements PortalStructureProvider
{
   private final POMSessionManager pomManager;
   private Map<String, PageInfo> pageInfos;
   private Map<String, String> windowIdToUUIDs;

   public MOPPortalStructureProvider(ExoContainer container)
   {
      pomManager = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      windowIdToUUIDs = new HashMap<String, String>();
   }

   public List<String> getPageIdentifiers()
   {
      if (pageInfos == null)
      {
         POMSession session = pomManager.getSession();
         Workspace workspace = session.getWorkspace();
         Collection<Site> sites = workspace.getSites(ObjectType.PORTAL_SITE);

         pageInfos = new HashMap<String, PageInfo>();
         for (Site site : sites)
         {
            Page page = site.getRootPage().getChild("pages");
            if (page != null)
            {
               processPage(page, true);
            }
         }
      }

      LinkedList<String> identifiers = new LinkedList<String>(pageInfos.keySet());
      Collections.sort(identifiers);
      return identifiers;
   }

   private void processPage(Page page, boolean ignoreCurrent)
   {
      if (!ignoreCurrent)
      {
         Described described = page.adapt(Described.class);
         PageInfo pageInfo = new PageInfo(page.getObjectId());
         pageInfos.put(described.getName(), pageInfo);
         UIContainer container = page.getRootComponent();
         processContainer(container, pageInfo);
      }

      Collection<Page> children = page.getChildren();
      if (ParameterValidation.existsAndIsNotEmpty(children))
      {
         for (Page child : children)
         {
            processPage(child, false);
         }
      }
   }

   public List<String> getWindowIdentifiersFor(String pageId)
   {
      PageInfo pageInfo = pageInfos.get(pageId);
      ParameterValidation.throwIllegalArgExceptionIfNull(pageInfo, "PageInfo for " + pageId);

      return pageInfo.getChildrenWindows();
   }

   private void processContainer(UIContainer container, PageInfo pageInfo)
   {
      List<UIComponent> components = container.getComponents();
      for (UIComponent component : components)
      {
         ObjectType<? extends UIComponent> type = component.getObjectType();
         if (ObjectType.WINDOW.equals(type))
         {
            Described described = component.adapt(Described.class);
            String name = described.getName();
            windowIdToUUIDs.put(name, component.getObjectId());
            pageInfo.addWindow(name);
         }
         else if (ObjectType.CONTAINER.equals(type))
         {
            processContainer((UIContainer)component, pageInfo);
         }
         else
         {
            // ignore
         }
      }
   }

   public void assignPortletToWindow(PortletContext portletContext, String windowId, String pageId)
   {
      String uuid = windowIdToUUIDs.get(windowId);
      ParameterValidation.throwIllegalArgExceptionIfNull(uuid, "UUID for " + windowId);

      // get the window
      POMSession session = pomManager.getSession();
      UIWindow window = session.findObjectById(ObjectType.WINDOW, uuid);

      // construct the new customization state
      WSRP wsrp = new WSRP();
      String portletId = portletContext.getId();
      wsrp.setPortletId(portletId);
      if (portletContext instanceof StatefulPortletContext)
      {
         StatefulPortletContext context = (StatefulPortletContext)portletContext;
         if (PortletStateType.OPAQUE.equals(context.getType()))
         {
            wsrp.setState((byte[])context.getState());
         }
         else
         {
            throw new IllegalArgumentException("Don't know how to deal with state: " + context.getState());
         }
      }

      // destroy existing customization as otherwise re-customizing will fail
      Customization<?> customization = window.getCustomization();
      customization.destroy();

      // and re-customize
      window.customize(WSRP.CONTENT_TYPE, portletId, wsrp);

      // mark page for cache invalidation otherwise DataCache will use the previous customization id when trying to set
      // the portlet state in UIPortlet.setState and will not find it resulting in an error
      Page page = window.getPage();
      session.scheduleForEviction(new PageKey("portal", page.getSite().getName(), page.getName()));

      // save
      session.close(true);
   }

   private static class PageInfo
   {
      private String uuid;
      private List<String> childrenWindows;

      private PageInfo(String uuid)
      {
         this.uuid = uuid;
         childrenWindows = new LinkedList<String>();
      }

      public String getUUID()
      {
         return uuid;
      }

      public List<String> getChildrenWindows()
      {
         return childrenWindows;
      }

      public void addWindow(String windowName)
      {
         childrenWindows.add(windowName);
      }
   }
}
