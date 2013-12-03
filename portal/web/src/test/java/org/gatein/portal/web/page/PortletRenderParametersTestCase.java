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

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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
public class PortletRenderParametersTestCase extends AbstractPortalTestCase {

    @Deployment(testable = false)
    public static WebArchive createPortal() {
        WebArchive portal = AbstractPortalTestCase.createPortal();
        portal.addAsWebInfResource(new StringAsset(descriptor(Portlet1.class, Portlet2.class).exportAsString()), "portlet.xml");
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

    @Test
    public void testHello() {
        String url = deploymentURL.toString() + "page2";
        driver.get(url);
        WebElement fooLink = driver.findElement(By.id("foo"));
        fooLink.click();
        Assert.assertEquals(Arrays.asList("foo:value1,", "foo:value2,"), Arrays.asList(foo));
        Assert.assertEquals(null, bar);
        WebElement barLink = driver.findElement(By.id("bar"));
        barLink.click();
        Assert.assertEquals(Arrays.asList("foo:value1,", "foo:value2,"), Arrays.asList(foo));
        Assert.assertEquals(Arrays.asList("bar:value,"), Arrays.asList(bar));
    }

    public static class Portlet1 extends GenericPortlet {

        @Override
        protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            foo = request.getParameterMap().get("foo");
            PortletURL url = response.createRenderURL();
            url.setParameter("foo", new String[]{"foo:value1,","foo:value2,"});
            response.getWriter().append("<a id='foo' href='").append(url.toString()).append("'>click</a>").close();
        }
    }

    public static class Portlet2 extends GenericPortlet {

        @Override
        protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            bar = request.getParameterMap().get("bar");
            PortletURL url = response.createRenderURL();
            url.setParameter("bar", "bar:value,");
            response.getWriter().append("<a id='bar' href='").append(url.toString()).append("'>click</a>").close();
        }
    }
}
