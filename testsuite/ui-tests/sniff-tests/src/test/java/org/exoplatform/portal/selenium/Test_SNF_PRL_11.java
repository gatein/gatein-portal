package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_11 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_11() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/public/classic/");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
selenium.clickAt("link=Application Registry", "1,1");
selenium.clickAt("//div[@class='ListContent']//div[@class='SelectedTab']//a[@class='ControlIcon CreateNewIcon']", "1,1");
selenium.clickAt("//input[@name='application' and @value='9']", "1,1");
selenium.type("displayName", "test10");
selenium.clickAt("css=form#UIAddApplicationForm div.UIAction div.ActionButton", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=category test edit")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=category test edit", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Permission")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Permission", "1,1");
selenium.clickAt("//div[@id='ListPermissionSelector']//a[@title='Organization']", "1,1");
selenium.clickAt("link=manager", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("test10")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Sign out")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("link=Sign out");
}

}
