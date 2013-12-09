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
package org.gatein.portal.appzu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import junit.framework.AssertionFailedError;
import juzu.impl.common.Completion;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import org.gatein.common.io.IOTools;
import org.gatein.portal.arquillian.api.Page;
import org.gatein.portal.arquillian.api.PortalTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Julien Viet
 */
@RunWith(Arquillian.class)
@PortalTest({
        @Page("view/page.xml"),
        @Page("viewurl/page.xml"),
        @Page("actionurl/page.xml"),
        @Page("live/page.xml"),
        @Page("compilationfailure/page.xml"),
        @Page("kernelinject/page.xml"),
        @Page("dotfiles/page.xml"),
        @Page("sample/page.xml"),
        @Page("asset/page.xml")
})
public class AppZuTestCase {

    @Deployment()
    public static WebArchive createPortal() {
        WebArchive portal = ShrinkWrap.create(WebArchive.class, "portal.war");
        portal.addAsWebResource(new StringAsset("foo"), "foo");
        URL fragmentResource = IWebdavStoreImpl.class.getResource("/META-INF/web-fragment.xml");
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
        jar.addAsManifestResource(fragmentResource, "web-fragment.xml");
        portal.addAsLibrary(jar);
        return portal;
    }

    @ArquillianResource
    URL url;

    @Drone
    WebDriver driver;

    private static void copy(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            if (dst.exists()) {
                throw new IOException();
            } else {
                if (!dst.mkdirs()) {
                    throw new IOException();
                } else {
                    File[] children = src.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            copy(child, new File(dst, child.getName()));
                        }
                    }
                }
            }
        } else {
            if (dst.exists()) {
                throw new IOException();
            } else {
                FileInputStream in = new FileInputStream(src);
                try {
                    FileOutputStream out = new FileOutputStream(dst);
                    try {
                        IOTools.copy(in, out);
                    } finally {
                        Tools.safeClose(out);
                    }
                } finally {
                    Tools.safeClose(in);
                }
            }
        }
    }

    private File deploy(String name) throws Exception {
        File dst = new File(System.getProperty("gatein.test.output.path", ""));
        if (!dst.exists()) {
            throw new IOException();
        } else {
            dst = new File(dst, "workspace");
            File root = dst = new File(dst, name);
            Name appName = Name.parse(getClass().getPackage().getName()).append(name);
            for (String atom : appName) {
                dst = new File(dst, atom);
            }
            File src = new File(getClass().getResource(name).toURI());
            copy(src, dst);
            ApplicationRepository repo = ApplicationRepository.instance;
            repo.addApplication(appName, "", new DiskFileSystem(new File(root.toURI())));
            return dst;
        }
    }

    @Test
    @RunAsClient
    public void testView() throws Exception {
        deploy("view");
        driver.get(url.toURI().resolve("./view").toString());
        WebElement target = driver.findElement(By.id("target"));
        Assert.assertEquals("THE VIEW", target.getText());
    }


    @Test
    @RunAsClient
    public void testViewURL() throws Exception {
        deploy("viewurl");
        driver.get(url.toURI().resolve("./viewurl").toString());
        WebElement target = driver.findElement(By.id("target"));
        target.click();
        target = driver.findElement(By.id("target"));
        Assert.assertEquals("hello world", target.getText());
    }

    @Test
    @RunAsClient
    public void testActionURL() throws Exception {
        deploy("actionurl");
        driver.get(url.toURI().resolve("./actionurl").toString());
        WebElement target = driver.findElement(By.id("target"));
        target.click();
        target = driver.findElement(By.id("target"));
        Assert.assertEquals("hello world", target.getText());
    }

    @Test
    @RunAsClient
    public void testLive() throws Exception {
        File app = deploy("live");
        driver.get(url.toURI().resolve("./live").toString());
        WebElement target = driver.findElement(By.id("target"));
        Assert.assertEquals("LIVE", target.getText());
        File controller = new File(app, "Controller.java");
        Assert.assertTrue(controller.exists());
        String s = Tools.read(controller);
        s = s.replace("LIVE", "FOO");
        Tools.write(s, controller);
        driver.get(url.toURI().resolve("./live").toString());
        target = driver.findElement(By.id("target"));
        Assert.assertEquals("FOO", target.getText());
    }

    @Test
    @RunAsClient
    public void testCompilationFailure() throws Exception {
        deploy("compilationfailure");
        driver.get(url.toURI().resolve("./compilationfailure").toString());
        Assert.assertTrue(driver.getPageSource().contains("incompatible types"));
    }

    @Test
    @RunAsClient
    public void testKernelInject() throws Exception {
        deploy("kernelinject");
        driver.get(url.toURI().resolve("./kernelinject").toString());
        Assert.assertTrue(driver.getPageSource().contains("pass"));
    }

    @Test
    @RunAsClient
    public void testDotFiles() throws Exception {
        File app = deploy("dotfiles");
        File dotFile = new File(app, "_Controller.java");
        Assert.assertTrue(dotFile.renameTo(new File(app, "._Controller.java")));
        driver.get(url.toURI().resolve("./dotfiles").toString());
        Assert.assertTrue(driver.getPageSource().contains("pass"));
    }

    @Test
    @RunAsClient
    public void testAddSample() throws Exception {
        App app = ApplicationRepository.getInstance().addApplication(Name.parse("org.gatein.portal.appzu.sample"), "", "helloworld");
        Completion<Boolean> completion = app.refresh();
        if (completion.isFailed()) {
            AssertionFailedError afe = new AssertionFailedError("Could not compile application");
            afe.initCause(completion.getCause());
            throw afe;
        } else {
            Assert.assertTrue(completion.get());
            app.bridge.getApplication().getClassLoader().loadClass("org.gatein.portal.appzu.sample.Controller");
        }
    }

    @Test
    @RunAsClient
    public void testAssets() throws Exception {
        deploy("asset");
        driver.get(url.toURI().resolve("./asset").toString());
        Assert.assertTrue(driver.getPageSource().contains("pass"));
        WebElement body = driver.findElement(By.tagName("body"));
        String bar = body.getAttribute("bar");
        Assert.assertEquals("bar_value", bar);
        String juu = body.getCssValue("juu");
        Assert.assertEquals("juu_value", juu);
    }
}
