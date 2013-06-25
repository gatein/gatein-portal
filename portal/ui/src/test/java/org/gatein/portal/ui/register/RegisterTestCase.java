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
package org.gatein.portal.ui.register;

import java.net.URL;
import java.util.concurrent.Callable;

import junit.framework.AssertionFailedError;
import juzu.arquillian.Helper;
import org.exoplatform.component.test.BaseGateInTest;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.gatein.portal.common.kernel.KernelLifeCycle;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.portletapp20.PortletDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import static org.junit.Assert.*;

/**
 * @author Julien Viet
 */
@RunWith(Arquillian.class)
public class RegisterTestCase {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "portal.war");
        Helper.createBasePortletDeployment(war, "guice", Controller.class);
        PortletDescriptor descriptor = Descriptors.
                create(PortletDescriptor.class).
                createPortlet().
                portletName("RegisterPortlet").
                portletClass(RegisterPortlet.class.getName()).
                up();
        war.addAsWebInfResource(new StringAsset(descriptor.exportAsString()), "portlet.xml");
        Node node = war.get("WEB-INF/web.xml");
        WebAppDescriptor webApp = Descriptors.importAs(WebAppDescriptor.class).fromStream(node.getAsset().openStream());
        webApp.displayName("portal").createFilter().filterName("KernelLifeCycle").filterClass(KernelLifeCycle.class.getName()).up().
                createFilterMapping().filterName("KernelLifeCycle").servletName("EmbedServlet").up();
        war.delete(node.getPath());
        war.setWebXML(new StringAsset(webApp.exportAsString()));
        war.addAsWebInfResource("org/gatein/portal/ui/register/configuration.xml", "conf/configuration.xml");
        return war;
    }

    @Drone
    WebDriver driver;

    @ArquillianResource
    URL deploymentURL;
    
    private String getBaseURL() {
        try {
            return deploymentURL.toURI().resolve("./embed/RegisterPortlet").toURL().toString();
        } catch (Exception e) {
            AssertionFailedError afe = new AssertionFailedError();
            afe.initCause(e);
            throw afe;
        }
    }

    @Test
    @InSequence(1)
    @RunAsClient
    public void testRegisterSuccess() {
        driver.get(getBaseURL());
        // fill the form
        driver.findElements(By.name("userName")).get(0).sendKeys("test_user_name");
        driver.findElements(By.name("password")).get(0).sendKeys("test_password");
        driver.findElements(By.name("confirmPassword")).get(0).sendKeys("test_password");
        driver.findElements(By.name("firstName")).get(0).sendKeys("test_first_name");
        driver.findElements(By.name("lastName")).get(0).sendKeys("test_last_name");
        driver.findElements(By.name("displayName")).get(0).sendKeys("test_display_name");
        driver.findElements(By.name("emailAddress")).get(0).sendKeys("test_email_address");

        driver.findElements(By.name("submit")).get(0).click();
        assertTrue(driver.findElement(By.id("registerMessage")).getText().contains("You have successfully registered a new account"));
    }

    @Test
    @InSequence(2)
     public void testUserExist() throws Exception {
        BaseGateInTest.inPortalContainer(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                OrganizationService orgService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class);
                UserHandler handler = orgService.getUserHandler();
                User user = handler.findUserByName("test_user_name");
                assertNotNull(user);
                assertEquals(null, user.getPassword());
                assertEquals("test_first_name", user.getFirstName());
                assertEquals("test_last_name", user.getLastName());
                assertEquals("test_display_name", user.getDisplayName());
                assertEquals("test_email_address", user.getEmail());
                return null;
            }
        });
    }

    @Test
    @InSequence(3)
    @RunAsClient
    public void testPasswordFail() {
        driver.get(getBaseURL());
        // fill the form
        driver.findElements(By.name("userName")).get(0).sendKeys("test_1");
        driver.findElements(By.name("password")).get(0).sendKeys("test_1");
        driver.findElements(By.name("confirmPassword")).get(0).sendKeys("test_1test_1");
        driver.findElements(By.name("firstName")).get(0).sendKeys("test_1");
        driver.findElements(By.name("lastName")).get(0).sendKeys("test_1");
        driver.findElements(By.name("displayName")).get(0).sendKeys("test_1");
        driver.findElements(By.name("emailAddress")).get(0).sendKeys("test_1");

        driver.findElements(By.name("submit")).get(0).click();
        assertTrue(driver.findElement(By.id("registerMessage")).getText().contains("Password and Confirm Password must be the same"));
    }

    @Test
    @InSequence(4)
    @RunAsClient
    public void testAccountExisted() {
        driver.get(getBaseURL());
        // fill the form
        driver.findElements(By.name("userName")).get(0).sendKeys("test_user_name");
        driver.findElements(By.name("password")).get(0).sendKeys("test");
        driver.findElements(By.name("confirmPassword")).get(0).sendKeys("test");
        driver.findElements(By.name("firstName")).get(0).sendKeys("test");
        driver.findElements(By.name("lastName")).get(0).sendKeys("test");
        driver.findElements(By.name("displayName")).get(0).sendKeys("test");
        driver.findElements(By.name("emailAddress")).get(0).sendKeys("test");

        driver.findElements(By.name("submit")).get(0).click();
        assertTrue(driver.findElement(By.id("registerMessage")).getText().contains("This user is already existed"));
    }

    @Test
    @InSequence(4)
    @RunAsClient
    public void testFormReset() {
        driver.get(getBaseURL());
        // fill the form
        driver.findElements(By.name("userName")).get(0).sendKeys("test form reset");
        assertTrue(driver.findElements(By.name("userName")).get(0).getAttribute("value").equalsIgnoreCase("test form reset"));

        driver.findElements(By.name("reset")).get(0).click();
        assertTrue(driver.findElements(By.name("userName")).get(0).getAttribute("value").isEmpty());
    }
}
