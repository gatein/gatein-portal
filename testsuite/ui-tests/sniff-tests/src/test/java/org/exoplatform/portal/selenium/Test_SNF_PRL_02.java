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
selenium.click("link=Register");
selenium.waitForPageToLoad("30000");
selenium.type("User Name:", "abc123");
selenium.type("Password:", "121212");
selenium.type("Confirm Password:", "121212");
selenium.type("First Name:", "Aha");
selenium.type("Last Name:", "Nguyen");
selenium.type("Email Address:", "th4nhc0n9z@yahoo.com");
selenium.clickAt("link=Subscribe", "1,1");
selenium.open("/portal/public/classic/");
System.out.println("!!! missing assert to check user exists !!!");
}

}
