/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.description;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.portal.mop.description.DescriptionStore;
import org.gatein.portal.mop.description.DescriptionState;
import org.gatein.common.util.Tools;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestDescriptionService extends AbstractMopServiceTest {

    /** . */
    protected DescriptionStore persistence;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //
        persistence = context.getDescriptionStore();
    }

    public void testResolveNoDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;

        //
        assertEquals(null, descriptionService.resolveDescription(id, null, Locale.ENGLISH));
    }

    public void testResolveDefaultDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;
        getNavigationPersistence().updateNode(id, NodeState.INITIAL.builder().label("foo_name").build());

        //
        assertEquals(null, descriptionService.resolveDescription(id, null, Locale.ENGLISH));
    }

    public void testResolveLocalizedDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;
        HashMap<Locale, DescriptionState> descriptions = new HashMap<Locale, DescriptionState>();
        descriptions.put(Locale.ENGLISH, new DescriptionState("name_en", null));
        descriptions.put(Locale.UK, new DescriptionState("name_en_GB", null));
        persistence.saveDescriptions(id, descriptions);

        //
        assertEquals(null, descriptionService.resolveDescription(id, null, Locale.GERMAN));
        assertEquals(null, descriptionService.resolveDescription(id, null, new Locale("", "GB")));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, Locale.ENGLISH, Locale.GERMAN));
        assertEquals(new DescriptionState("name_en_GB", null), descriptionService.resolveDescription(id, Locale.UK, Locale.GERMAN));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, Locale.US, Locale.GERMAN));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, null, Locale.ENGLISH));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, null, Locale.US));
        assertEquals(new DescriptionState("name_en_GB", null), descriptionService.resolveDescription(id, null, Locale.UK));
    }

    public void testResolveDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;
        getNavigationPersistence().updateNode(id, NodeState.INITIAL.builder().label("name").build());

        HashMap<Locale, DescriptionState> descriptions = new HashMap<Locale, DescriptionState>();
        descriptions.put(Locale.ENGLISH, new DescriptionState("name_en", null));
        descriptions.put(Locale.UK, new DescriptionState("name_en_GB", null));
        persistence.saveDescriptions(id, descriptions);

        //
        assertEquals(null, descriptionService.resolveDescription(id, null, Locale.GERMAN));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, Locale.ENGLISH, Locale.GERMAN));
        assertEquals(new DescriptionState("name_en_GB", null), descriptionService.resolveDescription(id, Locale.UK, Locale.GERMAN));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, Locale.US, Locale.GERMAN));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, null, Locale.ENGLISH));
        assertEquals(new DescriptionState("name_en", null), descriptionService.resolveDescription(id, null, Locale.US));
        assertEquals(new DescriptionState("name_en_GB", null), descriptionService.resolveDescription(id, null, Locale.UK));
    }

    public void testGetDefaultDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;
        getNavigationPersistence().updateNode(id, NodeState.INITIAL.builder().label("foo_name").build());

        //
        assertEquals(new DescriptionState("foo_name", null), descriptionService.loadDescription(id));
    }

    public void testSetDefaultDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;

        //
        assertNull(descriptionService.loadDescription(id));

        //
        descriptionService.saveDescription(id, new DescriptionState("foo_name", null));

        //
        NodeState state = getNavigationPersistence().loadNode(id).state;
        assertEquals("foo_name", state.getLabel());
    }

    public void testRemoveDefaultDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;
        getNavigationPersistence().updateNode(id, NodeState.INITIAL.builder().label("foo_name").build());

        //
        descriptionService.saveDescription(id, null);
    }

    public void testSetLocalizedDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;

        //
        descriptionService.saveDescription(id, Locale.ENGLISH, new DescriptionState("foo_english", null));

        //
        DescriptionState state = persistence.loadDescription(id, Locale.ENGLISH, false);
        assertEquals("foo_english", state.getName());
    }

    public void testSetInvalidLocaleDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;

        //
        try {
            descriptionService.saveDescription(id, new Locale("", "GB"), new DescriptionState("foo_invalid", null));
            fail();
        } catch (IllegalArgumentException e) {
        }

        //
        try {
            descriptionService.saveDescription(id, new Locale("en", "GB", "variant"), new DescriptionState("foo_invalid", null));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAddLocalizedDescription() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;
        persistence.saveDescription(id, Locale.ENGLISH, new DescriptionState("add_english", null));

        //
        descriptionService.saveDescription(id, Locale.FRENCH, new DescriptionState("add_french", null));

        //
        Map<Locale, DescriptionState> descriptions = persistence.loadDescriptions(id);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), descriptions.keySet());
        assertEquals("add_english", descriptions.get(Locale.ENGLISH).getName());
        assertEquals("add_french", descriptions.get(Locale.FRENCH).getName());
    }

    public void testGetDescriptions() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;

        //
        assertNull(descriptionService.loadDescriptions(id));

        //
        Map<Locale, DescriptionState> descriptions = new HashMap<Locale, DescriptionState>();
        descriptions.put(Locale.ENGLISH, new DescriptionState("foo_english", null));
        descriptions.put(Locale.FRENCH, new DescriptionState("foo_french", null));
        persistence.saveDescriptions(id, descriptions);

        //
        descriptions = descriptionService.loadDescriptions(id);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), descriptions.keySet());
        assertEquals(new DescriptionState("foo_english", null), descriptions.get(Locale.ENGLISH));
        assertEquals(new DescriptionState("foo_french", null), descriptions.get(Locale.FRENCH));
    }

    public void testSetDescriptions() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;

        //
        assertNull(descriptionService.loadDescriptions(id));

        //
        Map<Locale, DescriptionState> description = new HashMap<Locale, DescriptionState>();
        description.put(Locale.ENGLISH, new DescriptionState("bar_english", null));
        description.put(Locale.FRENCH, new DescriptionState("bar_french", null));
        descriptionService.saveDescriptions(id, description);

        //
        description = descriptionService.loadDescriptions(id);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), description.keySet());
        assertEquals(new DescriptionState("bar_english", null), description.get(Locale.ENGLISH));
        assertEquals(new DescriptionState("bar_french", null), description.get(Locale.FRENCH));

        //
        description = new HashMap<Locale, DescriptionState>();
        description.put(Locale.ENGLISH, new DescriptionState("bar_english_2", null));
        descriptionService.saveDescriptions(id, description);

        //
        description = descriptionService.loadDescriptions(id);
        assertEquals(Tools.toSet(Locale.ENGLISH), description.keySet());
        assertEquals(new DescriptionState("bar_english_2", null), description.get(Locale.ENGLISH));
    }

    public void testSetInvalidLocaleDescriptions() throws Exception {
        String id = createNavigatation(createSite(SiteType.PORTAL, "foo")).id;

        //
        try {
            descriptionService.saveDescriptions(id,
                    Collections.singletonMap(new Locale("", "GB"), new DescriptionState("bar_invalid", null)));
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            descriptionService.saveDescriptions(id,
                    Collections.singletonMap(new Locale("en", "GB", "variant"), new DescriptionState("bar_invalid", null)));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
