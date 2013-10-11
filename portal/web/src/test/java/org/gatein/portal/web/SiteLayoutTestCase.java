package org.gatein.portal.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.portlet.GenericPortlet;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import junit.framework.Assert;
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

/**
 * @author Julien Viet
 */
@RunWith(Arquillian.class)
public class SiteLayoutTestCase extends AbstractPortalTestCase {

    @Deployment(testable = false)
    public static WebArchive createPortal() {
        WebArchive portal = AbstractPortalTestCase.createPortal();

        String path = "/WEB-INF/conf/portal/portal/classic/portal.xml";
        Assert.assertNotNull(portal.delete(path));

        // This is ugly but we will do something better later
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<portal-config\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://www.gatein.org/xml/ns/gatein_objects_1_3 http://www.gatein.org/xml/ns/gatein_objects_1_3\"\n" +
                "    xmlns=\"http://www.gatein.org/xml/ns/gatein_objects_1_3\">\n" +
                "  <portal-name>test</portal-name>\n" +
                "  <locale>en</locale>\n" +
                "  <portal-layout>\n" +
                "        <container>\n" +
                "      <name>header</name>\n" +
                "      <portlet-application>\n" +
                "        <portlet>\n" +
                "          <application-ref>portal</application-ref>\n" +
                "          <portlet-ref>HeaderPortlet</portlet-ref>\n" +
                "        </portlet>\n" +
                "        <access-permissions>Everyone</access-permissions>\n" +
                "        <show-info-bar>false</show-info-bar>\n" +
                "        <show-application-state>false</show-application-state>\n" +
                "        <show-application-mode>false</show-application-mode>\n" +
                "      </portlet-application>\n" +
                "    </container>\n" +
                "        <container>\n" +
                "      <name>footer</name>\n" +
                "      <portlet-application>\n" +
                "        <portlet>\n" +
                "          <application-ref>portal</application-ref>\n" +
                "          <portlet-ref>FooterPortlet</portlet-ref>\n" +
                "        </portlet>\n" +
                "        <access-permissions>Everyone</access-permissions>\n" +
                "        <show-info-bar>false</show-info-bar>\n" +
                "        <show-application-state>false</show-application-state>\n" +
                "        <show-application-mode>false</show-application-mode>\n" +
                "      </portlet-application>\n" +
                "    </container>\n" +
                "<page-body></page-body>\n" +
                "  </portal-layout>\n" +
                "</portal-config>\n";

        portal.add(new StringAsset(xml), path);
        PortletDescriptor descriptor = portletXML();
        descriptor = descriptor(descriptor, HeaderPortlet.class).up();
        descriptor = descriptor(descriptor, Portlet1.class).up();
        descriptor = descriptor(descriptor, FooterPortlet.class).up();
        portal.addAsWebInfResource(new StringAsset(descriptor.exportAsString()), "portlet.xml");

        return portal;
    }

    @ArquillianResource
    URL deploymentURL;

    @Drone
    WebDriver driver;

    /** . */
    private static Portlet header, body, footer;

    // @Test
    public void testHeader() {

        header = new GenericPortlet() {
            @Override
            public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
                response.setContentType("text/html");
                PrintWriter writer = response.getWriter();
                writer.append("<span id='header'>HEADER</span>");
                writer.close();
            }
        };
        body = new GenericPortlet() {
            @Override
            public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
                response.setContentType("text/html");
                PrintWriter writer = response.getWriter();
                writer.append("<span id='body'>BODY</span>");
                writer.close();
            }
        };
        footer = new GenericPortlet() {
            @Override
            public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
                response.setContentType("text/html");
                PrintWriter writer = response.getWriter();
                writer.append("<span id='footer'>FOOTER</span>");
                writer.close();
            }
        };

        //
        driver.get(deploymentURL.toString() + "/page1");
        Assert.assertNotNull(driver.findElement(By.id("header")));
        Assert.assertNotNull(driver.findElement(By.id("body")));
        Assert.assertNotNull(driver.findElement(By.id("footer")));
    }

    @Test
    public void testURL() {

        class HeaderPortlet extends GenericPortlet {
            String foo;
            public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
                foo = request.getParameter("foo");
                response.setContentType("text/html");
                PrintWriter writer = response.getWriter();
                PortletURL url = response.createRenderURL();
                url.setParameter("foo", "foo_header");
                writer.append("<a id='header' href='").append(url.toString()).append("'>header</span>");
                writer.close();
            }
        }
        class BodyPortlet extends GenericPortlet {
            String foo;
            public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
                foo = request.getParameter("foo");
                response.setContentType("text/html");
                PrintWriter writer = response.getWriter();
                PortletURL url = response.createRenderURL();
                url.setParameter("foo", "foo_body");
                writer.append("<a id='body' href='").append(url.toString()).append("'>body</span>");
                writer.close();
            }
        }

        //
        HeaderPortlet header = new HeaderPortlet();
        BodyPortlet body = new BodyPortlet();
        SiteLayoutTestCase.header = header;
        SiteLayoutTestCase.body = body;

        //
        driver.get(deploymentURL.toString() + "/page1");
        Assert.assertEquals(null, header.foo);
        Assert.assertEquals(null, body.foo);

        //
        driver.findElement(By.id("body")).click();
        Assert.assertEquals(null, header.foo);
        Assert.assertEquals("foo_body", body.foo);

        //
        driver.findElement(By.id("header")).click();
        Assert.assertEquals("foo_header", header.foo);
        Assert.assertEquals("foo_body", body.foo);
    }

    public void testAction() {
        driver.get(deploymentURL.toString() + "/page1");
    }

    public static class HeaderPortlet extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            if (header != null) {
                header.render(request, response);
            }
        }
    }

    public static class Portlet1 extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            if (body != null) {
                body.render(request, response);
            }
        }
    }

    public static class FooterPortlet extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            if (footer != null) {
                footer.render(request, response);
            }
        }
    }
}
