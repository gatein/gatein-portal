/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.common.classloader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Unit tests for DelegatingClassLoader
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class DelegatingClassLoaderTest {

    private URLClassLoader clOne;
    private URLClassLoader clTwo;
    private URLClassLoader clThree;
    private URLClassLoader clClasses;

    @Before
    public void setup() throws URISyntaxException, MalformedURLException {
        URL root = getClass().getClassLoader().getResource("");
        ClassLoader parent = Thread.currentThread().getContextClassLoader();

        String [] filteredNames = {"test.properties", "nonexisting", "test.conf",
            "sub/test.properties", DelegatingClassLoaderTest.class.getName()};

        clOne = new FilteringClassLoader(parent, new URL[] {getClass().getResource("one/")}, filteredNames);
        clTwo = new FilteringClassLoader(parent, new URL[] {getClass().getResource("two/")}, filteredNames);
        clThree = new FilteringClassLoader(parent, new URL[] {getClass().getResource("three/")}, filteredNames);
        clClasses = new FilteringClassLoader(parent, new URL[] {root}, filteredNames);
    }

    private String loadProperty(URL url, String key) throws IOException {
        Properties p = loadProperties(url);
        return p.getProperty(key, null);
    }

    private Properties loadProperties(URL url) throws IOException {
        Properties p = new Properties();
        p.load(url.openStream());
        return p;
    }

    private List<URL> loadResources(ClassLoader cl, String name, int foundCount) throws IOException {
        Enumeration<URL> propsEnum = cl.getResources(name);
        Assert.assertNotNull("Expected to get enumeration as a result", propsEnum);

        List<URL> propsUrls = Collections.list(propsEnum);
        Assert.assertEquals("Expected to get " + foundCount + " properties files returned", foundCount, propsUrls.size());
        return propsUrls;
    }

    @Test
    public void testGetResource() throws IOException {
        ClassLoader cl = new DelegatingClassLoader(clOne, clTwo, clThree);
        URL propsUrl = cl.getResource("test.properties");
        String val = loadProperty(propsUrl, "key");
        Assert.assertEquals("one", val);

        propsUrl = cl.getResource("nonexisting");
        Assert.assertNull("Looking for non-existing resource must return null", propsUrl);

        propsUrl = cl.getResource("test.conf");
        val = loadProperty(propsUrl, "key");
        Assert.assertEquals("test.conf", val);

        propsUrl = cl.getResource("sub/test.properties");
        val = loadProperty(propsUrl, "key");
        Assert.assertEquals("sub one", val);

        // reorder
        cl = new DelegatingClassLoader(clTwo, clThree, clOne);
        propsUrl = cl.getResource("test.properties");
        val = loadProperty(propsUrl, "key");
        Assert.assertEquals("two", val);

        propsUrl = cl.getResource("sub/test.properties");
        val = loadProperty(propsUrl, "key");
        Assert.assertEquals("sub one", val);
    }

    @Test
    public void testGetResources() throws IOException {
        ClassLoader cl = new DelegatingClassLoader(clOne, clTwo, clThree);

        List<URL> propsUrls = loadResources(cl, "test.properties", 3);

        URL url = propsUrls.get(0);
        String val = loadProperty(url, "key");
        Assert.assertEquals("one", val);

        url = propsUrls.get(1);
        val = loadProperty(url, "key");
        Assert.assertEquals("two", val);

        url = propsUrls.get(2);
        val = loadProperty(url, "key");
        Assert.assertEquals("three", val);

        propsUrls = loadResources(cl, "nonexisting", 0);

        propsUrls = loadResources(cl, "test.conf", 1);
        url = propsUrls.get(0);
        val = loadProperty(url, "key");
        Assert.assertEquals("test.conf", val);

        propsUrls = loadResources(cl, "sub/test.properties", 1);
        url = propsUrls.get(0);
        val = loadProperty(url, "key");
        Assert.assertEquals("sub one", val);

        // reorder
        cl = new DelegatingClassLoader(clTwo, clThree, clOne);
        propsUrls = loadResources(cl, "test.properties", 3);

        url = propsUrls.get(0);
        val = loadProperty(url, "key");
        Assert.assertEquals("two", val);

        url = propsUrls.get(1);
        val = loadProperty(url, "key");
        Assert.assertEquals("three", val);

        url = propsUrls.get(2);
        val = loadProperty(url, "key");
        Assert.assertEquals("one", val);

        propsUrls = loadResources(cl, "sub/test.properties", 1);
        url = propsUrls.get(0);
        val = loadProperty(url, "key");
        Assert.assertEquals("sub one", val);
    }

    @Test
    public void testLoadClass() {
        ClassLoader cl = new DelegatingClassLoader(clOne, clTwo, clThree, clClasses);
        Class c = null;
        try {
            c = cl.loadClass(DelegatingClassLoaderTest.class.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load class via DelegatingClassLoader: " + DelegatingClassLoaderTest.class.getName(), e);
        }
        Assert.assertSame(clClasses, c.getClassLoader());

        cl = new DelegatingClassLoader(clOne, clTwo, clThree);
        try {
            cl.loadClass(DelegatingClassLoaderTest.class.getName());
            Assert.fail("Loading the class should fail: " + DelegatingClassLoaderTest.class.getName());
        } catch (ClassNotFoundException e) {
            // expected
        } catch (Exception e) {
            throw new RuntimeException("Failed to load class unexpectedly: " + DelegatingClassLoaderTest.class.getName(), e);
        }

        cl = new DelegatingClassLoader(clOne, clTwo, clThree, getClass().getClassLoader(), clClasses);
        try {
            c = cl.loadClass(DelegatingClassLoaderTest.class.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load class: " + DelegatingClassLoaderTest.class.getName(), e);
        }
        Assert.assertSame(DelegatingClassLoaderTest.class, c);
        Assert.assertNotSame(clClasses, c.getClassLoader());
    }
}
