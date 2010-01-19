package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_07 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_07() throws Exception {
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
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='ManagementIconContainer']/a[@class='GroupButton']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIOrganizationPortlet']/div[2]/div[2]/div[1]/div[2]/div[1]/div[2]/div/div/div/div[3]/div/a")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[3]//div[@class='ExpandIcon']/a", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("Management")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[3]//div[@class='ExpandIcon']/a", "1,1");
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='TitleBar']/a[@class='TreeActionIcon AddGroupIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("groupName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("groupName", "testgroup");
selenium.type("label", "testgroup label");
selenium.type("description", "testgroup description");
selenium.clickAt("//form[@id='UIGroupForm']//div[@class='ActionButton LightBlueStyle']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("testgroup label")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//form[@id='UIGroupMembershipForm']//div[@class='HorizontalLayout']//table[@class='UIFormGrid']//td[@class='FieldComponent']/a", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//form[@id='UIUserSelector']/div[2]/div[2]/table/tbody/tr/td/a[1]/div/div/div")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("demo");
selenium.click("john");
selenium.click("mary");
selenium.click("root");
selenium.clickAt("//form[@id='UIUserSelector']//div[@class='UIAction']//a[@class='ActionButton LightBlueStyle']", "1,1");
selenium.clickAt("link=Save", "1,1");
assertTrue(selenium.isTextPresent("demo"));
assertTrue(selenium.isTextPresent("john"));
assertTrue(selenium.isTextPresent("mary"));
assertTrue(selenium.isTextPresent("root"));
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
