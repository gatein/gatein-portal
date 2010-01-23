package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_15 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
}

public void testSNF_PRL_15() throws Exception {
setSpeed();
selenium.open("/portal/public/classic/");
System.out.println("-SiteMapAndLinkToPage-");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/sitemap");
System.out.println("--Expand the first submenu");
selenium.clickAt("css=div#UISiteMap div.ExpandIcon", "1,1");
System.out.println("--Select the first link of submenu");
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
selenium.clickAt("link=Sign out", "1,1");
}

}
