package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_24 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_24() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/private/classic/");
selenium.click("link=Group");
selenium.waitForPageToLoad("30000");
selenium.clickAt("//a[@class='EditProIcon']", "1,1");
selenium.type("description", "more and more");
selenium.clickAt("link=Save", "1,1");
assertTrue(selenium.isTextPresent(""));
}

}
