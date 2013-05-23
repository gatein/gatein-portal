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
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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
public class PortletActionURLTestCase extends AbstractPortalTestCase {

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

    @Test
    public void testHello() {

        class MyPortlet extends GenericPortlet {

            WindowState renderWindowState, actionWindowState;
            PortletMode renderPortletMode, actionPortletMode;
            String[] renderFoo, renderBar, actionFoo, actionBar;

            @Override
            public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
                actionFoo = request.getParameterMap().get("foo");
                actionBar = request.getParameterMap().get("bar");
                actionWindowState = request.getWindowState();
                actionPortletMode = request.getPortletMode();
                response.setRenderParameter("foo", "foo_value2");
                response.setRenderParameter("bar", new String[]{"bar_value3","bar_value4"});
                response.setPortletMode(PortletMode.HELP);
                response.setWindowState(WindowState.MINIMIZED);
            }

            @Override
            public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
                response.setContentType("text/html");
                PortletURL url = response.createActionURL();
                renderFoo = request.getParameterMap().get("foo");
                renderBar = request.getParameterMap().get("bar");
                renderWindowState = request.getWindowState();
                renderPortletMode = request.getPortletMode();
                url.setWindowState(WindowState.MAXIMIZED);
                url.setPortletMode(PortletMode.EDIT);
                url.setParameter("foo", "foo_value1");
                url.setParameter("bar", new String[]{"bar_value1","bar_value2"});
                response.getWriter().append("<a id='click' href='").append(url.toString()).append("'>click</a>").close();
            }
        }
        MyPortlet m = new MyPortlet();
        delegate = m;

        String url = deploymentURL.toString() + "page1";
        driver.get(url);
        WebElement click = driver.findElement(By.id("click"));
        m.renderFoo = m.renderBar = m.actionFoo = m.actionBar = null;
        m.renderWindowState = m.actionWindowState = null;
        m.renderPortletMode = m.actionPortletMode = null;
        click.click();
        Assert.assertEquals(PortletMode.EDIT, m.actionPortletMode);
        Assert.assertEquals(WindowState.MAXIMIZED, m.actionWindowState);
        Assert.assertNotNull(m.actionFoo);
        Assert.assertEquals(Arrays.asList("foo_value1"), Arrays.asList(m.actionFoo));
        Assert.assertNotNull(m.actionBar);
        Assert.assertEquals(Arrays.asList("bar_value1", "bar_value2"), Arrays.asList(m.actionBar));
        Assert.assertEquals(PortletMode.HELP, m.renderPortletMode);
        Assert.assertEquals(WindowState.MINIMIZED, m.renderWindowState);
        Assert.assertNotNull(m.renderFoo);
        Assert.assertEquals(Arrays.asList("foo_value2"), Arrays.asList(m.renderFoo));
        Assert.assertNotNull(m.renderBar);
        Assert.assertEquals(Arrays.asList("bar_value3", "bar_value4"), Arrays.asList(m.renderBar));
    }

    @Test
    public void testNoParametersAfterAction() {

        class MyPortlet extends GenericPortlet {

            String foo;

            @Override
            public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
            }

            @Override
            public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
                foo = request.getParameter("foo");
                response.setContentType("text/html");
                PortletURL renderURL = response.createRenderURL();
                renderURL.setParameter("foo", "foo_value");
                PrintWriter writer = response.getWriter();
                writer.append("<a id='action' href='").append(response.createActionURL().toString()).append("'>action</a>");
                writer.append("<a id='render' href='").append(renderURL.toString()).append("'>render</a>");
                writer.close();
            }
        }
        MyPortlet m = new MyPortlet();
        delegate = m;

        String url = deploymentURL.toString() + "page1";
        driver.get(url);
        Assert.assertNull(m.foo);
        driver.findElement(By.id("render")).click();
        Assert.assertEquals("foo_value", m.foo);
        driver.findElement(By.id("action")).click();
        Assert.assertNull(m.foo);
    }

    public static class Portlet1 extends GenericPortlet {

        @Override
        public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
            delegate.processAction(request, response);
        }

        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            delegate.render(request, response);
        }
    }
}
