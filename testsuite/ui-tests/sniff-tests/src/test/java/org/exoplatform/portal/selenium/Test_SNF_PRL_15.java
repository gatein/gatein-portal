package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_15 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_15() throws Exception {
selenium.setSpeed("500");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("link=Sign in");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/sitemap");
selenium.clickAt("//div[@id='UISiteMap']//div[@class='ClearFix']/div[2]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("Blog")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
assertTrue(selenium.isTextPresent("New Staff"));
assertTrue(selenium.isTextPresent("Application Registry"));
selenium.clickAt("//div[@id='UISiteMap']//div[@class='ClearFix']/div[1]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (!selenium.isElementPresent("Blog"))
 break;
 }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("css=div#UISiteMap div.ExpandIcon", "1,1");
selenium.clickAt("css=div#UISiteMap div.ChildrenContainer a", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (!selenium.isElementPresent("css=div#UISiteMap div.ChildrenContainer a"))
 break;
 }
 catch (Exception e) {}
Thread.sleep(1000);
}
}

}
