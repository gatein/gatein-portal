package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_23 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_23() throws Exception {
selenium.setSpeed("500");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/");
selenium.click("link=Group");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Navigation")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Navigation", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Cancel")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Cancel", "1,1");
selenium.clickAt("link=Edit Navigation", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Node")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Node", "1,1");
selenium.type("name", "grp_node");
selenium.type("label", "new_grp_node");
selenium.clickAt("//div[@class='CenterHorizontalTabs']//div[@class='NormalTab']//div[@class='MiddleTab']", "1,1");
selenium.clickAt("link=Search and Select Page", "1,1");
selenium.clickAt("//div[@id='UIRepeater']//img[@class='SelectPageIcon']", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.check("//a[@onclick='eXo.portal.logout();']");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=new_grp_node")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("link=new_grp_node");
selenium.waitForPageToLoad("30000");
}

}
