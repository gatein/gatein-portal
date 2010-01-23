package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_23 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
}

public void testSNF_PRL_23() throws Exception {
setSpeed();
selenium.open("/portal/public/classic");
System.out.println("-AddNavigation-");
selenium.clickAt("link=Sign in", "1,1");
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
selenium.type("name", "test_grp_node_23");
selenium.type("label", "test_grp_label_23");
System.out.println("--Choose \"Page Selector\" tab");
selenium.clickAt("//div[@class='CenterHorizontalTabs']//div[@class='NormalTab']//div[@class='MiddleTab']", "1,1");
selenium.clickAt("link=Search and Select Page", "1,1");
System.out.println("--Select the first page from pages list");
selenium.clickAt("//div[@id='UIRepeater']//img[@class='SelectPageIcon']", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=test_grp_label_23")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("link=test_grp_label_23");
selenium.waitForPageToLoad("30000");
System.out.println("--Delete new group navigation");
System.out.println("-----------");
selenium.clickAt("link=Sign out", "1,1");
}

}
