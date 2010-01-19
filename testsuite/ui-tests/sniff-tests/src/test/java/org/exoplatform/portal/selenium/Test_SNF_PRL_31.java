package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_31 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_31() throws Exception {
selenium.setSpeed("500");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Change Language")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Change Language", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=French")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=French", "1,1");
selenium.click("link=Apply");
selenium.waitForPageToLoad("30000");
assertTrue(selenium.isTextPresent("Accueil"));
selenium.clickAt("link=Changer la langue", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=anglais")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=anglais", "1,1");
selenium.click("link=Appliquer");
selenium.waitForPageToLoad("30000");
assertTrue(selenium.isTextPresent("Home"));
}

}
