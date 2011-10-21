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
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.ModelUnmarshaller;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.Page.PageSet;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.config.model.UnmarshalledObject;
import org.exoplatform.portal.config.model.Version;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.gatein.common.util.Tools;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;

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
         uctx.unmarshalDocument(new FileInputStream("src/test/resources/org/exoplatform/portal/config/conf/portal/classic/pages.xml"), null);
      assertEquals(Page.PageSet.class, obj.getClass());
   }

   public void testPortalConfigMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PortalConfig.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj =
         uctx.unmarshalDocument(new FileInputStream("src/test/resources/org/exoplatform/portal/config/conf/portal/classic/portal.xml"), null);
      assertEquals(PortalConfig.class, obj.getClass());
   }

   public void testNavigationMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PageNavigation.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj =
         uctx.unmarshalDocument(new FileInputStream("src/test/resources/org/exoplatform/portal/config/conf/portal/classic/navigation.xml"), null);
      assertEquals(PageNavigation.class, obj.getClass());
      
      PageNavigation pageNavigation = (PageNavigation)obj;
      assertEquals("portal::classic::homepage", pageNavigation.getFragment().getNode("home").getPageReference());

/*
      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      mctx.marshalDocument(obj, "UTF-8", null, new FileOutputStream("target/navigation.xml"));

      obj = uctx.unmarshalDocument(new FileInputStream("target/navigation.xml"), null);
      assertEquals(PageNavigation.class, obj.getClass());
*/
   }

   public void testPortletPreferencesMapping() throws Exception
   {
      IBindingFactory bfact = BindingDirectory.getFactory(PortalConfig.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj =
         uctx.unmarshalDocument(
            new FileInputStream("src/test/resources/org/exoplatform/portal/config/conf/portal/classic/portlet-preferences.xml"), null);
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

   public void testSimpleNavigationMapping() throws Exception
   {
      UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, new FileInputStream("src/test/resources/jibx/simple-navigation.xml"));;
      PageNavigation nav = obj.getObject();
      assertEquals(Version.V_1_2, obj.getVersion());

      //
      PageNode bar = nav.getFragment().getNode("bar");
      assertEquals("bar_label", bar.getLabel());
      ArrayList<LocalizedString> barLabels =  bar.getLabels();
      assertNotNull(barLabels);
      assertEquals(1, barLabels.size());
      assertEquals("bar_label", barLabels.get(0).getValue());
      assertEquals(null, barLabels.get(0).getLang());
      assertEquals(null, bar.getLabels().getExtended(Locale.ENGLISH));
   }

   public void testExtendedNavigationMapping() throws Exception
   {
      UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, new FileInputStream("src/test/resources/jibx/extended-navigation.xml"));;
      PageNavigation nav = obj.getObject();
      assertEquals(Version.V_1_2, obj.getVersion());

      //
      PageNode foo = nav.getFragment().getNode("foo");
      assertEquals("foo_label", foo.getLabel());
      ArrayList<LocalizedString> fooLabels =  foo.getLabels();
      assertNotNull(fooLabels);
      assertEquals(3, fooLabels.size());
      assertEquals("foo_label_en", fooLabels.get(0).getValue());
      assertEquals(Locale.ENGLISH, fooLabels.get(0).getLang());
      assertEquals("foo_label", fooLabels.get(1).getValue());
      assertEquals(null, fooLabels.get(1).getLang());
      assertEquals("foo_label_fr", fooLabels.get(2).getValue());
      assertEquals(Locale.FRENCH, fooLabels.get(2).getLang());
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), foo.getLabels().getExtended(Locale.ENGLISH).keySet());
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN), foo.getLabels().getExtended(Locale.GERMAN).keySet());

      //
      PageNode bar = nav.getFragment().getNode("bar");
      assertEquals("bar_label", bar.getLabel());
      ArrayList<LocalizedString> barLabels =  bar.getLabels();
      assertNotNull(barLabels);
      assertEquals(1, barLabels.size());
      assertEquals("bar_label", barLabels.get(0).getValue());
      assertEquals(null, barLabels.get(0).getLang());
      assertEquals(null, bar.getLabels().getExtended(Locale.ENGLISH));

      //
      PageNode juu = nav.getFragment().getNode("juu");
      assertEquals(null, juu.getLabel());
      ArrayList<LocalizedString> juuLabels =  juu.getLabels();
      assertNotNull(juuLabels);
      assertEquals(3, juuLabels.size());
      assertEquals("juu_label_en", juuLabels.get(0).getValue());
      assertEquals(Locale.ENGLISH, juuLabels.get(0).getLang());
      assertEquals("juu_label_fr", juuLabels.get(1).getValue());
      assertEquals(Locale.FRENCH, juuLabels.get(1).getLang());
      assertEquals("juu_label_fr_FR", juuLabels.get(2).getValue());
      assertEquals(Locale.FRANCE, juuLabels.get(2).getLang());
   }

   public void testNavigationFragment() throws Exception
   {
      UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, new FileInputStream("src/test/resources/jibx/fragment-navigation.xml"));;
      PageNavigation nav = obj.getObject();
      assertEquals(Version.V_1_2, obj.getVersion());

      //
      ArrayList<NavigationFragment> fragments = nav.getFragments();
      assertNotNull(fragments);
      assertEquals(1, fragments.size());
      NavigationFragment fragment = fragments.get(0);
      assertEquals("foo", fragment.getParentURI());
      assertEquals(1, fragment.getNodes().size());
      PageNode bar = fragment.getNode("bar");
      assertNotNull(bar);
   }
}
