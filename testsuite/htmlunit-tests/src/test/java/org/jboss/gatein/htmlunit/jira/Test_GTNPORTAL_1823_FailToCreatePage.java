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

package org.jboss.gatein.htmlunit.jira;

import org.jboss.gatein.htmlunit.AbstractWebTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Test_GTNPORTAL_1823_FailToCreatePage extends AbstractWebTest
{
   /** Logger */
   private static final Logger log = Logger.getLogger(Test_GTNPORTAL_1823_FailToCreatePage.class);

   /** Number of concurrent threads for the test */
   protected static final int TCOUNT = 2;

   /** Counter for id generation */
   protected static AtomicInteger idCounter = new AtomicInteger(1);

   /** Down counter used to synchronize threads before clicking Finish */
   protected static CountDownLatch sync = new CountDownLatch(TCOUNT);

   /**
    * Id for inclusion in page title so that every thread uses unique name for the newly added page
    *
    * @return id
    */
   public String nextId()
   {
      return "" + idCounter.getAndIncrement();
   }

   /**
    * This test method relies on TestNG parallel test execution facility
    * to perform two concurrent executions of this method on a single instance of the test class
    *
    * @throws Throwable if test fails
    */
   @Test(invocationCount = TCOUNT, threadPoolSize = TCOUNT, groups = {"GateIn", "jira", "htmlunit"})
   public void testMain() throws Throwable
   {
      try
      {
         test(false);
      }
      finally
      {
         // If exception occurs we don't want the other thread to lock up
         // - so we perform another countDown()
         sync.countDown();
      }
   }

   /**
    * Main body of the test
    *
    * @throws Throwable
    */
   protected void test(boolean navNodeTest) throws Throwable
   {
      String id = nextId();
      String categoryTitle = "Gadgets";
      String portletName = "Calculator";
      String pageName = "TestPage" + id;
      String portletElementToDnD = "//div[@id='" + categoryTitle + "/" + portletName + "']";

      openPortal(true);

      // Check that testPage with this name does not yet exist
      Assert.assertFalse(isElementPresent("link=" + pageName), "Page exists already: " + pageName);

      signInAsRoot();

      // Add new page, but don't click Finish yet at the end
      addNewPageUpToFinish(categoryTitle, portletName, pageName, portletElementToDnD);

      // Wait for the other thread ...
      sync.countDown();
      sync.await();


      if (navNodeTest && id.equals("2"))
      {
         // Finish click concurrency is inappropriate for navNodeTest where we need predictable order
         Thread.sleep(5000);
      }

      // Now click Finish (both threads at the same time)
      finishPageEdit();

      String failedText = "This node does not have any pages.";
      String textPresent = waitForTextPresent(portletName, failedText);

      Assert.assertNotSame(textPresent, failedText, "Concurrent Add Page issue reproduced!");
      Assert.assertEquals(textPresent, portletName, "");

      if (navNodeTest && id.equals("2"))
      {
         // Check that both test pages' navigation nodes are present
         Assert.assertTrue(isElementPresent("link=TestPage1"), "TestPage1 link presence");
         Assert.assertTrue(isElementPresent("link=TestPage2"), "TestPage2 link presence");
      }
      finished();
   }

   /**
    * Perform cleanup
    * @throws IOException
    */
   private void test2() throws IOException
   {
      openPortal(true);

      signInAsRoot();

      cleanup();
   }

   /**
    * TODO: Cleanup code doesn't yet work
    *
    * @throws IOException
    */
   private void cleanup() throws IOException
   {
      try
      {
         leavePageEdit();
      }
      catch (Exception ex)
      {
         log.warn("IGNORED: ", ex);
      }

      try
      {
         goToPageManagement();

         try
         {
            searchAndDeletePage("TestPage1");
         }
         catch (Throwable ex)
         {
            log.warn("IGNORED: Failed to delete TestPage1: ", ex);
         }

         try
         {
            searchAndDeletePage("TestPage2");
         }
         catch (Throwable ex)
         {
            log.warn("IGNORED: Failed to delete TestPage2: ", ex);
         }

      }
      catch (Throwable e)
      {
         log.warn("IGNORED: Failed to open PageManagement: ", e);
      }

      try
      {
         goToSiteManagement();

         try
         {
            editNavigation("classic");
            deleteNode("TestPage");
         }
         catch (Throwable ex)
         {
            log.warn("IGNORED: Failed to delete TestPage1: ", ex);
         }

         try
         {
            editNavigation("classic");
            deleteNode("TestPage2");
         }
         catch (Throwable ex)
         {
            log.warn("IGNORED: Failed to delete TestPage2: ", ex);
         }
      }
      catch (Throwable e)
      {
         log.warn("IGNORED: Failed to remove TestPage from navigation: ", e);
      }
   }
}