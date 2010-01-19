package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_29 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_29() throws Exception {
selenium.setSpeed("500");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add New Page")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add New Page", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("pageName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("pageName", "dashboardpage12");
selenium.type("pageDisplayName", "dashboard new page12");
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
selenium.clickAt("//div[@onclick='eXo.portal.UIPortal.toggleComposer(this)']", "1,1");
selenium.click("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]");
assertTrue(selenium.isTextPresent("dashboard new page"));
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
selenium.type("title", "new dashboard");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']", "1,1");
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
selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=Properties')\"]", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("//div[@id='UIPortalComposer']//a[@class='EdittedSaveButton']", "1,1");
}

}
