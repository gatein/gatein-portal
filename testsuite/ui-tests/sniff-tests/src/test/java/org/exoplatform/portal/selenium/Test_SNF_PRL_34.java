package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_34 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_34() throws Exception {
selenium.setSpeed("500");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/");
selenium.clickAt("link=Root Root", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("email")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("email", "mytest.exo10@gmail.com");
selenium.type("lastName", "Root");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=OK", "1,1");
selenium.clickAt("link=Close", "1,1");
}

}
