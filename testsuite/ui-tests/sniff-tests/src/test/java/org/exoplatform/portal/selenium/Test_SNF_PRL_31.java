package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_31 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
}

public void testSNF_PRL_31() throws Exception {
setSpeed();
selenium.open("/portal/public/classic");
System.out.println("-ChangeLanguagePrivateMode-");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Change Language")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Change Language", "1,1");
System.out.println("--Change to French");
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
System.out.println("--Verify");
assertTrue(selenium.isTextPresent("Accueil"));
selenium.clickAt("link=Changer la langue", "1,1");
System.out.println("--Change back to English");
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
selenium.clickAt("link=Sign out", "1,1");
}

}
