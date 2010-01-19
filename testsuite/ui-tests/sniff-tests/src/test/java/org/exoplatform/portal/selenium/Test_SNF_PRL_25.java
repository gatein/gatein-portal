package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_25 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_25() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/private/classic/");
selenium.click("link=Group");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Edit Navigation")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
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
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("name")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("name", "Hihizzz");
selenium.type("label", "Hohohozzz");
selenium.clickAt("css=div#UIGroupNavigationManagement div.UIPopupWindow div.TabsContainer div.NormalTab div.MiddleTab", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Search and Select Page")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Search and Select Page", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//img[@title='Select Page']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//img[@title='Select Page']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Save")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Hohohozzz", "1,1");
selenium.open("/portal/private/classic/");
}

}
