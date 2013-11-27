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
package org.gatein.portal.arquillian.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.gatein.common.xml.XMLTools;
import org.gatein.portal.arquillian.api.Page;
import org.gatein.portal.arquillian.api.PortalTest;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webfragment30.WebFragmentDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class GateInDeploymentEnricher implements ApplicationArchiveProcessor {

    public static final String PORTAL_PATH = "/";
    private static final String PORTAL_SERVLET_NAME = "PortalServlet";
    private static final String PORTAL_SERVLET_CLASS = "juzu.bridge.servlet.JuzuServlet";
    private static final String ASSET_PATH = "/assets/*";
    private static final String ASSET_SERVLET_NAME = "AssetServlet";
    private static final String ASSET_SERVLET_CLASS = "juzu.impl.asset.AssetServlet";
    private static final String KERNEL_FILTER_NAME = "KernelLifeCycle";
    private static final String KERNEL_FILTER_CLASS = "org.gatein.portal.common.kernel.KernelLifeCycle";
    private static final String CLIENT_REQUEST_FILTER_NAME = "ClientRequestFilter";
    private static final String CLIENT_REQUEST_FILTER_CLASS = "org.gatein.portal.web.servlet.ClientRequestFilter";
    private static final ArchivePath WEB_XML_PATH = ArchivePaths.create("WEB-INF/web.xml");

    /**
     * @see org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor#process(org.jboss.shrinkwrap.api.Archive, org.jboss.arquillian.test.spi.TestClass)
     */
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        PortalTest portalTest = testClass.getAnnotation(PortalTest.class);
        if (portalTest != null) {
            if (applicationArchive instanceof WebArchive) {
                WebArchive webArchive = (WebArchive) applicationArchive;

                /*
                // Add GateIn PC Embed and Deps to WEB-INF/lib
                File[] files = Maven.resolver()
                                        .loadPomFromFile("pom.xml")
                                        .resolve("org.gatein.pc:pc-embed")
                                        .withTransitivity()
                                        .as(File.class);
                webArchive.addAsLibraries(files);

                files = Maven.resolver()
                                 .loadPomFromFile("pom.xml")
                                 .resolve("javax.portlet:portlet-api")
                                 .withoutTransitivity()
                                 .as(File.class);
                webArchive.addAsLibraries(files);
                */

                // Add EmbedServlet to web.xml
                try {
                    addEmbed(webArchive, testClass, portalTest);
                } catch (IOException e) {
                    throw new RuntimeException("Could not generated descriptors", e);
                } catch (SAXException e) {
                    throw new RuntimeException("Could not generated descriptors", e);
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException("Could not generated descriptors", e);
                } catch (TransformerException e) {
                    throw new RuntimeException("Could not generated descriptors", e);
                }
            }
        }
    }

    private void addEmbed(WebArchive webArchive, TestClass testClass, PortalTest portalTest) throws
            IOException, SAXException, ParserConfigurationException, TransformerException {
        Node webXmlNode = webArchive.get(WEB_XML_PATH);
        if (null != webXmlNode) {
            WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(webXmlNode.getAsset().openStream());
            webXml.displayName("portal");
            webArchive.delete(WEB_XML_PATH);
            webArchive.setWebXML(new StringAsset(addEmbedToDescriptor(webXml).exportAsString()));
        } else {
            WebFragmentDescriptor webFrag = Descriptors.create(WebFragmentDescriptor.class);
            webFrag.displayName("portal");
            JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
            String fragment = addEmbedToFragment(webFrag).exportAsString();
            jar.addAsManifestResource(new StringAsset(fragment), "web-fragment.xml");
            webArchive.addAsLibrary(jar);
        }

        //
        String pagesXML =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<page-set\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://www.gatein.org/xml/ns/gatein_objects_2_0 http://www.gatein.org/xml/ns/gatein_objects_2_0\"\n" +
                "    xmlns=\"http://www.gatein.org/xml/ns/gatein_objects_2_0\">" +
                "</page-set>";
        String navigationXML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<navigation\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://www.gatein.org/xml/ns/gatein_objects_2_0 http://www.gatein.org/xml/ns/gatein_objects_2_0\"\n" +
                "    xmlns=\"http://www.gatein.org/xml/ns/gatein_objects_2_0\">\n" +
                "  <node>\n" +
                "  </node>\n" +
                "</navigation>\n";
        Document pagesDoc = XMLTools.toDocument(pagesXML);
        Document navigationDoc = XMLTools.toDocument(navigationXML);
        Element pageSetElt = pagesDoc.getDocumentElement();
        Element rootNodeElt = (Element) navigationDoc.getDocumentElement().getElementsByTagName("node").item(0);

        //
        Class<?> testJavaClass = testClass.getJavaClass();
        for (Page page : portalTest.value()) {
            String resource = page.value();
            InputStream in = testJavaClass.getResourceAsStream(resource);
            if (in == null) {
                throw new RuntimeException("Could not resolve page descriptor " + resource + " for test " + testJavaClass);
            } else {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document pageDOM = builder.parse(in);
                Element importedElt = (Element) pagesDoc.importNode(pageDOM.getDocumentElement(), true);
                importedElt = (Element) pageSetElt.appendChild(importedElt);
                List<Element> nameElts = XMLTools.getChildren(importedElt, "name");
                if (nameElts.size() != 1) {
                    throw new RuntimeException("The page " + resource + " does not have a name");
                } else {
                    Element nameElt = nameElts.get(0);
                    String name = XMLTools.asString(nameElt);
                    Element nodeElt = (Element) rootNodeElt.appendChild(navigationDoc.createElement("node"));
                    Element nodeNameElt = (Element) nodeElt.appendChild(navigationDoc.createElement("name"));
                    nodeNameElt.appendChild(navigationDoc.createTextNode(name));
                    Element nodePageRefElt = (Element) nodeElt.appendChild(navigationDoc.createElement("page-ref"));
                    nodePageRefElt.appendChild(navigationDoc.createTextNode("portal::classic::" + name));
                }
            }
        }

        // Now write the generated descriptors in the archive
        webArchive.addAsWebInfResource(new StringAsset(XMLTools.toString(pagesDoc)), "conf/portal/portal/classic/pages.xml");
        webArchive.addAsWebInfResource(new StringAsset(XMLTools.toString(navigationDoc)), "conf/portal/portal/classic/navigation.xml");

        // Add other configuration descriptors
        String[] resources = {
                "conf/configuration.xml",
                "conf/sso/oauth-configuration.xml",
                "conf/sso/security-sso-configuration.xml",
                "conf/portal/configuration.xml",
                "conf/portal/portal/sharedlayout.xml",
                "conf/portal/portal/classic/portal.xml"};
        for (String resource : resources) {
            URL resourceURL = GateInDeploymentEnricher.class.getResource(resource);
            webArchive.addAsWebInfResource(resourceURL, resource);
        }

        // Remove that later, for now it is fine
        String contextXML =
                "<Context privileged=\"true\">\n" +
                "  <Realm className='org.apache.catalina.realm.JAASRealm'\n" +
                "         appName='gatein-domain'\n" +
                "         userClassNames='org.exoplatform.services.security.jaas.UserPrincipal'\n" +
                "         roleClassNames='org.exoplatform.services.security.jaas.RolePrincipal'/>\n" +
                "  <Valve\n" +
                "      className='org.apache.catalina.authenticator.FormAuthenticator'\n" +
                "      characterEncoding='UTF-8'/>\n" +
                "  <!--org.gatein.portal.jaas.BytesLoungeLoginModule required debug=true;-->\n" +
                "\n" +
                "  <!--<Valve className=\"org.gatein.sso.agent.tomcat.ServletAccessValve\" />-->\n" +
                "</Context>";
        webArchive.addAsManifestResource(new StringAsset(contextXML), "context.xml");
    }

    private WebFragmentDescriptor addEmbedToFragment(WebFragmentDescriptor webFragment) {
        webFragment.createContextParam()
                        .paramName("juzu.run_mode")
                        .paramValue("dev")
                        .up()
                    .createContextParam()
                        .paramName("juzu.inject")
                        .paramValue("guice")
                        .up();

        // Remove that later, for now it's fine
        webFragment.createServlet()
                        .servletName("ContainerServlet")
                        .servletClass("org.gatein.wci.tomcat.TC7ContainerServlet")
                        .loadOnStartup(0)
                        .up();

        webFragment.createServlet()
                        .servletName(PORTAL_SERVLET_NAME)
                        .servletClass(PORTAL_SERVLET_CLASS)
                        .createInitParam()
                            .paramName("juzu.app_name")
                            .paramValue("org.gatein.portal.web")
                            .up()
                        .asyncSupported(true)
                        .up()
                    .createServletMapping()
                        .servletName(PORTAL_SERVLET_NAME)
                        .urlPattern(PORTAL_PATH)
                        .up();
        webFragment.createServlet()
                        .servletName(ASSET_SERVLET_NAME)
                        .servletClass(ASSET_SERVLET_CLASS)
                        .loadOnStartup(0)
                        .up()
                   .createServletMapping()
                        .servletName(ASSET_SERVLET_NAME)
                        .urlPattern(ASSET_PATH)
                        .up();
        webFragment.createFilter()
                        .filterName(KERNEL_FILTER_NAME)
                        .filterClass(KERNEL_FILTER_CLASS)
                        .asyncSupported(true)
                        .up()
                   .createFilterMapping()
                        .filterName(KERNEL_FILTER_NAME)
                        .servletName(PORTAL_SERVLET_NAME)
                        .up();
        webFragment.createFilter()
                   .filterName(CLIENT_REQUEST_FILTER_NAME)
                        .filterClass(CLIENT_REQUEST_FILTER_CLASS)
                        .asyncSupported(true)
                        .up()
                   .createFilterMapping()
                        .filterName(CLIENT_REQUEST_FILTER_NAME)
                        .servletName(PORTAL_SERVLET_NAME)
                        .up();
        return webFragment;
    }

    private WebAppDescriptor addEmbedToDescriptor(WebAppDescriptor webXml) {
        webXml.createServlet()
                  .servletName(PORTAL_SERVLET_NAME)
                  .servletClass(PORTAL_SERVLET_CLASS)
                  .asyncSupported(true)
                  .up()
              .createServletMapping()
                  .servletName(PORTAL_SERVLET_NAME)
                  .urlPattern(PORTAL_PATH)
                  .up();
        webXml.createServlet()
                  .servletName(ASSET_SERVLET_NAME)
                  .servletClass(ASSET_SERVLET_CLASS)
                  .loadOnStartup(0)
                  .up()
              .createServletMapping()
                  .servletName(ASSET_SERVLET_NAME)
                  .urlPattern(ASSET_PATH)
                  .up();
        webXml.createFilter()
              .filterName(KERNEL_FILTER_NAME)
                  .filterClass(KERNEL_FILTER_CLASS)
                  .asyncSupported(true)
                  .up()
              .createFilterMapping()
                  .filterName(KERNEL_FILTER_NAME)
                  .servletName(PORTAL_SERVLET_NAME)
                  .up();
        webXml.createFilter()
              .filterName(CLIENT_REQUEST_FILTER_NAME)
                  .filterClass(CLIENT_REQUEST_FILTER_CLASS)
                  .asyncSupported(true)
                  .up()
              .createFilterMapping()
                  .filterName(CLIENT_REQUEST_FILTER_NAME)
                  .servletName(PORTAL_SERVLET_NAME)
                  .up();
        return webXml;
    }
}
