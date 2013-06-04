package org.gatein.portal.portlet.register;

import com.thoughtworks.selenium.DefaultSelenium;

import juzu.impl.common.RunMode;
import juzu.impl.inject.spi.InjectorProvider;

import org.gatein.portal.AbstractPortalTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.net.URL;

@RunWith(Arquillian.class)
public class RegisterPortletTest extends AbstractPortalTestCase {

   @Deployment(testable = false)
   public static WebArchive createPortlet() {
      WebArchive portal = AbstractPortalTestCase.createPortal(InjectorProvider.INJECT_GUICE, RunMode.PROD);
      portal.addAsWebInfResource(new StringAsset(descriptor(RegisterPortlet.class).exportAsString()), "portlet.xml");
      return portal;
   }

   @ArquillianResource
   URL deploymentURL;

   @Drone
   WebDriver driver;
   
   DefaultSelenium br;

   @Test
   @InSequence(1)
   public void testRegisterSuccess() {
      driver.get(deploymentURL.toString() + "/register");
      // fill the form
      driver.findElements(By.name("userName")).get(0).sendKeys("test");
      driver.findElements(By.name("password")).get(0).sendKeys("test");
      driver.findElements(By.name("confirmPassword")).get(0).sendKeys("test");
      driver.findElements(By.name("firstName")).get(0).sendKeys("test");
      driver.findElements(By.name("lastName")).get(0).sendKeys("test");
      driver.findElements(By.name("displayName")).get(0).sendKeys("test");
      driver.findElements(By.name("emailAddress")).get(0).sendKeys("test");
      
      driver.findElements(By.name("submit")).get(0).click();
      Assert.assertTrue(driver.findElement(By.id("registerMessage")).getText().contains("You have successfully registered a new account"));
   }
   
   @Test
   @InSequence(2)
   public void testPasswordFail() {
      driver.get(deploymentURL.toString() + "/register");
      // fill the form
      driver.findElements(By.name("userName")).get(0).sendKeys("test_1");
      driver.findElements(By.name("password")).get(0).sendKeys("test_1");
      driver.findElements(By.name("confirmPassword")).get(0).sendKeys("test_1test_1");
      driver.findElements(By.name("firstName")).get(0).sendKeys("test_1");
      driver.findElements(By.name("lastName")).get(0).sendKeys("test_1");
      driver.findElements(By.name("displayName")).get(0).sendKeys("test_1");
      driver.findElements(By.name("emailAddress")).get(0).sendKeys("test_1");
      
      driver.findElements(By.name("submit")).get(0).click();
      Assert.assertTrue(driver.findElement(By.id("registerMessage")).getText().contains("Password and Confirm Password must be the same"));
   }
   
   @Test
   @InSequence(3)
   public void testAccountExisted() {      
      driver.get(deploymentURL.toString() + "/register");
      // fill the form
      driver.findElements(By.name("userName")).get(0).sendKeys("test");
      driver.findElements(By.name("password")).get(0).sendKeys("test");
      driver.findElements(By.name("confirmPassword")).get(0).sendKeys("test");
      driver.findElements(By.name("firstName")).get(0).sendKeys("test");
      driver.findElements(By.name("lastName")).get(0).sendKeys("test");
      driver.findElements(By.name("displayName")).get(0).sendKeys("test");
      driver.findElements(By.name("emailAddress")).get(0).sendKeys("test");
      
      driver.findElements(By.name("submit")).get(0).click();
      Assert.assertTrue(driver.findElement(By.id("registerMessage")).getText().contains("This user is already existed"));
   }
   
   @Test
   @InSequence(4)
   public void testFormReset() {      
      driver.get(deploymentURL.toString() + "/register");
      // fill the form
      driver.findElements(By.name("userName")).get(0).sendKeys("test form reset");
      Assert.assertTrue(driver.findElements(By.name("userName")).get(0).getAttribute("value").equalsIgnoreCase("test form reset"));
      
      driver.findElements(By.name("reset")).get(0).click();
      Assert.assertTrue(driver.findElements(By.name("userName")).get(0).getAttribute("value").isEmpty());
   }
}
