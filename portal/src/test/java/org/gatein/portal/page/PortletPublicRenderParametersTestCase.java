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
import org.jboss.shrinkwrap.descriptor.api.portletapp20.PortletDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@RunWith(Arquillian.class)
public class PortletPublicRenderParametersTestCase extends AbstractPortalTestCase {

    @Deployment(testable = false)
    public static WebArchive createPortal() {
        WebArchive portal = AbstractPortalTestCase.createPortal();
        PortletDescriptor descriptor = portletXML();
        descriptor = descriptor(descriptor, Portlet1.class).supportedPublicRenderParameter("foo").up();
        descriptor = descriptor(descriptor, Portlet2.class).supportedPublicRenderParameter("foo").up();
        descriptor = descriptor(descriptor, Portlet3.class).supportedPublicRenderParameter("foo").up();
        descriptor = descriptor.getOrCreatePublicRenderParameter().identifier("foo").name("_foo_").up();
        portal.addAsWebInfResource(new StringAsset(descriptor.exportAsString()), "portlet.xml");
        return portal;
    }

    @ArquillianResource
    URL deploymentURL;

    @Drone
    WebDriver driver;

    /** . */
    static String[] foo1;

    /** . */
    static String[] foo2;

    /** . */
    static String[] foo3;

    /** . */
    static String[] foo4;

    @Test
    public void testHello() {
        String url = deploymentURL.toString() + "page3";
        driver.get(url);
        Assert.assertEquals(null, foo1);
        Assert.assertEquals(null, foo2);
        Assert.assertEquals(null, foo3);
        Assert.assertEquals(null, foo4);
        WebElement link = driver.findElement(By.id("foo1"));
        link.click();
        Assert.assertEquals(Arrays.asList("foo:value1,", "foo:value2,"), Arrays.asList(foo1));
        Assert.assertEquals(Arrays.asList("foo:value1,", "foo:value2,"), Arrays.asList(foo2));
        Assert.assertEquals(Arrays.asList("foo:value1,", "foo:value2,"), Arrays.asList(foo3));
        Assert.assertEquals(null, foo4);
        link = driver.findElement(By.id("foo2"));
        link.click();
        Assert.assertEquals(Arrays.asList("foo:value3,", "foo:value4,"), Arrays.asList(foo1));
        Assert.assertEquals(Arrays.asList("foo:value3,", "foo:value4,"), Arrays.asList(foo2));
        Assert.assertEquals(Arrays.asList("foo:value3,", "foo:value4,"), Arrays.asList(foo3));
        Assert.assertEquals(null, foo4);
        link = driver.findElement(By.id("foo3"));
        link.click();
        Assert.assertEquals(Arrays.asList("foo:value5,", "foo:value6,"), Arrays.asList(foo1));
        Assert.assertEquals(Arrays.asList("foo:value5,", "foo:value6,"), Arrays.asList(foo2));
        Assert.assertEquals(Arrays.asList("foo:value5,", "foo:value6,"), Arrays.asList(foo3));
        Assert.assertEquals(Arrays.asList("foo:value3,", "foo:value4,"), Arrays.asList(foo4));
    }

    public static class Portlet1 extends GenericPortlet {

        @Override
        protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            foo1 = request.getParameterMap().get("foo");
            PortletURL url = response.createRenderURL();
            url.setParameter("foo", new String[]{"foo:value1,","foo:value2,"});
            response.getWriter().append("<a id='foo1' href='").append(url.toString()).append("'>click</a>").close();
        }
    }

    public static class Portlet2 extends GenericPortlet {

        @Override
        protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            foo2 = request.getParameterMap().get("foo");
            PortletURL url = response.createRenderURL();
            url.setParameter("foo", new String[]{"foo:value3,", "foo:value4,"});
            response.getWriter().append("<a id='foo2' href='").append(url.toString()).append("'>click</a>").close();
        }
    }

    public static class Portlet3 extends GenericPortlet {

        @Override
        public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
            foo4 = request.getPublicParameterMap().get("foo");
            response.setRenderParameter("foo", new String[]{"foo:value5,", "foo:value6,"});
        }

        @Override
        protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            foo3 = request.getParameterMap().get("foo");
            PortletURL url = response.createActionURL();
            response.getWriter().append("<a id='foo3' href='").append(url.toString()).append("'>click</a>").close();
        }
    }
}
