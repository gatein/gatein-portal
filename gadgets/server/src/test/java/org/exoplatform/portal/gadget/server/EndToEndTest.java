/**
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
package org.exoplatform.portal.gadget.server;

import org.apache.shindig.auth.BasicSecurityToken;
import org.apache.shindig.auth.BasicSecurityTokenCodec;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.exoplatform.portal.AbstractPortalTest;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Base class for end-to-end tests.
 *
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class EndToEndTest extends AbstractPortalTest
{
   private static final String[] EXPECTED_RESOURCES = {"hello.xml", "coreFeatures.xml", "testframework.js"};

   static private EndToEndServer server = null;

   private WebClient webClient;

   private CollectingAlertHandler alertHandler;

   private SecurityToken token;

   private String language;

   public void testCheckResources() throws Exception
   {
      for (String resource : EXPECTED_RESOURCES)
      {
         String url = EndToEndServer.SERVER_URL + '/' + resource;
         Page p = webClient.getPage(url);
         assertEquals("Failed to load test resource " + url, 200, p.getWebResponse().getStatusCode());
      }
   }

   public void testHelloWorld() throws Exception
   {
      HtmlPage page = executeAllPageTests("hello");
      final String pageAsText = page.asText();
      assertTrue(pageAsText.contains("Hello, world!"));
   }

   public void testCoreFeature() throws Exception
   {
      HtmlPage page = executeAllPageTests("coreFeatures");
      final String pageAsText = page.asText();
      assertTrue(pageAsText.contains("Core Features Test Cases!"));
   }

   @Override
   public void tearDown() throws Exception
   {
      server.stop();
   }

   @Override
   public void setUp() throws Exception
   {
      server = new EndToEndServer();
      server.start();

      webClient = new WebClient();
      // NicelyResynchronizingAjaxController changes XHR calls from asynchronous
      // to synchronous, saving the test from needing to wait or sleep for XHR
      // completion.
      webClient.setAjaxController(new NicelyResynchronizingAjaxController());
      webClient.waitForBackgroundJavaScript(2000);
      webClient.setHTMLParserListener(HTMLParserListener.LOG_REPORTER);
      webClient.setTimeout(3000);

      alertHandler = new CollectingAlertHandler();
      webClient.setAlertHandler(alertHandler);
      token = createToken("canonical", "john.doe");
      language = null;
      server.clearDataServiceError();
   }

   /**
    * Executes a page test by loading the HTML page.
    * @param testName name of the test, which must match a gadget XML file
    *     name in test/resources/endtoend (minus .xml).
    * @param testMethod name of the javascript method to execute
    * @return the parsed HTML page
    */
   private HtmlPage executePageTest(String testName, String testMethod, boolean caja) throws IOException
   {
      if (!testName.endsWith(".xml"))
      {
         testName = testName + ".xml";
      }

      String gadgetUrl = EndToEndServer.SERVER_URL + '/' + testName;
      String url = EndToEndServer.GADGET_BASEURL + "?url=" + URLEncoder.encode(gadgetUrl, "UTF-8");
      BasicSecurityTokenCodec codec = new BasicSecurityTokenCodec();
      url += "&st=" + URLEncoder.encode(codec.encodeToken(token), "UTF-8");
      if (testMethod != null)
      {
         url += "&testMethod=" + URLEncoder.encode(testMethod, "UTF-8");
      }
      if (caja)
      {
         url += "&caja=1&libs=caja";
      }

      url += "&nocache=1";
      if (language != null)
      {
         url += "&lang=" + language;
      }
      Page page = webClient.getPage(url);
      if (!(page instanceof HtmlPage))
      {
         fail("Got wrong page type. Was: " + page.getWebResponse().getContentType());
      }
      webClient.waitForBackgroundJavaScript(3000);
      return (HtmlPage)page;
   }

   /**
    * Executes all page test in a single XML file.
    * @param testName name of the test, which must match a gadget XML file
    *     name in test/resources/endtoend (minus .xml).
    * @throws IOException
    */
   private HtmlPage executeAllPageTests(String testName) throws IOException
   {
      return executePageTest(testName, "all", false);
   }

   private BasicSecurityToken createToken(String owner, String viewer) throws BlobCrypterException
   {
      return new BasicSecurityToken(owner, viewer, "test", "domain", "appUrl", "1", "default", null, null);
   }
}