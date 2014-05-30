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

package org.exoplatform.portal.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.test.mocks.servlet.MockServletContext;
import org.exoplatform.web.application.javascript.DependencyDescriptor;
import org.exoplatform.web.application.javascript.Javascript;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.Module.Local.Content;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestParser extends AbstractGateInTest {

    private static List<ScriptResourceDescriptor> parseScripts(String config) throws IOException, SAXException, ParserConfigurationException {
        DocumentSource source = DocumentSource.create("gatein-resources.xml", new ByteArrayInputStream(config.getBytes("UTF-8")));
        Document document = GateInResourcesSchemaValidator.validate(source);
        JavascriptConfigParser parser = new JavascriptConfigParser(new MockServletContext("mypath"), document);
        return parser.parse().getScriptResourceDescriptors();
    }


    public void testShared() throws Exception {
        String config = "" + "<gatein-resources>" + "<module>" + "<name>foo</name>" + "<script>" + "<name>foo_module</name>"
                + "<path>/foo_module.js</path>" + "</script>" + "<depends>" + "<module>bar</module>" + "</depends>"
                + "<depends>" + "<module>juu</module>" + "</depends>" + "</module>" +

                "<scripts>" + "<name>foo_scripts</name>" + "<script>" + "<name>foo_module</name>"
                + "<path>/foo_module.js</path>" + "</script>" + "<depends>" + "<scripts>bar</scripts>" + "</depends>"
                + "<depends>" + "<scripts>juu</scripts>" + "</depends>" + "</scripts>" +

                "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);
        assertEquals(2, scripts.size());
        ScriptResourceDescriptor desc = scripts.get(0);
        assertEquals(new ResourceId(ResourceScope.SHARED, "foo"), desc.getId());
        assertNull(desc.getAlias());
        assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")),
                new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());

        desc = scripts.get(1);
        assertEquals(new ResourceId(ResourceScope.SHARED, "foo_scripts"), desc.getId());
        assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")),
                new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());
    }

    public void testPortlet() throws Exception {
        String config = "" + "<gatein-resources>" + "<portlet>" + "<name>foo</name>" + "<module>" + "<script>"
                + "<name>foo_module</name>" + "<path>/foo_module.js</path>" + "</script>" + "<depends>"
                + "<module>bar</module>" + "</depends>" + "<depends>" + "<module>juu</module>" + "</depends>" + "</module>"
                + "</portlet>" + "</gatein-resources>";
        List<ScriptResourceDescriptor> scripts = parseScripts(config);
        assertEquals(1, scripts.size());
        ScriptResourceDescriptor desc = scripts.get(0);
        assertEquals(new ResourceId(ResourceScope.PORTLET, "mypath/foo"), desc.getId());
        assertNull(desc.getAlias());
        assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")),
                new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());
    }

    public void testPortal() throws Exception {
        String config = "" + "<gatein-resources>" + "<portal>" + "<name>foo</name>" + "<module>" + "<script>"
                + "<name>foo_module</name>" + "<path>/foo_module.js</path>" + "</script>" + "<depends>"
                + "<module>bar</module>" + "</depends>" + "<depends>" + "<module>juu</module>" + "</depends>" + "</module>"
                + "</portal>" + "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);
        assertEquals(1, scripts.size());
        ScriptResourceDescriptor desc = scripts.get(0);
        assertEquals(new ResourceId(ResourceScope.PORTAL, "foo"), desc.getId());
        assertNull(desc.getAlias());
        assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")),
                new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());
    }

    public void testModules() throws Exception {
        String config = "" + "<gatein-resources>" + "<portal>" + "<name>foo</name>" + "<module>" + "<script>"
                + "<name>local_module</name>" + "<path>/local_module.js</path>" + "</script>" + "</module>" + "</portal>"
                + "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);
        assertEquals(1, scripts.size());
        ScriptResourceDescriptor desc = scripts.get(0);

        List<Javascript> modules = desc.getModules();
        assertEquals(1, modules.size());

        Javascript local = modules.get(0);
        assertTrue(local instanceof Javascript.Local);
        assertEquals("/local_module.js", ((Javascript.Local) local).getContents()[0].getSource());
    }

    public void testResourceBundle() throws Exception {
        String config = "" + "<gatein-resources>" + "<portal>" + "<name>foo</name>" + "<module>" + "<script>"
                + "<name>foo_module</name>" + "<path>/foo_module.js</path>" + "<resource-bundle>my_bundle</resource-bundle>"
                + "</script>" + "</module>" + "</portal>" + "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);
        assertEquals(1, scripts.size());
        ScriptResourceDescriptor desc = scripts.get(0);
        assertEquals(new ResourceId(ResourceScope.PORTAL, "foo"), desc.getId());
        assertEquals(1, desc.getModules().size());
        Javascript.Local js = (Javascript.Local) desc.getModules().get(0);
        assertEquals("my_bundle", js.getResourceBundle());
    }

    public void testSupportedLocales() throws Exception {
        String config = "" + "<gatein-resources>" + "<portal>" + "<name>foo</name>" + "<module>"
                + "<supported-locale>EN</supported-locale>" + "<supported-locale>FR-fr</supported-locale>" + "</module>"
                + "</portal>" + "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);
        assertEquals(1, scripts.size());
        ScriptResourceDescriptor desc = scripts.get(0);
        List<Locale> locales = desc.getSupportedLocales();
        assertEquals(Arrays.asList(Locale.ENGLISH, Locale.FRANCE), locales);
    }

    public void testRemoteResource() throws Exception {

        String config = ""
                + "<gatein-resources xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.gatein.org/xml/ns/gatein_resources_1_3 http://www.gatein.org/xml/ns/gatein_resources_1_3\" xmlns=\"http://www.gatein.org/xml/ns/gatein_resources_1_3\">"
                + "<module><name>foo</name><url>http://jquery.com/jquery.js</url></module>" + "</gatein-resources>";

        List<ScriptResourceDescriptor> descs = parseScripts(config);

        assertEquals(1, descs.size());
        ScriptResourceDescriptor desc = descs.get(0);
        List<Javascript> scripts = desc.getModules();
        assertEquals(1, scripts.size());
        assertTrue(scripts.get(0) instanceof Javascript.Remote);
    }

    public void testAlias() throws Exception {
        String config = "" + "<gatein-resources>" + "<module>" + "<name>foo</name>" + "<as>f</as>" + "<depends>"
                + "<module>bar</module>" + "<as>b</as>" + "</depends>" + "</module>" + "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);
        ScriptResourceDescriptor desc = scripts.get(0);
        assertEquals("f", desc.getAlias());
        assertEquals("b", desc.getDependencies().get(0).getAlias());

        String config1 = "" + "<gatein-resources>" + "<portal>" + "<name>zoo</name>" + "<as>z</as>" + "<module>" + "<depends>"
                + "<module>zozo</module>" + "<as>zz</as>" + "</depends>" + "</module>" + "</portal>" + "</gatein-resources>";

        List<ScriptResourceDescriptor> ptScripts = parseScripts(config1);
        ScriptResourceDescriptor portalDesc = ptScripts.get(0);
        assertEquals("z", portalDesc.getAlias());
        assertEquals("zz", portalDesc.getDependencies().get(0).getAlias());
    }

    public void testLoadGroup() throws Exception {
        String config = "" + "<gatein-resources>" + "<module>" + "<name>foo_module</name>"
                + "<load-group>foo_group</load-group>" + "<script>" + "<name>foo_module</name>" + "<path>/foo_module.js</path>"
                + "</script>" + "</module>" +

                "<portal>" + "<name>foo_portal</name>" + "<module>" + "<load-group>foo_group</load-group>" + "<script>"
                + "<name>foo_portal</name>" + "<path>/foo_portal.js</path>" + "</script>" + "</module>" + "</portal>" +

                "<portlet>" + "<name>foo_portlet</name>" + "<module>" + "<load-group>foo_group</load-group>" + "<script>"
                + "<name>foo_portlet</name>" + "<path>/foo_portlet.js</path>" + "</script>" + "</module>" + "</portlet>"
                + "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);

        assertEquals(3, scripts.size());
        for (ScriptResourceDescriptor des : scripts) {
            assertEquals("foo_group", des.getGroup());
        }
    }

    public void testLoadGroupRestriction() throws Exception {
        String config = "" + "<gatein-resources>" + "<scripts>" + "<name>foo_scripts</name>"
                + "<load-group>foo_group</load-group>" + "<script>" + "<name>foo_module</name>" + "<path>/foo_module.js</path>"
                + "</script>" + "</scripts>" +

                "<module>" + "<name>foo_module</name>" + "<load-group>foo_group</load-group>" + "<url>testURL</url>"
                + "</module>" + "</gatein-resources>";
        List<ScriptResourceDescriptor> scripts = parseScripts(config);

        assertEquals(2, scripts.size());
        for (ScriptResourceDescriptor des : scripts) {
            assertNull(des.getGroup());
        }
    }

    public void testAdapter() throws Exception {
        String config = "" + "<gatein-resources>" + "<module>" + "<name>foo_module</name>" + "<script><name>foo</name>"
                + "<adapter>aaa;<include>/foo_module.js</include>bbb;</adapter>" + "</script>" + "</module>"
                + "</gatein-resources>";

        List<ScriptResourceDescriptor> scripts = parseScripts(config);

        ScriptResourceDescriptor des = scripts.get(0);
        Javascript.Local module = (Javascript.Local) des.getModules().get(0);
        Content[] contents = module.getContents();

        assertNotNull(contents);
        assertEquals(3, contents.length);
        assertEquals("aaa;", contents[0].getSource());
        assertFalse(contents[0].isPath());
        assertEquals("/foo_module.js", contents[1].getSource());
        assertTrue(contents[1].isPath());
    }

}
