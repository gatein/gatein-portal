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
import com.thoughtworks.selenium.SeleniumException;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The listener interface for receiving AbstractTestCase events.
 * The class that is interested in processing a AbstractTestCase
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTestCaseFailListener<code> method. When
 * the AbstractTestCase event occurs, that object's appropriate
 * method is invoked. <br/>
 * TestCaseFailListener is responsible for decoding most of the parameters which are passed to selenium tests.
 * <table>
 * <thead>
 * <tr>
 * <th>parameter name</th>
 * <th>valid values</th>
 * <th>default value</th>
 * <th>description</th>
 * </tr>
 * </thead>
 * <tr>
 * <td>screenshot</td>
 * <td>true/false</td>
 * <td>false</td>
 * <td>If true, screenshots are taken after the test failure. They are saved into directory specified by the "output-dir" parameter.</td>
 * </tr><tr>
 * <td>html-src</td><td>true/false</td><td>false</td><td>If true, html sources are taken after the test failure. They are saved into directory specified by the "output" parameter.</td>
 * </tr><tr>
 * <td>output-dir</td><td>&#60;path-to-dir&#62;</td><td>""</td><td>Path to the directory where html sources and screenshots are saved.</td>
 * </tr>
 * </table>
 */
public class TestCaseFailListener extends TestListenerAdapter
{

   public static Selenium selenium;
   protected static int count;
   protected static boolean screenshot = false;
   protected static boolean htmlSource = false;
   protected static String outputDir = "";

   static
   {
      String ss = System.getProperty("screenshot");
      if ("true".equals(ss))
      {
         screenshot = true;
      }

      String sh = System.getProperty("html-src");
      if ("true".equals(sh))
      {
         htmlSource = true;
      }

      String so = System.getProperty("output-dir");
      if (so != null)
      {
         outputDir = so;
      }
   }

   /* (non-Javadoc)
     * @see org.testng.TestListenerAdapter#onTestFailure(org.testng.ITestResult)
     */

   @Override
   public void onTestFailure(ITestResult tr)
   {

      String name = outputDir + "/" + "F_" + tr.getName() + "-" + count;

      if (screenshot)
      {
         try
         {
            selenium.windowMaximize();
            waitFor(5000);
            selenium.captureScreenshot(name + ".jpg");
         }
         catch (SeleniumException e2)
         {
            e2.printStackTrace();
         }
      }

      if (htmlSource)
      {
         try
         {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(name + ".html")));
            out.println(selenium.getHtmlSource());
            out.close();
         }
         catch (IOException e1)
         {
            e1.printStackTrace();
         }
      }
      count++;
   }

   /**
    * Waits for specified time in ms. Used mostly in AJAX based tests.
    *
    * @param time the time (in ms) to be waited for.
    */
   public void waitFor(long time)
   {
      try
      {
         Thread.sleep(time);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
   }

   public static void captureScreenshot(String testName)
   {
      String name = outputDir + "/" + "F_" + testName;
      if (screenshot)
      {
         try
         {
            selenium.captureScreenshot(name + ".jpg");
         }
         catch (SeleniumException e)
         {
            e.printStackTrace();
         }
      }
	}

}
