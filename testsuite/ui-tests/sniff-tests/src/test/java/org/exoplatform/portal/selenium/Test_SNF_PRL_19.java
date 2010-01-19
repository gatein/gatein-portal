package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_19 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_19() throws Exception {
selenium.setSpeed("500");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.open("/portal/private/classic/");
selenium.click("link=Site");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Edit Layout")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Edit Layout", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("css=div#Administration/ApplicationRegistryPortlet")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.dragAndDropToObject("css=div#Administration/ApplicationRegistryPortlet","css=div#Administration/ApplicationRegistryPortlet");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPortalComposer']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPortalComposer']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("classic")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Edit Navigation", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Node")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Node", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("name")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("name", "Node_test");
selenium.type("label", "New node");
selenium.clickAt("css=div#UISiteManagement &gt; div.UIPopupWindow div.TabsContainer div.NormalTab div.MiddleTab", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Search and Select Page")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Search and Select Page", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("//div[@id='UIRepeater']//table//tbody/tr/td[5]/div[@class='ActionContainer']/img");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Edit Portal's Properties", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Edit Permission Setting")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Edit Permission Setting", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Select Permission")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Select Permission", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Platform")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Platform", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Administrators")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Administrators", "1,1");
selenium.clickAt("link=Platform", "1,1");
selenium.clickAt("link=Administrators", "1,1");
selenium.clickAt("//div[@id='PermissionSelector']//div[2]/a", "1,1");
selenium.clickAt("//form[@id='UIPortalForm']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']", "1,1");
selenium.open("/portal/private/classic/");
selenium.clickAt("link=New node", "1,1");
}

}
