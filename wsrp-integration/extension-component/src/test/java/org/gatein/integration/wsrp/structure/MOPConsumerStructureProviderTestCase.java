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

import junit.framework.TestCase;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.services.listener.Event;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.pc.api.PortletContext;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MOPConsumerStructureProviderTestCase extends TestCase
{
   private MOPConsumerStructureProvider provider;
   private PortalStructureAccess structureAccess;
   private Page page1;

   public void testGetPageIdentifiers()
   {
      List<String> pageIdentifiers = provider.getPageIdentifiers();
      assertEquals(5, pageIdentifiers.size());
      assertTrue(pageIdentifiers.contains("page1"));
      assertTrue(pageIdentifiers.contains("page11"));
      assertTrue(pageIdentifiers.contains("page12"));
      assertTrue(pageIdentifiers.contains("page2"));
      assertTrue(pageIdentifiers.contains("page21"));
   }

   public void testGetWindowIdentifiersForInexistingPage()
   {
      try
      {
         provider.getWindowIdentifiersFor("inexisting");
         fail("Cannot retrieve windows for an inexistent page");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testGetWindowIdentifiersFor()
   {
      checkWindows("page1", "window11", "window12");
      checkWindows("page2");
      checkWindows("page11", "window111", "window112");
      checkWindows("page12", "window121");
      checkWindows("page21", "window211");
   }

   public void testAssignPortletToWindow()
   {
      String newCustomizationId = "/app.new";
      String newWindowName = "portlet";
      provider.assignPortletToWindow(PortletContext.createPortletContext(newCustomizationId), "window11", "page1", newWindowName);
      verify(structureAccess).getWindowFrom(getIdFor("window11"));

      UIWindow window11 = structureAccess.getWindowFrom(getIdFor("window11"));
      verify(structureAccess).saveChangesTo(window11);

      Described described = window11.adapt(Described.class);
      verify(described).setName(newWindowName + " (remote)");

      WSRP state = new WSRP();
      state.setPortletId(newCustomizationId);
      verify(window11).customize(WSRP.CONTENT_TYPE, newCustomizationId, state);

      Customization<?> customization = window11.getCustomization();
      assertEquals(WSRP.CONTENT_TYPE, customization.getType());
   }

   public void testPageCreationEvent() throws Exception
   {
      Page foo = createPage("foo", new String[]{"foo1"}, new String[]{"windowfoo1"});
      Page foo1 = foo.getChild("foo1");
      addWindows(foo1, "windowfoo11");
      org.exoplatform.portal.config.model.Page portalPage = mock(org.exoplatform.portal.config.model.Page.class);
      when(structureAccess.getPageFrom(portalPage)).thenReturn(foo);

      int pageNumber = provider.getPageIdentifiers().size();

      provider.onEvent(new Event<DataStorage, org.exoplatform.portal.config.model.Page>(DataStorage.PAGE_CREATED, null, portalPage));

      List<String> identifiers = provider.getPageIdentifiers();
      assertEquals(pageNumber + 2, identifiers.size());
      assertTrue(identifiers.contains("foo"));
      assertTrue(identifiers.contains("foo1"));

      checkWindows("foo", "windowfoo1");
      checkWindows("foo1", "windowfoo11");

      assertEquals(foo1.getRootComponent().get("windowfoo11"), structureAccess.getWindowFrom(getIdFor("windowfoo11")));
   }

   public void testPageDeletionEvent() throws Exception
   {
      String pageToRemove = "page1";

      org.exoplatform.portal.config.model.Page portalPage = mock(org.exoplatform.portal.config.model.Page.class);
      when(portalPage.getName()).thenReturn(createInternalNameFrom(pageToRemove));
      when(portalPage.getTitle()).thenReturn(pageToRemove);

      // on delete, we actually get the event after the page has been removed from JCR so we don't have an actual page
      when(structureAccess.getPageFrom(portalPage)).thenReturn(null);

      int pageNumber = provider.getPageIdentifiers().size();

      provider.onEvent(new Event<DataStorage, org.exoplatform.portal.config.model.Page>(DataStorage.PAGE_REMOVED, null, portalPage));

      List<String> identifiers = provider.getPageIdentifiers();
      assertEquals(pageNumber - 1, identifiers.size());
      // deleting a page doesn't delete its children, see GTNPORTAL-1630
      assertFalse(identifiers.contains(pageToRemove));
      assertTrue(identifiers.contains("page11"));
      assertTrue(identifiers.contains("page12"));
   }

   public void testPageUpdatedEvent() throws Exception
   {
      // todo!
   }

   @Override
   protected void setUp() throws Exception
   {
      structureAccess = mock(PortalStructureAccess.class);

      page1 = createPage("page1", new String[]{"page11", "page12"}, new String[]{"window11", "window12"});
      Page page2 = createPage("page2", new String[]{"page21"}, null);

      Page page11 = page1.getChild("page11");
      addWindows(page11, "window111", "window112");

      Page page12 = page1.getChild("page12");
      addWindows(page12, "window121");

      Page page21 = page2.getChild("page21");
      addWindows(page21, "window211");

      ArrayList<Page> pages = new ArrayList<Page>();
      pages.add(page1);
      pages.add(page11);
      pages.add(page12);
      pages.add(page2);
      pages.add(page21);
      when(structureAccess.getPages()).thenReturn(pages);

      provider = new MOPConsumerStructureProvider(structureAccess);

      // needed to initialize state
      provider.getPageIdentifiers();
   }

   private void checkWindows(final String pageName, String... windowNames)
   {
      List<String> windows = provider.getWindowIdentifiersFor(pageName);

      if (windowNames != null)
      {
         assertEquals(windowNames.length, windows.size());
         for (String windowName : windowNames)
         {
            assertTrue(windows.contains(windowName));
         }
      }
   }

   private Page createPage(String name, String[] childrenPages, String[] windowNames)
   {
      Page page = mock(Page.class);

      when(page.getName()).thenReturn(createInternalNameFrom(name));

      // mock call to adapt
      Described described = mock(Described.class);
      when(described.getName()).thenReturn(name);

      when(page.adapt(Described.class)).thenReturn(described);

      if (childrenPages != null)
      {
         List<Page> children = new ArrayList<Page>(childrenPages.length);
         for (String pageId : childrenPages)
         {
            Page childPage = createPage(pageId, null, null);
            when(page.getChild(pageId)).thenReturn(childPage);
            children.add(childPage);
         }

         when(page.getChildren()).thenReturn(children);
      }

      addWindows(page, windowNames);

      return page;
   }

   private String createInternalNameFrom(String name)
   {
      return name + "internal";
   }

   private void addWindows(Page page, String... windowNames)
   {
      if (windowNames != null)
      {
         // mock page container
         UIContainer root = mock(UIContainer.class);
         when(page.getRootComponent()).thenReturn(root);

         // for each provided window name, create a mock UIWindow...
         List<UIComponent> children = new ArrayList<UIComponent>(windowNames.length);
         for (String windowName : windowNames)
         {
            UIWindow window = mock(UIWindow.class);
            when(window.getName()).thenThrow(new RuntimeException("Window.getName returns the internal name, not the human readable one"));
            when(window.getObjectId()).thenReturn(getIdFor(windowName));

            // need to use thenAnswer instead of thenReturn here because it doesn't play well with generics
            when(window.getObjectType()).thenAnswer(new Answer<Object>()
            {
               public Object answer(InvocationOnMock invocationOnMock) throws Throwable
               {
                  return ObjectType.WINDOW;
               }
            });

            // mock call to adapt
            Described described = mock(Described.class);
            when(described.getName()).thenReturn(windowName);

            when(window.adapt(Described.class)).thenReturn(described);

            // mock Customization
            final Customization<WSRP> customization = mock(Customization.class);
            when(customization.getType()).thenReturn(WSRP.CONTENT_TYPE);
            when(window.getCustomization()).thenAnswer(new Answer<Object>()
            {
               public Object answer(InvocationOnMock invocationOnMock) throws Throwable
               {
                  return customization;
               }
            });

            // add it to the list of windows for this page
            children.add(window);

            // make sure that we return the window when we ask for it from its uuid
            when(structureAccess.getWindowFrom(getIdFor(windowName))).thenReturn(window);

            // make sure that we return the window if we ask the root component for it
            when(root.get(windowName)).thenReturn(window);
         }

         // the container should return the list of windows when asked for its components
         when(root.getComponents()).thenReturn(children);
      }
   }

   private String getIdFor(String windowName)
   {
      return windowName + "Id";
   }
}
