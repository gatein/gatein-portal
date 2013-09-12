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

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.DevicePropertyCondition;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.NodeMap;
import org.exoplatform.portal.config.model.PageBody;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.config.model.RedirectCondition;
import org.exoplatform.portal.config.model.RedirectMappings;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.config.model.UserAgentConditions;
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
        assertEquals("*:/platform/administrators", data.getEditPermission());
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
        assertArrayEquals(new String[] {"*:/platform/app-movers"}, layout.getMoveAppsPermissions());
        assertArrayEquals(new String[] {"*:/platform/container-movers"}, layout.getMoveContainersPermissions());
        List<ModelObject> children = data.getPortalLayout().getChildren();
        assertEquals(5, children.size());

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
            for (Iterator<Preference> it = portlet.iterator(); it.hasNext(); it.next()) {
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

        ContainerData layout = new ContainerData(null, null, "container-name", "container-icon", "container-template",
                "factoryId", "title", "description", "width", "height", Collections.singletonList("accessPermissions"),
                Collections.singletonList("moveAppsPermissions"), Collections.singletonList("moveContainersPermissions"),
                children);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        PortalData expectedData = new PortalData(null, "name", "type", "locale", "label", "description",
                Collections.singletonList("access-permissions"), "edit-permissions", properties, "skin", layout, null);

        PortalConfig expected = new PortalConfig(expectedData);
        Container expectedLayout = expected.getPortalLayout();

        SiteLayoutMarshaller marshaller = new SiteLayoutMarshaller();
        marshaller.marshal(expected, baos, false);

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
        assertEquals("edit-permissions", actual.getEditPermission());
        assertEquals(properties, actual.getProperties());
        assertEquals("skin", actual.getSkin());
        Container actualLayout = actual.getPortalLayout();
        assertNotNull(actualLayout);
        assertArrayEquals(expectedLayout.getMoveAppsPermissions(), actualLayout.getMoveAppsPermissions());
        assertArrayEquals(expectedLayout.getMoveContainersPermissions(), actualLayout.getMoveContainersPermissions());
        assertNotNull(actual.getPortalLayout().getChildren());
        assertEquals(2, actual.getPortalLayout().getChildren().size());

        compareComponents(expected.getPortalLayout().getChildren(), actual.getPortalLayout().getChildren());
    }

    public void testPortalWithPageBodyInContainer() {
        SiteLayoutMarshaller marshaller = new SiteLayoutMarshaller();
        PortalConfig data = marshaller.unmarshal(getClass().getResourceAsStream(
                "/org/exoplatform/portal/mop/management/portal-pagebody.xml"));
        assertNotNull(data);
        assertEquals("classic", data.getName());
        assertEquals("site-label", data.getLabel());
        assertEquals("site-description", data.getDescription());
        assertEquals("en", data.getLocale());
        assertEquals("Everyone", Utils.join(";", data.getAccessPermissions()));
        assertEquals("*:/platform/administrators", data.getEditPermission());
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

        // Verify container w/ page-body
        Container container = (Container) layout.getChildren().get(3);
        assertArrayEquals(new String[] {"*:/platform/app-movers"}, container.getMoveAppsPermissions());
        assertArrayEquals(new String[] {"*:/platform/container-movers"}, container.getMoveContainersPermissions());
        assertNotNull(container.getChildren());
        assertEquals(2, container.getChildren().size());
        Container container0 = (Container) container.getChildren().get(0);
        PageBody body = (PageBody) container0 .getChildren().get(0);
        assertNotNull(body);
    }

    public void testPortalWithRedirect() {
        SiteLayoutMarshaller marshaller = new SiteLayoutMarshaller();
        PortalConfig data = marshaller.unmarshal(getClass().getResourceAsStream(
                "/org/exoplatform/portal/mop/management/portal-redirects.xml"));

        // Create the objects that match what should be parsed from xml
        List<PortalRedirect> redirects = new ArrayList<PortalRedirect>(2);

        PortalRedirect redirect = new PortalRedirect();
        redirect.setRedirectSite("redirectA");
        redirect.setName("Redirect Site A");
        redirect.setEnabled(true);
        // condition 0
        redirect.getConditions().add(new ConditionBuilder("condition 0")
                .userAgent().contains(".*").build()
                .deviceProperty("foo").equals("bar").build()
                .deviceProperty("hello").matches("(?i)world").build()
                .deviceProperty("number").greaterThan(10.0f).lessThan(25.0f).build()
                .build());
        // condition 1
        redirect.getConditions().add(new ConditionBuilder("condition 1")
                .userAgent().contains("(?i)foo").doesNotContain("bar").build()
                .build());
        // condition 2
        redirect.getConditions().add(new ConditionBuilder("condition 2")
                .userAgent().contains("(?i)abc", "(?i)def").doesNotContain("world").build()
                .build());
        // condition 3
        redirect.getConditions().add(new ConditionBuilder("condition 3")
                .userAgent().contains("(?i)abc").doesNotContain("hello", "world").build()
                .build());
        // condition 4
        redirect.getConditions().add(new ConditionBuilder("condition 4")
                .userAgent().contains("(?i)abc", "(?i)def").doesNotContain("hello", "world").build()
                .build());
        // condition 5
        redirect.getConditions().add(new ConditionBuilder("condition 5")
                .userAgent().contains("(?i)abc", "(?i)def").build()
                .build());
        // condition 6
        redirect.getConditions().add(new ConditionBuilder("condition 6")
                .userAgent().doesNotContain("hello", "world").build()
                .build());
        // node mappings
        redirect.setMappings(new RedirectMappingsBuilder().nameMatching(false).unresolved(RedirectMappings.UnknownNodeMapping.COMMON_ANCESTOR_NAME_MATCH)
                .map("foo", "bar")
                .map("hello/world", "redirect/hello/world")
                .map("/", "redirect_root")
                .map("root", "/")
                .map("ABC/123/XYZ", "123")
                .map("/with_slash", "/with_slash")
                .map("/with_slash_two", "without_slash")
                .map("without_slash", "/with_slash_two")
                .build());
        // add
        redirects.add(redirect);

        // redirect 2
        redirect = new PortalRedirect();
        redirect.setRedirectSite("redirectB");
        redirect.setName("Redirect Site B");
        redirect.setEnabled(false);
        // add
        redirects.add(redirect);

        assertEquals(redirects, data.getPortalRedirects(), "redirects", PortalRedirect.class);
    }

    public void testPortalRedirectMarshalling() {
        SiteLayoutMarshaller marshaller = new SiteLayoutMarshaller();
        PortalConfig expected = marshaller.unmarshal(getClass().getResourceAsStream(
                "/org/exoplatform/portal/mop/management/portal-redirects.xml"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(expected, baos, false);

        PortalConfig actual = marshaller.unmarshal(new ByteArrayInputStream(baos.toByteArray()));

        assertEquals(expected.getPortalRedirects(), actual.getPortalRedirects(), "redirects", PortalRedirect.class);
    }

    private static void assertEquals(PortalRedirect expected, PortalRedirect actual) {
        if (expected == null) {
            assertNull("Actual redirect was NOT null.", actual);
            return;
        } else {
            assertNotNull("Actual redirect was null.", actual);
        }

        assertEquals(expected.getRedirectSite(), actual.getRedirectSite());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.isEnabled(), actual.isEnabled());
        assertEquals(expected.getConditions(), actual.getConditions(), "conditions", RedirectCondition.class);
        assertEquals(expected.getMappings(), actual.getMappings());
    }

    private static void assertEquals(RedirectCondition expected, RedirectCondition actual) {
        if (expected == null) {
            assertNull("Actual condition was NOT null.", actual);
            return;
        } else {
            assertNotNull("Actual condition was null.", actual);
        }
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getUserAgentConditions(), actual.getUserAgentConditions());
        assertEquals(expected.getDeviceProperties(), actual.getDeviceProperties(), "device properties", DevicePropertyCondition.class);
    }

    private static void assertEquals(UserAgentConditions expected, UserAgentConditions actual) {
        if (expected == null) {
            assertNull("Actual user agent conditions was NOT null.", actual);
            return;
        } else {
            assertNotNull("Actual user agent conditions was null.", actual);
        }

        assertEquals(expected.getContains(), actual.getContains());
        assertEquals(expected.getDoesNotContain(), actual.getDoesNotContain());
    }

    private static void assertEquals(DevicePropertyCondition expected, DevicePropertyCondition actual) {
        if (expected == null) {
            assertNull("Actual device property condition was NOT null.", actual);
            return;
        } else {
            assertNotNull("Actual device property condition was null.", actual);
        }

        assertEquals(expected.getPropertyName(), actual.getPropertyName());
        assertEquals(expected.getGreaterThan(), actual.getGreaterThan());
        assertEquals(expected.getLessThan(), actual.getLessThan());
        assertEquals(expected.getEquals(), actual.getEquals());
        assertEquals(expected.getMatches(), actual.getMatches());
    }

    private static void assertEquals(RedirectMappings expected, RedirectMappings actual) {
        if (expected == null) {
            assertNull("Actual mappings was NOT null.", actual);
            return;
        } else {
            assertNotNull("Actual mappings was null.", actual);
        }

        assertEquals(expected.isUseNodeNameMatching(), actual.isUseNodeNameMatching());
        assertEquals(expected.getUnresolvedNode(), actual.getUnresolvedNode());
        assertEquals(expected.getMappings(), actual.getMappings(), "mappings", NodeMap.class);
    }

    private static void assertEquals(NodeMap expected, NodeMap actual) {
        if (expected == null) {
            assertNull("Actual node map was NOT null.", actual);
            return;
        } else {
            assertNotNull("Actual node map was null.", actual);
        }

        assertEquals(expected.getOriginNode(), actual.getOriginNode());
        assertEquals(expected.getRedirectNode(), actual.getRedirectNode());
    }

    // Would be nice if the actual objects properly supported equals...
    private static <T> void assertEquals(List<T> expected, List<T> actual, String elements, Class<T> clazz) {
        if (expected == null) {
            assertNull("Actual " + elements + " was NOT null.", actual);
            return;
        } else {
            assertNotNull("Actual " + elements + " was null.", actual);
            assertEquals("Number of " + elements + ".", expected.size(), actual.size());
        }

        for (int i=0; i<expected.size(); i++) {
            if (clazz == PortalRedirect.class) {
                assertEquals((PortalRedirect) expected.get(i), (PortalRedirect) actual.get(i));
            } else if (clazz == RedirectCondition.class) {
                assertEquals((RedirectCondition) expected.get(i), (RedirectCondition) actual.get(i));
            } else if (clazz == DevicePropertyCondition.class) {
                assertEquals((DevicePropertyCondition) expected.get(i), (DevicePropertyCondition) actual.get(i));
            } else if (clazz == NodeMap.class) {
                assertEquals((NodeMap) expected.get(i), (NodeMap) actual.get(i));
            }
        }
    }

    private static class ConditionBuilder {

        private String name;
        private UserAgentConditions userAgentConditions;
        private ArrayList<DevicePropertyCondition> devicePropertyConditions;

        public ConditionBuilder(String name) {
            this.name = name;
        }

        public UserAgentConditionsBuilder userAgent() {
            return new UserAgentConditionsBuilder(this);
        }

        public DevicePropertyConditionBuilder deviceProperty(String propertyName) {
            if (devicePropertyConditions == null) {
                devicePropertyConditions = new ArrayList<DevicePropertyCondition>();
            }

            return new DevicePropertyConditionBuilder(propertyName, this);
        }

        public RedirectCondition build() {
            RedirectCondition condition = new RedirectCondition();
            condition.setName(name);
            condition.setUserAgentConditions(userAgentConditions);
            condition.setDeviceProperties(devicePropertyConditions);

            return condition;
        }
    }

    private static class UserAgentConditionsBuilder {

        private final ConditionBuilder conditionBuilder;
        private String[] contains;
        private String[] doesNotContain;

        private UserAgentConditionsBuilder(ConditionBuilder conditionBuilder) {
            this.conditionBuilder = conditionBuilder;
        }

        public UserAgentConditionsBuilder contains(String...contains) {
            this.contains = contains;
            return this;
        }

        public UserAgentConditionsBuilder doesNotContain(String...doesNotContain) {
            this.doesNotContain = doesNotContain;
            return this;
        }

        public ConditionBuilder build() {
            UserAgentConditions uac = new UserAgentConditions();
            if (contains != null) {
                uac.setContains(new ArrayList<String>(Arrays.asList(contains)));
            }
            if (doesNotContain != null) {
                uac.setDoesNotContain(new ArrayList<String>(Arrays.asList(doesNotContain)));
            }
            conditionBuilder.userAgentConditions = uac;
            return conditionBuilder;
        }
    }

    private static class DevicePropertyConditionBuilder {

        private final ConditionBuilder conditionBuilder;
        private final String propertyName;
        private Float greaterThan;
        private Float lessThan;
        private String equals;
        private String matches;

        private DevicePropertyConditionBuilder(String propertyName, ConditionBuilder conditionBuilder) {
            this.conditionBuilder = conditionBuilder;
            this.propertyName = propertyName;
        }

        public DevicePropertyConditionBuilder greaterThan(Float greaterThan) {
            this.greaterThan = greaterThan;
            return this;
        }

        public DevicePropertyConditionBuilder lessThan(Float lessThan) {
            this.lessThan = lessThan;
            return this;
        }

        public DevicePropertyConditionBuilder equals(String equals) {
            this.equals = equals;
            return this;
        }

        public DevicePropertyConditionBuilder matches(String matches) {
            this.matches = matches;
            return this;
        }

        public ConditionBuilder build() {
            DevicePropertyCondition condition = new DevicePropertyCondition();
            condition.setPropertyName(propertyName);
            condition.setGreaterThan(greaterThan);
            condition.setLessThan(lessThan);
            condition.setEquals(equals);
            condition.setMatches(matches);

            conditionBuilder.devicePropertyConditions.add(condition);
            return conditionBuilder;
        }
    }

    private static class RedirectMappingsBuilder {
        private Boolean useNodeNameMatching;
        private RedirectMappings.UnknownNodeMapping unresolvedNode;
        private ArrayList<NodeMap> mappings;

        public RedirectMappingsBuilder nameMatching(boolean matching) {
            useNodeNameMatching = matching;
            return this;
        }

        public RedirectMappingsBuilder unresolved(RedirectMappings.UnknownNodeMapping unresolvedNode) {
            this.unresolvedNode = unresolvedNode;
            return this;
        }

        public RedirectMappingsBuilder map(String origin, String redirect) {
            if (mappings == null) {
                mappings = new ArrayList<NodeMap>();
            }
            mappings.add(new NodeMap(origin, redirect));
            return this;
        }

        public RedirectMappings build() {
            RedirectMappings mapping = new RedirectMappings();
            if (useNodeNameMatching != null) {
                mapping.setUseNodeNameMatching(useNodeNameMatching);
            }
            if (unresolvedNode != null) {
                mapping.setUnresolvedNode(unresolvedNode);
            }
            if (mappings != null) {
                mapping.setMappings(mappings);
            }

            return mapping;
        }
    }
}
