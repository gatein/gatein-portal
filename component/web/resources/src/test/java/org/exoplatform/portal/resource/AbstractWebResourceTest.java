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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.test.mocks.servlet.MockServletContext;
import org.exoplatform.test.mocks.servlet.MockServletRequest;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.URIWriter;
import org.gatein.common.io.IOTools;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 7/5/11
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/resource-compressor-service-configuration.xml") })
public abstract class AbstractWebResourceTest extends AbstractKernelTest {

    protected static class MockControllerContext extends ControllerContext {
        public MockControllerContext() {
            super(null, null, new MockServletRequest(null, null), null, null);
        }

        @Override
        public void renderURL(Map<QualifiedName, String> parameters, URIWriter uriWriter) throws IOException {
            uriWriter.append('/');
            uriWriter.appendSegment("mock_context");
            uriWriter.append('/');
            uriWriter.appendSegment("mock_url_of_" + parameters.get(QualifiedName.create("gtn", "resource")) + ".js");
        }
    }

    public static class MockJSServletContext extends MockServletContext {
        protected Map<String, String> resources;

        public MockJSServletContext(String contextName, Map<String, String> resources) {
            super(contextName);
            this.resources = expandDirectories(resources);
        }

        /**
         * Creates a {@link Map} that contains directory entries present implicitly in the given map.
         *
         * E.g. for a map containing single entry {@code ["/path/to/amds", "aaa"]} it would return
         * a map containing the following entries:
         * {@code ["/path", null]}, {@code ["/path/to", null]} and {@code ["/path/to/amds", "aaa"]}.
         *
         * @param resources
         * @return
         */
        private Map<String, String> expandDirectories(Map<String, String> resources) {
            Map<String, String> result = new TreeMap<String, String>(resources);
            for (String path : resources.keySet()) {
                if (path.charAt(0) != '/') {
                    throw new IllegalArgumentException("Resource path '"+ path +"' does not start with slash.");
                }
                for (int slashPos = path.indexOf('/', 1); slashPos >= 0; slashPos = path.indexOf('/', slashPos + 1)) {
                    String parentPath = path.substring(0, slashPos + 1);
                    if (!result.containsKey(parentPath)) {
                        result.put(parentPath, null);
                    }
                }
            }
            return result;
        }

        public String getContextPath() {
            return "/" + getServletContextName();
        }

        @Override
        public InputStream getResourceAsStream(String s) {
            String input = resources.get(s);
            if (input != null) {
                return new ByteArrayInputStream(input.getBytes());
            } else {
                return null;
            }
        }

        /**
         * @see org.exoplatform.test.mocks.servlet.MockServletContext#getResourcePaths(java.lang.String)
         */
        @Override
        public Set<?> getResourcePaths(String prefix) {
            if (!prefix.endsWith("/")) {
                throw new IllegalArgumentException("Only prefixes ending with '/' are supported.");
            }
            Set<String> result = new TreeSet<String>();

            for (String resourcePath : resources.keySet()) {
                if (resourcePath.startsWith(prefix)) {

                    int slashPos = resourcePath.indexOf('/', prefix.length());
                    int restLength = resourcePath.length() - prefix.length();
                    if (restLength > 0 && (slashPos < 0 || slashPos == resourcePath.length() - 1)) {
                        /* a file residing directly under prefix directory
                         * or a subdirectory of the prefix directory */
                        result.add(resourcePath);
                    }
                }
            }
            return result.size() == 0 ? null : result;
        }

    }

    protected static void assertReader(String expected, Reader actual) throws Exception {
        StringWriter buffer = new StringWriter();
        IOTools.copy(actual, buffer, 1);
        assertEquals(expected, buffer.toString());
    }

}
