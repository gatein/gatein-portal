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

package org.gatein.portal.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import junit.framework.AssertionFailedError;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.InjectorProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


@RunWith(Arquillian.class)
public class PortalLoginTestCase extends AbstractPortalTestCase {

    @Deployment(testable = false)
    public static WebArchive createPortal() {
        //WebArchive portal = AbstractPortalTestCase.createPortal(InjectorProvider.INJECT_GUICE, RunMode.DEV);
        WebArchive portal = ShrinkWrap.create(WebArchive.class, "portal.war");

        String servlet;
        try {
            servlet = Tools.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("web.xml"));
        } catch (IOException e) {
            AssertionFailedError afe = new AssertionFailedError("Could not read web xml deployment descriptor");
            afe.initCause(e);
            throw afe;
        }

        servlet = String.format(servlet, InjectorProvider.GUICE.getValue(), RunMode.DEV.getValue());

        portal.setWebXML(new StringAsset(servlet));


        portal.merge(ShrinkWrap.
                create(GenericArchive.class).
                as(ExplodedImporter.class).
                importDirectory("src/main/webapp/WEB-INF/classes").
                as(GenericArchive.class), "/WEB-INF/classes", Filters.exclude("web.xml"));

        portal.merge(ShrinkWrap.
                create(GenericArchive.class).
                as(ExplodedImporter.class).
                importDirectory("src/test/resources/WEB-INF/conf").
                as(GenericArchive.class), "/WEB-INF/conf", Filters.exclude("web.xml"));

        portal.merge(ShrinkWrap.
                create(GenericArchive.class).
                as(ExplodedImporter.class).
                importDirectory("src/main/webapp/META-INF").
                as(GenericArchive.class), "/META-INF", Filters.exclude("web.xml"));

        portal.addAsWebInfResource(new StringAsset(descriptor(Portlet1.class).exportAsString()), "portlet.xml");

        return portal;
    }

    @ArquillianResource
    URL deploymentURL;

    @Drone
    WebDriver driver;

    @Test
    @RunAsClient
    public void testHasLoginForm() {
        String url = deploymentURL.toString() + "/login";
        driver.get(url);
        WebElement form = driver.findElement(By.tagName("form"));
        WebElement username = form.findElement(By.name("username"));
        WebElement password = form.findElement(By.name("password"));

        Assert.assertNotNull(form);
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);
    }

    @Test
    @RunAsClient
    public void testNotLogin() throws InterruptedException, URISyntaxException, MalformedURLException {
        String portletURL = deploymentURL + "page1";
        driver.get(portletURL);
        WebElement body = driver.findElement(By.tagName("body"));
        WebElement linkElement = driver.findElement(By.id("login-user"));
        Assert.assertNotNull(linkElement);
        String username = linkElement.getText();
        Assert.assertEquals("__GUEST__", username);
    }

    @Test
    @RunAsClient
    public void testDoLogin() throws InterruptedException {
        String url = deploymentURL.toString() + "/dologin?initURL="+ deploymentURL + "page1";
        driver.get(url);
        WebElement form = driver.findElement(By.tagName("form"));
        WebElement username = form.findElement(By.name("username"));
        WebElement password = form.findElement(By.name("password"));

        Assert.assertNotNull(form);
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        username.sendKeys("root");
        password.sendKeys("exo");
        form.submit();

        String portletURL = deploymentURL + "page1";
        driver.get(portletURL);

        WebElement linkElement = driver.findElement(By.id("login-user"));
        Assert.assertNotNull(linkElement);
        String name = linkElement.getText();
        Assert.assertEquals("root", name);
    }

    @Test
    @RunAsClient
    public void testLoginFailure() {
        String url = deploymentURL.toString() + "/dologin?initURL="+ deploymentURL + "page1";
        driver.get(url);
        WebElement form = driver.findElement(By.tagName("form"));
        WebElement username = form.findElement(By.name("username"));
        WebElement password = form.findElement(By.name("password"));

        Assert.assertNotNull(form);
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        username.sendKeys("root");
        password.sendKeys("gtn1111");
        form.submit();

        WebElement body = driver.findElement(By.tagName("body"));
        Assert.assertTrue(body.getText().contains("Username or password incorrect!"));

        String portletURL = deploymentURL + "page1";
        driver.get(portletURL);

        WebElement linkElement = driver.findElement(By.id("login-user"));
        Assert.assertNotNull(linkElement);
        String name = linkElement.getText();
        Assert.assertEquals("__GUEST__", name);
    }


    public static class Portlet1 extends GenericPortlet {
        @Override
        protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            String username = request.getRemoteUser();
            if(username == null || username.isEmpty()) {
                username = "__GUEST__";
            }
            writer.append("<span id='login-user'>"+username+"</span>");
            writer.close();
        }
    }
}
