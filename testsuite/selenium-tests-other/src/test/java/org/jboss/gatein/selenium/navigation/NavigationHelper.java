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

package org.jboss.gatein.selenium.navigation;

import org.jboss.gatein.selenium.AbstractContextual;
import org.testng.Assert;

import static org.jboss.gatein.selenium.common.CommonHelper.*;

public class NavigationHelper extends AbstractContextual
{

   public static enum CopyType
   {
      COPY, CUT, CLONE;
   }

   public static void addNewNode(String nodeName, String nodeLabel, boolean useLink, String elementToAddWithRightClick, String pageName, String pageTitle, boolean verifyPage, boolean verifyNode) throws Exception
   {
      System.out.println("--Adding new node at navigation--");
      editNavigation();
      if (useLink)
      {
         waitForElementPresent("link=Add Node");
         click("link=Add Node");
      }
      else
      {
         waitForElementPresent(elementToAddWithRightClick);
         click(elementToAddWithRightClick);
         pause(2000);
         contextMenuOnElement(elementToAddWithRightClick);
         pause(3000);
         waitForElementPresent("//div[@id='UINavigationManagement']/div[2]/div[3]/div/div/div/div/div/div/div/div[2]/div/div/div//a");
         click("//div[@id='UINavigationManagement']/div[2]/div[3]/div/div/div/div/div/div/div/div[2]/div/div/div//a");
      }
      waitForTextPresent("Page Node Setting");
      waitForElementPresent("name");
      type("name", nodeName);
      type("label", nodeLabel);
      waitForElementPresent("//div[text() = 'Page Selector']");
      click("//div[text() = 'Page Selector']");
      pause(3000);
      if (pageName != null & pageTitle != null)
      {
         System.out.println("--Create new page");
         waitForElementPresent("pageName");
         type("pageName", pageName);
         type("pageTitle", pageTitle);
         waitForElementPresent("link=Create Page");
         click("link=Create Page");
         if (verifyPage)
         {
            waitForElementNotPresent("link=Create Page");
         }
         else
         {
            return;
         }
      }
      else
      {
         System.out.println("--Select Page");
         waitForElementPresent("link=Search and Select Page");
         click("link=Search and Select Page");
         waitForElementPresent("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img");
         click("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img");
      }
      System.out.println("--Save");
      waitForElementPresent("link=Save");
      click("link=Save");
      if (verifyNode)
      {
         waitForTextNotPresent("Page Node Setting");
         if (elementToAddWithRightClick != null)
         {
            waitForElementPresent(elementToAddWithRightClick);
            click(elementToAddWithRightClick);
         }
         waitForElementPresent("//a[@title='" + nodeLabel + "']");
         waitForElementPresent("link=Save");
         click("link=Save");
         waitForTextNotPresent("Navigation Management");
         waitForElementPresent("link=" + nodeLabel);
      }
   }

   public static void deleteNode(String nodeLabel, String navigation) throws Exception
   {
      System.out.println("--Deleting node from navigation--");
      editNavigation();
      if (navigation != null)
      {
         String[] nav = navigation.split("/");
         for (String navig : nav)
         {
            waitForElementPresent("//a[@title='" + navig + "']");
            click("//a[@title='" + navig + "']");
         }
      }
      deleteNode(nodeLabel);
   }

   public static void deleteNode(String nodeLabel) throws Exception
   {
      System.out.println("--Deleting node from navigation--");
      waitForElementPresent("//a[@title='" + nodeLabel + "']");
      pause(2000);
      contextMenuOnElement("//a[@title='" + nodeLabel + "']");
      pause(3000);
      waitForElementPresent("//div[@id='UINavigationNodeSelector']//div[@id='NavigationNodePopupMenu']//a[@class='ItemIcon DeleteNode16x16Icon']");
      click("//div[@id='UINavigationNodeSelector']//div[@id='NavigationNodePopupMenu']//a[@class='ItemIcon DeleteNode16x16Icon']");
      waitForConfirmation("Are you sure you want to delete this node?");
      waitForElementNotPresent("//a[@title='" + nodeLabel + "']");
      waitForElementPresent("link=Save");
      click("link=Save");
      waitForTextNotPresent("Navigation Management");
      waitForTextNotPresent(nodeLabel);
   }

   public static void editNavigation() throws Exception
   {
      waitForElementPresent("link=Edit Navigation");
      click("link=Edit Navigation");
      waitForTextPresent("Navigation Management");
   }

   public static void editNavigation(String site) throws Exception
   {
      System.out.println("--Edit Navigation : " + site + "--");
      String navLink = "//table[@class='ManagementBlock' and //tr/td/div/text() = '"
            + site + "']//a[text() = 'Edit Navigation']";
      waitForElementPresent(navLink);
      click(navLink);
      waitForElementPresent("link=Add Node");
   }

   public static void copyNode(CopyType type, String navigation, String nodeLabel, String pasteToElement, String verifyElement) throws Exception
   {
      System.out.println("--" + ("COPY".equals(type.name()) ? "Copy" : "Cut") + " a node--");
      if (navigation != null)
      {
         String[] nav = navigation.split("/");
         for (String navig : nav)
         {
            waitForElementPresent("//a[@title='" + navig + "']");
            click("//a[@title='" + navig + "']");
         }
      }
      waitForElementPresent("//a[@title='" + nodeLabel + "']");
      pause(2000);
      contextMenuOnElement("//div[@id='UINavigationNodeSelector']//a[@class='NodeIcon DefaultPageIcon' and @title='" + nodeLabel + "']");
      pause(3000);
      switch (type)
      {
         case COPY:
            waitForElementPresent("link=Copy Node");
            click("link=Copy Node");
            break;
         case CUT:
            waitForElementPresent("link=Cut Node");
            click("link=Cut Node");
            break;
         case CLONE:
            waitForElementPresent("link=Clone Node");
            click("link=Clone Node");
            break;
         default:
            break;
      }
      System.out.println("--Paste a node--");
      waitForElementPresent(pasteToElement);
      pause(2000);
      contextMenuOnElement(pasteToElement);
      pause(3000);
      waitForElementPresent("//div[@id='UINavigationNodeSelector']//div[@id='NavigationNodePopupMenu']//a[@class='ItemIcon PasteNode16x16Icon']");
      click("//div[@id='UINavigationNodeSelector']//div[@id='NavigationNodePopupMenu']//a[@class='ItemIcon PasteNode16x16Icon']");
      if (verifyElement != null)
      {
         waitForElementPresent(verifyElement);
         Assert.assertTrue(isElementPresent(verifyElement));
      }
   }
}
