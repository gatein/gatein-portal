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
package org.gatein.portal.web.page;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

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
import org.gatein.portal.web.AbstractPortalTestCase;
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
    static GenericPortlet delegate;

    public static class Portlet1 extends GenericPortlet {

        @Override
        public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
            delegate.serveResource(request, response);
        }

        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            delegate.render(request, response);
        }
    }

    class RenderParametersPortlet extends GenericPortlet {

        /** . */
        String wantedCacheability;

        /** . */
        String[] wantedRenderParam1;

        /** . */
        String[] wantedRenderParam2;

        /** . */
        String id;

        /** . */
        String[] renderParam1;

        /** . */
        String[] renderParam2;

        /** . */
        String[] resourceParam1;

        /** . */
        String[] resourceParam2;

        /** . */
        WindowState windowState;

        /** . */
        PortletMode portletMode;

        /** . */
        String cacheability;

        @Override
        public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
            cacheability = request.getCacheability();
            id = request.getResourceID();
            renderParam1 = request.getParameterMap().get("render_param1");
            renderParam2 = request.getParameterMap().get("render_param2");
            resourceParam1 = request.getParameterMap().get("resource_param1");
            resourceParam2 = request.getParameterMap().get("resource_param2");
            windowState = request.getWindowState();
            portletMode = request.getPortletMode();
            response.setContentType("text/plain");
            response.getWriter().append("the resource").close();
        }

        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            renderParam1 = request.getParameterMap().get("render_param1");
            renderParam2 = request.getParameterMap().get("render_param2");
            resourceParam1 = request.getParameterMap().get("resource_param1");
            resourceParam2 = request.getParameterMap().get("resource_param2");
            windowState = request.getWindowState();
            portletMode = request.getPortletMode();
            ResourceURL resourceURL = response.createResourceURL();
            resourceURL.setResourceID("the_id");
            resourceURL.setCacheability(wantedCacheability);
            resourceURL.setParameter("resource_param1", "juu_value");
            resourceURL.setParameter("resource_param2", new String[]{"daa_value1","daa_value2"});
            PortletURL renderURL = response.createRenderURL();
            renderURL.setPortletMode(PortletMode.EDIT);
            renderURL.setWindowState(WindowState.MAXIMIZED);
            renderURL.setParameter("render_param1", wantedRenderParam1);
            renderURL.setParameter("render_param2", wantedRenderParam2);
            response.getWriter().
                    append("<a id='resource' href='").append(resourceURL.toString()).append("'>resource</a>").
                    append("<a id='render' href='").append(renderURL.toString()).append("'>render</a>").
                    close();
        }
    }

    @Test
    public void testRenderParameters() {
        RenderParametersPortlet portlet = new RenderParametersPortlet();
        delegate = portlet;
        testRenderParameters(
                portlet,
                ResourceURL.PAGE,
                Arrays.asList("foo_value"),
                Arrays.asList("bar_value1", "bar_value2"),
                Arrays.asList("foo_value"),
                Arrays.asList("bar_value1", "bar_value2"),
                PortletMode.EDIT,
                WindowState.MAXIMIZED);
        testRenderParameters(
                portlet,
                ResourceURL.PORTLET,
                Arrays.asList("foo_value"),
                Arrays.asList("bar_value1", "bar_value2"),
                Arrays.asList("foo_value"),
                Arrays.asList("bar_value1", "bar_value2"),
                PortletMode.EDIT,
                WindowState.MAXIMIZED);
        testRenderParameters(
                portlet,
                ResourceURL.FULL,
                null,
                null,
                Arrays.asList("foo_value"),
                Arrays.asList("bar_value1", "bar_value2"),
                PortletMode.VIEW,
                WindowState.NORMAL);
    }

    private void testRenderParameters(
            RenderParametersPortlet portlet,
            String expectedCacheability,
            List<String> expectedRenderParam1,
            List<String> expectedRenderParam2,
            List<String> renderParam1,
            List<String> renderParam2,
            PortletMode expectedPortletMode,
            WindowState expectedWindowState) {
        portlet.wantedCacheability = expectedCacheability;
        portlet.wantedRenderParam1 = renderParam1.toArray(new String[renderParam1.size()]);
        portlet.wantedRenderParam2 = renderParam2.toArray(new String[renderParam2.size()]);
        String url = deploymentURL.toString() + "page1";
        driver.get(url);
        WebElement render = driver.findElement(By.id("render"));
        portlet.id = null;
        portlet.renderParam1 = portlet.renderParam2 = portlet.resourceParam1 = portlet.resourceParam2 = null;
        portlet.windowState = null;
        portlet.portletMode = null;
        portlet.cacheability = null;
        render.click();
        Assert.assertNull(portlet.id);
        Assert.assertNull(portlet.cacheability);
        Assert.assertEquals(PortletMode.EDIT, portlet.portletMode);
        Assert.assertEquals(WindowState.MAXIMIZED, portlet.windowState);
        Assert.assertNotNull(portlet.renderParam1);
        Assert.assertEquals(Arrays.asList("foo_value"), Arrays.asList(portlet.renderParam1));
        Assert.assertNotNull(portlet.renderParam2);
        Assert.assertEquals(Arrays.asList("bar_value1", "bar_value2"), Arrays.asList(portlet.renderParam2));
        Assert.assertNull(portlet.resourceParam1);
        Assert.assertNull(portlet.resourceParam2);
        WebElement resource = driver.findElement(By.id("resource"));
        portlet.id = null;
        portlet.renderParam1 = portlet.renderParam2 = portlet.resourceParam1 = portlet.resourceParam2 = null;
        portlet.windowState = null;
        portlet.portletMode = null;
        portlet.cacheability = null;
        resource.click();
        Assert.assertEquals("the_id", portlet.id);
        Assert.assertEquals(expectedCacheability, portlet.cacheability);
        Assert.assertEquals(expectedPortletMode, portlet.portletMode);
        Assert.assertEquals(expectedWindowState, portlet.windowState);
        if (expectedRenderParam1 == null) {
            Assert.assertNull(portlet.renderParam1);
        } else {
            Assert.assertNotNull(portlet.renderParam1);
            Assert.assertEquals(expectedRenderParam1, Arrays.asList(portlet.renderParam1));
        }
        if (expectedRenderParam2 == null) {
            Assert.assertNull(portlet.renderParam2);
        } else {
            Assert.assertNotNull(portlet.renderParam2);
            Assert.assertEquals(expectedRenderParam2, Arrays.asList(portlet.renderParam2));
        }
        Assert.assertNotNull(portlet.resourceParam1);
        Assert.assertEquals(Arrays.asList("juu_value"), Arrays.asList(portlet.resourceParam1));
        Assert.assertNotNull(portlet.resourceParam2);
        Assert.assertEquals(Arrays.asList("daa_value1", "daa_value2"), Arrays.asList(portlet.resourceParam2));
    }

    class CacheLevelPortlet extends GenericPortlet {

        /** . */
        String wantedCacheability;

        /** . */
        String cacheability;

        @Override
        public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
            cacheability = request.getCacheability();
            response.setContentType("text/plain");
            response.getWriter().append("the resource").close();
        }

        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            ResourceURL resourceURL = response.createResourceURL();
            resourceURL.setResourceID("the_id");
            resourceURL.setCacheability(wantedCacheability);
            response.getWriter().
                    append("<a id='resource' href='").append(resourceURL.toString()).append("'>resource</a>").
                    close();
        }
    }

    @Test
    public void testCacheLevel() {

        //
        CacheLevelPortlet portlet = new CacheLevelPortlet();
        delegate = portlet;
        testCacheLevel(portlet, ResourceURL.PAGE);
        testCacheLevel(portlet, ResourceURL.PORTLET);
        testCacheLevel(portlet, ResourceURL.FULL);
    }

    private void testCacheLevel(CacheLevelPortlet portlet, String expectedCacheability) {
        portlet.wantedCacheability = expectedCacheability;
        String url = deploymentURL.toString() + "page1";
        driver.get(url);
        WebElement resource = driver.findElement(By.id("resource"));
        portlet.cacheability = null;
        resource.click();
        Assert.assertEquals(expectedCacheability, portlet.cacheability);
    }
}
