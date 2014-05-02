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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.ModelUnmarshaller;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.Page.PageSet;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.config.model.UnmarshalledObject;
import org.exoplatform.portal.config.model.Version;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.gatein.common.util.Tools;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

/**
 * Thu, May 15, 2003 @
 *
 * @author: Tuan Nguyen
 * @version: $Id: TestConverter.java,v 1.6 2004/07/20 12:41:09 tuan08 Exp $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestJIBXXmlMapping extends AbstractGateInTest {

    public TestJIBXXmlMapping(String name) {
        super(name);
    }

    public void setUp() throws Exception {

    }

    public void testPageSetMapping() throws Exception {
        InputStream input = new FileInputStream(
                "src/test/resources/org/exoplatform/portal/config/conf/portal/classic/pages.xml");
        UnmarshalledObject<PageSet> obj = ModelUnmarshaller.unmarshall(PageSet.class, input);
        PageSet pages = obj.getObject();
        assertNotNull(pages);
        assertEquals(3, pages.getPages().size());
    }

    public void testPortalConfigMapping() throws Exception {
        InputStream input = new FileInputStream(
                "src/test/resources/org/exoplatform/portal/config/conf/portal/classic/portal.xml");
        UnmarshalledObject<PortalConfig> obj = ModelUnmarshaller.unmarshall(PortalConfig.class, input);
        PortalConfig portalConfig = obj.getObject();
        assertNotNull(portalConfig);
        assertEquals("classic", portalConfig.getName());
    }

    public void testNavigationMapping() throws Exception {
        InputStream input = new FileInputStream(
                "src/test/resources/org/exoplatform/portal/config/conf/portal/classic/navigation.xml");
        UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, input);
        PageNavigation pageNavigation = obj.getObject();
        assertEquals("portal::classic::homepage", pageNavigation.getFragment().getNode("home").getPageReference());

        input = new FileInputStream(
                "src/test/resources/org/exoplatform/portal/mop/management/navigation-empty.xml");
        obj = ModelUnmarshaller.unmarshall(PageNavigation.class, input);
        pageNavigation = obj.getObject();
        assertEquals(PageNavigation.UNDEFINED_PRIORITY, pageNavigation.getPriority());

        /*
         * IMarshallingContext mctx = bfact.createMarshallingContext(); mctx.setIndent(2); mctx.marshalDocument(obj, "UTF-8",
         * null, new FileOutputStream("target/navigation.xml"));
         *
         * obj = uctx.unmarshalDocument(new FileInputStream("target/navigation.xml"), null); assertEquals(PageNavigation.class,
         * obj.getClass());
         */
    }

    public void testPortletApplicationMapping() throws Exception {
        IBindingFactory bfact = BindingDirectory.getFactory(PortalConfig.class);
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        @SuppressWarnings("unchecked")
        Application<Portlet> app = (Application<Portlet>) uctx.unmarshalDocument(new FileInputStream(
                "src/test/resources/jibx/portlet-application.xml"), null);
        TransientApplicationState<Portlet> portletState = (TransientApplicationState<Portlet>) app.getState();
        assertNotNull(portletState);
        assertEquals("web/BannerPortlet", portletState.getContentId());
        Portlet preferences = (Portlet) portletState.getContentState();
        assertEquals(new PortletBuilder().add("template", "template_value").build(), preferences);
    }

    public void testSimpleNavigationMapping() throws Exception {
        UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/jibx/simple-navigation.xml"));
        ;
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_1_7, obj.getVersion());

        //
        PageNode bar = nav.getFragment().getNode("bar");
        assertEquals("bar_label", bar.getLabel());
        ArrayList<LocalizedString> barLabels = bar.getLabels();
        assertNotNull(barLabels);
        assertEquals(1, barLabels.size());
        assertEquals("bar_label", barLabels.get(0).getValue());
        assertEquals(null, barLabels.get(0).getLang());
        assertEquals(null, bar.getLabels().getExtended(Locale.ENGLISH));
    }

    public void testExtendedNavigationMapping() throws Exception {
        UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/jibx/extended-navigation.xml"));
        ;
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_1_7, obj.getVersion());

        //
        PageNode foo = nav.getFragment().getNode("foo");
        assertEquals("foo_label", foo.getLabel());
        ArrayList<LocalizedString> fooLabels = foo.getLabels();
        assertNotNull(fooLabels);
        assertEquals(3, fooLabels.size());
        assertEquals("foo_label_en", fooLabels.get(0).getValue());
        assertEquals(Locale.ENGLISH, fooLabels.get(0).getLang());
        assertEquals("foo_label", fooLabels.get(1).getValue());
        assertEquals(null, fooLabels.get(1).getLang());
        assertEquals("foo_label_fr", fooLabels.get(2).getValue());
        assertEquals(Locale.FRENCH, fooLabels.get(2).getLang());
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), foo.getLabels().getExtended(Locale.ENGLISH).keySet());
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN), foo.getLabels().getExtended(Locale.GERMAN)
                .keySet());

        //
        PageNode bar = nav.getFragment().getNode("bar");
        assertEquals("bar_label", bar.getLabel());
        ArrayList<LocalizedString> barLabels = bar.getLabels();
        assertNotNull(barLabels);
        assertEquals(1, barLabels.size());
        assertEquals("bar_label", barLabels.get(0).getValue());
        assertEquals(null, barLabels.get(0).getLang());
        assertEquals(null, bar.getLabels().getExtended(Locale.ENGLISH));

        //
        PageNode juu = nav.getFragment().getNode("juu");
        assertEquals(null, juu.getLabel());
        ArrayList<LocalizedString> juuLabels = juu.getLabels();
        assertNotNull(juuLabels);
        assertEquals(3, juuLabels.size());
        assertEquals("juu_label_en", juuLabels.get(0).getValue());
        assertEquals(Locale.ENGLISH, juuLabels.get(0).getLang());
        assertEquals("juu_label_fr", juuLabels.get(1).getValue());
        assertEquals(Locale.FRENCH, juuLabels.get(1).getLang());
        assertEquals("juu_label_fr_FR", juuLabels.get(2).getValue());
        assertEquals(Locale.FRANCE, juuLabels.get(2).getLang());
    }

    public void testNavigationFragment() throws Exception {
        UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/jibx/fragment-navigation.xml"));
        ;
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_1_7, obj.getVersion());

        //
        ArrayList<NavigationFragment> fragments = nav.getFragments();
        assertNotNull(fragments);
        assertEquals(1, fragments.size());
        NavigationFragment fragment = fragments.get(0);
        assertEquals("foo", fragment.getParentURI());
        assertEquals(1, fragment.getNodes().size());
        PageNode bar = fragment.getNode("bar");
        assertNotNull(bar);
        assertEquals(new Properties(), bar.getProperties());
    }

    public void testNavigationNodeProperties() throws Exception {
        UnmarshalledObject<PageNavigation> obj = ModelUnmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/jibx/navigation-with-properties.xml"));
        ;
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_1_7, obj.getVersion());

        //
        ArrayList<NavigationFragment> fragments = nav.getFragments();
        assertNotNull(fragments);
        assertEquals(1, fragments.size());
        NavigationFragment fragment = fragments.get(0);
        List<PageNode> nodes = fragment.getNodes();
        assertEquals(1, nodes.size());

        PageNode n1 = fragment.getNode("bar");
        assertNotNull(n1);

        Properties props1 = n1.getProperties();
        assertNotNull(props1);
        assertEquals(4, props1.size());
        assertEquals("http://example.com", props1.get("externalURI"));
        assertEquals("true", props1.get("openInNewWindow"));
        assertEquals(256, props1.getIntValue("intKey"));
        assertTrue(Math.abs(props1.getDoubleValue("dblKey") - 3.14) < 0.00001);

    }
}
