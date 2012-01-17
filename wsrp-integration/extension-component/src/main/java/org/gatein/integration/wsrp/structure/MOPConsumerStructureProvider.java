/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.integration.wsrp.structure;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.gatein.common.util.ParameterValidation;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.wsrp.api.context.ConsumerStructureProvider;

import java.util.ArrayList;
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
public class MOPConsumerStructureProvider extends Listener<DataStorage, org.exoplatform.portal.config.model.Page> implements ConsumerStructureProvider
{
   private final PortalStructureAccess structureAccess;
   private Map<String, PageInfo> pageInfos;
   private boolean pagesHaveBeenInitialized;

   public MOPConsumerStructureProvider(PortalStructureAccess structureAccess)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(structureAccess, "PortalStructureAccess");

      this.structureAccess = structureAccess;
      pageInfos = new HashMap<String, PageInfo>();
   }

   public List<String> getPageIdentifiers()
   {
      if (!pagesHaveBeenInitialized)
      {
         // initialize page information
         Collection<Page> pages = structureAccess.getPages();
         for (Page page : pages)
         {
            addPage(page);
         }

         pagesHaveBeenInitialized = true;
      }

      LinkedList<String> identifiers = new LinkedList<String>(pageInfos.keySet());
      Collections.sort(identifiers);
      return identifiers;
   }

   private void addPage(Page page)
   {
      Described described = page.adapt(Described.class);
      PageInfo pageInfo = new PageInfo(page.getObjectId(), described.getName(), page.getName());
      pageInfos.put(pageInfo.getName(), pageInfo);
      UIContainer container = page.getRootComponent();
      processContainer(container, pageInfo);

      Collection<Page> children = page.getChildren();
      if (ParameterValidation.existsAndIsNotEmpty(children))
      {
         for (Page child : children)
         {
            addPage(child);
         }
      }
   }

   public List<String> getWindowIdentifiersFor(String pageId)
   {
      PageInfo pageInfo = pageInfos.get(pageId);
      if (pageInfo == null)
      {
         throw new IllegalArgumentException("Page '" + pageId + "' does not exist.");
      }

      return pageInfo.getChildrenWindows();
   }

   private void processContainer(UIContainer container, PageInfo pageInfo)
   {
      if (container != null)
      {
         List<UIComponent> components = container.getComponents();
         for (UIComponent component : components)
         {
            ObjectType<? extends UIComponent> type = component.getObjectType();
            if (ObjectType.WINDOW.equals(type))
            {
               Described described = component.adapt(Described.class);
               String name = described.getName();

               pageInfo.addWindow(name, component.getObjectId());
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
   }

   public void assignPortletToWindow(PortletContext portletContext, String windowId, String pageId, String exportedPortletHandle)
   {
      PageInfo pageInfo = pageInfos.get(pageId);
      String uuid = pageInfo.getWindowUUID(windowId);
      ParameterValidation.throwIllegalArgExceptionIfNull(uuid, "UUID for " + windowId);

      // get the window
      UIWindow window = structureAccess.getWindowFrom(uuid);

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

      // Change the window's name so that it's less confusing to users
      Described described = window.adapt(Described.class);
      String newName = exportedPortletHandle + " (remote)";
      described.setName(newName); // should be the same as ApplicationRegistryService.REMOTE_DISPLAY_NAME_SUFFIX

      // update window mappings
      pageInfo.updateWindowName(windowId, newName);

      structureAccess.saveChangesTo(window);
   }

   @Override
   public void onEvent(Event<DataStorage, org.exoplatform.portal.config.model.Page> event) throws Exception
   {
      String eventName = event.getEventName();

      // get the MOP page from the event data
      org.exoplatform.portal.config.model.Page portalPage = event.getData();
      Page page = structureAccess.getPageFrom(portalPage);

      if (page == null && DataStorage.PAGE_REMOVED.equals(eventName))
      {
         // if we try to remove a page, when we get this event, the page has already been removed from JCR
         // so we need to work around that fact by retrieving the corresponding PageInfo from the portal page title
         // which should match the Described name and check that it matches the internal name before removing it
         removePage(portalPage.getTitle(), portalPage.getName());

         return;
      }

      if (page != null)
      {
         if (DataStorage.PAGE_CREATED.equals(eventName))
         {
            // add information for new page
            addPage(page);
         }
         else if (DataStorage.PAGE_UPDATED.equals(eventName))
         {
            removePage(page);
            addPage(page);
         }
      }
   }

   private void removePage(Page page)
   {
      Described described = page.adapt(Described.class);
      String name = described.getName();

      removePage(name, page.getName());
   }

   private void removePage(String name, String internalName)
   {
      PageInfo pageInfo = pageInfos.get(name);
      if (pageInfo != null && internalName.equals(pageInfo.getInternalName()))
      {
         // remove page info
         pageInfos.remove(name);
      }
   }

   private static class PageInfo
   {
      private final String uuid;
      private final Map<String, String> childrenWindows = new HashMap<String, String>();

      /** Name as provided by Described */
      private final String name;

      /** Name as automatically generated */
      private final String internalName;

      private PageInfo(String uuid, String name, String internalName)
      {
         this.uuid = uuid;
         this.name = name;
         this.internalName = internalName;
      }

      public String getUUID()
      {
         return uuid;
      }

      public String getInternalName()
      {
         return internalName;
      }

      public List<String> getChildrenWindows()
      {
         return new ArrayList<String>(childrenWindows.keySet());
      }

      public void addWindow(String windowName, String uuid)
      {
         // if we don't have a window name, use the UUID
         if(ParameterValidation.isNullOrEmpty(windowName))
         {
            windowName = uuid;
         }

         // add suffix in case we have several windows with the same name in the page
         if (childrenWindows.containsKey(windowName))
         {
            if (windowName.endsWith("|"))
            {
               windowName += "|";
            }
            else
            {
               windowName += windowName + " |";
            }
         }

         childrenWindows.put(windowName, uuid);
      }

      public void updateWindowName(String oldWindowName, String newWindowName)
      {
         String windowUUID = getWindowUUID(oldWindowName);
         childrenWindows.remove(oldWindowName);
         childrenWindows.put(newWindowName, windowUUID);
      }

      public String getName()
      {
         return name;
      }

      public String getWindowUUID(String windowId)
      {
         return childrenWindows.get(windowId);
      }
   }
}
