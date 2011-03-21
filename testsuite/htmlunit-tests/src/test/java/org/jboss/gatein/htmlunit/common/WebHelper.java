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

import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.log4testng.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

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

   /** Short pause period (in seconds) */
   private int shortPause = 3;

   /** Dump current page before throwing exception */
   private boolean dumpPageOnFail = false;

   /** Dump current page on exit */
   private boolean dumpPageOnExit = false;

   /** Properties to use for configuration overrides */
   private Properties props = System.getProperties();

   /** Current page */
   private HtmlPage page;


   /**
    * Get host property.
    *
    * @return value of host property
    */
   public String getHost()
   {
      return host;
   }

   /**
    * Get port property.
    *
    * @return value of port property
    */
   public int getPort()
   {
      return port;
   }

   /**
    * Get portalContainer property.
    *
    * @return value of portalContainer property
    */
   public String getPortalContainer()
   {
      return portalContainer;
   }

   /**
    * Get properties used for initialization.
    *
    * @return Properties
    */
   public Properties getProps()
   {
      return props;
   }

   /**
    * Get shortPause property.
    *
    * @return value of shortPause property
    */
   public int getShortPause()
   {
      return shortPause;
   }

   /**
    * Get waitTimeout property.
    *
    * @return value of waitTimeout property
    */
   public int getWaitTimeout()
   {
      return waitTimeout;
   }

   /**
    * Is dumpPageOnFail flag true.
    *
    * @return value of dumpPageOnFail property
    */
   public boolean isDumpPageOnFail()
   {
      return dumpPageOnFail;
   }

   /**
    * Is dumpPageOnExit flag true.
    *
    * @return value of dumpPageOnExit property
    */
   public boolean isDumpPageOnExit()
   {
      return dumpPageOnExit;
   }

   /**
    * Get HTMLUnit WebClient instance representing the current session.
    *
    * @return WebClient instance representing a browser window
    */
   public WebClient getWebClient()
   {
      if (webClient == null)
      {
         webClient = new WebClient();
         webClient.setThrowExceptionOnFailingStatusCode(false);
         webClient.setThrowExceptionOnScriptError(false);

/*
         //
         // all this seems to have no effect on logging
         //

         webClient.setHTMLParserListener(new HTMLParserListener()
         {
            public void error(String message, URL url, int line, int column, String key)
            {

            }

            public void warning(String message, URL url, int line, int column, String key)
            {

            }
         });
         webClient.setIncorrectnessListener(new IncorrectnessListener() {
            public void notify(String message, Object origin)
            {

            }
         });

         LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");

         System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "fatal");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
*/

         initFromProps();
      }
      return webClient;
   }

   /**
    * Get current page.
    *
    * @return current page
    */
   public HtmlPage getPage()
   {
      return page;
   }

   /**
    * Properties object containing configuration overrides.
    *
    * @return properties object
    */
   public Properties getProperties()
   {
      return props;
   }

   /**
    * Set configuration overrides.
    *
    * @param props Properties object containing configuration overrides
    */
   public void setProperties(Properties props)
   {
      if (props == null)
         throw new IllegalArgumentException("Null properties");
      this.props = props;
   }

   /**
    * Initialize from properties.
    */
   private void initFromProps()
   {
      String val = props.getProperty("test.host");
      if (val != null)
         host = val;

      val = props.getProperty("test.port");
      if (val != null)
         port = Integer.parseInt(val);

      val = props.getProperty("test.portalContainer");
      if (val != null)
         portalContainer = val;

      val = props.getProperty("test.waitTimeout");
      if (val != null)
         waitTimeout = Integer.parseInt(val);

      val = props.getProperty("test.shortPause");
      if (val != null)
         shortPause = Integer.parseInt(val);

      val = props.getProperty("test.dumpPageOnFail");
      if (val != null)
         dumpPageOnFail = Boolean.parseBoolean(val);

      val = props.getProperty("test.dumpPageOnExit");
      if (val != null)
         dumpPageOnExit = Boolean.parseBoolean(val);

      log("Initial properties:\n\thost: " + host
            + "\n\tport: " + port
            + "\n\tportalContainer: " + portalContainer
            + "\n\twaitTimeout: " + waitTimeout
            + "\n\tshortPause: " + shortPause
            + "\n\tdumpPageOnFail: " + dumpPageOnFail
            + "\n\tdumpPageOnExit: " + dumpPageOnExit);
   }

   /**
    * Open GateIn's home page.
    *
    * @param publicMode if true 'public' portal page is used, otherwise 'private', which requires signing in
    * @throws IOException
    */
   public void openPortal(boolean publicMode) throws IOException
   {
      log("--Open portal home--");
      page = getWebClient().getPage(getStartUrl(publicMode));
      HtmlElement desc = page.getFirstByXPath(".//div[contains(text(), 'All rights reserved')]");
      if (desc == null)
         throw new RuntimeException("Portal page content corrupted\r\n" + page.asText());
   }

   /**
    * Compose portal's home page url.
    *
    * @param publicMode
    * @return
    */
   private String getStartUrl(boolean publicMode)
   {
      return "http://" + host + ":" + port + "/" + portalContainer + "/" + (publicMode ? "public" : "private");
   }

   /**
    * Sign in as root.
    */
   public void signInAsRoot()
   {
      signIn("root", "gtn");
   }

   /**
    * Sign in with username and password.
    *
    * @param user username
    * @param pass password
    */
   public void signIn(String user, String pass)
   {
      log("--Sign in as " + user + "--");
      waitForElementPresent("link=Sign in");
      click("link=Sign in");
      waitForElementPresent("username");
      type("username", user);
      type("password", pass);
      click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
      //waitForPageToLoad(timeout);
   }

   /**
    * Fill form field with text.
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
    * Simulate clicking an element in the page.
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

   /**
    * Wait for element to be present in the current page,
    * for a maximum time determined by <tt>waitSeconds</tt> property.
    *
    * @param el element specification
    */
   public void waitForElementPresent(String el)
   {
      for (int second = 0; ; second++)
      {
         if (second >= waitTimeout)
         {
            if (dumpPageOnFail)
               dumpPage();
            Assert.fail("Timeout at waitForElementPresent: " + el);
         }
         if (isElementPresent(el))
         {
            break;
         }
         pause(1000);
      }
   }

   /**
    * Wait for element to not be present any more in the current page,
    * for a maximum time determined by <tt>waitSeconds</tt> property.
    *
    * @param el element specification
    */
   public void waitForElementNotPresent(String el)
   {
      for (int second = 0; ; second++)
      {
         if (second >= waitTimeout)
         {
            if (dumpPageOnFail)
               dumpPage();
            Assert.fail("Timeout at waitForElementPresent: " + el);
         }
         if (!isElementPresent(el))
         {
            break;
         }
         pause(1000);
      }
   }

   /**
    * Check if specified element is present in current page.
    *
    * @param el element specification
    * @return true if present
    */
   public boolean isElementPresent(String el)
   {
      HtmlElement node = getElement(el);
      return node != null;
   }

   /**
    * Get first occurence of specified element in current page.
    *
    * <p>
    * Element specification can have one of several forms:
    * <ul>
    *    <li>link=<em>link-text</em> - matches the first %lt;a&gt; element with specified link-text</li>
    *    <li>xpath=<em>xpath-query</em> - matches the first element that the specified xpath query returns</li>
    *    <li>//<em>xpath-query</em> - matches the first element that the specified xpath query returns</li>
    *    <li><em>name-id</em> - matches the first element with id or name attribute equal to <em>name-id</em></li>
    * </ul>
    *
    * @param el element specification
    * @return element if present, null otherwise
    */
   public HtmlElement getElement(String el)
   {
      // it seems that page state is often changing at the time we call getElement()
      // which causes concurrency issues and results in RuntimeExceptions
      RuntimeException e = null;
      for (int i=0; i<3; i++)
      {
         try
         {
            return _getElement(el);
         }
         catch (RuntimeException ex)
         {
            e = ex;
            try
            {
               Thread.sleep(1000);
            }
            catch(InterruptedException ex2)
            {
               throw new RuntimeException("Interrupted!");
            }
         }
      }
      throw e;
   }

   private HtmlElement _getElement(String el)
   {
      if (el.startsWith("link="))
      {
         String xpath = convertFromLinkElSpec(el);
         return page.getFirstByXPath(xpath);
      }
      else if (el.startsWith("xpath="))
      {
         String xpath = convertFromXPathElSpec(el);
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

   private String convertFromXPathElSpec(String el)
   {
      if (el.startsWith("xpath="))
         el = el.substring(6);
      return el;
   }

   private String convertFromLinkElSpec(String el)
   {
      if (el.startsWith("link="))
         el = el.substring(5);
      return ".//a[text() = '" + el + "']";
   }

   public String waitForTextPresent(String ... text)
   {
      for (int second = 0; ; second++)
      {
         if (second >= waitTimeout)
         {
            if (dumpPageOnFail)
               dumpPage();
            Assert.fail("Timeout at waitForTextPresent: " + Arrays.asList(text));
         }
         String found = getTextPresent(text);
         if (found != null)
         {
            return found;
         }
         pause(1000);
      }
   }

   /**
    * Dump the current page as XML
    */
   public void dumpPage()
   {
      log("Page dump: " + page.asXml());
   }

   /**
    * Wait for text to not be present any more in the current page,
    * for a maximum time determined by <tt>waitSeconds</tt> property.
    *
    * @param text text to not be present any more
    */
   public void waitForTextNotPresent(String text)
   {
      for (int second = 0; ; second++)
      {
         if (second >= waitTimeout)
         {
            if (dumpPageOnFail)
               dumpPage();
            Assert.fail("Timeout at waitForTextPresent: " + text);
         }
         if (isTextPresent(text) == false)
         {
            break;
         }
         pause(1000);
      }
   }

   /**
    * Check if specified text is present
    *
    * @param text
    * @return true if text is present
    */
   public boolean isTextPresent(String text)
   {
      try
      {
         return page.asText().indexOf(text) > -1;
      }
      catch (Exception e)
      {
         shortPause();
         return page.asText().indexOf(text) > -1;
      }
   }

   /**
    * Check if any of the passed arguments is present as text in the page.
    * When any is encountered it is returned.
    *
    * @param text string items to check for presence
    * @return found item or null of none of the items is present
    */
   public String getTextPresent(String ... text)
   {
      String pageText;

      try
      {
         pageText = page.asText();
      }
      catch (Exception e)
      {
         shortPause();
         pageText = page.asText();
      }

      for (String item: text)
      {
         if (pageText.indexOf(item) != -1)
            return item;
      }

      return null;
   }

   /**
    * Pause for a few seconds - length controlled by shortPause property
    */
   public void shortPause()
   {
      pause(shortPause * 1000);
   }

   /**
    * Pause for a specified number of millis
    *
    * @param millis
    */
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
      log("--Drag and drop to object--");
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
      log("--Finish Page Edit--");
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
      log("--Add new page up to Finish--");

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

   public void goToPageManagement()
   {
      log("--Go to Page Management--");
      waitForElementPresent("link=Page Management");
      click("link=Page Management");
      //waitForPageToLoad();  // no need for this - we're synchronous
   }

   public void searchAndDeletePage(String title)
   {
      searchPageByTitle(title, null);
      deletePage(title, false, null);
   }

   public void setupConfirmation(final String text)
   {
      webClient.setConfirmHandler(new ConfirmHandler()
      {
         public boolean handleConfirm(Page page, String message)
         {
            if (text.equals(message))
            {
               return true;
            }
            else
               throw new RuntimeException("Unexpected message: " + message);
         }
      });
   }

   public void resetConfirmation()
   {
      webClient.setConfirmHandler(null);
   }

   private void deletePage(String title, boolean closeDialog, String verifyText)
   {
      log("--Delete page: " + title + "--");
      String delButton = "//tbody[@class='FeedBox']//tr[*/div[@title='" + title + "']]//img[@title='Delete Page' and contains(@onclick, 'op=Delete')]";
      waitForElementPresent(delButton);

      setupConfirmation("Do you want to delete this page?");
      click(delButton);
      resetConfirmation();

      if (verifyText != null)
      {
         waitForTextNotPresent(verifyText);
      }
      //if (closeDialog)
      //{
      //   closeMessageDialog();
      //}
   }

   public void searchPageByTitle(String title, String verifyText)
   {
      log("--Searching page: " + title + "--");
      waitForElementPresent("searchTerm");
      type("searchTerm", title);
      select("searchOption", "label=Title");
      waitForElementPresent("xpath=//form[@id='UIPageSearch']/div[2]/a");
      click("xpath=//form[@id='UIPageSearch']/div[2]/a");
      shortPause();
      if (verifyText != null)
      {
         waitForTextPresent(verifyText);
      }
   }

   private void select(String selectEl, String select)
   {
      HtmlElement el = getElement(selectEl);
      if (el instanceof HtmlSelect)
      {
         // find SelectOption
         HtmlSelect sel = (HtmlSelect) el;
         HtmlOption opt = null;

         if (select.startsWith("label=") || select.indexOf("=") == -1)
         {
            String label = select.substring(6);
            opt = sel.getOptionByText(label);
            if (opt == null)
               throw new RuntimeException("No such option (" + label + ") for Select Input " + selectEl);
            page = (HtmlPage) sel.setSelectedAttribute(opt, true);
         }
         else
         {
            throw new RuntimeException("Unsupported options specification format: " + select);
         }
      }
      else
      {
         throw new RuntimeException("Specified element not of type HtmlSelect (" + selectEl + "): " + el);
      }
   }

   public void goToSiteManagement()
   {
      log("--Go to Page Management--");
      waitForElementPresent("link=Page Management");
      click("link=Page Management");
   }

   public void editNavigation(String site)
   {
      log("--Edit Navigation : " + site + "--");
      String navLink = "//table[@class='ManagementBlock' and //tr/td/div/text() = '"
            + site + "']//a[text() = 'Edit Navigation']";
      waitForElementPresent(navLink);
      click(navLink);
      waitForElementPresent("link=Add Node");
   }

   public void deleteNode(String nodeLabel)
   {
      log("--Deleting node from navigation--");
      waitForElementPresent("//a[@title='" + nodeLabel + "']");
      shortPause();
      contextMenuOnElement("//a[@title='" + nodeLabel + "']");
      shortPause();
      waitForElementPresent("//div[@id='UINavigationNodeSelector']//div[@id='NavigationNodePopupMenu']//a[@class='ItemIcon DeleteNode16x16Icon']");
      setupConfirmation("Are you sure you want to delete this node?");
      click("//div[@id='UINavigationNodeSelector']//div[@id='NavigationNodePopupMenu']//a[@class='ItemIcon DeleteNode16x16Icon']");
      resetConfirmation();
      waitForElementNotPresent("//a[@title='" + nodeLabel + "']");
      waitForElementPresent("link=Save");
      click("link=Save");
      waitForTextNotPresent("Navigation Management");
      waitForTextNotPresent(nodeLabel);
   }

   public void contextMenuOnElement(String element)
   {
      String source = "selenium.doComponentExoContextMenu(\"" + element + "\")";
      webClient.getJavaScriptEngine().execute(page, source, "script", 1);
   }

   public void leavePageEdit()
   {
      log("-- Leave PageEdit --");
      // we could have an error window popped up
      String closeButton = "//div[@class='ExoMessageDecorator' and //div[@class='TabsContainer']//div[@class='SelectedTab']]//div[@class='CloseButton']";
      click(closeButton);

      // also we may have to abort
      String abortButton = "//table[@class='ActionContainer']//div[contains(@onclick, 'action=Abort')]//a[text()='Abort']";
      click(abortButton);

      waitForTextNotPresent("Page Editor");
   }

   public void log(String msg)
   {
      System.out.println(msg);
   }
}
