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
import java.util.Collections;
import java.util.HashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.gatein.portal.web.AbstractPortalTestCase;
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
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@RunWith(Arquillian.class)
public class PortletPreferencesTestCase extends AbstractPortalTestCase {

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

    @Test
    public void testAction() {
        String url = deploymentURL.toString() + "page5";
        driver.get(url);
        Assert.assertEquals(Collections.singleton("pref_name"), Portlet1.preferences.keySet());
        String[] prefs = Portlet1.preferences.get("pref_name");
        Assert.assertEquals(1, prefs.length);
        Assert.assertEquals("pref_value", prefs[0]);
        WebElement elt = driver.findElement(By.id("click"));
        elt.click();
        Assert.assertEquals(Collections.singleton("pref_name"), Portlet1.preferences.keySet());
        prefs = Portlet1.preferences.get("pref_name");
        Assert.assertEquals(1, prefs.length);
        Assert.assertEquals("pref_bar", prefs[0]);
    }

    public static class Portlet1 extends GenericPortlet {

        /** . */
        static HashMap<String, String[]> preferences = null;

        @Override
        public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
            PortletPreferences prefs = request.getPreferences();
            prefs.setValue("pref_name", "pref_bar");
            prefs.store();
        }

        @Override
        protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            preferences = new HashMap<String, String[]>(request.getPreferences().getMap());
            response.setContentType("text/html");
            PortletURL url = response.createActionURL();
            response.getWriter().append("<a id=\"click\" href=\"").append(url.toString()).append("\">save</a>").close();
        }
    }
}
