package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_10 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
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
selenium.clickAt("//div[@id='UIApplicationOrganizer']//div[@class='UIControlbar']/div[1]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("name")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("name", "category_test");
selenium.type("displayName", "Category test name");
selenium.type("description", "category test description");
selenium.click("//div[@class='WorkingArea']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Permission")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Permission", "1,1");
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
 if (selenium.isElementPresent("link=manager")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=manager", "1,1");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIApplicationOrganizer']//a[@class='ControlIcon EditIcon']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIApplicationOrganizer']//a[@class='ControlIcon EditIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("displayName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("displayName", "category test edit");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("category test edit")) 
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
