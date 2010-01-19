package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_06 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_06() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/public/classic/");
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
selenium.clickAt("//div[@id='UIListUsersGird']//tbody/tr[3]//td[5]//div//img", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("firstName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("firstName", "exo2");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton", "1,1");
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
selenium.type("user.name.given", "test");
selenium.type("user.name.given", "test05");
selenium.type("user.name.family", "test05family");
selenium.type("user.name.nickName", "testnick");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
assertTrue(selenium.isTextPresent("The user profile has been updated."));
selenium.clickAt("css=div#UIOrganizationPortlet div.ManagementTabContent &gt; div.UIPopupWindow div.ActionButton", "1,1");
selenium.clickAt("link=Cancel", "1,1");
assertTrue(selenium.isTextPresent("exo2"));
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
