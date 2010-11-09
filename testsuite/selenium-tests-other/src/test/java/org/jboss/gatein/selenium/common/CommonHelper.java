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

package org.jboss.gatein.selenium.common;

import com.thoughtworks.selenium.SeleniumException;
import org.jboss.gatein.selenium.AbstractContextual;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommonHelper extends AbstractContextual
{

   public static void waitForElementPresent(String element) throws Exception
   {
      setUp();

      for (int second = 0; ; second++)
      {
         if (second >= timeoutSecInt)
         {
            Assert.fail("Timeout at waitForElementPresent: " + element);
         }
         try
         {
            if (selenium.isElementPresent(element))
            {
               break;
            }
         }
         catch (Exception e)
         {
         }
         Thread.sleep(1000);
      }
   }

   public static void waitForTextPresent(String text) throws Exception
   {
      setUp();

      for (int second = 0; ; second++)
      {
         if (second >= timeoutSecInt)
         {
            Assert.fail("Timeout at waitForTextPresent: " + text);
         }
         try
         {
            if (ieFlag)
            {
               if (selenium.isElementPresent("//*[contains(text(), '" + text + "')]") && selenium.isVisible("//*[contains(text(), '" + text + "')]"))
               {
                  break;
               }
            }
            else
            {
               if (selenium.isTextPresent(text))
               {
                  break;
               }
            }
         }
         catch (Exception e)
         {
         }
         Thread.sleep(1000);
      }
   }

   public static void waitForTextNotPresent(String text) throws Exception
   {
      setUp();

      for (int second = 0; ; second++)
      {
         if (second >= timeoutSecInt)
         {
            Assert.fail("Timeout at waitForTextNotPresent: " + text);
         }
         try
         {
            if (ieFlag)
            {
               if (!selenium.isElementPresent("//*[contains(text(), '" + text + "')]") || !selenium.isVisible("//*[contains(text(), '" + text + "')]"))
               {
                  break;
               }
            }
            else
            {
               if (!selenium.isTextPresent(text))
               {
                  break;
               }
            }
         }
         catch (Exception e)
         {
         }
         Thread.sleep(1000);
      }
   }

   public static void waitForElementNotPresent(String element) throws Exception
   {
      setUp();

      for (int second = 0; ; second++)
      {
         if (second >= timeoutSecInt)
         {
            Assert.fail("Timeout at waitForElementNotPresent: " + element);
         }
         try
         {
            if (!selenium.isElementPresent(element))
            {
               break;
            }
         }
         catch (Exception e)
         {
         }
         Thread.sleep(1000);
      }
   }

   public static void waitForConfirmation(String confirmationText) throws Exception
   {
      setUp();

      for (int second = 0; ; second++)
      {
         if (second >= timeoutSecInt)
         {
            Assert.fail("Timeout at waitForConfirmation: " + confirmationText);
         }
         try
         {
            if (selenium.getConfirmation().equals(confirmationText))
            {
               break;
            }
         }
         catch (Exception e)
         {
         }
         Thread.sleep(1000);
      }
   }

   public static String getText(String element) throws Exception
   {
      setUp();

      return selenium.getText(element);
   }

   public static boolean isTextPresent(String text) throws Exception
   {
      setUp();

      if (ieFlag)
      {
         return selenium.isElementPresent("//*[contains(text(), '" + text + "')]") && selenium.isVisible("//*[contains(text(), '" + text + "')]");
      }
      else
      {
         return selenium.isTextPresent(text);
      }
   }

   public static boolean isTextNotPresent(String text) throws Exception
   {
      setUp();

      if (ieFlag)
      {
         return !selenium.isElementPresent("//*[contains(text(), '" + text + "')]") || !selenium.isVisible("//*[contains(text(), '" + text + "')]");
      }
      else
      {
         return !selenium.isTextPresent(text);
      }
   }

   public static boolean isElementPresent(String element) throws Exception
   {
      setUp();

      return selenium.isElementPresent(element);
   }

   public static boolean isElementNotPresent(String element) throws Exception
   {
      setUp();

      return !selenium.isElementPresent(element);
   }

   public static boolean isOrdered(String element1, String element2)
   {
      setUp();

      return selenium.isOrdered(element1, element2);
   }

   public static void waitForPageToLoad(String timeout)
   {
      setUp();

      selenium.waitForPageToLoad(timeout);
   }

   public static void dragAndDropToObject(String source, String target)
   {
      setUp();

      System.out.println("--Drag and drop to object--");
      selenium.dragAndDropToObject(source, target);
   }

   public static void dragAndDrop(String source, String movementString)
   {
      setUp();

      System.out.println("--Drag and drop--");
      selenium.dragAndDrop(source, movementString);
   }

   public static void contextMenuOnElement(String element)
   {
      selenium.getEval("selenium.doComponentExoContextMenu(\"" + element + "\")");
   }

   public static void doubleClickOnElement(String element)
   {
      selenium.getEval("selenium.doComponentExoDoubleClick(\"" + element + "\")");
   }

   public static void keyPressOnElement(String element, String key)
   {
      selenium.keyPress(element, key);
   }

   public static void open(String location)
   {
      setUp();

      System.out.println("--Opening location " + location + "--");
      long start = System.currentTimeMillis();
      RuntimeException re = null;
      do
      {
         try
         {
            selenium.open(location);
            return;
         }
         catch (SeleniumException ex)
         {
            if (!ex.getMessage().startsWith("Timed out"))
            {
               throw ex;
            }
            re = ex;
         }
      }
      while (System.currentTimeMillis() - start < timeoutSecInt * 1000);

      if (re != null)
      {
         throw re;
      }
   }

   public static void click(String element)
   {
      setUp();

      selenium.click(element);
   }

   public static void type(String element, String value)
   {
      setUp();

      selenium.type(element, value);
   }

   public static void select(String element, String option)
   {
      setUp();

      selenium.select(element, option);
   }

   public static void check(String element)
   {
      setUp();

      selenium.check(element);
   }

   public static boolean isCheck(String element)
   {
      setUp();

      return selenium.isChecked(element);
   }

   public static void uncheck(String element)
   {
      setUp();

      selenium.uncheck(element);
   }

   public static void fireEvent(String element, String event)
   {
      setUp();

      selenium.fireEvent(element, event);
   }

   public static void mouseOver(String element)
   {
      setUp();

      selenium.mouseOver(element);
   }

   public static void pause(long timeInMillis)
   {
      try
      {
         Thread.sleep(timeInMillis);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
   }

   public static String getValue(String element)
   {
      setUp();

      return selenium.getValue(element);
   }

   public static String getXpathCount(String element)
   {
      setUp();

      return selenium.getXpathCount(element).toString();
   }

   public static boolean isTextAtElementEqual(String element, String text) throws Exception
   {
      setUp();

      return getText(element).equals(text);
   }


   public static void openPortal(boolean publicMode)
   {
      setUp();

      open(publicMode ? portalPath : portalPath.replace("public", "private"));
   }

   public static void signInAsRoot() throws Exception
   {
      signIn("root", "gtn");
   }

   public static void signInAsJohn() throws Exception
   {
      signIn("john", "gtn");
   }

   public static void signInAsMary() throws Exception
   {
      signIn("mary", "gtn");
   }

   public static void signInAsDemo() throws Exception
   {
      signIn("demo", "gtn");
   }

   public static void signIn(String username, String password) throws Exception
   {
      setUp();

      System.out.println("--Signing  in as " + username + "--");
      waitForElementPresent("link=Sign in");
      click("link=Sign in");
      waitForElementPresent("username");
      type("username", username);
      type("password", password);
      click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
      waitForPageToLoad(timeout);
   }

   public static void signInWithRememberChecked(String username, String password) throws Exception
   {
      setUp();

      System.out.println("--Signing  in as " + username + " with checked remember me--");
      waitForElementPresent("link=Sign in");
      click("link=Sign in");
      waitForElementPresent("username");
      type("username", username);
      type("password", password);
      System.out.println("--Check \"remember my login\"");
      click("rememberme");
      click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
      waitForPageToLoad(timeout);
   }

   public static void signOut() throws Exception
   {
      setUp();

      signOutWithLanguage("Sign out");
   }

   public static void signOutWithLanguage(String signOutLabel) throws Exception
   {
      setUp();

      System.out.println("--Signing out--");
      waitForElementPresent("link=" + signOutLabel);
      click("link=" + signOutLabel);
      waitForPageToLoad(timeout);
   }

   public static void goToApplicationRegistry() throws Exception
   {
      setUp();

      System.out.println("--Go to App.registry--");
      waitForElementPresent("link=Application Registry");
      click("link=Application Registry");
      waitForPageToLoad(timeout);
   }

   public static void goToPageManagement() throws Exception
   {
      setUp();

      System.out.println("--Go to Page Management--");
      waitForElementPresent("link=Page Management");
      click("link=Page Management");
      waitForPageToLoad(timeout);
   }

   public static void goToUsersManagement() throws Exception
   {
      setUp();

      System.out.println("--Go to Users and groups management--");
      waitForElementPresent("link=Users and groups management");
      click("link=Users and groups management");
      waitForPageToLoad(timeout);
   }

   public static void goToNewStaff() throws Exception
   {
      setUp();

      System.out.println("--Go to New Staff--");
      waitForElementPresent("link=New Staff");
      click("link=New Staff");
      waitForPageToLoad(timeout);
   }

   public static void goToSiteManagement() throws Exception
   {
      setUp();

      System.out.println("--Go to Site Management--");
      waitForElementPresent("link=Site");
      click("link=Site");
      waitForPageToLoad(timeout);
   }

   public static void goToGroupManagement() throws Exception
   {
      setUp();

      System.out.println("--Go to Group Management--");
      waitForElementPresent("link=Group");
      click("link=Group");
      waitForPageToLoad(timeout);
   }

   public static void goToDashboard() throws Exception
   {
      setUp();

      System.out.println("--Go to Dashboard--");
      waitForElementPresent("link=Dashboard");
      click("link=Dashboard");
      waitForPageToLoad(timeout);
   }

   public static void goToClassicPortal() throws Exception
   {
      setUp();

      System.out.println("--Go to portal classic--");
      waitForElementPresent("link=classic");
      click("link=classic");
      waitForPageToLoad(timeout);
   }

   public static void goToPage(String pageLabel) throws Exception
   {
      setUp();

      System.out.println("--Go to page: " + pageLabel + "--");
      waitForElementPresent("link=" + pageLabel);
      click("link=" + pageLabel);
      waitForPageToLoad(timeout);
   }

   public static void closeMessageDialog() throws Exception
   {
      System.out.println("--Closing message dialog--");
      waitForElementPresent("//div[@id='UIPortalApplication']/div[@class='UIPopupWindow UIDragObject']//div[@class='MiddleLeftSideDecorator']//div[@class='UIPopupMessages']/div[@class='MessageActionBar']//a");
      click("//div[@id='UIPortalApplication']/div[@class='UIPopupWindow UIDragObject']//div[@class='MiddleLeftSideDecorator']//div[@class='UIPopupMessages']/div[@class='MessageActionBar']//a");
   }

   public static void waitForMessage(String message) throws Exception
   {
      System.out.println("--Verify message: " + message);
      waitForElementPresent("//div[@class='PopupMessage']");
      waitForTextPresent(message);
   }

   public static void waitForPopupElementWithMessage(String message) throws Exception
   {
      System.out.println("--Verify message: " + message);
      waitForElementPresent("//div[@class='PopupMessage']");
      String value = getText("//div[@class='PopupMessage']");
      if (!value.contains(message))
      {
         throw new Exception("Message to verify is not valid, current message value is: " + value);
      }
   }

   public static void assertConfirmation(String msg) throws Exception
   {
      Assert.assertTrue(selenium.getConfirmation().matches("^" + msg + "$"));
   }

   public static void verifyTextPresent(String text) throws Exception
   {
      Assert.assertTrue(selenium.isTextPresent(text));
   }

   public static void importApplications() throws Exception
   {
      System.out.println("--Import applications--");
      waitForElementPresent("//div[@id='UIApplicationRegistryPortlet']/div[2]/div[2]/div/div/div[2]");
      click("//div[@id='UIApplicationRegistryPortlet']/div[2]/div[2]/div/div/div[2]");
      assertConfirmation("This action will automatically create categories and import all the gadgets and portlets on it.");
   }

   public static void verifyPortletInstalled(String appName, String portletName) throws Exception
   {
      System.out.println("--Verify portlet is installed: " + appName + "/" + portletName + "--");
      waitForElementPresent("link=" + appName);
      click("link=" + appName);
      waitForTextPresent(portletName);
      verifyTextPresent(portletName);
   }

   public static void echo(String msg)
   {
      System.out.println(msg);
   }

   /**
    * Copy input to output.
    *
    * @param in  the input
    * @param out the output
    * @throws IOException for any I/O error
    */
   public static void copyAndClose(InputStream in, OutputStream out) throws IOException
   {
      try
      {
         byte[] buf = new byte[20000];
         int rc;
         while ((rc = in.read(buf)) != -1)
         {
            out.write(buf, 0, rc);
         }
         out.flush();
         out.close();
      }
      finally
      {
         try
         {
            in.close();
         }
         catch (Exception ignored)
         {
         }
      }
   }
}
