package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_21 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_21() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/private/classic/");
selenium.click("link=New_portal");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/portalnavigation");
selenium.click("link=New_portal");
selenium.waitForPageToLoad("30000");
selenium.clickAt("link=New_portal", "1,1");
}

}
