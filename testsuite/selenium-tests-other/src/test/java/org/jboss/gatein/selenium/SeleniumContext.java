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

public class SeleniumContext
{

   public Selenium getSelenium()
   {
      return selenium;
   }

   public String getTimeout()
   {
      return timeout;
   }

   public int getTimeoutSecInt()
   {
      return timeoutSecInt;
   }

   public String getPortalPath()
   {
      return portalPath;
   }

   public String getBrowser()
   {
      return browser;
   }

   private Selenium selenium;
   private String timeout;
   private int timeoutSecInt;
   private String portalPath;
   private String browser;

   public SeleniumContext(Selenium selenium, String timeout, String portalPath, String browser)
   {
      this.selenium = selenium;
      this.timeout = timeout;
      this.timeoutSecInt = Integer.parseInt(timeout) / 1000;
      this.portalPath = portalPath;
      this.browser = browser;
   }
}
