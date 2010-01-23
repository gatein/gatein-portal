package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_18 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
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
selenium.clickAt("//div[@id='UISiteManagement']//div[@class='UIAction']//div[@class='ActionButton BlueButton']", "1,1");
System.out.println("--Select portal settings");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']", "1,1");
selenium.type("name", "test_portal_18");
System.out.println("--Select permission settings");
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[4]//div[@class='MiddleTab']", "1,1");
selenium.clickAt("publicMode", "1,1");
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
 if (selenium.isTextPresent("test_portal_18")) 
break; }
 catch (Exception e) {}
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
if (second >= 30) fail("timeout");
try {
if (selenium.getConfirmation().equals("Are you sure you want to delete this portal?")) {
break;
}
}
catch (Exception e) {
}
Thread.sleep(1000);
}
selenium.clickAt("link=Sign out", "1,1");
}

}
