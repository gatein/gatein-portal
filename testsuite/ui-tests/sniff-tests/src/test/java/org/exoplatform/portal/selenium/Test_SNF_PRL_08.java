package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_08 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_08() throws Exception {
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
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='ManagementIconContainer']/a[@class='MembershipButton']", "1,1");
selenium.type("name", "demomembership");
selenium.type("description", "demo scripts");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("demomembership")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//table[@class='UIGrid']//tbody/tr[3]/td[5]//img", "1,1");
selenium.type("description", "demo scripts add more text");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("demo scripts add more text")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("link=classic");
selenium.waitForPageToLoad("30000");
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
