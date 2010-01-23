package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_07 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
}

public void testSNF_PRL_07() throws Exception {
setSpeed();
selenium.open("/portal/public/classic/");
System.out.println("-GroupManagement-");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
System.out.println("--Select \"Users and groups management\" in menu");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Group")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Group", "1,1");
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
System.out.println("--Select \"Organization\" group from group tree");
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[3]//div[@class='ExpandIcon']/a", "1,1");
System.out.println("--Select \"Management group\" from group tree");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("Management")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[3]//div[@class='ExpandIcon']/a", "1,1");
System.out.println("--Click Add new group icon");
selenium.clickAt("//div[@id='UIOrganizationPortlet']//div[@class='TitleBar']/a[@class='TreeActionIcon AddGroupIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("groupName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("groupName", "test_group_name_07");
selenium.type("label", "test_group_label_07");
selenium.type("description", "test_group_description_07");
System.out.println("--Click \"Save\" to complete adding new group");
selenium.clickAt("//form[@id='UIGroupForm']//div[@class='ActionButton LightBlueStyle']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("test_group_label_07")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
System.out.println("--Click \"Select User\" icon");
selenium.clickAt("//form[@id='UIGroupMembershipForm']//div[@class='HorizontalLayout']//table[@class='UIFormGrid']//td[@class='FieldComponent']/a", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//form[@id='UIUserSelector']/div[2]/div[2]/table/tbody/tr/td/a[1]/div/div/div")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("demo", "1,1");
selenium.clickAt("john", "1,1");
selenium.clickAt("mary", "1,1");
selenium.clickAt("root", "1,1");
System.out.println("--Click \"Add\" button");
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
selenium.clickAt("link=Sign out", "1,1");
}

}
