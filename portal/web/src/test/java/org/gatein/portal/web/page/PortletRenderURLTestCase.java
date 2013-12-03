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

import javax.portlet.*;
import javax.portlet.WindowState;

import org.junit.Assert;
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
public class PortletRenderURLTestCase extends AbstractPortalTestCase {

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
    static String[] foo;

    /** . */
    static String[] bar;

    /** . */
    static WindowState windowState;

    /** . */
    static PortletMode portletMode;

    @Test
    public void testHello() {
        String url = deploymentURL.toString() + "page1";
        driver.get(url);
        WebElement click = driver.findElement(By.id("click"));
        foo = null;
        bar = null;
        windowState = null;
        portletMode = null;
        click.click();
        Assert.assertEquals(PortletMode.EDIT, portletMode);
        Assert.assertEquals(WindowState.MAXIMIZED, windowState);
        Assert.assertNotNull(foo);
        Assert.assertEquals(Arrays.asList("foo_value"), Arrays.asList(foo));
        Assert.assertNotNull(bar);
        Assert.assertEquals(Arrays.asList("bar_value1", "bar_value2"), Arrays.asList(bar));
    }

    public static class Portlet1 extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            foo = request.getParameterMap().get("foo");
            bar = request.getParameterMap().get("bar");
            windowState = request.getWindowState();
            portletMode = request.getPortletMode();
            PortletURL url = response.createRenderURL();
            url.setParameter("foo", "foo_value");
            url.setParameter("bar", new String[]{"bar_value1","bar_value2"});
            url.setWindowState(WindowState.MAXIMIZED);
            url.setPortletMode(PortletMode.EDIT);
            response.getWriter().append("<a id='click' href='").append(url.toString()).append("'>click</a>").close();
        }
    }
}
