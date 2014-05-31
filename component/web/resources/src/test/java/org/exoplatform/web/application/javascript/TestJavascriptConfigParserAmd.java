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

package org.exoplatform.web.application.javascript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.portal.resource.GateInResourcesSchemaValidator;
import org.exoplatform.portal.resource.TestJavascriptConfigService;
import org.exoplatform.portal.resource.TestJavascriptConfigService.MockJSServletContext;
import org.exoplatform.web.application.javascript.Javascript;
import org.exoplatform.web.application.javascript.Javascript.Local;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.exoplatform.web.application.javascript.ScriptResources;
import org.gatein.portal.controller.resource.script.Module.Local.Content;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 */
public class TestJavascriptConfigParserAmd extends junit.framework.TestCase {

    private static final String GATEIN_RESOURCES_ELEMENT = "<gatein-resources xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://www.gatein.org/xml/ns/gatein_resources_1_5 http://www.gatein.org/xml/ns/gatein_resources_1_5\""
            + " xmlns=\"http://www.gatein.org/xml/ns/gatein_resources_1_5\">";

    /**
     * Ensure that {@link MockAMDServletContext} does what it is supposed to do.
     */
    public void testMockAMDServletContext() {
        ServletContext c = new MockAMDServletContext();
        assertEquals(
                new TreeSet<String>(Arrays.asList("/js/",  "/root.js")),
                c.getResourcePaths("/")
        );
        assertEquals(
                new TreeSet<String>(Arrays.asList("/js/amd/mod1.js",  "/js/amd/mod2.js", "/js/amd/package1/", "/js/amd/package2/")),
                c.getResourcePaths("/js/amd/")
        );
        assertEquals(
                new TreeSet<String>(new HashSet<String>(Arrays.asList("/js/amd/package1/mod3.js",  "/js/amd/package1/mod4.js"))),
                c.getResourcePaths("/js/amd/package1/")
        );
        assertNull(
                c.getResourcePaths("/js/not/there/")
        );
    }


    /**
     * No explicit includes and excludes.
     *
     * @throws Exception
     */
    public void testAmdAll() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd</directory>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/mod1.js",
                "/js/amd/mod2.js",
                "/js/amd/package1/mod3.js",
                "/js/amd/package1/mod4.js",
                "/js/amd/package2/mod5.js",
                "/js/amd/package2/subpack/mod6.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdSingleTopDir() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd</directory>"
                + "<includes><include>*.js</include></includes>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/mod1.js",
                "/js/amd/mod2.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdSpecificFileName() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd</directory>"
                + "<includes>"
                + "<include>mod2.js</include>"
                + "</includes>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/mod2.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdSingleSecondLevelDir() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd/package1</directory>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/package1/mod3.js",
                "/js/amd/package1/mod4.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdSingleSecondLevelDirWithTerminalSlash() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd/package1/</directory>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/package1/mod3.js",
                "/js/amd/package1/mod4.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdSingleSecondLevelDirWithSlash() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd/package1/</directory>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/package1/mod3.js",
                "/js/amd/package1/mod4.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }


    public void testAmdSubtree() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd</directory>"
                + "<includes>"
                + "<include>package2/**/*.js</include>"
                + "</includes>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/package2/mod5.js",
                "/js/amd/package2/subpack/mod6.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdSubtrees() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd</directory>"
                + "<includes>"
                + "<include>package?/**/*.js</include>"
                + "</includes>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/package1/mod3.js",
                "/js/amd/package1/mod4.js",
                "/js/amd/package2/mod5.js",
                "/js/amd/package2/subpack/mod6.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdSubtreeWithExclude() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd</directory>"
                + "<includes>"
                + "<include>package2/**/*.js</include>"
                + "</includes>"
                + "<excludes>"
                + "<exclude>**/subpack/*.js</exclude>"
                + "</excludes>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/package2/mod5.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    public void testAmdRepeatedIncludes() throws Exception {
        String config = GATEIN_RESOURCES_ELEMENT +"<amd>"
                + "<fileset>"
                + "<directory>/js/amd</directory>"
                + "<includes>"
                + "<include>**/mod5.js</include>"
                + "<include>**/mod5.js</include>"
                + "<include>**/mod5.js</include>"
                + "</includes>"
                + "</fileset>"
                + "</amd></gatein-resources>";

        String[] expectedPaths = new String[] {
                "/js/amd/package2/mod5.js",
        };
        assertConfigMatchesResult(config, expectedPaths);
    }

    /**
     * @param config
     * @param expectedPaths
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws UnsupportedEncodingException
     */
    private void assertConfigMatchesResult(String config, String[] expectedPaths) throws SAXException, IOException,
            ParserConfigurationException, UnsupportedEncodingException {
        DocumentSource source = DocumentSource.create("gatein-resources.xml", config.getBytes("UTF-8"));
        Document document = GateInResourcesSchemaValidator.validate(source);

        JavascriptConfigParser parser = new JavascriptConfigParser(new MockAMDServletContext(), document);
        ScriptResources resources = parser.parse();
        List<ScriptResourceDescriptor> scripts = resources.getScriptResourceDescriptors();
        assertEqualPaths(new TreeSet<String>(Arrays.asList(expectedPaths)), scripts);
    }

    public static void assertEqualPaths(TreeSet<String> amdFiles, Collection<ScriptResourceDescriptor> scripts) {
        TreeSet<String> foundPaths = new TreeSet<String>();
        for (ScriptResourceDescriptor d : scripts) {
            foundPaths.add(getPath(d));
        }
        assertEquals(amdFiles, foundPaths);
    }

    public static String getPath(ScriptResourceDescriptor d) {
        List<Javascript> mods = d.getModules();
        assertEquals(1, mods.size());
        Local js = (Local) mods.get(0);
        Content[] conts = js.getContents();
        assertEquals(1, conts.length);
        return conts[0].getSource();
    }

    private static class MockAMDServletContext extends TestJavascriptConfigService.MockJSServletContext {

        public static final Map<String, String> RESOURCES;
        static {
            Map<String, String> amdResources = new HashMap<String, String>();
            amdResources.put("/js/script1.js", "aaa;");
            amdResources.put("/js/script2.js", "bbb;");
            amdResources.put("/js/module1.js", "ccc;");
            amdResources.put("/js/module2.js", "ddd;");
            amdResources.put("/js/common.js", "kkk;");
            amdResources.put("/js/pluginTest.js", "iii;");
            amdResources.put("/js/amd/mod1.js", "m1;");
            amdResources.put("/js/amd/mod2.js", "m2;");
            amdResources.put("/js/amd/package1/mod3.js", "m3;");
            amdResources.put("/js/amd/package1/mod4.js", "m4;");
            amdResources.put("/js/amd/package2/mod5.js", "m5;");
            amdResources.put("/js/amd/package2/subpack/mod6.js", "m6;");
            amdResources.put("/js/amd.js", "amd;");
            amdResources.put("/root.js", "root;");
            RESOURCES = Collections.unmodifiableMap(amdResources);
        }

        public MockAMDServletContext() {
            super("/amd", RESOURCES);
        }

    }


}
