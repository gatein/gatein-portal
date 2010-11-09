/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.gatein.selenium.jira;

import static org.jboss.gatein.selenium.common.CommonHelper.*;
import static org.jboss.gatein.selenium.navigation.NavigationHelper.*;
import static org.jboss.gatein.selenium.page.PageHelper.*;

import org.jboss.gatein.selenium.AbstractContextual;
import org.jboss.gatein.selenium.AbstractSingleTestWithAnt;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Test_GTNPORTAL_1257_SeamSessionOutlivesTheGateInSession extends AbstractSingleTestWithAnt
{

   @BeforeTest(alwaysRun = true)
   public void deploySeamBooking() throws Exception
   {

      if (!expectAntBuildFile())
      {
         log.warn("JBoss Portlet Bridge Seam Booking example may not be available");
         return;
      }

      prepareAndExecuteAntBuild();
   }

   @Test(groups = {"GateIn", "jira"})
   public void testGTNPORTAL_1257_SeamSessionOutlivesTheGateInSession() throws Throwable
   {
      performTest();
   }

   @Override
   protected void mainTest() throws Throwable
   {
      try
      {
         AbstractContextual.setTemporaryTimeoutSecInt(60);        // give it at least one minute
         openPortal(true);
      }
      finally
      {
         AbstractContextual.restoreTimeoutSecInt();
      }

      signInAsRoot();

      goToApplicationRegistry();

      installPortlet();

      goToPage("Home");

      try
      {
         AbstractContextual.setTemporaryTimeoutSecInt(60);        // give it at least one minute
         addNewPageWithEditor(null, "TestPage", "TestPage", "portalBookingDemo", "seamBookingPortlet",
               "//div[@id='portalBookingDemo/seamBookingPortlet']");
      }
      catch (Throwable th)
      {
         try
         {
            leavePageEdit();
         }
         catch (Exception ex)
         {
            log.warn("IGNORED: ", ex);
         }
         throw th;
      }
      finally
      {
         AbstractContextual.restoreTimeoutSecInt();
      }

      waitForTextPresent("Search Hotels");

      verifyTextPresent("Welcome, root");

      signOut();

      signInAsMary();

      goToPage("TestPage");

      waitForTextPresent("Search Hotels");
      verifyTextPresent("Welcome, mary");

   }

   private void leavePageEdit()
   {
      // we could have an error window popped up
      String closeButton = "//div[@class='ExoMessageDecorator' and //div[@class='TabsContainer']//div[@class='SelectedTab']]//div[@class='CloseButton']";
      click(closeButton);

      // also we may have to abort
      String abortButton = "//table[@class='ActionContainer']//div[contains(@onclick, 'action=Abort')]//a[text()='Abort']";
      click(abortButton);
   }

   private void installPortlet() throws Throwable
   {
      // try several times - to give dependency deployment some time
      Throwable ex = null;
      for (int i = 0; i < 3; i++)
      {
         importApplications();
         try
         {
            verifyPortletInstalled("portalBookingDemo", "seamBookingPortlet");
            return;
         }
         catch (Throwable th)
         {
            ex = th;
         }
      }
      throw ex;
   }

   @Override
   protected void exception(Throwable ex)
   {
      log.error("Exception during test: ", ex);
   }

   @Override
   protected void cleanup()
   {
      System.out.println("\n== Cleanup ==");

      try
      {
         signOut();
      }
      catch (Throwable e)
      {
         log.warn("IGNORED: Failed to sign out: ", e);
      }

      try
      {
         signInAsRoot();
      }
      catch (Throwable e)
      {
         log.warn("IGNORED: Failed to sign in as root: ", e);
      }

      try
      {
         goToPageManagement();
         searchAndDeletePage("TestPage");
      }
      catch (Throwable e)
      {
         log.warn("IGNORED: Failed to delete testPage: ", e);
      }

      try
      {
         goToSiteManagement();
         editNavigation("classic");
         deleteNode("TestPage");
      }
      catch (Throwable e)
      {
         log.warn("IGNORED: Failed to remove TestPage from navigation: ", e);
      }

      try
      {
         signOut();
      }
      catch (Throwable e)
      {
         log.warn("IGNORED: Failed to sign out: ", e);
      }
   }
}