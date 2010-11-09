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

package org.jboss.gatein.selenium.page;

import org.jboss.gatein.selenium.AbstractContextual;

import static org.jboss.gatein.selenium.common.CommonHelper.*;

public class PageHelper extends AbstractContextual
{

   public static enum PageType
   {
      USER, PORTAL, GROUP;
   }

   public static void editPage() throws Exception
   {
      waitForElementPresent("link=Edit Page");
      click("link=Edit Page");
      waitForTextPresent("Page Editor");
   }

   public static void finishPageEdit() throws Exception
   {
      waitForElementPresent("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]");
      click("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]");
      waitForTextNotPresent("Page Editor");
   }

   public static void addNewPageWithEditorAtFirstLevel(String pageName, String displayName, String categoryTitle, String portletName, String portletElementToDnD) throws Exception
   {
      System.out.println("-- Add new page with editor at first level--");
      waitForElementPresent("link=Add New Page");
      click("link=Add New Page");
      waitForElementPresent("//a[@class='LevelUpArrowIcon' and @title='Up Level']");
      click("//a[@class='LevelUpArrowIcon' and @title='Up Level']");
      pause(3000);
      fillPageEditor(pageName, displayName, categoryTitle, portletName, portletElementToDnD);
   }

   public static void addNewPageWithEditorAtFirstLevelWithMorePortlets(String pageName, String displayName, String categoryTitle, String portletNameSWithPortletElements) throws Exception
   {
      System.out.println("-- Add new page with editor at first level with more portlets--");
      waitForElementPresent("link=Add New Page");
      click("link=Add New Page");
      waitForElementPresent("//a[@class='LevelUpArrowIcon' and @title='Up Level']");
      click("//a[@class='LevelUpArrowIcon' and @title='Up Level']");
      pause(3000);
      waitForElementPresent("pageName");
      type("pageName", pageName);
      type("pageDisplayName", displayName);
      click("//table[@class='ActionContainer']/tbody/tr/td/div[2]");
      waitForTextPresent("Empty Layout");
      click("//table[@class='ActionContainer']/tbody/tr/td/div[2]");
      if (categoryTitle != null & portletNameSWithPortletElements != null)
      {
         waitForElementPresent("//div[contains(@class, 'Tab')]/a[@title='" + categoryTitle + "']");
         click("//div[contains(@class, 'Tab')]/a[@title='" + categoryTitle + "']");
         String[] portlets = portletNameSWithPortletElements.split(";");
         for (String portlet : portlets)
         {
            String[] portletAttributes = portlet.split("---");
            waitForTextPresent(portletAttributes[0]);
            pause(3000);
            dragAndDropToObject(portletAttributes[1], "//div[@class='UIComponentBlock']");
            pause(3000);
         }
      }
      finishPageEdit();
      waitForElementPresent("link=classic");
   }

   public static void addNewPageWithEditor(String navigation, String pageName, String displayName, String categoryTitle, String portletName, String portletElementToDnD) throws Exception
   {
      System.out.println("--Add new page with editor--");
      waitForElementPresent("link=Add New Page");
      click("link=Add New Page");
      if (navigation != null)
      {
         String[] navig = navigation.split("/");
         for (String nav : navig)
         {
            waitForElementPresent("//a[@title='" + nav + "']");
            click("//a[@title='" + nav + "']");
         }
      }
      pause(3000);
      fillPageEditor(pageName, displayName, categoryTitle, portletName, portletElementToDnD);
   }

   private static void fillPageEditor(String pageName, String displayName, String categoryTitle, String portletName, String portletElementToDnD) throws Exception
   {
      waitForElementPresent("pageName");
      type("pageName", pageName);
      type("pageDisplayName", displayName);
      click("//table[@class='ActionContainer']/tbody/tr/td/div[2]");
      waitForTextPresent("Empty Layout");
      click("//table[@class='ActionContainer']/tbody/tr/td/div[2]");
      if (categoryTitle != null & portletName != null & portletElementToDnD != null)
      {
         waitForElementPresent("//div[contains(@class, 'Tab')]/a[@title='" + categoryTitle + "']");
         click("//div[contains(@class, 'Tab')]/a[@title='" + categoryTitle + "']");
         waitForTextPresent(portletName);
         pause(3000);
         dragAndDropToObject(portletElementToDnD, "//div[@class='UIComponentBlock']");
         pause(3000);
      }
      finishPageEdit();
      waitForElementPresent("link=classic");
   }

