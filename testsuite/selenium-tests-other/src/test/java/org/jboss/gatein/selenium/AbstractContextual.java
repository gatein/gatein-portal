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

package org.jboss.gatein.selenium;

import com.thoughtworks.selenium.Selenium;

public class AbstractContextual
{

   protected static Selenium selenium;
   protected static String timeout;
   protected static int timeoutSecInt;
   protected static String portalPath;
   protected static String browser;
   protected static boolean ieFlag;

   private static boolean inited;
   private static int savedTimeoutSecInt;

   private static SeleniumContext getSeleniumContext()
   {
      return AbstractTestCase.getCurrentSeleniumContext();
   }

   protected static void setUp()
   {
      if (inited)
      {
         return;
      }

      final SeleniumContext seleniumContext = getSeleniumContext();
      selenium = seleniumContext.getSelenium();
      timeout = seleniumContext.getTimeout();
      timeoutSecInt = seleniumContext.getTimeoutSecInt();
      portalPath = seleniumContext.getPortalPath();
      browser = seleniumContext.getBrowser();
      ieFlag = browser.contains("explore");
      inited = true;
   }

   public static void setTemporaryTimeoutSecInt(int secs)
   {
      setUp();

      savedTimeoutSecInt = timeoutSecInt;
      if (secs > timeoutSecInt)
      {
         timeoutSecInt = secs;
      }
   }

   public static void restoreTimeoutSecInt()
   {
      timeoutSecInt = savedTimeoutSecInt;
   }
}
