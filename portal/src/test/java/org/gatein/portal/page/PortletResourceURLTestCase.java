/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.page;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.portlet.WindowState;

import junit.framework.Assert;
import org.gatein.portal.AbstractPortalTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@RunWith(Arquillian.class)
public class PortletResourceURLTestCase extends AbstractPortalTestCase {

    @Deployment(testable = false)
    public static WebArchive createPortal() {
        WebArchive portal = AbstractPortalTestCase.createPortal();
        portal.addAsWebInfResource(new StringAsset(descriptor(Portlet1.class).exportAsString()), "portlet.xml");
        return portal;
    }

    @ArquillianResource
    URL deploymentURL;

    @Drone
    WebDriver driver;

    /** . */
    static String wantedCacheability;

    /** . */
    static String[] foo;

    /** . */
    static String[] bar;

    /** . */
    static String[] juu;

    /** . */
    static String[] daa;

    /** . */
    static WindowState windowState;

    /** . */
    static PortletMode portletMode;

    /** . */
    static String cacheability;

    @Test
    public void testPageLevel() {
        testPageLevel(ResourceURL.PAGE);
    }

    private void testPageLevel(String expectedCacheability) {
        wantedCacheability = expectedCacheability;
        String url = deploymentURL.toString() + "page1";
        driver.get(url);
        WebElement render = driver.findElement(By.id("render"));
        foo = bar = juu = daa = null;
        windowState = null;
        portletMode = null;
        cacheability = null;
        render.click();
        Assert.assertNull(cacheability);
        Assert.assertEquals(PortletMode.EDIT, portletMode);
        Assert.assertEquals(WindowState.MAXIMIZED, windowState);
        Assert.assertNotNull(foo);
        Assert.assertEquals(Arrays.asList("foo_value"), Arrays.asList(foo));
        Assert.assertNotNull(bar);
        Assert.assertEquals(Arrays.asList("bar_value1", "bar_value2"), Arrays.asList(bar));
        Assert.assertNull(juu);
        Assert.assertNull(daa);
        WebElement resource = driver.findElement(By.id("resource"));
        foo = bar = juu = daa = null;
        windowState = null;
        portletMode = null;
        cacheability = null;
        resource.click();
        Assert.assertEquals(expectedCacheability, cacheability);
        Assert.assertEquals(PortletMode.EDIT, portletMode);
        Assert.assertEquals(WindowState.MAXIMIZED, windowState);
        Assert.assertNotNull(foo);
        Assert.assertEquals(Arrays.asList("foo_value"), Arrays.asList(foo));
        Assert.assertNotNull(bar);
        Assert.assertEquals(Arrays.asList("bar_value1", "bar_value2"), Arrays.asList(bar));
        Assert.assertNotNull(juu);
        Assert.assertEquals(Arrays.asList("juu_value"), Arrays.asList(juu));
        Assert.assertNotNull(daa);
        Assert.assertEquals(Arrays.asList("daa_value1", "daa_value2"), Arrays.asList(daa));
    }

    public static class Portlet1 extends GenericPortlet {

        @Override
        public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
            cacheability = request.getCacheability();
            foo = request.getParameterMap().get("foo");
            bar = request.getParameterMap().get("bar");
            juu = request.getParameterMap().get("juu");
            daa = request.getParameterMap().get("daa");
            windowState = request.getWindowState();
            portletMode = request.getPortletMode();
            response.setContentType("text/plain");
            response.getWriter().append("the resource").close();
        }

        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            foo = request.getParameterMap().get("foo");
            bar = request.getParameterMap().get("bar");
            juu = request.getParameterMap().get("juu");
            daa = request.getParameterMap().get("daa");
            windowState = request.getWindowState();
            portletMode = request.getPortletMode();
            ResourceURL resourceURL = response.createResourceURL();
            resourceURL.setCacheability(wantedCacheability);
            resourceURL.setParameter("juu", "juu_value");
            resourceURL.setParameter("daa", new String[]{"daa_value1","daa_value2"});
            PortletURL renderURL = response.createRenderURL();
            renderURL.setPortletMode(PortletMode.EDIT);
            renderURL.setWindowState(WindowState.MAXIMIZED);
            renderURL.setParameter("foo", "foo_value");
            renderURL.setParameter("bar", new String[]{"bar_value1","bar_value2"});
            response.getWriter().
                    append("<a id='resource' href='").append(resourceURL.toString()).append("'>resource</a>").
                    append("<a id='render' href='").append(renderURL.toString()).append("'>render</a>").
                    close();
        }
    }
}
