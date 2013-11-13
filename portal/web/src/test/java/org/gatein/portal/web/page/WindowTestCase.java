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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.gatein.portal.web.AbstractPortalTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.portletapp20.PortletDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Julien Viet
 */
@RunWith(Arquillian.class)
public class WindowTestCase extends AbstractPortalTestCase {


    @Deployment(testable = false)
    public static WebArchive createPortal() {
        WebArchive portal = AbstractPortalTestCase.createPortal();
        PortletDescriptor desc = descriptor(
                Portlet1.class,
                Portlet2.class,
                Portlet3.class,
                Portlet4.class
        );
        desc = desc.getAllPortlet().get(0).supportedPublicRenderParameter("color").up();
        desc = desc.getOrCreatePublicRenderParameter().identifier("color").name("color").up();
        portal.addAsWebInfResource(new StringAsset(desc.exportAsString()), "portlet.xml");
        return portal;
    }

    @ArquillianResource
    URL deploymentURL;

    @Test
    public void testRender() throws Exception {
        URIBuilder uri = new URIBuilder(deploymentURL.toString() + "window/portlet/portal/Portlet1").
                addParameter("javax.portlet.url", deploymentURL + "mypage");
        JSON json = assertRender(uri.toString());
        assertEquals("Hello", json.get("title"));
        assertEquals("color=null", json.get("content"));
        assertNotNull(json.get("name"));

        //
        uri = new URIBuilder(deploymentURL.toString() + "window/portlet/portal/Portlet1").
                addParameter("javax.portlet.url", deploymentURL + "mypage?color=red");
        json = assertRender(uri.toString());
        assertEquals("Hello", json.get("title"));
        assertEquals("color=red", json.get("content"));
        assertNotNull(json.get("name"));
    }

    @Test
    public void testRenderURL() throws Exception {
        URIBuilder uri = new URIBuilder(deploymentURL.toString() + "window/portlet/portal/Portlet2").
                addParameter("javax.portlet.url", deploymentURL + "mypage?color=blue");
        JSON json = assertRender(uri.toString());
        assertEquals("Hello", json.get("title"));
        URIBuilder content = new URIBuilder(json.getString("content").replace("&amp;", "&"));
        assertEquals("/portal/mypage", content.getPath());
        Map<String, String> parameters = parameterMap(content);
        assertEquals("blue", parameters.remove("color"));
        assertEquals(Collections.<String, String>emptyMap(), parameters);
    }

    @Test
    public void testActionURL() throws Exception {
        URIBuilder uri = new URIBuilder(deploymentURL.toString() + "window/portlet/portal/Portlet3").
                addParameter("javax.portlet.url", deploymentURL + "mypage");
        JSON json = assertRender(uri.toString());
        assertEquals("Hello", json.get("title"));
        String name = json.getString("name");
        assertNotNull(name);
        URIBuilder content = new URIBuilder(json.getString("content").replace("&amp;", "&"));
        Map<String, String> parameters = parameterMap(content);
        assertEquals("/portal/mypage", content.getPath());
        assertEquals("action", parameters.remove("javax.portlet.a"));
        assertEquals(name, parameters.remove("javax.portlet.t"));
        assertEquals("action_param_value", parameters.remove("action_param"));
        assertEquals(Collections.<String, String>emptyMap(), parameters);
    }

    @Test
    public void testResourceURL() throws Exception {
        URIBuilder uri = new URIBuilder(deploymentURL.toString() + "window/portlet/portal/Portlet4").
                addParameter("javax.portlet.url", deploymentURL + "mypage");
        JSON json = assertRender(uri.toString());
        assertEquals("Hello", json.get("title"));
        String name = json.getString("name");
        assertNotNull(name);
        URIBuilder content = new URIBuilder(json.getString("content").replace("&amp;", "&"));
        Map<String, String> parameters = parameterMap(content);
        assertEquals("/portal/mypage", content.getPath());
        assertEquals("resource", parameters.remove("javax.portlet.a"));
        assertEquals("the_id", parameters.remove("javax.portlet.r"));
        assertEquals("", parameters.remove("javax.portlet.p"));
        String target = parameters.remove("javax.portlet.t");
        assertEquals(name, target);
        assertEquals("", parameters.remove("javax.portlet.p." + target));
        assertEquals("resource_param_value", parameters.remove("resource_param"));
        assertEquals(Collections.<String, String>emptyMap(), parameters);
    }

    private JSON assertRender(String uri) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(uri);
        HttpResponse response = client.execute(get);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", response.getEntity().getContentType().getElements()[0].getName());
        return (JSON) JSON.parse(Tools.read(response.getEntity().getContent()));
    }

    public static class Portlet1 extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            String color = request.getParameter("color");
            response.getWriter().append("color=").append(color).close();
        }
    }

    public static class Portlet2 extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            PortletURL url = response.createRenderURL();
            response.getWriter().append(url.toString()).close();
        }
    }

    public static class Portlet3 extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            PortletURL url = response.createActionURL();
            url.setParameter("action_param", "action_param_value");
            response.getWriter().append(url.toString()).close();
        }
    }

    public static class Portlet4 extends GenericPortlet {
        @Override
        public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
            response.setContentType("text/html");
            ResourceURL url = response.createResourceURL();
            url.setResourceID("the_id");
            url.setParameter("resource_param", "resource_param_value");
            response.getWriter().append(url.toString()).close();
        }
    }
}
