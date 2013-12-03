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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;

import org.junit.Assert;
import juzu.impl.common.Tools;
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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@RunWith(Arquillian.class)
public class PortletResourceServingTestCase extends AbstractPortalTestCase {

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
    static ResourceServingPortlet delegate;

    @Test
    public void testStatus() throws Exception {
        driver.get(deploymentURL.toString() + "page1");
        delegate = new ResourceServingPortlet() {
            @Override
            public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
                response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "" + 204);
            }
        };
        URL url = new URL(driver.findElement(By.id("resource")).getAttribute("href"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(204, conn.getResponseCode());
    }

    @Test
    public void testHeader() throws Exception {
        driver.get(deploymentURL.toString() + "page1");
        delegate = new ResourceServingPortlet() {
            @Override
            public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
                response.setProperty("X-Powered-By", "GateIn/4.0.0");
            }
        };
        URL url = new URL(driver.findElement(By.id("resource")).getAttribute("href"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(200, conn.getResponseCode());
        Map<String, String> headers = Tools.responseHeaders(conn);
        Assert.assertEquals("GateIn/4.0.0", headers.get("X-Powered-By"));
    }

    @Test
    public void testChars() throws Exception {
        driver.get(deploymentURL.toString() + "page1");
        delegate = new ResourceServingPortlet() {
            @Override
            public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
                response.setContentType("text/plain");
                response.getWriter().append("the resource").close();
            }
        };
        URL url = new URL(driver.findElement(By.id("resource")).getAttribute("href"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(200, conn.getResponseCode());
        Map<String, String> headers = Tools.responseHeaders(conn);
        String contentType = headers.get("Content-Type");
        Assert.assertTrue("Was expecting text/plain mime type " + contentType, contentType.startsWith("text/plain"));
        String read = Tools.read(conn.getInputStream());
        Assert.assertEquals("the resource", read);
    }

    @Test
    public void testBinary() throws Exception {
        driver.get(deploymentURL.toString() + "page1");
        delegate = new ResourceServingPortlet() {
            @Override
            public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
                response.setContentType("application/octet-stream;");
                OutputStream out = response.getPortletOutputStream();
                out.write(3);
                out.write(1);
                out.write(4);
                out.close();
            }
        };
        URL url = new URL(driver.findElement(By.id("resource")).getAttribute("href"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(200, conn.getResponseCode());
        Map<String, String> headers = Tools.responseHeaders(conn);
        String contentType = headers.get("Content-Type");
        Assert.assertTrue("Was expecting application/octet-stream mime type " + contentType, contentType.startsWith("application/octet-stream"));
        byte[] bytes = Tools.bytes(conn.getInputStream());
        Assert.assertEquals(3, bytes.length);
        Assert.assertEquals(3, bytes[0]);
        Assert.assertEquals(1, bytes[1]);
        Assert.assertEquals(4, bytes[2]);
    }

    @Test
    public void testContentType() throws Exception {
        driver.get(deploymentURL.toString() + "page1");
        delegate = new ResourceServingPortlet() {
            @Override
            public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
                response.setContentType("application/octet-stream;");
                response.setCharacterEncoding("UTF-16");
                response.getWriter().close();
            }
        };
        URL url = new URL(driver.findElement(By.id("resource")).getAttribute("href"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(200, conn.getResponseCode());
        Map<String, String> headers = Tools.responseHeaders(conn);
        String contentType = headers.get("Content-Type");
        Assert.assertTrue("Was expecting application/octet-stream mime type " + contentType, contentType.startsWith("application/octet-stream"));
        Assert.assertTrue("Was expecting UTF-16 charset " + contentType, contentType.endsWith(";charset=UTF-16"));
    }

    public static class Portlet1 extends GenericPortlet {

        @Override
        public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
            if (delegate != null) {
                delegate.serveResource(request, response);
            }
        }

        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            response.getWriter().append("<a id='resource' href='").append(response.createResourceURL().toString()).append("'></a>").close();
        }
    }
}
