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

package org.jboss.gatein.htmlunit;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.jboss.gatein.htmlunit.common.WebHelper;

import java.io.IOException;

/**
 * This class provides every thread of execution with a single instance of WebHelper
 * which represents a single session with the web application.
 *
 * By extending this class test code can be written as if the state exists at the test class instance level
 * (shared across concurrent method calls), when in fact every thread has it's own copy.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public abstract class AbstractWebTest
{
   /** Thread specific WebHelper storage */
   private static final ThreadLocal<WebHelper> tcl = new ThreadLocal<WebHelper>();

   /**
    * Get current WebHelper - lazy initialized if none yet exists.
    *
    * @return current WebHelper
    */
   protected WebHelper getWebHelper()
   {
      WebHelper ret = tcl.get();
      if (ret == null)
      {
         tcl.set(new WebHelper());
         ret = tcl.get();
      }
      return ret;
   }

   /**
    * Get current WebClient
    *
    * @return current WebClient
    */
   protected WebClient getWebClient()
   {
      return getWebHelper().getWebClient();
   }

   /**
    * @see WebHelper#openPortal(boolean)
    */
   protected void openPortal(boolean publicMode) throws IOException
   {
      getWebHelper().openPortal(publicMode);
   }

   /**
    * @see WebHelper#signInAsRoot()
    */
   protected void signInAsRoot()
   {
      getWebHelper().signInAsRoot();
   }

   /**
    * @see WebHelper#signIn(String, String)
    */
   protected void signIn(String user, String pass)
   {
      getWebHelper().signIn(user, pass);
   }

   /**
    * @see WebHelper#type(String, String)
    */
   protected void type(String field, String value)
   {
      getWebHelper().type(field, value);
   }

   /**
    * @see WebHelper#click(String)
    */
   protected void click(String el)
   {
      getWebHelper().click(el);
   }

   /**
    * @see WebHelper#waitForElementPresent(String)
    */
   protected void waitForElementPresent(String el)
   {
      getWebHelper().waitForElementPresent(el);
   }

   /**
    * @see WebHelper#isElementPresent(String)
    */
   protected boolean isElementPresent(String el)
   {
      return getWebHelper().isElementPresent(el);
   }

   /**
    * @see WebHelper#getElement(String)
    */
   protected HtmlElement getElement(String el)
   {
      return getWebHelper().getElement(el);
   }

   /**
    * @see WebHelper#waitForTextPresent(String ...)
    */
   protected String waitForTextPresent(String ... text)
   {
      return getWebHelper().waitForTextPresent(text);
   }

   /**
    * @see WebHelper#waitForTextNotPresent(String)
    */
   protected void waitForTextNotPresent(String text)
   {
      getWebHelper().waitForTextNotPresent(text);
   }

   /**
    * @see WebHelper#isTextPresent(String)
    */
   protected boolean isTextPresent(String text)
   {
      return getWebHelper().isTextPresent(text);
   }

   /**
    * @see WebHelper#shortPause
    */
   protected void shortPause()
   {
      getWebHelper().shortPause();
   }

   /**
    * @see WebHelper#pause(long)
    */
   protected void pause(long millis)
   {
      WebHelper.pause(millis);
   }

   /**
    * @see WebHelper#dragAndDropToObject(String, String)
    */
   protected void dragAndDropToObject(String sourceEl, String targetEl)
   {
      getWebHelper().dragAndDropToObject(sourceEl, targetEl);
   }

   /**
    * @see WebHelper#finishPageEdit
    */
   protected void finishPageEdit()
   {
      getWebHelper().finishPageEdit();
   }

   /**
    * @see WebHelper#addNewPageUpToFinish(String, String, String, String)
    */
   protected void addNewPageUpToFinish(String categoryTitle, String portletName, String pageName, String portletElementToDnD)
   {
      getWebHelper().addNewPageUpToFinish(categoryTitle, portletName, pageName, portletElementToDnD);
   }

   protected void goToPageManagement()
   {
      getWebHelper().goToPageManagement();
   }

   protected void searchAndDeletePage(String testPage)
   {
      getWebHelper().searchAndDeletePage(testPage);
   }

   protected void goToSiteManagement()
   {
      getWebHelper().goToSiteManagement();
   }

   protected void editNavigation(String portalContainer)
   {
      getWebHelper().editNavigation(portalContainer);
   }

   protected void deleteNode(String nodeLabel)
   {
      getWebHelper().deleteNode(nodeLabel);
   }

   protected void leavePageEdit()
   {
      getWebHelper().leavePageEdit();
   }

   protected void finished()
   {
      if (getWebHelper().isDumpPageOnExit())
         getWebHelper().dumpPage();
   }
}
