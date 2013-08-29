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

package org.exoplatform.web.filter;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.test.mocks.servlet.MockServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by The eXo Platform SAS Author : Nicolas Filotto nicolas.filotto@exoplatform.com 25 sept. 2009
 */
public class TestExtensibleFilter extends AbstractGateInTest {

    public void testDoFilter() throws IOException, ServletException {
        String pathRequest = "/testPath";
        ExtensibleFilter exFilter = new ExtensibleFilter();
        MockFilterOKTF mockFilterOKTF = new MockFilterOKTF();
        MockFilterOKWTF mockFilterOKWTF = new MockFilterOKWTF();
        MockFilterChain chain = new MockFilterChain();
        exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF), getFilterDefinition(mockFilterOKWTF)));
        exFilter.doFilter(new MockServletRequest(null, null), null, chain, pathRequest);
        assertTrue(mockFilterOKTF.start);
        assertTrue(mockFilterOKTF.end);
        assertTrue(mockFilterOKWTF.start);
        assertTrue(mockFilterOKWTF.end);
        assertTrue(chain.called);
        exFilter = new ExtensibleFilter();
        mockFilterOKTF = new MockFilterOKTF();
        mockFilterOKWTF = new MockFilterOKWTF();
        chain = new MockFilterChain();
        exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF), getFilterDefinition(mockFilterOKWTF),
                getFilterDefinition(new MockFilterKO())));
        exFilter.doFilter(new MockServletRequest(null, null), null, chain, pathRequest);
        assertTrue(mockFilterOKTF.start);
        assertTrue(mockFilterOKTF.end);
        assertTrue(mockFilterOKWTF.start);
        assertTrue(mockFilterOKWTF.end);
        assertFalse(chain.called);
        exFilter = new ExtensibleFilter();
        mockFilterOKTF = new MockFilterOKTF();
        mockFilterOKWTF = new MockFilterOKWTF();
        chain = new MockFilterChain();
        exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF), getFilterDefinition(mockFilterOKWTF),
                getFilterDefinition(new MockFilterKOIO())));
        try {
            exFilter.doFilter(new MockServletRequest(null, null), null, chain, pathRequest);
            fail("IOException is expected");
        } catch (IOException e) {
        }
        assertTrue(mockFilterOKTF.start);
        assertTrue(mockFilterOKTF.end);
        assertTrue(mockFilterOKWTF.start);
        assertFalse(mockFilterOKWTF.end);
        assertFalse(chain.called);
        exFilter = new ExtensibleFilter();
        mockFilterOKTF = new MockFilterOKTF();
        mockFilterOKWTF = new MockFilterOKWTF();
        chain = new MockFilterChain();
        exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF), getFilterDefinition(mockFilterOKWTF),
                getFilterDefinition(new MockFilterKOSE())));
        try {
            exFilter.doFilter(new MockServletRequest(null, null), null, chain, pathRequest);
            fail("ServletException is expected");
        } catch (ServletException e) {
        }
        assertTrue(mockFilterOKTF.start);
        assertTrue(mockFilterOKTF.end);
        assertTrue(mockFilterOKWTF.start);
        assertFalse(mockFilterOKWTF.end);
        assertFalse(chain.called);
        exFilter = new ExtensibleFilter();
        mockFilterOKTF = new MockFilterOKTF();
        mockFilterOKWTF = new MockFilterOKWTF();
        chain = new MockFilterChain();
        exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF), getFilterDefinition(mockFilterOKWTF),
                getFilterDefinition(new MockFilterKORE())));
        try {
            exFilter.doFilter(new MockServletRequest(null, null), null, chain, pathRequest);
            fail("RuntimeException is expected");
        } catch (RuntimeException e) {
        }
        assertTrue(mockFilterOKTF.start);
        assertTrue(mockFilterOKTF.end);
        assertTrue(mockFilterOKWTF.start);
        assertFalse(mockFilterOKWTF.end);
        assertFalse(chain.called);
        exFilter = new ExtensibleFilter();
        mockFilterOKTF = new MockFilterOKTF();
        mockFilterOKWTF = new MockFilterOKWTF();
        chain = new MockFilterChain();
        exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF), getFilterDefinition(mockFilterOKWTF),
                getFilterDefinition(new MockFilterKOER())));
        try {
            exFilter.doFilter(new MockServletRequest(null, null), null, chain, pathRequest);
            fail("Error is expected");
        } catch (Error e) {
        }
        assertTrue(mockFilterOKTF.start);
        assertTrue(mockFilterOKTF.end);
        assertTrue(mockFilterOKWTF.start);
        assertFalse(mockFilterOKWTF.end);
        assertFalse(chain.called);
        exFilter = new ExtensibleFilter();
        mockFilterOKTF = new MockFilterOKTF();
        mockFilterOKWTF = new MockFilterOKWTF();
        MockFilterOKTF mockFilterOKTF2 = new MockFilterOKTF();
        chain = new MockFilterChain();
        exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF), getFilterDefinition(mockFilterOKWTF),
                getFilterDefinition(new MockFilterKOIO()), getFilterDefinition(mockFilterOKTF2)));
        try {
            exFilter.doFilter(new MockServletRequest(null, null), null, chain, pathRequest);
            fail("IOException is expected");
        } catch (IOException e) {
        }
        assertTrue(mockFilterOKTF.start);
        assertTrue(mockFilterOKTF.end);
        assertTrue(mockFilterOKWTF.start);
        assertFalse(mockFilterOKWTF.end);
        assertFalse(chain.called);
        assertFalse(mockFilterOKTF2.start);
        assertFalse(mockFilterOKTF2.end);
    }

    private FilterDefinition getFilterDefinition(Filter filter) {
        return new FilterDefinition(filter, Collections.singletonList(".*"));
    }

    private static class MockFilterChain implements FilterChain {
        private boolean called;

        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            called = true;
        }
    }

    private static class MockFilterOKTF implements Filter {

        private boolean start;

        private boolean end;

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            try {
                start = true;
                chain.doFilter(request, response);
            } finally {
                end = true;
            }
        }
    }

    private static class MockFilterOKWTF implements Filter {

        private boolean start;

        private boolean end;

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            start = true;
            chain.doFilter(request, response);
            end = true;
        }
    }

    private static class MockFilterKO implements Filter {
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
        }
    }

    private static class MockFilterKOIO implements Filter {

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            throw new IOException();
        }
    }

    private static class MockFilterKOSE implements Filter {

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            throw new ServletException();
        }
    }

    private static class MockFilterKORE implements Filter {

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            throw new RuntimeException();
        }
    }

    private static class MockFilterKOER implements Filter {

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
            throw new Error();
        }
    }
}
