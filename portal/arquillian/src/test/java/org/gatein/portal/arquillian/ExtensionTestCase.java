/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gatein.portal.arquillian;

import java.net.URL;

import juzu.impl.common.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gatein.portal.arquillian.api.Page;
import org.gatein.portal.arquillian.api.PortalTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Julien Viet
 */
@RunWith(Arquillian.class)
@PortalTest(@Page("mypage.xml"))
public class ExtensionTestCase {

    @Deployment
    public static WebArchive getDeployment() {

        String portletXML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<portlet-app xmlns=\"http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd\" version=\"2.0\"\n" +
                "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd\n" +
                "   http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd\">\n" +
                "  <portlet>\n" +
                "    <portlet-name>MyPortlet</portlet-name>\n" +
                "    <portlet-class>org.gatein.portal.arquillian.MyPortlet</portlet-class>\n" +
                "    <supports>\n" +
                "      <mime-type>text/html</mime-type>\n" +
                "      <portlet-mode>view</portlet-mode>\n" +
                "    </supports>\n" +
                "    <portlet-info>\n" +
                "      <title>My Portlet</title>\n" +
                "    </portlet-info>\n" +
                "  </portlet>\n" +
                "</portlet-app>\n";

        //
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "portal.war");
        archive.addAsWebInfResource(new StringAsset(portletXML), "portlet.xml");

        //
        return archive;
    }

    @ArquillianResource
    URL url;

    @Test
    @RunAsClient
    public void testFoo() throws Exception {
        HttpGet get = new HttpGet(url.toURI().resolve("./mypage"));
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(get);
        String s = Tools.read(response.getEntity().getContent());
        Assert.assertTrue(s.contains("HELLO WORLD"));
    }
}
