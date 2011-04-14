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

package org.exoplatform.portal.config;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.application.PortletPreferences.PortletPreferencesSet;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.config.model.Page.PageSet;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Thu, May 15, 2003 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestConverter.java,v 1.6 2004/07/20 12:41:09 tuan08 Exp $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestJIBXXmlMapping extends AbstractGateInTest
{

   public TestJIBXXmlMapping(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {

   }

   public void testPageSetMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PageSet.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj =
         uctx.unmarshalDocument(new FileInputStream("src/test/resources/portal/portal/classic/pages.xml"), null);
      assertEquals(Page.PageSet.class, obj.getClass());
   }

   public void testPortalConfigMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PortalConfig.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj =
         uctx.unmarshalDocument(new FileInputStream("src/test/resources/portal/portal/classic/portal.xml"), null);
      assertEquals(PortalConfig.class, obj.getClass());
   }

   public void testNavigationMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PageNavigation.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj =
         uctx.unmarshalDocument(new FileInputStream("src/test/resources/portal/portal/classic/navigation.xml"), null);
      assertEquals(PageNavigation.class, obj.getClass());
      
      PageNavigation pageNavigation = (PageNavigation)obj;
      assertEquals("portal::classic::homepage", pageNavigation.getNode("home").getPageReference());
      assertEquals("portal", pageNavigation.getOwnerType());
      assertEquals("classic", pageNavigation.getOwnerId());

      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      mctx.marshalDocument(obj, "UTF-8", null, new FileOutputStream("target/navigation.xml"));

      obj = uctx.unmarshalDocument(new FileInputStream("target/navigation.xml"), null);
      assertEquals(PageNavigation.class, obj.getClass());
   }

   public void testPortletPreferencesMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PortalConfig.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj =
         uctx.unmarshalDocument(
            new FileInputStream("src/test/resources/portal/portal/classic/portlet-preferences.xml"), null);
      assertEquals(PortletPreferencesSet.class, obj.getClass());

      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      mctx.marshalDocument(obj, "UTF-8", null, new FileOutputStream("target/portlet-preferences.xml"));
      assertEquals(PortletPreferencesSet.class, obj.getClass());
   }

   public void testPortletApplicationMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PortalConfig.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Application<Portlet> app =
         (Application<Portlet>)uctx.unmarshalDocument(new FileInputStream(
            "src/test/resources/jibx/portlet-application.xml"), null);
      TransientApplicationState portletState = (TransientApplicationState)app.getState();
      assertNotNull(portletState);
      assertEquals("web/BannerPortlet", portletState.getContentId());
      Portlet preferences = (Portlet)portletState.getContentState();
      assertEquals(new PortletBuilder().add("template", "template_value").build(), preferences);
   }

   public void testGadgetApplicationMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PortalConfig.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      @SuppressWarnings("unchecked")
      Application<Gadget> app =
         (Application<Gadget>)uctx.unmarshalDocument(new FileInputStream(
            "src/test/resources/jibx/gadget-application.xml"), null);

      assertEquals(ApplicationType.GADGET, app.getType());
         TransientApplicationState gadgetState = (TransientApplicationState) app.getState();
      assertNotNull(gadgetState);
      assertEquals("Calendar", gadgetState.getContentId());
      assertNull(gadgetState.getContentState());
      // Add test for user-prefs when supported...
   }
}