   public static void addNewPageAtPageManagement(String name, String title, PageType type, String groupId, String verifyText) throws Exception
   {
      System.out.println("-- Add new " + type.name() + " page at page management--");
      waitForElementPresent("xpath=//div[@id='UIPageBrowser']/div[2]/table/tbody/tr/td/div/div/div/div/a");
      click("xpath=//div[@id='UIPageBrowser']/div[2]/table/tbody/tr/td/div/div/div/div/a");
      waitForTextPresent("Page Setting");
      waitForElementPresent("name");
      type("name", name);
      type("title", title);

      switch (type)
      {
         case USER:
            break;
         case PORTAL:
            waitForElementPresent("ownerType");
            select("ownerType", "label=portal");
            waitForElementPresent("//option[@value='portal']");
            waitForElementPresent("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPageForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPageForm','SelectTab','&objectId=PermissionSetting')\"]");
            click("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPageForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPageForm','SelectTab','&objectId=PermissionSetting')\"]");
            waitForElementPresent("link=Access Permission Setting");
            click("link=Access Permission Setting");
            waitForElementPresent("link=Edit Permission Setting");
            click("link=Edit Permission Setting");
            break;
         case GROUP:
            waitForElementPresent("ownerType");
            select("ownerType", "label=group");
            waitForElementPresent("xpath=//option[@value='group']");
            pause(5000);
            waitForElementPresent("ownerId");
            if (groupId != null)
            {
               select("ownerId", "label=" + groupId);
               waitForElementPresent("//option[@value='" + groupId + "']");
            }
            waitForElementPresent("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPageForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPageForm','SelectTab','&objectId=PermissionSetting')\"]");
            click("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPageForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPageForm','SelectTab','&objectId=PermissionSetting')\"]");
            waitForElementPresent("link=Access Permission Setting");
            click("link=Access Permission Setting");
            waitForElementPresent("link=Edit Permission Setting");
            click("link=Edit Permission Setting");
            break;
         default:
            break;
      }
      waitForElementPresent("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPageForm', 'UIPageTemplateOptions');javascript:eXo.webui.UIForm.submitEvent('UIPageForm','SelectTab','&objectId=UIPageTemplateOptions')\"]");
      click("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPageForm', 'UIPageTemplateOptions');javascript:eXo.webui.UIForm.submitEvent('UIPageForm','SelectTab','&objectId=UIPageTemplateOptions')\"]");
      waitForElementPresent("link=Save");
      click("link=Save");
      if (verifyText != null)
      {
         waitForTextNotPresent("Page Setting");
         waitForTextPresent(verifyText);
      }
   }

   public static void searchPageByTitle(String title, String verifyText) throws Exception
   {
      System.out.println("--Searching page: " + title + "--");
      waitForElementPresent("searchTerm");
      type("searchTerm", title);
      select("searchOption", "label=Title");
      waitForElementPresent("xpath=//form[@id='UIPageSearch']/div[2]/a");
      click("xpath=//form[@id='UIPageSearch']/div[2]/a");
      pause(3000);
      if (verifyText != null)
      {
         waitForTextPresent(verifyText);
      }
   }

   private static void deletePage(String title, boolean closeDialog, String verifyText) throws Exception
   {
      System.out.println("--Delete page: " + title + "--");
      String delButton = "//tbody[@class='FeedBox']//tr[*/div[@title='" + title + "']]//img[@title='Delete Page' and contains(@onclick, 'op=Delete')]";
      waitForElementPresent(delButton);
      click(delButton);
      waitForConfirmation("Do you want to delete this page?");
      if (verifyText != null)
      {
         waitForTextNotPresent(verifyText);
      }
      if (closeDialog)
      {
         closeMessageDialog();
      }
   }

   public static void deletePage(String name, String title, boolean closeDialog, String verifyText) throws Exception
   {
      System.out.println("--Delete page: " + title + "--");
      waitForElementPresent("xpath=//img[@title='Delete Page' and contains(@onclick, 'op=Delete') and contains(@onclick, '" + name + "')]");
      click("xpath=//img[@title='Delete Page' and contains(@onclick, 'op=Delete') and contains(@onclick, '" + name + "')]");
      waitForConfirmation("Do you want to delete this page?");
      if (verifyText != null)
      {
         waitForTextNotPresent(verifyText);
      }
      if (closeDialog)
      {
         closeMessageDialog();
      }
   }

   public static void searchAndDeletePage(String name, String title, boolean closeDialog, String verifyText) throws Exception
   {
      searchPageByTitle(title, verifyText);
      deletePage(name, title, closeDialog, verifyText);
   }

   public static void searchAndDeletePage(String title) throws Exception
   {
      searchPageByTitle(title, null);
      deletePage(title, false, null);
   }
}
