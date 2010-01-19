package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_05 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_05() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/public/classic/");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("rememberme");
selenium.clickAt("//div[@id='UIPortalLoginFormAction']//div[@class='ButtonMiddle']/a", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Sign out")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("link=Sign out");
selenium.clickAt("link=Sign in", "1,1");
verifyTrue(selenium.isChecked("rememberme"));
selenium.clickAt("link=Discard", "1,1");
}

}
