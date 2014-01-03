/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management.binding.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.PageBody;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.BodyData;
import org.exoplatform.portal.pom.data.BodyType;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SiteLayoutMarshallerTest extends AbstractMarshallerTest {
    public void testPortalDataUnmarshalling() {
        SiteLayoutMarshaller marshaller = new SiteLayoutMarshaller();
        PortalConfig data = marshaller.unmarshal(getClass().getResourceAsStream(
                "/org/exoplatform/portal/mop/management/portal.xml"));
        assertNotNull(data);
        assertEquals("classic", data.getName());
        assertEquals("site-label", data.getLabel());
        assertEquals("site-description", data.getDescription());
        assertEquals("en", data.getLocale());
        assertEquals("Everyone", Utils.join(";", data.getAccessPermissions()));
        assertEquals("*:/platform/administrators", Utils.join(";", data.getEditPermissions()));
        assertNotNull(data.getProperties());
        assertEquals(1, data.getProperties().size());
        assertTrue(data.getProperties().containsKey("sessionAlive"));
        assertEquals("onDemand", data.getProperties().get("sessionAlive"));

        // Verify portal layout container only has children
        assertNotNull(data.getPortalLayout());
        Container layout = data.getPortalLayout();
        assertNull(layout.getStorageId());
        assertNull(layout.getId());
        assertNull(layout.getName());
        assertNull(layout.getIcon());
        assertNull(layout.getTemplate());
        assertNull(layout.getFactoryId());
        assertNull(layout.getTitle());
        assertNull(layout.getDescription());
        assertNull(layout.getWidth());
        assertNull(layout.getHeight());
        assertNull(layout.getAccessPermissions());
        List<ModelObject> children = data.getPortalLayout().getChildren();
        assertEquals(5, children.size());
        int bodyCount = 0;
        for (ModelObject component : children) {
            if (component instanceof Application) {
            } else if (component instanceof PageBody) {
                bodyCount++;
            } else {
                fail("Only application data and one body data should be created for a portal layout.");
            }
        }
        assertEquals(1, bodyCount);

        // Verify banner portlet app
        {
            @SuppressWarnings("unchecked")
            Application<Portlet> application = (Application<Portlet>) children.get(0);
            assertTrue(application.getType() == ApplicationType.PORTLET);
            ApplicationState<Portlet> state = application.getState();
            assertNotNull(state);
            assertTrue(state instanceof TransientApplicationState);
            TransientApplicationState<Portlet> tas = (TransientApplicationState<Portlet>) state;
            assertEquals("web/BannerPortlet", tas.getContentId());
            Portlet portlet = tas.getContentState();
            int count = 0;
            for (Preference pref : portlet) {
                count++;
            }
            assertEquals(1, count);
            Preference pref = portlet.getPreference("template");
            assertNotNull(pref);
            assertEquals("template", pref.getName());
            assertEquals("par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl", pref.getValue());
            assertFalse(pref.isReadOnly());

            assertEquals("Default:DefaultTheme::Mac:MacTheme::Vista:VistaTheme", application.getTheme());
            assertEquals("Banner", application.getTitle());
            assertEquals("*:/platform/administrators;*:/organization/management/executive-board",
                    Utils.join(";", application.getAccessPermissions()));
            assertFalse(application.getShowInfoBar());
            assertTrue(application.getShowApplicationState());
            assertFalse(application.getShowApplicationMode());
            assertEquals("Banner Portlet", application.getDescription());
            assertEquals("PortletIcon", application.getIcon());
            assertEquals("250px", application.getWidth());
            assertEquals("350px", application.getHeight());
        }

        // Verify navigation portlet app
        {
            @SuppressWarnings("unchecked")
            Application<Portlet> application = (Application<Portlet>) children.get(1);
            assertTrue(application.getType() == ApplicationType.PORTLET);
            ApplicationState<Portlet> state = application.getState();
            assertNotNull(state);
            assertTrue(state instanceof TransientApplicationState);
            TransientApplicationState<Portlet> tas = (TransientApplicationState<Portlet>) state;
            assertEquals("web/NavigationPortlet", tas.getContentId());
            assertNull(tas.getContentState());

            assertNull(application.getTheme());
            assertNull(application.getTitle());
            assertEquals("Everyone", Utils.join(";", application.getAccessPermissions()));
            assertFalse(application.getShowInfoBar());
            assertTrue(application.getShowApplicationState());
            assertTrue(application.getShowApplicationMode());
            assertNull(application.getDescription());
            assertNull(application.getIcon());
            assertNull(application.getWidth());
            assertNull(application.getHeight());
        }
    }

    public void testPortalDataMarshalling() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Portlet portlet = new Portlet();
        portlet.putPreference(new Preference("pref-1", "value-1", true));
        portlet.putPreference(new Preference("pref-2", "value-2", false));
        portlet.putPreference(new Preference("multi-value-pref", Arrays.asList("one", "two", "three"), false));
        portlet.putPreference(new Preference("no-value-pref", (String) null, true));

        ApplicationState<Portlet> state = new TransientApplicationState<Portlet>("app-ref/portlet-ref", portlet);
        ApplicationData<Portlet> application = new ApplicationData<Portlet>(null, null, ApplicationType.PORTLET, state, null,
                "app-title", "app-icon", "app-description", false, true, false, "app-theme", "app-wdith", "app-height",
                new HashMap<String, String>(), Collections.singletonList("app-edit-permissions"));

        List<ComponentData> children = new ArrayList<ComponentData>();
        children.add(application);
        children.add(new BodyData(null, BodyType.PAGE));

        ContainerData layout = new ContainerData(null, null, null, "container-name", "container-icon", "container-template",
                "factoryId", "title", "description", "width", "height", Collections.singletonList("blah"), children);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        PortalData expectedData = new PortalData(null, "name", "type", "locale", "label", "description",
                Collections.singletonList("access-permissions"), Collections.singletonList("edit-permissions"), properties, "skin", layout, null);

        PortalConfig expected = new PortalConfig(expectedData);

        SiteLayoutMarshaller marshaller = new SiteLayoutMarshaller();
        marshaller.marshal(expected, baos);

        // System.out.println(baos.toString());

        PortalConfig actual = marshaller.unmarshal(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(actual);
        assertNull(actual.getStorageId());
        assertNull(actual.getStorageName());
        assertEquals("name", actual.getName());
        assertEquals("label", actual.getLabel());
        assertEquals("description", actual.getDescription());
        assertEquals("portal", actual.getType());
        assertEquals("locale", actual.getLocale());
        assertEquals("access-permissions", Utils.join(";", actual.getAccessPermissions()));
        assertEquals("edit-permissions", Utils.join(";", actual.getEditPermissions()));
        assertEquals(properties, actual.getProperties());
        assertEquals("skin", actual.getSkin());
        assertNotNull(actual.getPortalLayout());
        assertNotNull(actual.getPortalLayout().getChildren());
        assertEquals(2, actual.getPortalLayout().getChildren().size());

        compareComponents(expected.getPortalLayout().getChildren(), actual.getPortalLayout().getChildren());
    }
}
