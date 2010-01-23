package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_10 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
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
selenium.clickAt("//div[@id='UIApplicationOrganizer']//div[@class='UIControlbar']/div[1]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("name")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("name", "test_name_category_10");
selenium.type("displayName", "test_displayname_category_10");
selenium.type("description", "test_description_category_10");
System.out.println("--Select permissions");
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
assertTrue(selenium.isTextPresent("test_displayname_category_10"));
System.out.println("--Edit Category");
selenium.clickAt("//div[@id='UIApplicationOrganizer']//a[@class='ControlIcon EditIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("displayName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("displayName", "test_displayname_edit_10");
selenium.clickAt("link=Save", "1,1");
assertTrue(selenium.isTextPresent("test_displayname_edit_10"));
System.out.println("--Delete Category");
selenium.clickAt("//div[@class='SelectedTab']//a[@class='ControlIcon DeleteIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (selenium.getConfirmation().equals("Are you sure to delete this category and all applications on it?")) {
break;
}
}
catch (Exception e) {
}
Thread.sleep(1000);
}
assertFalse(selenium.isTextPresent("test_displayname_edit_10"));
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
