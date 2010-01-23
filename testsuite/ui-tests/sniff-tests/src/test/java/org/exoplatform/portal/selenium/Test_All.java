package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;

public class Test_All extends SeleneseTestCase {
  public String speed   = "1000";

  public String browser = "firefox";

  public void setSpeed() {
    selenium.setSpeed(speed);
  }

  public void setUp() throws Exception {
    setUp("http://localhost:8080/portal/", "*" + browser);
  }

  public void testSNF_PRL_02() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-New Account-");
    System.out.println("--Register new account");
    selenium.click("link=Register");
    selenium.waitForPageToLoad("30000");
    selenium.type("User Name:", "test_user_02");
    selenium.type("Password:", "test_pwd_02");
    selenium.type("Confirm Password:", "test_pwd_02");
    selenium.type("First Name:", "test_name_first_02");
    selenium.type("Last Name:", "test_name_last_02");
    selenium.type("Email Address:", "test_user_02@yahoo.com");
    selenium.clickAt("link=Subscribe", "1,1");
    selenium.open("/portal/public/classic/");
    System.out.println("--Verification");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "test_user_02");
    selenium.type("password", "test_pwd_02");
    selenium.clickAt("//div[@id='UIPortalLoginFormAction']//a", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_name_first_02 test_name_last_02"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_name_first_02 test_name_last_02"));
    selenium.clickAt("link=Sign out", "1,1");
    System.out.println("--Delete new user");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.clickAt("//div[@id='UIPortalLoginFormAction']//a", "1,1");
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
    selenium.clickAt("//div[@class='UIListUsers']//tbody/tr[5]//img[@class='DeleteUserIcon']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure you want to delete test_user_02 user?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_03() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-Change Language-");
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
    assertTrue(selenium.isElementPresent("link=Sign in"));
  }

  public void testSNF_PRL_04() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-SignInOut-");
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
    assertTrue(selenium.isElementPresent("link=Sign in"));
  }

  public void testSNF_PRL_07() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-GroupManagement-");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Select \"Users and groups management\" in menu");
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
    selenium.clickAt("link=Group", "1,1");
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
    System.out.println("--Select \"Organization\" group from group tree");
    selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[3]//div[@class='ExpandIcon']/a",
                     "1,1");
    System.out.println("--Select \"Management group\" from group tree");
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
    System.out.println("--Click Add new group icon");
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
    selenium.type("groupName", "test_group_name_07");
    selenium.type("label", "test_group_label_07");
    selenium.type("description", "test_group_description_07");
    System.out.println("--Click \"Save\" to complete adding new group");
    selenium.clickAt("//form[@id='UIGroupForm']//div[@class='ActionButton LightBlueStyle']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_group_label_07"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Click \"Select User\" icon");
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
    selenium.clickAt("demo", "1,1");
    selenium.clickAt("john", "1,1");
    selenium.clickAt("mary", "1,1");
    selenium.clickAt("root", "1,1");
    System.out.println("--Click \"Add\" button");
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
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_08() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-MembershipManagement-");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Select \"Users and groups management\" in menu");
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
    System.out.println("--Choose \"Memebership Management\" tab");
    selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='ManagementIconContainer']/a[@class='MembershipButton']",
                     "1,1");
    System.out.println("--Create new membership");
    selenium.type("name", "test_name_08");
    selenium.type("description", "test_description_08");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_name_08"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_name_08"));
    System.out.println("--Edit membership");
    selenium.clickAt("//table[@class='UIGrid']//tbody/tr[2]/td[5]//img[@class='EditMembershipIcon']",
                     "1,1");
    selenium.type("description", "test_description_edit_08");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_description_edit_08"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_description_edit_08"));
    System.out.println("--Delete membership");
    selenium.clickAt("//table[@class='UIGrid']//tbody/tr[2]/td[5]//img[@class='DeleteMembershipIcon']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure you want to delete this membership?")) {
          break;
        }
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
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-AutoImport-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Select \"Application Registry\"");
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
    System.out.println("--Auto Import");
    assertFalse(selenium.isTextPresent("WSRP Admin Portlet"));
    selenium.clickAt("//div[@id='UIApplicationOrganizer']//div[@class='UIControlbar']//div[@class='IconControl ImportIcon']",
                     "1,1");
    String autoimport = selenium.getConfirmation();
    assertTrue(selenium.isTextPresent("WSRP Admin Portlet"));
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
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_10() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-CategoryManagement-");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Select \"Application Registry\"");
    selenium.clickAt("link=Application Registry", "1,1");
    System.out.println("--Add Category");
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
    selenium.type("name", "test_name_category_10");
    selenium.type("displayName", "test_displayname_category_10");
    selenium.type("description", "test_description_category_10");
    System.out.println("--Select permissions");
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
    assertTrue(selenium.isTextPresent("test_displayname_category_10"));
    System.out.println("--Edit Category");
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
    selenium.type("displayName", "test_displayname_edit_10");
    selenium.clickAt("link=Save", "1,1");
    assertTrue(selenium.isTextPresent("test_displayname_edit_10"));
    System.out.println("--Delete Category");
    selenium.clickAt("//div[@class='SelectedTab']//a[@class='ControlIcon DeleteIcon']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation()
                    .equals("Are you sure to delete this category and all applications on it?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertFalse(selenium.isTextPresent("test_displayname_edit_10"));
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

  public void testSNF_PRL_12() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-ViewAllPortlets-");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Application Registry", "1,1");
    System.out.println("--Verify details of Administration>>Application Registry");
    selenium.clickAt("//div[@class='UIVerticalSlideTabs']//div[@class='UIVTabContent']//div[1]//a",
                     "1,1");
    assertTrue(selenium.isTextPresent("ApplicationRegistryPortlet"));
    System.out.println("--Verify details of Administration>>Organisation Management");
    selenium.clickAt("//div[@class='UIVerticalSlideTabs']//div[@class='UIVTabContent']//div[2]//a",
                     "1,1");
    assertTrue(selenium.isTextPresent("OrganizationPortlet"));
    System.out.println("--Verify details of Administration>>NewAccount");
    selenium.clickAt("//div[@class='UIVerticalSlideTabs']//div[@class='UIVTabContent']//div[3]//a",
                     "1,1");
    assertTrue(selenium.isTextPresent("AccountPortlet"));
    System.out.println("--Verify details of Dashboard>>Dashboard Portlet");
    selenium.clickAt("//div[@class='UIVerticalSlideTabs']/div[2]//a", "1,1");
    selenium.clickAt("//div[@class='UIVerticalSlideTabs']//div[@class='UIVTabContent']//div[1]//a",
                     "1,1");
    assertTrue(selenium.isTextPresent("DashboardPortlet"));
    System.out.println("--Verify details of Dashboard>>Gadget Wrapper Portlet");
    selenium.clickAt("//div[@class='UIVerticalSlideTabs']//div[@class='UIVTabContent']//div[2]//a",
                     "1,1");
    assertTrue(selenium.isTextPresent("GadgetPortlet"));
    System.out.println("--It is possible to verify all portlets.......");
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

  public void testSNF_PRL_13() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-AddGadget-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Application Registry", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Gadget"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Gadget", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@class='UIControlbar']/div[1]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@class='UIControlbar']/div[1]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("url"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("url", "http://www.google.com/ig/modules/datetime.xml");
    selenium.clickAt("link=Add", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("Gadget Details"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIGadgetInfo']//div[@class='UIBreadcumb']/div[@class='DownLoadIcon ControlIcon']",
                     "1,1");
    System.out.println("https://jira.jboss.org/jira/browse/GTNPORTAL-439");
    assertTrue(selenium.isTextPresent("Gadget Details"));
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
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_14() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-ImportApplicationIcon-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Application Registry", "1,1");
    System.out.println("---Have no idea on how to do this");
    selenium.clickAt("link=Edit Page", "1,1");
    selenium.mouseOver("//div[@class='UIComponentBlock']");
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
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_15() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-SiteMapAndLinkToPage-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.open("/portal/private/classic/sitemap");
    System.out.println("--Expand the first submenu");
    selenium.clickAt("css=div#UISiteMap div.ExpandIcon", "1,1");
    System.out.println("--Select the first link of submenu");
    selenium.clickAt("css=div#UISiteMap div.ChildrenContainer a", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (!selenium.isElementPresent("css=div#UISiteMap div.ChildrenContainer a"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_16() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-ExpandAll-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.open("/portal/private/classic/sitemap");
    System.out.println("--Expand All");
    selenium.clickAt("//div[@id='UISiteMap']//div[@class='ClearFix']/div[2]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("Blog"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("New Staff"));
    assertTrue(selenium.isTextPresent("Application Registry"));
    assertFalse(selenium.isElementPresent("//div[@class='ExpandIcon FloatLeft']"));
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_17() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-CollapseAll-");
    selenium.click("link=Sign in");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.open("/portal/private/classic/sitemap");
    System.out.println("--Expand SiteMap tree");
    selenium.clickAt("//div[@id='UISiteMap']//div[@class='ClearFix']/div[2]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("Blog"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("New Staff"));
    assertTrue(selenium.isTextPresent("Application Registry"));
    System.out.println("--Collapse SiteMap Tree");
    selenium.clickAt("//div[@id='UISiteMap']//div[@class='ClearFix']/div[1]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (!selenium.isElementPresent("Blog"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertFalse(selenium.isElementPresent("//div[@class='CollapseIcon FloatLeft']"));
    selenium.click("link=Sign out");
  }

  public void testSNF_PRL_18() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-CreateNewPortal-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.open("/portal/private/classic/portalnavigation");
    System.out.println("--Add new portal");
    selenium.clickAt("//div[@id='UISiteManagement']//div[@class='UIAction']//div[@class='ActionButton BlueButton']",
                     "1,1");
    System.out.println("--Select portal settings");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']",
                     "1,1");
    selenium.type("name", "test_portal_18");
    System.out.println("--Select permission settings");
    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[4]//div[@class='MiddleTab']",
                     "1,1");
    selenium.clickAt("publicMode", "1,1");
    selenium.clickAt("link=Edit Permission Setting", "1,1");
    selenium.clickAt("link=Select Permission", "1,1");
    selenium.clickAt("link=Platform", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='PermissionSelector']//a[@title='Administrators']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='PermissionSelector']//a[@title='Administrators']", "1,1");
    selenium.clickAt("//div[@id='PermissionSelector']//a[@title='manager']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (!selenium.isElementPresent("Permission Selector"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//form[@id='UIPortalForm']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']//div[@class='ButtonMiddle']/a",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_portal_18"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Verify portal creation");
    assertTrue(selenium.isElementPresent("link=test_portal_18"));
    System.out.println("--Delete new portal");
    selenium.clickAt("link=Site", "1,1");
    assertTrue(selenium.isTextPresent("Portal Navigation"));
    selenium.clickAt("//div[@id='UISiteManagement']/table[2]//a[@class='DeleteIcon']", "1,1");
    assertFalse(selenium.isTextPresent("test_portal_18"));
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure you want to delete this portal?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_20() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-DeletePortal-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Site", "1,1");
    System.out.println("--Add new portal");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add New Portal"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add New Portal", "1,1");
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PortalSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=PortalSetting')\"]",
                     "1,1");
    selenium.type("name", "test_portal_name_20");
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=Properties')\"]",
                     "1,1");
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=PermissionSetting')\"]",
                     "1,1");
    selenium.clickAt("publicMode", "1,1");
    selenium.clickAt("link=Edit Permission Setting", "1,1");
    selenium.clickAt("link=Select Permission", "1,1");
    selenium.clickAt("link=Platform", "1,1");
    selenium.clickAt("link=Platform", "1,1");
    selenium.clickAt("link=exact:*", "1,1");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_portal_name_20"));
    System.out.println("--Delete portal");
    selenium.click("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure you want to delete this portal?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertFalse(selenium.isTextPresent("test_portal_name_20"));
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
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_21() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-ChangePortal-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Site", "1,1");
    System.out.println("--Add new portal");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add New Portal"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add New Portal", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PortalSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=PortalSetting')\"]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PortalSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=PortalSetting')\"]",
                     "1,1");
    selenium.type("name", "test_portal_name_21");
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=Properties')\"]",
                     "1,1");
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=PermissionSetting')\"]",
                     "1,1");
    selenium.clickAt("publicMode", "1,1");
    selenium.clickAt("link=Edit Permission Setting", "1,1");
    selenium.clickAt("link=Select Permission", "1,1");
    selenium.clickAt("link=Platform", "1,1");
    selenium.clickAt("link=Platform", "1,1");
    selenium.clickAt("link=exact:*", "1,1");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_portal_name_21"));
    System.out.println("--View new portal");
    selenium.clickAt("link=test_portal_name_21", "1,1");
    selenium.clickAt("link=Site", "1,1");
    System.out.println("--Delete new portal");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UISiteManagement']/table[2]//td[3]/a[4]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UISiteManagement']/table[2]//td[3]/a[4]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure you want to delete this portal?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
  }

  public void testSNF_PRL_23() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-AddNavigation-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.open("/portal/private/classic/");
    selenium.click("link=Group");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Navigation"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Navigation", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Cancel"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Cancel", "1,1");
    selenium.clickAt("link=Edit Navigation", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Node"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Node", "1,1");
    selenium.type("name", "test_grp_node_23");
    selenium.type("label", "test_grp_label_23");
    System.out.println("--Choose \"Page Selector\" tab");
    selenium.clickAt("//div[@class='CenterHorizontalTabs']//div[@class='NormalTab']//div[@class='MiddleTab']",
                     "1,1");
    selenium.clickAt("link=Search and Select Page", "1,1");
    System.out.println("--Select the first page from pages list");
    selenium.clickAt("//div[@id='UIRepeater']//img[@class='SelectPageIcon']", "1,1");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=test_grp_label_23"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=test_grp_label_23");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Delete new group navigation");
    System.out.println("-----------");
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_24() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-EditDeleteNavigation-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.open("/portal/private/classic/");
    selenium.click("link=Group");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Navigation"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Navigation", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Cancel"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Cancel", "1,1");
    selenium.clickAt("link=Edit Navigation", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Node"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Node", "1,1");
    selenium.type("name", "test_grp_node_24");
    selenium.type("label", "test_grp_label_24");
    System.out.println("--Choose \"Page Selector\" tab");
    selenium.clickAt("//div[@class='CenterHorizontalTabs']//div[@class='NormalTab']//div[@class='MiddleTab']",
                     "1,1");
    selenium.clickAt("link=Search and Select Page", "1,1");
    System.out.println("--Select the first page from pages list");
    selenium.clickAt("//div[@id='UIRepeater']//img[@class='SelectPageIcon']", "1,1");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=test_grp_label_24"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=test_grp_label_24");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Edit navigation properties");
    selenium.click("link=Group");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("//div[@id='UIGroupNavigationGrid']/table[4]//td[@class='ActionBlock']//a[@class='EditProIcon']",
                     "1,1");
    selenium.type("description", "test_description_edit_24");
    selenium.select("priority", "10");
    selenium.clickAt("link=Save", "1,1");
    assertTrue(selenium.isTextPresent("Description: test_description_edit_24"));
    System.out.println("--Delete new group navigation");
    System.out.println("-----------");
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_25() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-EditNavActions_Rightclickmenu-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.click("link=Group");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Edit Navigation"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Edit Navigation", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Node"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Node", "1,1");
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
    selenium.type("name", "test_grp_node_25");
    selenium.type("label", "test_grp_label_25");
    selenium.clickAt("css=div#UIGroupNavigationManagement div.UIPopupWindow div.TabsContainer div.NormalTab div.MiddleTab",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Search and Select Page"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Search and Select Page", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//img[@title='Select Page']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//img[@title='Select Page']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Save"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=test_grp_label_25", "1,1");
    System.out.println("--Edit node's page");
    System.out.println("---Rightclick on link in group");
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_26() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-MoveUp/DownNode-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Create node");
    selenium.click("link=Group");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Edit Navigation"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Edit Navigation", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Node"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Node", "1,1");
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
    selenium.type("name", "test_grp_node_26");
    selenium.type("label", "test_grp_label_26");
    selenium.clickAt("css=div#UIGroupNavigationManagement div.UIPopupWindow div.TabsContainer div.NormalTab div.MiddleTab",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Search and Select Page"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Search and Select Page", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//img[@title='Select Page']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//img[@title='Select Page']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Save"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=test_grp_label_26", "1,1");
    System.out.println("--Edit node's position");
    System.out.println("---Rightclick on link in group");
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_27() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-AddEditGroupPageWizard-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Page Management", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPageBrowser']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Create new");
    selenium.clickAt("link=Add New Page", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("pageName"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("pageName", "test_page_27");
    selenium.type("pageDisplayName", "test_page_name_27");
    System.out.println("--Click \"Next\" to move to step2 to choose page layout");
    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
    System.out.println("--Keep \"Empty layout\" and Click \"Next\" to move to step 3");
    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
    System.out.println("--Open Page Editor pane");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='Administration/AccountPortlet']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=View Page properties", "1,1");
    System.out.println("--Select Permission Setting tab");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("Show Max Window"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
    selenium.clickAt("link=Edit Permission Setting", "1,1");
    System.out.println("--Do not change anything in Page properties");
    selenium.clickAt("link=Cancel", "1,1");
    selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//img[@alt='']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Click Save to complete adding new page by wizard with no content (no portlet)");
    selenium.clickAt("link=Edit Page", "1,1");
    System.out.println("--Show form to edit page by wizard");
    selenium.clickAt("link=View Page properties", "1,1");
    System.out.println("--Select Permission Setting tab");
    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
    selenium.clickAt("link=Add Permission", "1,1");
    selenium.clickAt("link=Platform", "1,1");
    selenium.clickAt("link=Administrators", "1,1");
    selenium.clickAt("//div[@id='UIPageFormPopupGroupMembershipSelector']//div[@class='MembershipSelector']//a",
                     "1,1");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
    System.out.println("--Edit layout");
    selenium.open("/portal/private/classic/");
    selenium.clickAt("link=Edit Layout", "1,1");
    selenium.clickAt("link=Portal Properties", "1,1");
    selenium.select("locale", "label=French (France)");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("//div[@id='UIPortalComposer']/div[1]/div/div/div/a[2]", "1,1");
    selenium.open("/portal/private/classic/");
    selenium.clickAt("link=Edit Layout", "1,1");
    selenium.clickAt("link=Portal Properties", "1,1");
    assertTrue(selenium.isTextPresent("French (France)"));
    selenium.clickAt("link=Cancel", "1,1");
    selenium.clickAt("//div[@id='UIPortalComposer']/div[1]/div/div/div/a[1]", "1,1");
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_30() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-DashboardSiteManagement-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Dashboard", "1,1");
    System.out.println("--Add new page in dashboard");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add New Page"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add New Page", "1,1");
    System.out.println("--Choose \"root\" node");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("pageName"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("pageName", "test_dashboardpage_30");
    selenium.type("pageDisplayName", "test_dashboardpage_name_30");
    System.out.println("--Click Next to move to step 2");
    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
    System.out.println("--Click Next to move to step 3, keep Empty layout");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
    System.out.println("--Open Editor pane");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']", "1,1");
    System.out.println("--Click Save to complete adding page");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_dashboardpage_name_30"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_dashboardpage_name_30"));
    System.out.println("--Edit page in dashboard");
    selenium.clickAt("link=Edit Page", "1,1");
    selenium.clickAt("link=View Page properties", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("title"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("title", "test_dashboardpage_edit_30");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Page Management"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Edit Dashboard layout");
    selenium.clickAt("link=test_dashboardpage_name_30", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Edit Layout"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Edit Layout", "1,1");
    selenium.clickAt("link=Portal Properties", "1,1");
    selenium.select("locale", "label=English");
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=Properties')\"]",
                     "1,1");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
    System.out.println("--Delete page");
    selenium.clickAt("//div[@class='SelectedTab']//img[@class='CloseIcon']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Really want to remove this dashboard?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertFalse(selenium.isTextPresent("test_dashboardpage_name_30"));
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_31() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-ChangeLanguagePrivateMode-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Change Language"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Change Language", "1,1");
    System.out.println("--Change to French");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=French"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=French", "1,1");
    selenium.click("link=Apply");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Verify");
    assertTrue(selenium.isTextPresent("Accueil"));
    selenium.clickAt("link=Changer la langue", "1,1");
    System.out.println("--Change back to English");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=anglais"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=anglais", "1,1");
    selenium.click("link=Appliquer");
    selenium.waitForPageToLoad("30000");
    assertTrue(selenium.isTextPresent("Home"));
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_32() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-ChangeDisplaySkin-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Change Skin"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Change Skin", "1,1");
    selenium.clickAt("//div[@id='UITabContent']//div[@class='ItemListContainer']//div[@class='ItemList']//div[@class='SelectedItem Item']",
                     "1,1");
    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='ActionButton LightBlueStyle']",
                     "1,1");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Verify");
    System.out.println("---------");
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_05() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-RememberMyLogin-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.clickAt("rememberme", "1,1");
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
    selenium.clickAt("link=Sign out", "1,1");
    selenium.clickAt("link=Sign in", "1,1");
    System.out.println("##\"Sign out\" resets \"rememberme\"");
    verifyTrue(selenium.isChecked("rememberme"));
    selenium.clickAt("link=Discard", "1,1");
  }

  public void testSNF_PRL_06() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-UserManagement-");
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
    System.out.println("--Edit fields");
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
    selenium.type("firstName", "test_user_06");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton",
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
    selenium.type("user.name.given", "test_name_given_06");
    selenium.type("user.name.family", "test_name_family_06");
    selenium.type("user.name.nickName", "test_name_nick_06");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("The user profile has been updated."));
    selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton",
                     "1,1");
    selenium.clickAt("link=Cancel", "1,1");
    System.out.println("--Verify changes");
    assertTrue(selenium.isTextPresent("test_user_06"));
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
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_11() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-AddApplicationToCategory-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Application Registry", "1,1");
    System.out.println("--Add application to Administration Category");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@class='ListContent']//div[@class='SelectedTab']//a[@class='ControlIcon CreateNewIcon']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@class='ListContent']//div[@class='SelectedTab']//a[@class='ControlIcon CreateNewIcon']",
                     "1,1");
    System.out.println("--Select first application in list");
    selenium.clickAt("//input[@name='application' and @value='1']", "1,1");
    selenium.type("displayName", "test_displayname_11");
    selenium.clickAt("css=form#UIAddApplicationForm div.UIAction div.ActionButton", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//a[@class='TabLabel' and @title='Administration']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Edit category permissions");
    selenium.clickAt("//a[@class='TabLabel' and @title='Administration']", "1,1");
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
    selenium.clickAt("//div[@id='ListPermissionSelector']//a[@title='Organization']", "1,1");
    selenium.clickAt("link=manager", "1,1");
    assertTrue(selenium.isTextPresent("test_displayname_11"));
    selenium.clickAt("//div[@class='IconControl ImportIcon']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation()
                    .equals("This action will automatically create categories and import all the gadgets and portlets on it.")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Delete application");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@class='ListContent']//div[@class='UIVTabContent']/div[8]//a[@class='ControlIcon DeletePortalIcon']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@class='ListContent']//div[@class='UIVTabContent']/div[8]//a[@class='ControlIcon DeletePortalIcon']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure to delete this application?")) {
          break;
        }
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
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_19() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-EditPortalNavigation-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.click("link=Site");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Edit Portal layout, currently do not change anything");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Edit Layout"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Edit Layout", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("css=div#Administration/ApplicationRegistryPortlet"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.dragAndDropToObject("css=div#Administration/ApplicationRegistryPortlet",
                                 "css=div#Administration/ApplicationRegistryPortlet");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPortalComposer']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPortalComposer']//div[@class='OverflowContainer']/a[@class='CloseButton']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("classic"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    System.out.println("--Edit nav: add node, actions ...");
    selenium.clickAt("link=Edit Navigation", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Node"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Node", "1,1");
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
    selenium.type("name", "test_nodename_19");
    selenium.type("label", "test_node_label_19");
    System.out.println("--Select Page");
    selenium.clickAt("css=div#UISiteManagement > div.UIPopupWindow div.TabsContainer div.NormalTab div.MiddleTab",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Search and Select Page"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Search and Select Page", "1,1");
    System.out.println("--Select the first page");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img");
    System.out.println("--Save");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=Save", "1,1");
    System.out.println("--Edit Portal Properties");
    selenium.clickAt("link=Edit Portal's Properties", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Edit Permission Setting"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Edit Permission Setting", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Select Permission"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Select Permission", "1,1");
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
        if (selenium.isElementPresent("link=Administrators"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Administrators", "1,1");
    selenium.clickAt("//div[@id='PermissionSelector']//div[2]/a", "1,1");
    selenium.clickAt("//form[@id='UIPortalForm']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']",
                     "1,1");
    selenium.open("/portal/private/classic/");
    System.out.println("--Select new node");
    selenium.clickAt("link=test_node_label_19", "1,1");
    assertTrue(selenium.isElementPresent("//div[@class='SelectedNavigationTab']//a"));
    System.out.println("--Delete node");
    selenium.clickAt("link=Edit Navigation", "1,1");
    selenium.click("link=Delete Node");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure you want to delete this node?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("link=Save");
    System.out.println("--Verify Deletion");
    selenium.click("link=Home");
    assertFalse(selenium.isTextPresent("test_node_label_19"));
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_28() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-ActionsDashboardpage-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Dashboard", "1,1");
    System.out.println("--Add gadgets into dashboard page");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Gadgets"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Gadgets", "1,1");
    System.out.println("--By url");
    selenium.type("//input[@id='url']", "http://www.google.com/ig/modules/datetime.xml");
    selenium.clickAt("//img[@class='AddNewNodeIcon']", "1,1");
    selenium.clickAt("//div[@id='UIAddGadgetPopup']//div[@class='CloseButton']", "1,1");
    assertTrue(selenium.isElementPresent("//div[@class='GadgetTitle']"));
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@class='CloseGadget IconControl' and @title='Delete Gadget']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@class='CloseGadget IconControl' and @title='Delete Gadget']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure to delete this gadget?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add Gadgets"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add Gadgets", "1,1");
    System.out.println("--By drag and drop");
    selenium.dragAndDropToObject("//div[@class='GadgetTitle' and @title='Calculator']",
                                 "//div[@class='GadgetTitle' and @title='Calculator']");
    selenium.clickAt("//div[@id='UIAddGadgetPopup']//div[@class='CloseButton']", "1,1");
    assertTrue(selenium.isElementPresent("//div[@class='GadgetTitle']"));
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@class='CloseGadget IconControl']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@title='Delete Gadget']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure to delete this gadget?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("Drag your gadgets here."));
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_29() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-AddEditPageEditLayoutDashboard-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Dashboard", "1,1");
    System.out.println("--Add new page in dashboard");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Add New Page"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Add New Page", "1,1");
    System.out.println("--Choose \"root\" node");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("pageName"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.type("pageName", "test_dashboardpage_29");
    selenium.type("pageDisplayName", "test_dashboardpage_name_29");
    System.out.println("--Click Next to move to step 2");
    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
    System.out.println("--Click Next to move to step 3, keep Empty layout");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
    System.out.println("--Open Editor pane");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']", "1,1");
    System.out.println("--Click Save to complete adding page");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_dashboardpage_name_29"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_dashboardpage_name_29"));
    System.out.println("--Edit page in dashboard");
    selenium.type("1", "test_dashboardpage_edit_29");
    System.out.println("--Edit Dashboard layout");
    selenium.clickAt("link=test_dashboardpage_edit_29", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Edit Layout"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Edit Layout", "1,1");
    selenium.clickAt("link=Portal Properties", "1,1");
    selenium.select("locale", "label=English");
    selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=Properties')\"]",
                     "1,1");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
    System.out.println("--Delete page");
    selenium.clickAt("//div[@class='SelectedTab']//img[@class='CloseIcon']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Really want to remove this dashboard?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertFalse(selenium.isTextPresent("test_dashboardpage_edit_29"));
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_33() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-AccountSetting-");
    System.out.println("--Register new account");
    selenium.clickAt("link=Register", "1,1");
    selenium.type("User Name:", "test_user_33");
    selenium.type("Password:", "test_pwd_33");
    selenium.type("Confirm Password:", "test_pwd_33");
    selenium.type("First Name:", "test_name_first_33");
    selenium.type("Last Name:", "test_name_last_33");
    selenium.type("Email Address:", "test_user_33@yahoo.com");
    selenium.clickAt("link=Subscribe", "1,1");
    selenium.open("/portal/public/classic/");
    System.out.println("--Sign in and modify");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "test_user_33");
    selenium.type("password", "test_pwd_33");
    selenium.clickAt("//div[@id='UIPortalLoginFormAction']//a", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_name_first_33 test_name_last_33"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=test_name_first_33 test_name_last_33", "1,1");
    selenium.clickAt("link=Change Password", "1,1");
    selenium.type("currentpass", "test_pwd_33");
    selenium.type("newpass", "test_pwd_33_edit");
    selenium.type("confirmnewpass", "test_pwd_33_edit");
    selenium.clickAt("link=Save", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=OK"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=OK", "1,1");
    selenium.clickAt("link=Close", "1,1");
    selenium.click("link=Sign out");
    selenium.waitForPageToLoad("30000");
    System.out.println("--Verify modifications");
    selenium.clickAt("link=Sign in", "1,1");
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
    selenium.type("username", "test_user_33");
    selenium.type("password", "test_pwd_33_edit");
    selenium.clickAt("//div[@id='UIPortalLoginFormAction']//a", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isTextPresent("test_name_first_33 test_name_last_33"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("test_name_first_33 test_name_last_33"));
    selenium.clickAt("link=Sign out", "1,1");
    System.out.println("--Delete new user");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.clickAt("//div[@id='UIPortalLoginFormAction']//a", "1,1");
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
    selenium.clickAt("//div[@class='UIListUsers']//tbody/tr[5]//img[@class='DeleteUserIcon']",
                     "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.getConfirmation().equals("Are you sure you want to delete test_user_33 user?")) {
          break;
        }
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_34() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic");
    System.out.println("-LogoPortletAccSetting-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Dashboard", "1,1");
    System.out.println("--Edit Logo Picture");
    selenium.clickAt("link=Edit Layout", "1,1");
    selenium.clickAt("//form[@id='UILogoEditMode']/div[2]/div/div/table/tbody/tr/td/div/div/div/div",
                     "1,1");
    selenium.type("logoUrl", "url to define !!");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("link=Close", "1,1");
    selenium.clickAt("//div[@id='UIPortalComposer']/div[1]/div/div/div/a[2]", "1,1");
    assertTrue(selenium.isElementPresent("//div[@id='UILogoPortlet']/a/img[@src='/eXoResources/skin/sharedImages/DashboardIcon.png']"));
    selenium.clickAt("link=Sign out", "1,1");
  }

  public void testSNF_PRL_22() throws Exception {
    setSpeed();
    selenium.open("/portal/public/classic/");
    System.out.println("-EditPortalLayout-");
    selenium.clickAt("link=Sign in", "1,1");
    selenium.type("username", "root");
    selenium.type("password", "gtn");
    selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
    selenium.waitForPageToLoad("30000");
    selenium.click("link=Site");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Edit Layout", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Portal Properties"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Portal Properties", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("locale"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.select("locale", "label=French (France)");
    selenium.click("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']//div[3]//div[@class='MiddleTab']");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("publicMode"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.click("publicMode");
    selenium.clickAt("link=Edit Permission Setting", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Select Permission"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Select Permission", "1,1");
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
        if (selenium.isElementPresent("link=Users"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Users", "1,1");
    selenium.clickAt("link=exact:*", "1,1");
    selenium.clickAt("link=Save", "1,1");
    selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=classic"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=classic", "1,1");
    System.out.println("--Verify");
    selenium.click("link=Site");
    selenium.waitForPageToLoad("30000");
    selenium.clickAt("link=Edit Layout", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=Portal Properties"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=Portal Properties", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("locale"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    assertTrue(selenium.isTextPresent("French (France)"));
    selenium.select("locale", "label=French (France)");
    selenium.click("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']//div[3]//div[@class='MiddleTab']");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("publicMode"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
    for (int second = 0;; second++) {
      if (second >= 30)
        fail("timeout");
      try {
        if (selenium.isElementPresent("link=classic"))
          break;
      } catch (Exception e) {
      }
      Thread.sleep(1000);
    }
    selenium.clickAt("link=classic", "1,1");
    selenium.clickAt("link=Sign out", "1,1");
  }

}
