package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_02 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_02() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/public/classic/");
System.out.println("-New Account-");
System.out.println("--Register new account");
selenium.click("link=Register");
selenium.waitForPageToLoad("30000");
selenium.type("User Name:", "test_user_02");
selenium.type("Password:", "test_pwd_02");
selenium.type("Confirm Password:", "test_pwd_02");
selenium.type("First Name:", "test_name_first_02");
selenium.type("Last Name:", "test_name_last_02");
selenium.type("Email Address:", "test_user_02@yahoo.com");
selenium.clickAt("link=Subscribe", "1,1");
selenium.open("/portal/public/classic/");
System.out.println("--Verification");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "test_user_02");
selenium.type("password", "test_pwd_02");
selenium.clickAt("//div[@id='UIPortalLoginFormAction']//a", "1,1");
assertTrue(selenium.isTextPresent("test_name_first_02 test_name_last_02"));
selenium.clickAt("link=Sign out", "1,1");
System.out.println("--Delete new user");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.clickAt("//div[@id='UIPortalLoginFormAction']//a", "1,1");
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
selenium.clickAt("//div[@class='UIListUsers']//tbody/tr[5]//img[@class='DeleteUserIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (selenium.getConfirmation().equals("Are you sure you want to delete test_user_02 user?")) {
break;
}
}
catch (Exception e) {
}
Thread.sleep(1000);
}
selenium.clickAt("link=Sign out", "1,1");
}

}
