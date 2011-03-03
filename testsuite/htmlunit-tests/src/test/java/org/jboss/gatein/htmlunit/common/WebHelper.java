/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.gatein.htmlunit.common;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.testng.Assert;

import java.io.IOException;

/**
 * WebHelper class contains helper methods to easily perform GateIn portal operations
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class WebHelper
{
   /** HTMLUnit Webclient represents a web session */
   private WebClient webClient;

   /** Host to use for web session */
   private String host = "localhost";

   /** Port to use for web session */
   private int port = 8080;

   /** Portal container to use for web session */
   private String portalContainer = "portal";

   /** Time to wait (in seconds) for element or text to appear in the page */
   private int waitTimeout = 20;

   /** Short pause period (in seconds)*/
   private int shortPause = 3;

   /** Current page */
   private HtmlPage page;


   /** Get HTMLUnit WebClient instance representing the current session */
   public WebClient getWebClient()
   {
      if (webClient == null)
      {
         webClient = new WebClient();
         webClient.setThrowExceptionOnFailingStatusCode(false);
         webClient.setThrowExceptionOnScriptError(false);

         initFromSystemProps();
      }
      return webClient;
   }

   private void initFromSystemProps()
   {
      String val = System.getProperty("test.host");
      if (val != null)
         host = val;

      val = System.getProperty("test.port");
      if (val != null)
         port = Integer.parseInt(val);

      val = System.getProperty("test.portalContainer");
      if (val != null)
         portalContainer = val;

      val = System.getProperty("test.waitTimeout");
      if (val != null)
         waitTimeout = Integer.parseInt(val);

      val = System.getProperty("test.shortPause");
      if (val != null)
         shortPause = Integer.parseInt(val);
   }

   /**
    * Open GateIn's home page.
    *
    * @param publicMode if true 'public' portal page is used, otherwise 'private', which requires signing in
    * @throws IOException
    */
   public void openPortal(boolean publicMode) throws IOException
   {
      System.out.println("--Open portal home--");
      page = getWebClient().getPage(getStartUrl(publicMode));
      HtmlElement desc = page.getFirstByXPath(".//div[contains(text(), 'All rights reserved')]");
      if (desc == null)
         throw new RuntimeException("Portal page content corrupted\r\n" + page.asText());
   }

   private String getStartUrl(boolean publicMode)
   {
      return "http://" + host + ":" + port + "/" + portalContainer + "/" + (publicMode ? "public" : "private");
   }

   public void signInAsRoot()
   {
      signIn("root", "gtn");
   }

   public void signIn(String user, String pass)
   {
      System.out.println("--Sign in as " + user + "--");
      waitForElementPresent("link=Sign in");
      click("link=Sign in");
      waitForElementPresent("username");
      type("username", user);
      type("password", pass);
      click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
      //waitForPageToLoad(timeout);
   }

   /**
    * Fill form field with text
    *
    * @param field
    * @param value
    */
   public void type(String field, String value)
   {
      HtmlElement textField = getElement(field);
      if (textField instanceof HtmlInput == false)
         throw new RuntimeException("Element not text input: " + textField);

      ((HtmlInput) textField).setValueAttribute(value);
   }

   /**
    * Simulate clicking an element in the page
    *
    * @param el expression identifying an element
    */
   public void click(String el)
   {
      HtmlElement node = getElement(el);
      try
      {
         page = node.click();
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Click failed [" + el + "]" , ex);
      }
   }

   public void waitForElementPresent(String el)
   {
      for (int second = 0; ; second++)
      {
         if (second >= waitTimeout)
         {
            Assert.fail("Timeout at waitForElementPresent: " + el);
         }
         if (isElementPresent(el))
         {
            break;
         }
         pause(1000);
      }
   }

   public boolean isElementPresent(String el)
   {
      HtmlElement node = getElement(el);
      return node != null;
   }

   public HtmlElement getElement(String el)
   {
      if (el.startsWith("link="))
      {
         String xpath = convertFromLinkElSpec(el);
         return page.getFirstByXPath(xpath);
      }
      else if (el.startsWith("//"))
      {
         return page.getFirstByXPath(el);
      }
      else
      {
         return page.getFirstByXPath(".//node()[@name='" + el + "' or @id='" + el + "']");
      }
   }

   private String convertFromLinkElSpec(String el)
   {
      if (el.startsWith("link="))
         el = el.substring(5);
      return ".//a[text() = '" + el + "']";
   }

   public void waitForTextPresent(String text)
   {
      for (int second = 0; ; second++)
      {
         if (second >= waitTimeout)
         {
            //System.out.println("[DEBUG] " + page.asXml());
            Assert.fail("Timeout at waitForTextPresent: " + text);
         }
         if (isTextPresent(text))
         {
            break;
         }
         pause(1000);
      }
   }

   public void waitForTextNotPresent(String text)
   {
      for (int second = 0; ; second++)
      {
         if (second >= waitTimeout)
         {
            Assert.fail("Timeout at waitForTextPresent: " + text);
         }
         if (isTextPresent(text) == false)
         {
            break;
         }
         pause(1000);
      }
   }

   public boolean isTextPresent(String text)
   {
      return page.asText().indexOf(text) > -1;
   }

   public void shortPause()
   {
      pause(shortPause * 1000);
   }

   public static void pause(long millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException("Interrupted!");
      }
   }

   /**
    * Simulate dragging source element onto a target element
    *
    * @param sourceEl source element specification
    * @param targetEl target element specification
    */
   public void dragAndDropToObject(String sourceEl, String targetEl)
   {
      System.out.println("--Drag and drop to object--");
      HtmlElement src = getElement(sourceEl);
      if (src == null)
         throw new RuntimeException("No source element: " + sourceEl);

      HtmlElement target = getElement(targetEl);
      if (target == null)
         throw new RuntimeException("No target element: " + targetEl);

      src.mouseDown();
      src.mouseMove();
      target.mouseMove();
      target.mouseUp();
   }

   /**
    * Simulate clicking Finish inside Page Editor
    */
   public void finishPageEdit()
   {
      System.out.println("--Finish Page Edit--");
      waitForElementPresent("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]");
      click("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]");
      waitForTextNotPresent("Page Editor");
   }

   /**
    * Add a new page with the specified name, containing one specific portlet.
    *
    * @param categoryTitle application registry category title that contains the portlet to use
    * @param portletName portlet name in application registry
    * @param pageName name of the new page - used for both navigation node, and page title
    * @param portletElementToDnD - element specification pointing to the div that is dragged onto
    * the content placeholder within the new page
    */
   public void addNewPageUpToFinish(String categoryTitle, String portletName, String pageName, String portletElementToDnD)
   {
      System.out.println("--Add new page up to Finish--");

      waitForElementPresent("link=Add New Page");
      click("link=Add New Page");

      waitForElementPresent("pageName");
      type("pageName", pageName);
      type("pageDisplayName", pageName);
      click("//table[@class='ActionContainer']/tbody/tr/td/div[2]");
      waitForTextPresent("Empty Layout");
      click("//table[@class='ActionContainer']/tbody/tr/td/div[2]");
      if (categoryTitle != null & portletName != null & portletElementToDnD != null)
      {
         waitForElementPresent("//div[contains(@class, 'Tab')]/a[@title='" + categoryTitle + "']");
         click("//div[contains(@class, 'Tab')]/a[@title='" + categoryTitle + "']");
         waitForTextPresent(portletName);
         shortPause();
         dragAndDropToObject(portletElementToDnD, "//div[@class='UIComponentBlock']");
         shortPause();
      }
   }

}
