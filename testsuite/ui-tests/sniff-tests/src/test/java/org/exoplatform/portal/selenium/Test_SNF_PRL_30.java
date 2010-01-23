package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_30 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
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
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add New Page")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add New Page", "1,1");
System.out.println("--Choose \"root\" node");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("pageName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("pageName", "test_dashboardpage_30");
selenium.type("pageDisplayName", "test_dashboardpage_name_30");
System.out.println("--Click Next to move to step 2");
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
System.out.println("--Click Next to move to step 3, keep Empty layout");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
System.out.println("--Open Editor pane");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']", "1,1");
System.out.println("--Click Save to complete adding page");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("test_dashboardpage_name_30")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
assertTrue(selenium.isTextPresent("test_dashboardpage_name_30"));
System.out.println("--Edit page in dashboard");
selenium.clickAt("link=Edit Page", "1,1");
selenium.clickAt("link=View Page properties", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("title")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("title", "test_dashboardpage_edit_30");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Page Management")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
System.out.println("--Edit Dashboard layout");
selenium.clickAt("link=test_dashboardpage_name_30", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Edit Layout")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Edit Layout", "1,1");
selenium.clickAt("link=Portal Properties", "1,1");
selenium.select("locale", "label=English");
selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&objectId=Properties')\"]", "1,1");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
System.out.println("--Delete page");
selenium.clickAt("//div[@class='SelectedTab']//img[@class='CloseIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (selenium.getConfirmation().equals("Really want to remove this dashboard?")) {
break;
}
}
catch (Exception e) {
}
Thread.sleep(1000);
}
assertFalse(selenium.isTextPresent("test_dashboardpage_name_30"));
selenium.clickAt("link=Sign out", "1,1");
}

}
