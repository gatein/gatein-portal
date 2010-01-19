package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;

public class Test_All extends SeleneseTestCase {
  public void setUp() throws Exception {
    setUp("http://localhost:8080/portal/", "*firefox");
  }

  public void testSNF_PRL_02() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Register");
    selenium.waitForPageToLoad("30000");
    selenium.type("User Name:", "abc123");
    selenium.type("Password:", "121212");
    selenium.type("Confirm Password:", "121212");
    selenium.type("First Name:", "Aha");
    selenium.type("Last Name:", "Nguyen");
    selenium.type("Email Address:", "th4nhc0n9z@yahoo.com");
    selenium.clickAt("link=Subscribe", "1,1");
    selenium.open("/portal/public/classic/");
    System.out.println("!!! missing assert to check user exists !!!");
  }

  public void testSNF_PRL_03() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.clickAt("//a[@class='Language']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Vietnamese"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Vietnamese", "1,1");
    selenium.clickAt("link=Apply", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (!selenium.isElementPresent("link=Apply"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//a[@class='Language']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UITabContent']//div[2]/a"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UITabContent']//div[2]/a", "1,1");
    selenium.click("link=Áp d?ng");
    selenium.waitForPageToLoad("30000");
  }

  public void testSNF_PRL_04() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Sign in");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("username"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']//a");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign out"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Sign out");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign in"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
  }

  public void testSNF_PRL_05() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("rememberme");
    selenium.clickAt("//div[@id='UIPortalLoginFormAction']//div[@class='ButtonMiddle']/a", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign out"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Sign out");
    selenium.clickAt("link=Sign in", "1,1");
    verifyTrue(selenium.isChecked("rememberme"));
    selenium.clickAt("link=Discard", "1,1");
  }

  public void testSNF_PRL_06() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Group"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Group");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Users and groups management"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Users and groups management");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("User Name"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("Last Name"));
    assertTrue(selenium.isTextPresent("First Name"));
    assertTrue(selenium.isTextPresent("Email"));
    selenium.clickAt("//div[@id='UIListUsersGird']//tbody/tr[3]//td[5]//div//img", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("firstName"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("firstName", "exo2");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIUserManagement']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIUserManagement']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("user.name.given"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("user.name.given", "test");
    selenium.type("user.name.given", "test05");
    selenium.type("user.name.family", "test05family");
    selenium.type("user.name.nickName", "testnick");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("The user profile has been updated."));
    selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton",
                     "1,1");
    selenium.clickAt("link=Cancel", "1,1");
    assertTrue(selenium.isTextPresent("exo2"));
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign out"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Sign out");
  }

  public void testSNF_PRL_07() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Group"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Group");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Users and groups management"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Users and groups management");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='ManagementIconContainer']/a[@class='GroupButton']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIOrganizationPortlet']/div[2]/div[2]/div[1]/div[2]/div[1]/div[2]/div/div/div/div[3]/div/a"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[3]//div[@class='ExpandIcon']/a",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("Management"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[3]//div[@class='ExpandIcon']/a",
                     "1,1");
    selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='TitleBar']/a[@class='TreeActionIcon AddGroupIcon']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("groupName"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("groupName", "testgroup");
    selenium.type("label", "testgroup label");
    selenium.type("description", "testgroup description");
    selenium.clickAt("//form[@id='UIGroupForm']//div[@class='ActionButton LightBlueStyle']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("testgroup label"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//form[@id='UIGroupMembershipForm']//div[@class='HorizontalLayout']//table[@class='UIFormGrid']//td[@class='FieldComponent']/a",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//form[@id='UIUserSelector']/div[2]/div[2]/table/tbody/tr/td/a[1]/div/div/div"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("demo");
    selenium.click("john");
    selenium.click("mary");
    selenium.click("root");
    selenium.clickAt("//form[@id='UIUserSelector']//div[@class='UIAction']//a[@class='ActionButton LightBlueStyle']",
                     "1,1");
    selenium.clickAt("link=Save", "1,1");
    assertTrue(selenium.isTextPresent("demo"));
    assertTrue(selenium.isTextPresent("john"));
    assertTrue(selenium.isTextPresent("mary"));
    assertTrue(selenium.isTextPresent("root"));
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign out"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Sign out");
  }

  public void testSNF_PRL_08() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Group"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Group");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Users and groups management"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Users and groups management");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='ManagementIconContainer']/a[@class='MembershipButton']",
                     "1,1");
    selenium.type("name", "demomembership");
    selenium.type("description", "demo scripts");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("demomembership"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//table[@class='UIGrid']//tbody/tr[3]/td[5]//img", "1,1");
    selenium.type("description", "demo scripts add more text");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("demo scripts add more text"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=classic");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign out"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Sign out");
  }

  public void testSNF_PRL_09() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Application Registry", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("Import Applications"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.chooseOkOnNextConfirmation();
    selenium.clickAt("//div[@id='UIApplicationOrganizer']//div[@class='UIControlbar']//div[@class='IconControl ImportIcon']",
                     "1,1");
    String autoimport = selenium.getConfirmation();
    System.out.println("!! missing assert !!");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign out"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Sign out");
  }

  public void testSNF_PRL_10() throws Exception {
    selenium.setSpeed("500");
    selenium.open("/portal/public/classic/");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Application Registry", "1,1");
    selenium.clickAt("//div[@id='UIApplicationOrganizer']//div[@class='UIControlbar']/div[1]",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("name"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("name", "category_test");
    selenium.type("displayName", "Category test name");
    selenium.type("description", "category test description");
    selenium.click("//div[@class='WorkingArea']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Permission"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Permission", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Platform"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Platform", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=manager"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=manager", "1,1");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIApplicationOrganizer']//a[@class='ControlIcon EditIcon']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIApplicationOrganizer']//a[@class='ControlIcon EditIcon']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("displayName"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("displayName", "category test edit");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("category test edit"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Sign out"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Sign out");
  }

//  public void testSNF_PRL_11() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/public/classic/");
//    selenium.click("link=Sign in");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
//    selenium.waitForPageToLoad("30000");
//    selenium.clickAt("link=Application Registry", "1,1");
//    selenium.clickAt("//div[@class='ListContent']//div[@class='SelectedTab']//a[@class='ControlIcon CreateNewIcon']",
//                     "1,1");
//    selenium.clickAt("//input[@name='application' and @value='9']", "1,1");
//    selenium.type("displayName", "test10");
//    selenium.clickAt("css=form#UIAddApplicationForm div.UIAction div.ActionButton", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=category test edit"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=category test edit", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add Permission"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add Permission", "1,1");
//    selenium.clickAt("//div[@id='ListPermissionSelector']//a[@title='Organization']", "1,1");
//    selenium.clickAt("link=manager", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isTextPresent("test10"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Sign out"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.click("link=Sign out");
//  }
//
//  public void testSNF_PRL_13() throws Exception {
//    selenium.setSpeed("500");
//    selenium.clickAt("link=Gadget", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@class='UIControlbar']/div[1]"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@class='UIControlbar']/div[1]", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("url"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("url", "http://www.google.com/ig/modules/colorjunction.xml");
//    selenium.clickAt("link=Add", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isTextPresent("Gadget Details"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIGadgetInfo']//div[@class='UIBreadcumb']/div[@class='DownLoadIcon ControlIcon']",
//                     "1,1");
//    System.out.println("https://jira.jboss.org/jira/browse/GTNPORTAL-439");
//    assertTrue(selenium.isTextPresent("Gadget Details"));
//  }
//
//  public void testSNF_PRL_15() throws Exception {
//    selenium.setSpeed("500");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("link=Sign in");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/sitemap");
//    selenium.clickAt("//div[@id='UISiteMap']//div[@class='ClearFix']/div[2]", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isTextPresent("Blog"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    assertTrue(selenium.isTextPresent("New Staff"));
//    assertTrue(selenium.isTextPresent("Application Registry"));
//    selenium.clickAt("//div[@id='UISiteMap']//div[@class='ClearFix']/div[1]", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (!selenium.isElementPresent("Blog"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("css=div#UISiteMap div.ExpandIcon", "1,1");
//    selenium.clickAt("css=div#UISiteMap div.ChildrenContainer a", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (!selenium.isElementPresent("css=div#UISiteMap div.ChildrenContainer a"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//  }
//
//  public void testSNF_PRL_18() throws Exception {
//    selenium.setSpeed("500");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("link=Sign in");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/portalnavigation");
//    selenium.clickAt("//div[@id='UISiteManagement']//div[@class='UIAction']//div[@class='ActionButton BlueButton']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.click("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']");
//    selenium.type("name", "New_portal");
//    selenium.click("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[4]//div[@class='MiddleTab']");
//    selenium.click("publicMode");
//    selenium.clickAt("link=Edit Permission Setting", "1,1");
//    selenium.clickAt("link=Select Permission", "1,1");
//    selenium.clickAt("link=Platform", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='PermissionSelector']//a[@title='Administrators']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='PermissionSelector']//a[@title='Administrators']", "1,1");
//    selenium.clickAt("//div[@id='PermissionSelector']//a[@title='manager']", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (!selenium.isElementPresent("Permission Selector"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//form[@id='UIPortalForm']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']//div[@class='ButtonMiddle']/a",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isTextPresent("New_portal"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//  }
//
//  public void testSNF_PRL_19() throws Exception {
//    selenium.setSpeed("500");
//    selenium.click("link=Sign in");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.open("/portal/private/classic/");
//    selenium.click("link=Site");
//    selenium.waitForPageToLoad("30000");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Edit Layout"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Edit Layout", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("css=div#Administration/ApplicationRegistryPortlet"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.dragAndDropToObject("css=div#Administration/ApplicationRegistryPortlet",
//                                 "css=div#Administration/ApplicationRegistryPortlet");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIPortalComposer']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIPortalComposer']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isTextPresent("classic"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Edit Navigation", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add Node"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add Node", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("name"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("name", "Node_test");
//    selenium.type("label", "New node");
//    selenium.clickAt("css=div#UISiteManagement &gt; div.UIPopupWindow div.TabsContainer div.NormalTab div.MiddleTab",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Search and Select Page"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Search and Select Page", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.click("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("link=Edit Portal's Properties", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Edit Permission Setting"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Edit Permission Setting", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Select Permission"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Select Permission", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Platform"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Platform", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Administrators"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Administrators", "1,1");
//    selenium.clickAt("link=Platform", "1,1");
//    selenium.clickAt("link=Administrators", "1,1");
//    selenium.clickAt("//div[@id='PermissionSelector']//div[2]/a", "1,1");
//    selenium.clickAt("//form[@id='UIPortalForm']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']",
//                     "1,1");
//    selenium.open("/portal/private/classic/");
//    selenium.clickAt("link=New node", "1,1");
//  }
//
//  public void testSNF_PRL_20() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/private/classic/");
//    selenium.clickAt("link=Site", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add New Portal"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add New Portal", "1,1");
//    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PortalSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=PortalSetting')\"]",
//                     "1,1");
//    selenium.type("name", "Haha");
//    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=Properties')\"]",
//                     "1,1");
//    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=PermissionSetting')\"]",
//                     "1,1");
//    selenium.clickAt("publicMode", "1,1");
//    selenium.clickAt("link=Edit Permission Setting", "1,1");
//    selenium.clickAt("link=Select Permission", "1,1");
//    selenium.clickAt("link=Platform", "1,1");
//    selenium.clickAt("link=Platform", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@class='CollapseIcon']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=exact:*", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.click("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]");
//    selenium.waitForPageToLoad("30000");
//    assertFalse(selenium.isTextPresent("Haha"));
//  }
//
//  public void testSNF_PRL_21() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/private/classic/");
//    selenium.click("link=New_portal");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/portalnavigation");
//    selenium.click("link=New_portal");
//    selenium.waitForPageToLoad("30000");
//    selenium.clickAt("link=New_portal", "1,1");
//  }
//
//  public void testSNF_PRL_22() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/private/classic/");
//    selenium.click("link=Site");
//    selenium.waitForPageToLoad("30000");
//    selenium.clickAt("link=Edit Layout", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Portal Properties"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Portal Properties", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("locale"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.select("locale", "label=French (France)");
//    selenium.click("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']//div[3]//div[@class='MiddleTab']");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("publicMode"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.click("publicMode");
//    selenium.clickAt("link=Edit Permission Setting", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Select Permission"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Select Permission", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Platform"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Platform", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Users"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Users", "1,1");
//    selenium.clickAt("link=exact:*", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=classic"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=classic", "1,1");
//  }
//
//  public void testSNF_PRL_23() throws Exception {
//    selenium.setSpeed("500");
//    selenium.click("link=Sign in");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/");
//    selenium.click("link=Group");
//    selenium.waitForPageToLoad("30000");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add Navigation"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add Navigation", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Cancel"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Cancel", "1,1");
//    selenium.clickAt("link=Edit Navigation", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add Node"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add Node", "1,1");
//    selenium.type("name", "grp_node");
//    selenium.type("label", "new_grp_node");
//    selenium.clickAt("//div[@class='CenterHorizontalTabs']//div[@class='NormalTab']//div[@class='MiddleTab']",
//                     "1,1");
//    selenium.clickAt("link=Search and Select Page", "1,1");
//    selenium.clickAt("//div[@id='UIRepeater']//img[@class='SelectPageIcon']", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.check("//a[@onclick='eXo.portal.logout();']");
//    selenium.waitForPageToLoad("30000");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=new_grp_node"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.click("link=new_grp_node");
//    selenium.waitForPageToLoad("30000");
//  }
//
//  public void testSNF_PRL_24() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/private/classic/");
//    selenium.click("link=Group");
//    selenium.waitForPageToLoad("30000");
//    selenium.clickAt("//a[@class='EditProIcon']", "1,1");
//    selenium.type("description", "more and more");
//    selenium.clickAt("link=Save", "1,1");
//    assertTrue(selenium.isTextPresent(""));
//  }
//
//  public void testSNF_PRL_25() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/private/classic/");
//    selenium.click("link=Group");
//    selenium.waitForPageToLoad("30000");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Edit Navigation"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Edit Navigation", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add Node"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add Node", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("name"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("name", "Hihizzz");
//    selenium.type("label", "Hohohozzz");
//    selenium.clickAt("css=div#UIGroupNavigationManagement div.UIPopupWindow div.TabsContainer div.NormalTab div.MiddleTab",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Search and Select Page"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Search and Select Page", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//img[@title='Select Page']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//img[@title='Select Page']", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Save"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("link=Hohohozzz", "1,1");
//    selenium.open("/portal/private/classic/");
//  }
//
//  public void testSNF_PRL_27_1() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/private/classic/");
//    selenium.clickAt("link=Page Management", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIPageBrowser']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add New Page", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("pageName"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("pageName", "newpage21211");
//    selenium.type("pageDisplayName", "new page21211");
//    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
//    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='Administration/AccountPortlet']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=View Page properties", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isTextPresent("Show Max Window"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
//    selenium.clickAt("link=Edit Permission Setting", "1,1");
//    selenium.clickAt("link=Cancel", "1,1");
//    selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//img[@alt='']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Edit Page", "1,1");
//    selenium.clickAt("link=View Page properties", "1,1");
//    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
//    selenium.clickAt("link=Add Permission", "1,1");
//    selenium.clickAt("link=Platform", "1,1");
//    selenium.clickAt("//div[@id='UIPageFormPopupGroupMembershipSelector']//div[@class='MembershipSelector']//a",
//                     "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
//  }
//
//  public void testSNF_PRL_27_2() throws Exception {
//    selenium.setSpeed("500");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
//    selenium.waitForPageToLoad("30000");
//    selenium.click("link=Group");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/");
//    selenium.clickAt("link=Edit Layout", "1,1");
//    selenium.clickAt("link=Portal Properties", "1,1");
//    selenium.select("locale", "label=English");
//    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
//    selenium.clickAt("publicMode", "1,1");
//    selenium.clickAt("link=Edit Permission Setting", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isTextPresent("Current Permission"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Select Permission", "1,1");
//    selenium.click("link=Select Permission");
//    selenium.clickAt("//div[@id='PermissionSelector']/div/div[2]/div/div[2]/div/div/div[1]/a",
//                     "1,1");
//    selenium.clickAt("link=Platform", "1,1");
//    selenium.clickAt("link=exact:*", "1,1");
//    selenium.clickAt("//div[@id='UIPortalComposer']/div[1]/div/div/div/a[2]", "1,1");
//    selenium.clickAt("link=Save", "1,1");
//  }
//
//  public void testSNF_PRL_28() throws Exception {
//    selenium.setSpeed("500");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/");
//    selenium.clickAt("link=Dashboard", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add Gadgets"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add Gadgets", "1,1");
//    selenium.type("//input[@id='url']",
//                  "http://hosting.gmodules.com/ig/gadgets/file/112581010116074801021/treefrog.xml");
//    selenium.clickAt("//img[@class='AddNewNodeIcon']", "1,1");
//    selenium.clickAt("//div[@id='UIAddGadgetPopup']//div[@class='CloseButton']", "1,1");
//  }
//
//  public void testSNF_PRL_29() throws Exception {
//    selenium.setSpeed("500");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
//    selenium.waitForPageToLoad("30000");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add New Page"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add New Page", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("pageName"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("pageName", "dashboardpage12");
//    selenium.type("pageDisplayName", "dashboard new page12");
//    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
//    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
//    selenium.clickAt("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']", "1,1");
//    selenium.click("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]");
//    assertTrue(selenium.isTextPresent("dashboard new page"));
//    selenium.clickAt("link=Edit Page", "1,1");
//    selenium.clickAt("link=View Page properties", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("title"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("title", "new dashboard");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Edit Layout"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Edit Layout", "1,1");
//    selenium.clickAt("link=Portal Properties", "1,1");
//    selenium.select("locale", "label=English");
//    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=Properties')\"]",
//                     "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
//  }
//
//  public void testSNF_PRL_30() throws Exception {
//    selenium.setSpeed("500");
//    selenium.open("/portal/private/classic/");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Add New Page"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Add New Page", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIPageNodeSelector']//div[@class='HomeNode']/a"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIPageNodeSelector']//div[@class='HomeNode']/a", "1,1");
//    selenium.type("pageName", "test9");
//    selenium.type("pageDisplayName", "test9");
//    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=View Page properties"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=View Page properties", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("title"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("title", "test9_changed");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("css=a.EdittedSaveButton", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Edit Page"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Edit Page", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=View Page properties"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=View Page properties", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("title"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    assertEquals("test9_changed", selenium.getValue("title"));
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[3]//div[@class='MiddleTab']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[3]//div[@class='MiddleTab']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("publicMode"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("publicMode", "1,1");
//    selenium.clickAt("link=Add Permission", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Platform"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Platform", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Administrators"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Administrators", "1,1");
//    selenium.clickAt("link=exact:*", "1,1");
//    selenium.clickAt("link=Edit Permission Setting", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Select Permission"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Select Permission", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Platform"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Platform", "1,1");
//    selenium.clickAt("//div[@id='PermissionSelector']//div[@class='MembershipSelector']//div[@class='OverflowContainer']/div[3]/a",
//                     "1,1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("//div[@id='UIPageEditor']//div[@class='TLPortalComposer']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']",
//                     "1,1");
//    assertTrue(selenium.isTextPresent("test9"));
//    selenium.open("/portal/private/classic/");
//  }
//
//  public void testSNF_PRL_31() throws Exception {
//    selenium.setSpeed("500");
//    selenium.click("link=Sign in");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Change Language"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Change Language", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=French"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=French", "1,1");
//    selenium.click("link=Apply");
//    selenium.waitForPageToLoad("30000");
//    assertTrue(selenium.isTextPresent("Accueil"));
//    selenium.clickAt("link=Changer la langue", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=anglais"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=anglais", "1,1");
//    selenium.click("link=Appliquer");
//    selenium.waitForPageToLoad("30000");
//    assertTrue(selenium.isTextPresent("Home"));
//  }
//
//  public void testSNF_PRL_32() throws Exception {
//    selenium.setSpeed("500");
//    selenium.click("link=Sign in");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Change Skin"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Change Skin", "1,1");
//    selenium.clickAt("//div[@id='UITabContent']//div[@class='ItemListContainer']//div[@class='ItemList']//div[@class='SelectedItem Item']",
//                     "1,1");
//    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='ActionButton LightBlueStyle']",
//                     "1,1");
//    selenium.waitForPageToLoad("30000");
//  }
//
//  public void testSNF_PRL_34() throws Exception {
//    selenium.setSpeed("500");
//    selenium.click("link=Sign in");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
//    selenium.waitForPageToLoad("30000");
//    selenium.open("/portal/private/classic/");
//    selenium.clickAt("link=Root Root", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("email"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("email", "mytest.exo10@gmail.com");
//    selenium.type("lastName", "Root");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("link=OK", "1,1");
//    selenium.clickAt("link=Close", "1,1");
//  }
//
//  public void testSNF_PRL_35() throws Exception {
//    selenium.setSpeed("500");
//    selenium.type("username", "root");
//    selenium.type("password", "gtn");
//    selenium.clickAt("link=Sign in", "1,1");
//    selenium.open("/portal/private/classic/administration/pageManagement");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIPageBrowser']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIPageBrowser']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("name"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.type("name", "user_page1");
//    selenium.type("title", "user_page_title1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("//div[@id='UIPageBrowser']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.select("ownerType", "label=portal");
//    selenium.type("name", "portal_page1");
//    selenium.type("title", "portal_page_title1");
//    selenium.clickAt("link=Save", "1,1");
//    selenium.clickAt("//div[@id='UIPageBrowser']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.select("ownerType", "label=group");
//    selenium.clickAt("//option[@value='group']", "1,1");
//    selenium.type("name", "group_page1");
//    selenium.type("title", "group_page_title1");
//    selenium.clickAt("link=Save", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIRepeater']//img[@class='EditInfoIcon']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIRepeater']//img[@class='EditInfoIcon']", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIPageEditor']//div[@class='PageProfileIcon']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIPageEditor']//div[@class='PageProfileIcon']", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']//div[3]//div[@class='MiddleTab']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']//div[3]//div[@class='MiddleTab']",
//                     "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIListPermissionSelector']//input[@class='checkbox']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIListPermissionSelector']//input[@class='checkbox']", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("link=Save"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("link=Save", "1,1");
//    for (int second = 0;; second++) {
//      if (second >= 30)
//        fail("timeout");
//      try {
//        if (selenium.isElementPresent("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']"))
//          break;
//      } catch (Exception e) {
//      }
//      Thread.sleep(1000);
//    }
//    selenium.clickAt("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']",
//                     "1,1");
//    assertTrue(selenium.isTextPresent("user_page_title1"));
//    assertTrue(selenium.isTextPresent("group_page_title1"));
//    assertTrue(selenium.isTextPresent("portal_page_title1"));
//  }

}
