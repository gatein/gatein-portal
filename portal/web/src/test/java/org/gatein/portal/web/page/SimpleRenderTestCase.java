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

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.gatein.portal.content.Result;
import org.gatein.portal.web.AbstractPortalTestCase;
import org.gatein.portal.web.content.simple.SimpleContent;
import org.gatein.portal.web.content.simple.SimpleContentLogic;
import org.gatein.portal.web.content.simple.SimpleContentProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@RunWith(Arquillian.class)
public class SimpleRenderTestCase extends AbstractPortalTestCase {

    @Deployment(testable = false)
    public static WebArchive createPortal() {
        return AbstractPortalTestCase.createPortal();
    }

    @ArquillianResource
    URL deploymentURL;

    @Drone
    WebDriver driver;

    @Test
    public void testViewFragment() {
        SimpleContentProvider.deploy("the_id", new SimpleContentLogic() {
            @Override
            public Result.View render(SimpleContent content) {
                return new Result.Fragment(
                        Collections.<Map.Entry<String, String>>emptyList(),
                        Collections.<Element>emptyList(),
                        "the_title",
                        "<div id=\"" + content.getId() +  "\">the_content</div>");
            }
        });
        String url = deploymentURL.toString() + "page4";
        driver.get(url);
        WebElement element = driver.findElement(By.id("the_id"));
        Assert.assertEquals("the_content", element.getText());
    }

    @Test
    public void testViewError() {
        final Exception cause = new Exception("foobar");
        SimpleContentProvider.deploy("the_id", new SimpleContentLogic() {
            @Override
            public Result.View render(SimpleContent content) {
                return new Result.Error(false, cause);
            }
        });
        String url = deploymentURL.toString() + "page4";
        driver.get(url);
        String source = driver.getPageSource();
        Assert.assertTrue(source.contains("Error: foobar"));
    }
}
