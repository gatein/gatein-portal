/*
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
package org.gatein.portal;

import java.net.URL;
import java.util.List;

import juzu.impl.common.RunMode;
import juzu.impl.inject.spi.InjectorProvider;

import org.gatein.portal.ui.Portlet1;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
@RunWith(Arquillian.class)
public class JuzuPortletTestCase extends AbstractPortalTestCase {

  @Deployment(testable = false)
  public static WebArchive createPortal() {
      WebArchive portal = AbstractPortalTestCase.createPortal(InjectorProvider.INJECT_GUICE, RunMode.PROD);
      portal.addAsWebInfResource(new StringAsset(descriptor(Portlet1.class).exportAsString()), "portlet.xml");
      return portal;
  }

  @ArquillianResource
  URL deploymentURL;

  @Drone
  WebDriver driver;

  @Test
  public void testRenderURL() throws Exception {
      driver.get(deploymentURL.toString() + "/page1");
      WebElement element = driver.findElement(By.id("index"));
      Assert.assertTrue(element.getText().contains("view"));
  }

  @Test
  public void testAction() throws Exception {
     driver.get(deploymentURL.toString() + "/page1");
     WebElement element = driver.findElement(By.id("action"));
     element.click();
     Assert.assertTrue(driver.findElement(By.id("action")).getText().contains("action"));
  }
  
  @Test
  public void testServeResource() throws Exception {
      driver.get(deploymentURL.toString() + "/page1");
      WebElement element = driver.findElement(By.id("resource"));
      element.click();
      Assert.assertTrue(driver.findElement(By.id("view")).getText().contains("view"));
      Assert.assertTrue(driver.findElement(By.id("action")).getText().contains("action"));
  }

  @Test
  public void testAsset() throws Exception {
     driver.get(deploymentURL.toString() + "/page1");
     List<WebElement> scripts = driver.findElements(By.tagName("script"));
     boolean found = false;
     for (WebElement script : scripts) {
        String src = script.getAttribute("src");
        if (src != null && src.endsWith("/portal/assets/org/gatein/portal/ui/assets/test.js")) {
           found = true;
        }
     }
     Assert.assertTrue(found);
  }
}
