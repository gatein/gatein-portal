package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_09 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_09() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/public/classic/");
System.out.println("-AutoImport-");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
System.out.println("--Select \"Application Registry\"");
selenium.clickAt("link=Application Registry", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("Import Applications")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
System.out.println("--Auto Import");
assertFalse(selenium.isTextPresent("WSRP Admin Portlet"));
selenium.clickAt("//div[@id='UIApplicationOrganizer']//div[@class='UIControlbar']//div[@class='IconControl ImportIcon']", "1,1");
String autoimport = selenium.getConfirmation();
assertTrue(selenium.isTextPresent("WSRP Admin Portlet"));
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Sign out")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Sign out", "1,1");
}

}
