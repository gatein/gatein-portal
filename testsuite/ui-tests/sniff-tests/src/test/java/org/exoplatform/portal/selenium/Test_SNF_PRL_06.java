package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_06 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
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
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Group")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("link=Group");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Users and groups management")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("link=Users and groups management");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("User Name")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
assertTrue(selenium.isTextPresent("Last Name"));
assertTrue(selenium.isTextPresent("First Name"));
assertTrue(selenium.isTextPresent("Email"));
System.out.println("--Edit fields");
selenium.clickAt("//div[@id='UIListUsersGird']//tbody/tr[3]//td[5]//div//img", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("firstName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("firstName", "test_user_06");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIUserManagement']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIUserManagement']//div[@class='TabsContainer']/div[2]//div[@class='MiddleTab']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("user.name.given")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("user.name.given", "test_name_given_06");
selenium.type("user.name.family", "test_name_family_06");
selenium.type("user.name.nickName", "test_name_nick_06");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
assertTrue(selenium.isTextPresent("The user profile has been updated."));
selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent > div.UIPopupWindow div.ActionButton", "1,1");
selenium.clickAt("link=Cancel", "1,1");
System.out.println("--Verify changes");
assertTrue(selenium.isTextPresent("test_user_06"));
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Sign out")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Sign out", "1,1");
}

}
