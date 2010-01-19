package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_28 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_28() throws Exception {
selenium.setSpeed("500");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/");
selenium.clickAt("link=Dashboard", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Gadgets")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Gadgets", "1,1");
selenium.type("//input[@id='url']", "http://hosting.gmodules.com/ig/gadgets/file/112581010116074801021/treefrog.xml");
selenium.clickAt("//img[@class='AddNewNodeIcon']", "1,1");
selenium.clickAt("//div[@id='UIAddGadgetPopup']//div[@class='CloseButton']", "1,1");
}

}
