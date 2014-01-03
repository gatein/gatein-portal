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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.ModelUnmarshaller;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.Page;
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
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.gatein.common.util.Tools;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageKey;
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

    /** . */
    private static final ModelUnmarshaller unmarshaller = new ModelUnmarshaller();
    
    public TestJIBXXmlMapping(String name) {
        super(name);
    }

    public void setUp() throws Exception {

    }

    public void testPageSetMapping() throws Exception {
        InputStream input = new FileInputStream(
                "src/test/resources/org/exoplatform/portal/config/conf/portal/classic/pages.xml");
        UnmarshalledObject<PageSet> obj = unmarshaller.unmarshall(PageSet.class, input);
        PageSet pages = obj.getObject();
        assertNotNull(pages);
        assertEquals(2, pages.getPages().size());
    }

    public void testPortalConfigMapping() throws Exception {
        InputStream input = new FileInputStream(
                "src/test/resources/org/exoplatform/portal/config/conf/portal/classic/portal.xml");
        UnmarshalledObject<PortalConfig> obj = unmarshaller.unmarshall(PortalConfig.class, input);
        PortalConfig portalConfig = obj.getObject();
        assertNotNull(portalConfig);
        assertEquals("classic", portalConfig.getName());
    }

    public void testNavigationMapping() throws Exception {
        InputStream input = new FileInputStream(
                "src/test/resources/org/exoplatform/portal/config/conf/portal/classic/navigation.xml");
        UnmarshalledObject<PageNavigation> obj = unmarshaller.unmarshall(PageNavigation.class, input);
        PageNavigation pageNavigation = obj.getObject();
        assertEquals("portal::classic::homepage", pageNavigation.getFragment().getNode("home").getPageReference());

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
        Application<Portlet> app = (Application<Portlet>) uctx.unmarshalDocument(new FileInputStream(
                "src/test/resources/jibx/portlet-application.xml"), null);
        TransientApplicationState portletState = (TransientApplicationState) app.getState();
        assertNotNull(portletState);
        assertEquals("web/BannerPortlet", portletState.getContentId());
        Portlet preferences = (Portlet) portletState.getContentState();
        assertEquals(new PortletBuilder().add("template", "template_value").build(), preferences);
    }

    public void testSimpleNavigationMapping() throws Exception {
        UnmarshalledObject<PageNavigation> obj = unmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/jibx/simple-navigation.xml"));
        ;
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_1_3, obj.getVersion());

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
        UnmarshalledObject<PageNavigation> obj = unmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/jibx/extended-navigation.xml"));
        ;
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_1_3, obj.getVersion());

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
        UnmarshalledObject<PageNavigation> obj = unmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/jibx/fragment-navigation.xml"));
        ;
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_1_3, obj.getVersion());

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

    public void testNavigation2_0() throws Exception {
        UnmarshalledObject<PageNavigation> obj = unmarshaller.unmarshall(PageNavigation.class, new FileInputStream(
                "src/test/resources/xml/navigation.xml"));
        PageNavigation nav = obj.getObject();
        assertEquals(Version.V_2_0, obj.getVersion());

        assertEquals(2, nav.getFragments().size());

        NodeState state = nav.getFragments().get(0).getState();
        assertNotNull(state);
        assertEquals("root_label", state.getLabel());
        assertEquals(PageKey.parse("portal::classic::root"), state.getPageRef());

        PageNode bar = nav.getFragments().get(0).getNode("bar");
        assertEquals("bar_label", bar.getLabel());
        ArrayList<LocalizedString> barLabels = bar.getLabels();
        assertNotNull(barLabels);
        assertEquals(1, barLabels.size());
        assertEquals("bar_label", barLabels.get(0).getValue());
        assertEquals(null, barLabels.get(0).getLang());
        assertEquals(null, bar.getLabels().getExtended(Locale.ENGLISH));

        //
        PageNode foo = nav.getFragments().get(1).getNode("foo");
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
        bar = nav.getFragments().get(1).getNode("bar");
        assertEquals("bar_label", bar.getLabel());
        barLabels = bar.getLabels();
        assertNotNull(barLabels);
        assertEquals(1, barLabels.size());
        assertEquals("bar_label", barLabels.get(0).getValue());
        assertEquals(null, barLabels.get(0).getLang());
        assertEquals(null, bar.getLabels().getExtended(Locale.ENGLISH));

        //
        PageNode juu = nav.getFragments().get(1).getNode("juu");
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

    public void testPage2_0() throws Exception {
        UnmarshalledObject<PageSet> obj = unmarshaller.unmarshall(PageSet.class, new FileInputStream(
                "src/test/resources/xml/pages.xml"));
        PageSet set = obj.getObject();
        assertEquals(Version.V_2_0, obj.getVersion());

        //
        assertEquals(2, set.getPages().size());

        //
        Page foo = set.getPages().get(0);
        assertEquals(Collections.singletonList("abc"), Arrays.asList(foo.getAccessPermissions()));
        assertEquals(Collections.singletonList("def"), Arrays.asList(foo.getEditPermissions()));
        assertEquals("foo", foo.getName());
        assertLayout(foo);

        //
        Page bar = set.getPages().get(1);
        assertEquals("bar", bar.getName());
    }

    private void assertLayout(Container foo) {
        assertEquals(1, foo.getChildren().size());
        assertEquals("1", foo.getFactoryId());
        Container zone1 = (Container)foo.getChildren().get(0);
        assertEquals("1", zone1.getStorageName());
        assertEquals(1, zone1.getChildren().size());
        Application<Portlet> foobar = (Application<Portlet>) zone1.getChildren().get(0);
        assertEquals("foobar", foobar.getStorageName());
        assertEquals("ezfzef", foobar.getTitle());
        assertEquals(Arrays.asList("zef"), Arrays.asList(foobar.getAccessPermissions()));
        assertFalse(foobar.getShowInfoBar());
        assertFalse(foobar.getShowApplicationState());
        assertTrue(foobar.getShowApplicationMode());
        TransientApplicationState<Portlet> foobarState = (TransientApplicationState<Portlet>) foobar.getState();
        assertEquals("ef/zeezf", foobarState.getContentId());
        Iterator<Preference> i = foobarState.getContentState().iterator();
        assertTrue(i.hasNext());
        Preference pref = i.next();
        assertEquals("abc", pref.getName());
        assertEquals(Arrays.asList("d1", "d2"), pref.getValues());
        assertFalse(pref.isReadOnly());
        assertFalse(i.hasNext());
    }

    public void testSite2_0() throws Exception {
        UnmarshalledObject<PortalConfig> obj = unmarshaller.unmarshall(PortalConfig.class, new FileInputStream(
                "src/test/resources/xml/site.xml"));
        PortalConfig site = obj.getObject();
        assertEquals(Version.V_2_0, obj.getVersion());

        //
        assertEquals("the_site", site.getName());
        assertEquals("the_display_name", site.getLabel());
        assertEquals("the_description", site.getDescription());
        assertEquals("the_locale", site.getLocale());
        Properties properties = site.getProperties();
        assertEquals(1, properties.size());
        assertEquals(Collections.singleton("entry_key"), properties.keySet());
        assertEquals("entry_value", properties.get("entry_key"));
        assertEquals(Collections.singletonList("an_access_permission"), Arrays.asList(site.getAccessPermissions()));
        assertEquals(Collections.singletonList("an_edit_permission"), Arrays.asList(site.getEditPermissions()));
        assertLayout(site.getPortalLayout());
    }
}
