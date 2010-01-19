package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_18 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_18() throws Exception {
selenium.setSpeed("500");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("link=Sign in");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/portalnavigation");
selenium.clickAt("//div[@id='UISiteManagement']//div[@class='UIAction']//div[@class='ActionButton BlueButton']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']");
selenium.type("name", "New_portal");
selenium.click("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[4]//div[@class='MiddleTab']");
selenium.click("publicMode");
selenium.clickAt("link=Edit Permission Setting", "1,1");
selenium.clickAt("link=Select Permission", "1,1");
selenium.clickAt("link=Platform", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='PermissionSelector']//a[@title='Administrators']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='PermissionSelector']//a[@title='Administrators']", "1,1");
selenium.clickAt("//div[@id='PermissionSelector']//a[@title='manager']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (!selenium.isElementPresent("Permission Selector"))
 break;
 }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//form[@id='UIPortalForm']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']//div[@class='ButtonMiddle']/a", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("New_portal")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
}

}
