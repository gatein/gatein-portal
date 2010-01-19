package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_32 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_32() throws Exception {
selenium.setSpeed("500");
selenium.click("link=Sign in");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Change Skin")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Change Skin", "1,1");
selenium.clickAt("//div[@id='UITabContent']//div[@class='ItemListContainer']//div[@class='ItemList']//div[@class='SelectedItem Item']", "1,1");
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='ActionButton LightBlueStyle']", "1,1");
selenium.waitForPageToLoad("30000");
}

}
