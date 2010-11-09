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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.log4testng.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractTestCase
{

   public Selenium selenium;

   public String speed = "100";
   public String timeout = "30000";
   public int timeoutSecInt = 30;
   public String browser = "firefox";
   public String host = "localhost";
   public String hostPort = "8080";
   public String host2 = "localhost";
   public String host2Port = "8080";
   public String seleniumPort = "6666";
   public String portalPath = "/portal/public/classic";
   public boolean ieFlag = false;
   public String wsrpVersion = "v1";

   protected Logger log = Logger.getLogger(getClass());

   protected static Properties propsMes;

   static
   {
      try
      {
         propsMes = getProperties("org/jboss/gatein/selenium/messages.properties");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   protected static SeleniumContext currentSeleniumContext;

   public static SeleniumContext getCurrentSeleniumContext()
   {
      return currentSeleniumContext;
   }

   public void setInitialTestParameters()
   {
      setTimeout();
      setSpeed();
   }

   public void setTimeout()
   {
      selenium.setTimeout(timeout);
   }

   public void setSpeed()
   {
      selenium.setSpeed(speed);
   }

   @BeforeClass(alwaysRun = true)
   public void setUp() throws Exception
   {
      initSystemProperties();
      selenium = new DefaultSelenium("127.0.0.1", Integer.parseInt(seleniumPort), "*" + browser, "http://" + host + ":" + hostPort);
      TestCaseFailListener.selenium = selenium;
      selenium.start();
      currentSeleniumContext = new SeleniumContext(selenium, timeout, portalPath, browser);
      setInitialTestParameters(); // initialization of speed and timeout
      selenium.windowMaximize(); // running tests in maximize window
   }

   @AfterClass(alwaysRun = true)
   public void tearDown() throws Exception
   {
      selenium.stop();
      currentSeleniumContext = null;
   }

   public void initSystemProperties()
   {
      browser = System.getProperty("selenium.browser", browser);
      timeout = System.getProperty("selenium.timeout", timeout);
      timeoutSecInt = Integer.parseInt(timeout) / 1000;
      speed = System.getProperty("selenium.speed", speed);
      host = System.getProperty("selenium.host", host);
      hostPort = System.getProperty("selenium.host.port", hostPort);
      host2 = System.getProperty("selenium.host2", host2);
      host2Port = System.getProperty("selenium.host2.port", host2Port);
      seleniumPort = System.getProperty("selenium.port", seleniumPort);
      portalPath = System.getProperty("portal.path", portalPath);
      wsrpVersion = System.getProperty("wsrp.version", wsrpVersion);
      ieFlag = browser.contains("iexplore");
   }

   public static String getMessage(String property)
   {
      return getProperty(propsMes, property, null);
   }

   public static String getMessage(String property, String subst)
   {
      return getProperty(propsMes, property, subst);
   }

   private static String getProperty(Properties properties, String property,
                                     String subst)
   {

      if (subst == null || "".equals(subst))
      {
         subst = "Substitude not set";
      }

      if (properties == null)
      {
         return subst;
      }
      else
      {
         String message = properties.getProperty(property);
         return message != null ? message : subst;
      }
   }

   private static Properties getProperties(String resource) throws IOException
   {
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      InputStream is = cl.getResourceAsStream(resource);
      Properties props = new Properties();
      props.load(is);
      return props;
   }
}
